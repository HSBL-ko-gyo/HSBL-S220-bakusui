package jp.hsbl.bakusui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ShortsGuardService : Service() {

    companion object {
        const val CHANNEL_BG = "shorts_guard_bg"
        const val CHANNEL_ALERT = "shorts_guard_alert"
        const val NOTIF_ID = 2001

        const val ACTION_START = "jp.hsbl.bakusui.shorts.START"
        const val ACTION_STOP = "jp.hsbl.bakusui.shorts.STOP"
        const val ACTION_TEST = "jp.hsbl.bakusui.shorts.TEST"

        const val EXTRA_MIN1 = "min1" // 1st threshold (ms)
        const val EXTRA_MIN2 = "min2" // 2nd threshold (ms)

        private const val YT_PKG = "com.google.android.youtube"
        private val DEFAULT_TARGET_PKGS = setOf(
            YT_PKG,
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill" // TikTokの別パッケージ
        )

        // Debug keys for SharedPreferences
        const val DBG_PKG = "shorts_dbg_pkg"
        const val DBG_SINCE = "shorts_dbg_since"
        const val DBG_NOW = "shorts_dbg_now"
        const val DBG_ELAPSED = "shorts_dbg_elapsed"
        const val DBG_ALERT1 = "shorts_dbg_alert1"
        const val DBG_ALERT2 = "shorts_dbg_alert2"
        const val DBG_EVENTS = "shorts_dbg_events"
    }

    // バックグラウンドでも確実にループを動かすため、MainではなくDefaultを使用
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var running = false
    private var threshold1Ms: Long = 5 * 60_000L
    private var threshold2Ms: Long = 10 * 60_000L

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                threshold1Ms = intent.getLongExtra(EXTRA_MIN1, threshold1Ms)
                threshold2Ms = intent.getLongExtra(EXTRA_MIN2, threshold2Ms)
                // 設定を保存（バックアップ）
                try {
                    getSharedPreferences("bakusui_prefs", Context.MODE_PRIVATE)
                        .edit()
                        .putInt("shorts_min1", (threshold1Ms / 60000L).toInt())
                        .putInt("shorts_min2", (threshold2Ms / 60000L).toInt())
                        .apply()
                } catch (_: Throwable) { }
                try {
                    startForeground(NOTIF_ID, buildBgNotification("監視中"))
                } catch (_: Throwable) {
                    // 一部端末で通知権限未許可時に例外が出ることがある
                }
                if (!hasUsageAccess()) {
                    updateNotification("権限がありません（使用状況へのアクセス）")
                    try { stopForeground(STOP_FOREGROUND_REMOVE) } catch (_: Throwable) {}
                    stopSelf()
                    return START_NOT_STICKY
                }
                if (!running) {
                    running = true
                    scope.launch { loop() }
                }
            }
            ACTION_STOP -> {
                running = false
                try { stopForeground(STOP_FOREGROUND_REMOVE) } catch (_: Throwable) {}
                stopSelf()
            }
            ACTION_TEST -> {
                showAlert("テスト通知", "この通知が見えればアラートは機能しています")
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun loop() {
        val usm = try {
            getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        } catch (_: Throwable) { null }
        // 安定化用（観測→確定までの遅延でフラつきを抑制）
        var stablePkg: String? = run {
            val now = System.currentTimeMillis()
            currentForeground(usm, now - 120_000L, now) ?: mostRecentApp(usm, now - 600_000L, now)
        }
        var stableClass: String? = null
        var stableSince = System.currentTimeMillis()
        var pendingPkg: String? = null
        var pendingClass: String? = null
        var pendingSince = System.currentTimeMillis()

        var alerted1 = false
        var lastAlertAtMs = 0L
        var prevPolicyKey: String? = null
        var wasQualified = false
        var measureSince = 0L
        var accumulatedQualifiedMs = 0L
        var lastTick = 0L
        var lastQualifiedAt = 0L

        // 初期の背景通知だけ掲示（以降は更新しない）
        updateNotification("監視中")

        while (running && scope.isActive) {
            try {
                val now = System.currentTimeMillis()
                // 直近120秒のイベントから最前面推定（RESUMED優先 → FOREGROUND）。無ければusage statsで補完
                var observedPkg: String? = null
                var observedClass: String? = null
                if (usm != null) {
                    val detail = currentForegroundDetail(usm, now - 120_000L, now)
                    observedPkg = detail.first
                    observedClass = detail.second
                }
                if (usm != null) {
                    captureRecentEvents(usm, now - 60_000L, now)
                }
                if (observedPkg == null) {
                    observedPkg = mostRecentApp(usm, now - 600_000L, now)
                    observedClass = null
                }

                // 観測結果を安定化（3秒同一観測で確定）
                if (observedPkg != pendingPkg) {
                    pendingPkg = observedPkg
                    pendingClass = observedClass
                    pendingSince = now
                } else {
                    if (stablePkg != pendingPkg && now - pendingSince >= 3000L) {
                        // 確定切替
                        stablePkg = pendingPkg
                        stableClass = pendingClass
                        stableSince = now
                        alerted1 = false
                        lastAlertAtMs = 0L
                    }
                }

                val pkg = stablePkg
                if (pkg != null) {
                    // 監視対象リストとYouTubeショートのみ設定をPrefsから取得
                    val prefs = getSharedPreferences("bakusui_prefs", Context.MODE_PRIVATE)
                    val selected = prefs.getStringSet("shorts_targets", null)?.toSet() ?: DEFAULT_TARGET_PKGS
                    val youtubeOnly = prefs.getBoolean("shorts_youtube_only", false)

                    // 設定変更時は起点をリセット（直前の積み上げで即通知されないように）
                    val policyKey = buildString {
                        append("ytOnly=").append(youtubeOnly).append(';')
                        append("targets=").append(selected.sorted().joinToString(","))
                    }
                    if (policyKey != prevPolicyKey) {
                        prevPolicyKey = policyKey
                        alerted1 = false
                        lastAlertAtMs = 0L
                        wasQualified = false
                        measureSince = 0L
                        accumulatedQualifiedMs = 0L
                        lastTick = 0L
                    }

                    val shortsOk = if (youtubeOnly && pkg == YT_PKG) {
                        // 1) アクセシビリティの結果を優先
                        val accOk = try {
                            val isShorts = prefs.getBoolean("yt_is_shorts", false)
                            val whenTs = prefs.getLong("yt_last_event", 0L)
                            val accTtlMs = 120_000L
                            isShorts && (now - whenTs <= accTtlMs)
                        } catch (_: Throwable) { false }
                        if (accOk) true else {
                            // 2) class名 or 3) UsageEvents走査
                            val cls = stableClass
                            if (!cls.isNullOrBlank()) {
                                cls.contains("Short", ignoreCase = true) || cls.contains("Shorts", ignoreCase = true)
                            } else {
                                usm?.let { isYouTubeShortsInWindow(it, now - 120_000L, now) } == true
                            }
                        }
                    } else true
                    val qualified = (pkg in selected) && shortsOk
                    if (qualified) {
                        lastQualifiedAt = now
                        if (!wasQualified) {
                            wasQualified = true
                            if (measureSince == 0L) {
                                measureSince = now
                            }
                            lastTick = now
                        } else {
                            // 前回からの経過を加算（対象外の時間は加算しない）
                            accumulatedQualifiedMs += (now - lastTick)
                            lastTick = now
                        }
                        val elapsed = accumulatedQualifiedMs
                        // Debug: 記録（経過は加算時間）
                        writeDebug(pkg, measureSince, now, elapsed, alerted1, false)
                        val snoozeMs = threshold2Ms
                        if (!alerted1 && elapsed >= threshold1Ms) {
                            val mins = (elapsed / 60000L).toInt()
                            showAlert("視聴アラート", "${mins}分経過")
                            alerted1 = true
                            lastAlertAtMs = now
                        } else if (alerted1 && snoozeMs > 0 && now - lastAlertAtMs >= snoozeMs) {
                            val mins = (elapsed / 60000L).toInt()
                            showAlert("視聴アラート", "${mins}分経過")
                            lastAlertAtMs = now
                        }
                    } else {
                        // 対象外中は計測を一時停止（累積は保持）。再度qualifiedになれば加算再開。
                        if (wasQualified) {
                            wasQualified = false
                            // lastTickは保持しない（次回qualifiedで更新）
                        }
                        // 6時間以上ショート（対象）を見ていなければ累積を破棄
                        val sixHoursMs = 6L * 60L * 60L * 1000L
                        if (lastQualifiedAt > 0L && now - lastQualifiedAt >= sixHoursMs) {
                            accumulatedQualifiedMs = 0L
                            measureSince = 0L
                            alerted1 = false
                            lastAlertAtMs = 0L
                            lastQualifiedAt = 0L
                        }
                        writeDebug(pkg, measureSince, now, accumulatedQualifiedMs, alerted1, false)
                    }
                } else {
                    writeDebug("", stableSince, now, 0L, alerted1, false)
                }
                // 状態を保存（継続用・任意）
                try {
                    val sp = getSharedPreferences("bakusui_prefs", Context.MODE_PRIVATE)
                    sp.edit()
                        .putLong("shorts_accum_ms", accumulatedQualifiedMs)
                        .putLong("shorts_measure_since", if (measureSince == 0L) 0L else measureSince)
                        .putBoolean("shorts_was_qualified", wasQualified)
                        .putLong("shorts_last_tick", lastTick)
                        .putString("shorts_policy_key", prevPolicyKey)
                        .apply()
                } catch (_: Throwable) { }
            } catch (_: Throwable) {
                // 背景通知テキストは固定のまま（煩雑な更新は避ける）
            }
            delay(1000L)
        }
    }

    private fun hasUsageAccess(): Boolean {
        return try {
            val appOps = getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
            val mode = appOps.checkOpNoThrow(
                "android:get_usage_stats",
                android.os.Process.myUid(),
                packageName
            )
            mode == android.app.AppOpsManager.MODE_ALLOWED
        } catch (_: Throwable) { false }
    }

    private fun currentForeground(usm: UsageStatsManager?, begin: Long, end: Long): String? {
        return try {
            if (usm == null) return null
            val events: UsageEvents = usm.queryEvents(begin, end)
            var latestTs = -1L
            var latestPkg: String? = null
            var latestCls: String? = null
            val ev = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(ev)
                val type = ev.eventType
                val ts = ev.timeStamp
                // ACTIVITY_RESUMED(7)優先、なければMOVE_TO_FOREGROUND(1)
                val isResume = type == 7 /* ACTIVITY_RESUMED */ || type == UsageEvents.Event.ACTIVITY_RESUMED
                val isFg = type == UsageEvents.Event.MOVE_TO_FOREGROUND
                if ((isResume || isFg) && ts >= latestTs) {
                    latestTs = ts
                    latestPkg = ev.packageName
                    latestCls = ev.className
                }
            }
            latestPkg
        } catch (_: Throwable) {
            null
        }
    }

    private fun currentForegroundDetail(usm: UsageStatsManager, begin: Long, end: Long): Pair<String?, String?> {
        return try {
            val events: UsageEvents = usm.queryEvents(begin, end)
            var latestTs = -1L
            var latestPkg: String? = null
            var latestCls: String? = null
            val ev = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(ev)
                val type = ev.eventType
                val ts = ev.timeStamp
                val isResume = type == 7 /* ACTIVITY_RESUMED */ || type == UsageEvents.Event.ACTIVITY_RESUMED
                val isFg = type == UsageEvents.Event.MOVE_TO_FOREGROUND
                if ((isResume || isFg) && ts >= latestTs) {
                    latestTs = ts
                    latestPkg = ev.packageName
                    latestCls = ev.className
                }
            }
            Pair(latestPkg, latestCls)
        } catch (_: Throwable) {
            Pair(null, null)
        }
    }

    private fun isYouTubeShortsInWindow(usm: UsageStatsManager, begin: Long, end: Long): Boolean {
        return try {
            val events: UsageEvents = usm.queryEvents(begin, end)
            val ev = UsageEvents.Event()
            val patterns = arrayOf("Short", "Shorts") // 将来必要なら拡張
            while (events.hasNextEvent()) {
                events.getNextEvent(ev)
                val pkg = ev.packageName ?: continue
                if (pkg != YT_PKG) continue
                val cls = ev.className ?: continue
                val match = patterns.any { p -> cls.contains(p, ignoreCase = true) }
                if (match) return true
            }
            false
        } catch (_: Throwable) { false }
    }

    private fun mostRecentApp(usm: UsageStatsManager?, begin: Long, end: Long): String? {
        return try {
            if (usm == null) return null
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, begin, end)
            stats.maxByOrNull { it.lastTimeUsed }?.packageName
        } catch (_: Throwable) { null }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            // 背景: 最小重要度（常時表示は必須だが極力目立たせない）
            nm.createNotificationChannel(NotificationChannel(
                CHANNEL_BG, "視聴アラート（監視）",
                NotificationManager.IMPORTANCE_MIN
            ))
            // アラート: 高重要度（しきい値到達時のみ表示）
            nm.createNotificationChannel(NotificationChannel(
                CHANNEL_ALERT, "視聴アラート（アラート）",
                NotificationManager.IMPORTANCE_HIGH
            ))
        }
    }

    private fun captureRecentEvents(usm: UsageStatsManager, begin: Long, end: Long) {
        try {
            val sb = StringBuilder()
            val evs = usm.queryEvents(begin, end)
            val ev = UsageEvents.Event()
            val list = ArrayList<String>(16)
            while (evs.hasNextEvent()) {
                evs.getNextEvent(ev)
                val ch = when (ev.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> "R"
                    UsageEvents.Event.MOVE_TO_FOREGROUND -> "F"
                    UsageEvents.Event.MOVE_TO_BACKGROUND -> "B"
                    else -> continue
                }
                list.add("${ch}:${ev.packageName.takeLast(20)}:${(ev.timeStamp/1000)%100000}")
            }
            // 末尾10件だけ
            val tail = if (list.size > 10) list.takeLast(10) else list
            sb.append(tail.joinToString(" | "))
            getSharedPreferences("bakusui_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString(DBG_EVENTS, sb.toString())
                .apply()
        } catch (_: Throwable) { }
    }

    private fun buildBgNotification(text: String): Notification {
        val stopIntent = Intent(this, ShortsGuardService::class.java).apply { action = ACTION_STOP }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        val stopPi = PendingIntent.getService(this, 10, stopIntent, flags)

        return NotificationCompat.Builder(this, CHANNEL_BG)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("視聴アラート")
            .setContentText(text)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "監視停止", stopPi)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(text: String) {
        with(NotificationManagerCompat.from(this)) {
            notify(NOTIF_ID, buildBgNotification(text))
        }
    }

    private fun showAlert(title: String, text: String) {
        val n = NotificationCompat.Builder(this, CHANNEL_ALERT)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        with(NotificationManagerCompat.from(this)) {
            notify((System.currentTimeMillis() % 100000).toInt(), n)
        }
    }

    private fun writeDebug(pkg: String, since: Long, now: Long, elapsedMs: Long, a1: Boolean, a2: Boolean) {
        try {
            getSharedPreferences("bakusui_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString(DBG_PKG, pkg)
                .putLong(DBG_SINCE, since)
                .putLong(DBG_NOW, now)
                .putLong(DBG_ELAPSED, elapsedMs)
                .putBoolean(DBG_ALERT1, a1)
                .putBoolean(DBG_ALERT2, a2)
                .apply()
        } catch (_: Throwable) { }
    }
}

