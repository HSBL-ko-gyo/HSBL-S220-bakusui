package jp.hsbl.bakusui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import jp.hsbl.bakusui.player.AudioPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PlaybackService : Service() {

    companion object {
        const val CHANNEL_ID = "playback"
        const val NOTIF_ID = 1001

        const val ACTION_START = "jp.hsbl.bakusui.action.START"
        const val ACTION_TOGGLE = "jp.hsbl.bakusui.action.TOGGLE"
        const val ACTION_STOP = "jp.hsbl.bakusui.action.STOP"
        const val ACTION_VOLUME = "jp.hsbl.bakusui.action.VOLUME"
        const val ACTION_DWELL = "jp.hsbl.bakusui.action.DWELL"

        const val EXTRA_MODE = "mode"
        const val EXTRA_DWELL = "dwell"
        const val EXTRA_SEED = "seed"
        const val EXTRA_VOLUME = "volume"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var player: AudioPlayer
    private var isPlaying: Boolean = false
    private var currentMode: String = "random"
    private var currentDwell: Int = 7
    private var currentSeed: Long = System.currentTimeMillis()

    override fun onCreate() {
        super.onCreate()
        player = AudioPlayer(applicationContext)
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_START -> {
                currentMode = intent.getStringExtra(EXTRA_MODE) ?: "random"
                currentDwell = intent.getIntExtra(EXTRA_DWELL, 7)
                currentSeed = intent.getLongExtra(EXTRA_SEED, System.currentTimeMillis())
                val vol = intent.getFloatExtra(EXTRA_VOLUME, 1.0f).coerceIn(0f, 1f)
                player.setVolume(vol)
                try {
                    startForeground(NOTIF_ID, buildNotification(true))
                } catch (_: Throwable) {
                    // フォアグラウンド起動失敗時も落とさない
                }
                scope.launch {
                    if (player.isPlaying()) player.stop()
                    player.start(mode = currentMode, dwellSeconds = currentDwell, seed = currentSeed)
                    isPlaying = true
                    updateNotification(true)
                }
            }
            ACTION_TOGGLE -> {
                if (isPlaying) {
                    scope.launch {
                        player.requestStopAfterCurrent()
                        isPlaying = false
                        updateNotification(false)
                    }
                } else {
                    scope.launch {
                        try {
                            startForeground(NOTIF_ID, buildNotification(true))
                        } catch (_: Throwable) { }
                        player.start(mode = currentMode, dwellSeconds = currentDwell, seed = currentSeed)
                        isPlaying = true
                        updateNotification(true)
                    }
                }
            }
            ACTION_STOP -> {
                scope.launch {
                    player.requestStopAfterCurrent()
                    isPlaying = false
                    updateNotification(false)
                    try {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    } catch (_: Throwable) { }
                    stopSelf()
                }
            }
            ACTION_VOLUME -> {
                val vol = intent.getFloatExtra(EXTRA_VOLUME, 1.0f).coerceIn(0f, 1f)
                player.setVolume(vol)
                updateNotification(isPlaying)
            }
            ACTION_DWELL -> {
                val dwell = intent.getIntExtra(EXTRA_DWELL, currentDwell)
                currentDwell = dwell
                player.setDwellSeconds(dwell)
                // 通知文面は変えないが最新状態として更新
                updateNotification(isPlaying)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID, "再生コントロール",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
    }

    private fun buildNotification(playing: Boolean): Notification {
        val toggleIntent = Intent(this, PlaybackService::class.java).apply { action = ACTION_TOGGLE }
        val stopIntent = Intent(this, PlaybackService::class.java).apply { action = ACTION_STOP }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        val togglePi = PendingIntent.getService(this, 1, toggleIntent, flags)
        val stopPi = PendingIntent.getService(this, 2, stopIntent, flags)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("バクスイ")
            .setContentText(if (playing) "再生中" else "停止中")
            .setOngoing(playing)
            .addAction(
                if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (playing) "一時停止" else "再生",
                togglePi
            )
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "停止", stopPi)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private fun updateNotification(playing: Boolean) {
        with(NotificationManagerCompat.from(this)) {
            notify(NOTIF_ID, buildNotification(playing))
        }
    }
}

