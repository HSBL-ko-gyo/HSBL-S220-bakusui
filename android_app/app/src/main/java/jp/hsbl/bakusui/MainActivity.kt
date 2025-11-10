package jp.hsbl.bakusui

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Switch
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Surface
import jp.hsbl.bakusui.player.AudioPlayer
import jp.hsbl.bakusui.ui.theme.BakusuiTheme
import kotlinx.coroutines.launch
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.app.PendingIntent
import android.content.Intent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Android 13+ 通知許可
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }
        createPlaybackChannel()
        setContent {
            BakusuiTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "start") {
                        composable("start") {
                            StartScreen(
                                onSelectShuffle = { navController.navigate("home") },
                                onSelectShorts = { navController.navigate("shorts") },
                                onOpenAbout = { navController.navigate("about") }
                            )
                        }
                        composable("home") {
                            HomeScreen(onSelect = { mode ->
                                navController.navigate("player?mode=$mode")
                            })
                        }
                        composable("shorts") {
                            ShortsGuardScreen(onBack = { navController.popBackStack() })
                        }
                        composable("player?mode={mode}") { backStackEntry ->
                            val mode = backStackEntry.arguments?.getString("mode") ?: "random"
                            PlayerScreen(mode = mode)
                        }
                        composable("about") {
                            AboutScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }

    private fun createPlaybackChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "playback",
                "再生コントロール",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val mgr = getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(channel)
        }
    }
}

@Composable
private fun StartScreen(onSelectShuffle: () -> Unit, onSelectShorts: () -> Unit, onOpenAbout: () -> Unit) {
    // TOPだけ紫基調の背景（全画面グラデーション）
    val topGradient = Brush.verticalGradient(listOf(Color(0xFF6A5AAE), Color(0xFF4E4090)))
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(topGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // ロゴのみ（枠なし・フラット）
        Image(
            painter = painterResource(id = R.drawable.ic_bakusui_logo),
            contentDescription = "バクスイ ロゴ",
            modifier = Modifier.size(160.dp)
        )
        Text("バクスイ - 睡眠お助けツール", style = MaterialTheme.typography.titleLarge, color = Color.White)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            val tileColor = Color.White.copy(alpha = 0.14f)
            Surface(
                color = tileColor,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f, fill = true)
                    .height(140.dp)
                    .clickable { onSelectShuffle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(6.dp))
                    Text("認知シャッフル睡眠法", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    // 説明は省略（文言同期箇所を増やさない）
                }
            }
            Surface(
                color = tileColor,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f, fill = true)
                    .height(140.dp)
                    .clickable { onSelectShorts() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(6.dp))
                    Text("視聴アラート", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    // 説明は省略
                }
            }
        }
        // フッター: バージョン情報 / ライセンス
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "バージョン情報 / ライセンス",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.clickable { onOpenAbout() },
                fontSize = 12.sp,
                textDecoration = TextDecoration.Underline
            )
        }
        }
    }
}

@Composable
private fun ShortsGuardScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("bakusui_prefs", Context.MODE_PRIVATE) }
    // 既に許可されているかの簡易チェック
    fun hasUsageAccess(ctx: Context): Boolean {
        return try {
            val appOps = ctx.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
            val mode = appOps.checkOpNoThrow(
                "android:get_usage_stats",
                android.os.Process.myUid(),
                ctx.packageName
            )
            mode == android.app.AppOpsManager.MODE_ALLOWED
        } catch (_: Throwable) { false }
    }

    var min1Expanded by remember { mutableStateOf(false) }
    var min2Expanded by remember { mutableStateOf(false) }
    val minuteOptions = listOf(1, 3, 5, 10, 15)
    var min1 by remember { mutableStateOf(prefs.getInt("shorts_min1", 5)) }
    var min2 by remember { mutableStateOf(prefs.getInt("shorts_min2", 10)) }

    // デバッグ表示は削除（整合性維持のため非表示）

    var isMonitoring by remember { mutableStateOf(false) }

    // 監視対象アプリ（候補）
    data class AppOption(val label: String, val packages: Set<String>)
    val appOptions = listOf(
        AppOption("YouTube", setOf("com.google.android.youtube")),
        AppOption("TikTok", setOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill")),
        AppOption("Instagram", setOf("com.instagram.android")),
        AppOption("X(Twitter)", setOf("com.twitter.android")),
        AppOption("Facebook", setOf("com.facebook.katana")),
        AppOption("LINE", setOf("jp.naver.line.android"))
    )
    val defaultTargets = setOf("com.google.android.youtube", "com.zhiliaoapp.musically", "com.ss.android.ugc.trill")
    var selectedPkgs by remember {
        mutableStateOf(prefs.getStringSet("shorts_targets", null)?.toMutableSet() ?: defaultTargets.toMutableSet())
    }
    var youtubeOnly by remember { mutableStateOf(prefs.getBoolean("shorts_youtube_only", false)) }
    // アクセシビリティ有効化誘導
    fun openAccessibilitySettings(ctx: Context) {
        try {
            ctx.startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Throwable) { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("視聴アラート", style = MaterialTheme.typography.titleLarge)

        // 対象アプリ選択
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("監視対象アプリ", style = MaterialTheme.typography.titleMedium)
            appOptions.forEach { opt ->
                val checked = opt.packages.all { it in selectedPkgs }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(opt.label)
                    Checkbox(checked = checked, onCheckedChange = { isChecked: Boolean ->
                        val newSet = selectedPkgs.toMutableSet()
                        if (isChecked) newSet.addAll(opt.packages) else newSet.removeAll(opt.packages)
                        selectedPkgs = newSet
                        prefs.edit().putStringSet("shorts_targets", newSet).apply()
                    })
                }
                if (opt.label.startsWith("YouTube")) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("YouTubeショートのみ（高精度）")
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Switch(
                                checked = youtubeOnly,
                                onCheckedChange = { checked: Boolean ->
                                    youtubeOnly = checked
                                    prefs.edit().putBoolean("shorts_youtube_only", checked).apply()
                                },
                                enabled = "com.google.android.youtube" in selectedPkgs
                            )
                            Button(
                                onClick = { openAccessibilitySettings(context) },
                                enabled = "com.google.android.youtube" in selectedPkgs
                            ) { Text("設定") }
                        }
                    }
                }
            }
        }

        // しきい値設定
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = { min1Expanded = true }) {
                    Text("1回目: ${min1}分")
                }
                DropdownMenu(expanded = min1Expanded, onDismissRequest = { min1Expanded = false }) {
                    minuteOptions.forEach { m ->
                        DropdownMenuItem(
                            text = { Text("${m}分") },
                            onClick = {
                                min1 = m
                                prefs.edit().putInt("shorts_min1", m).apply()
                                min1Expanded = false
                            }
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = { min2Expanded = true }) {
                    Text("スヌーズ: ${min2}分")
                }
                DropdownMenu(expanded = min2Expanded, onDismissRequest = { min2Expanded = false }) {
                    minuteOptions.forEach { m ->
                        DropdownMenuItem(
                            text = { Text("${m}分") },
                            onClick = {
                                min2 = m
                                prefs.edit().putInt("shorts_min2", m).apply()
                                min2Expanded = false
                            }
                        )
                    }
                }
            }
        }

        FilledIconButton(
            onClick = {
                if (!hasUsageAccess(context)) {
                    try {
                        context.startActivity(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    } catch (_: Throwable) { }
                    return@FilledIconButton
                }
                if (isMonitoring) {
                    val it = Intent(context, ShortsGuardService::class.java).apply {
                        action = ShortsGuardService.ACTION_STOP
                    }
                    context.startService(it)
                    isMonitoring = false
                } else {
                    prefs.edit().putInt("shorts_min1", min1).putInt("shorts_min2", min2).apply()
                    val it = Intent(context, ShortsGuardService::class.java).apply {
                        action = ShortsGuardService.ACTION_START
                        putExtra(ShortsGuardService.EXTRA_MIN1, min1 * 60_000L)
                        putExtra(ShortsGuardService.EXTRA_MIN2, min2 * 60_000L)
                    }
                    ContextCompat.startForegroundService(context, it)
                    isMonitoring = true
                }
            },
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                imageVector = if (isMonitoring) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isMonitoring) "停止" else "開始",
                modifier = Modifier.size(48.dp)
            )
        }

        Button(onClick = {
            try {
                context.startActivity(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (_: Throwable) { }
        }) { Text("権限を開く") }

        // テスト通知（チャネルがブロックされていないか確認用）
        Button(onClick = {
            val it = Intent(context, ShortsGuardService::class.java).apply {
                action = ShortsGuardService.ACTION_TEST
            }
            context.startService(it)
        }) { Text("テスト通知") }

        Button(onClick = onBack) { Text("戻る") }
    }
}

@Composable
private fun HomeScreen(onSelect: (String) -> Unit) {
    val tiles = listOf(
        "random" to "ミックス",
        "sleep" to "やわらかミックス",
        "letter" to "文字ベース",
        "category" to "カテゴリミックス"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("シャッフル内容を選択", style = MaterialTheme.typography.titleLarge)
        val chunked = tiles.chunked(2)
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                rowItems.forEach { (id, label) ->
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .weight(1f, fill = true)
                            .height(96.dp)
                            .clickable { onSelect(id) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutScreen(onBack: () -> Unit) {
    val versionName = jp.hsbl.bakusui.BuildConfig.VERSION_NAME
    val versionCode = jp.hsbl.bakusui.BuildConfig.VERSION_CODE
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        Text("バージョン情報 / ライセンス", style = MaterialTheme.typography.titleLarge)
        Text("アプリ名: バクスイ", style = MaterialTheme.typography.bodyMedium)
        Text("バージョン: ${versionName} (${versionCode})", style = MaterialTheme.typography.bodyMedium)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
        Text("クレジット", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "音声合成: VOICEVOX:四国めたん",
            style = MaterialTheme.typography.bodyMedium
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
        Text("オープンソースライセンス", style = MaterialTheme.typography.titleMedium)
        Text(
            text = """
本アプリは以下のオープンソースソフトウェアを使用しています：

• AndroidX (Core KTX, Lifecycle, Activity Compose) - Apache License 2.0
• Jetpack Compose (UI / Graphics / Material3) - Apache License 2.0
• AndroidX Navigation Compose - Apache License 2.0
• AndroidX Media3 (ExoPlayer) - Apache License 2.0
• Kotlinx Coroutines - Apache License 2.0
• Gson - Apache License 2.0

各ライブラリの詳細なライセンス条文は、それぞれのプロジェクトリポジトリをご確認ください。
            """.trimIndent(),
            style = MaterialTheme.typography.bodySmall
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) { Text("戻る") }
    }
}

@Composable
private fun PlayerScreen(mode: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("bakusui_prefs", Context.MODE_PRIVATE) }
    // サービス制御に切替
    var isPlaying by remember { mutableStateOf(false) }
    var dwellSeconds by remember { mutableIntStateOf(prefs.getInt("dwell_seconds", 7)) }
    var menuExpanded by remember { mutableStateOf(false) }
    val dwellOptions = listOf(0, 1, 2, 3, 5, 7, 10)
    var preview by remember { mutableStateOf<List<String>>(emptyList()) }
    var sequenceSeed by remember { mutableStateOf(System.currentTimeMillis()) }
    var volumePercent by remember { mutableIntStateOf(prefs.getInt("app_volume_percent", 100)) }

    fun computeSequence(files: List<String>, mode: String, seed: Long): List<String> {
        val base = files.filter { it.endsWith(".wav") }
        if (base.isEmpty()) return emptyList()
        val rnd = kotlin.random.Random(seed)
        fun parseMeta(file: String): Triple<String?, String?, Char?> {
            val name = file.removeSuffix(".wav")
            val parts = name.split("__")
            val category = parts.getOrNull(0)
            val text = parts.getOrNull(1)
            val head = text?.firstOrNull()
            return if (parts.size >= 2 && !category.isNullOrBlank() && !text.isNullOrBlank()) {
                Triple(category, text, head)
            } else Triple(null, null, null)
        }
        return when (mode) {
            // やわらかミックス: 隣接の同カテゴリ/同頭文字を避ける
            "sleep" -> {
                val pool = base.shuffled(rnd).toMutableList()
                val out = ArrayList<String>(pool.size)
                var lastCat: String? = null
                var lastHead: Char? = null
                while (pool.isNotEmpty()) {
                    val idx = pool.indexOfFirst {
                        val (cat, _, head) = parseMeta(it)
                        (cat != null && cat != lastCat) && (head != null && head != lastHead)
                    }.let { i -> if (i >= 0) i else pool.indexOfFirst {
                        val (cat, _, _) = parseMeta(it); cat != lastCat
                    }.let { j -> if (j >= 0) j else 0 } }
                    val pick = pool.removeAt(idx)
                    val (cat, _, head) = parseMeta(pick)
                    if (cat != null) lastCat = cat
                    if (head != null) lastHead = head
                    out += pick
                }
                out
            }
            // 文字ベース
            "letter" -> {
                val byHead = base.map { it to parseMeta(it) }
                    .groupBy { it.second.third }
                    .filterKeys { it != null }
                    .mapKeys { it.key!! }
                    .mapValues { (_, list) -> list.map { it.first }.shuffled(rnd).toMutableList() }
                    .toMutableMap()
                if (byHead.isEmpty()) {
                    base.shuffled(rnd)
                } else {
                    // 頭文字の開始位置をランダムにし、尽きたら次の頭文字へ巡回
                    val headOrder = byHead.keys.toList().shuffled(rnd)
                    val out = ArrayList<String>(base.size)
                    var added = true
                    while (added) {
                        added = false
                        for (h in headOrder) {
                            val pool = byHead[h] ?: mutableListOf()
                            if (pool.isNotEmpty()) {
                                out += pool.removeAt(0)
                                added = true
                            }
                        }
                    }
                    out
                }
            }
            // カテゴリミックス
            "category" -> {
                val byCat = base.groupBy { parseMeta(it).first ?: "unknown" }.toMutableMap()
                val catOrder = byCat.keys.shuffled(rnd)
                val out = ArrayList<String>(base.size)
                var added = true
                while (added) {
                    added = false
                    for (c in catOrder) {
                        val list = byCat[c] ?: emptyList()
                        if (list.isNotEmpty()) {
                            val pick = list.shuffled(rnd).first()
                            out += pick
                            byCat[c] = list.filterNot { it == pick }
                            added = true
                        }
                    }
                }
                out
            }
            else -> base.shuffled(rnd)
        }
    }

    fun parseDisplayName(file: String): String {
        val name = file.removeSuffix(".wav")
        if (name.contains("__")) {
            val parts = name.split("__")
            if (parts.size >= 2) {
                val roman = parts[1]
                return roman.replace("-", " ")
            }
        }
        return name
    }

    // プレビュー生成：再生時と同じアルゴリズム・同じseedで先頭N件を表示
    fun generatePreview(): List<String> {
        val files = context.assets.list("audio")?.toList().orEmpty()
        val seq = computeSequence(files, mode, sequenceSeed)
        return seq.take(12).map { parseDisplayName(it) }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val start = Intent(context, PlaybackService::class.java).apply {
                action = PlaybackService.ACTION_START
                putExtra(PlaybackService.EXTRA_MODE, mode)
                putExtra(PlaybackService.EXTRA_DWELL, dwellSeconds)
                putExtra(PlaybackService.EXTRA_SEED, sequenceSeed)
                putExtra(PlaybackService.EXTRA_VOLUME, volumePercent / 100f)
            }
            ContextCompat.startForegroundService(context, start)
        } else {
            val stop = Intent(context, PlaybackService::class.java).apply { action = PlaybackService.ACTION_STOP }
            context.startService(stop)
        }
    }
    LaunchedEffect(mode) {
        sequenceSeed = System.currentTimeMillis()
        preview = generatePreview()
    }
    LaunchedEffect(volumePercent) {
        prefs.edit().putInt("app_volume_percent", volumePercent).apply()
        val vol = Intent(context, PlaybackService::class.java).apply {
            action = PlaybackService.ACTION_VOLUME
            putExtra(PlaybackService.EXTRA_VOLUME, volumePercent / 100f)
        }
        context.startService(vol)
    }
    LaunchedEffect(dwellSeconds) {
        prefs.edit().putInt("dwell_seconds", dwellSeconds).apply()
        if (isPlaying) {
            val dwell = Intent(context, PlaybackService::class.java).apply {
                action = PlaybackService.ACTION_DWELL
                putExtra(PlaybackService.EXTRA_DWELL, dwellSeconds)
            }
            context.startService(dwell)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 選択モードの表示（上部に控えめに）
        val modeLabel = when (mode) {
            "random" -> "ミックス"
            "sleep" -> "やわらかミックス"
            "letter" -> "文字ベース"
            "category" -> "カテゴリミックス"
            else -> mode
        }
        Text(
            text = "モード: $modeLabel",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // モードのシャッフル説明
        val modeDesc = when (mode) {
            "random" -> "一覧をランダムにシャッフルします。"
            "sleep" -> "隣接で同カテゴリ・同頭文字を避けるやわらかなミックス。"
            "letter" -> "頭文字ごとに巡回して並べます（尽きたら次へ）。"
            "category" -> "カテゴリを切り替えながら1つずつ取り出します。"
            else -> ""
        }
        if (modeDesc.isNotEmpty()) {
            Text(
                text = modeDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 再生予定（プレビュー）
        if (preview.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "再生予定",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = preview.joinToString(" ・ "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // アプリ内音量
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
                text = "音量: ${volumePercent}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            androidx.compose.material3.Slider(
                value = volumePercent.toFloat(),
                onValueChange = { volumePercent = it.toInt().coerceIn(0, 100) },
                valueRange = 0f..100f
            )
        }
        FilledIconButton(
            onClick = {
            isPlaying = !isPlaying
            },
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "一時停止" else "再生",
                modifier = Modifier.size(48.dp)
            )
        }
        Button(onClick = { menuExpanded = true }) {
            Text("単語の間隔: ${dwellSeconds}秒")
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            dwellOptions.forEach { sec ->
                DropdownMenuItem(
                    text = { Text("${sec}秒") },
                    onClick = {
                        dwellSeconds = sec
                        prefs.edit().putInt("dwell_seconds", sec).apply()
                        menuExpanded = false
                    }
                )
            }
        }
        // 余計な説明文は表示しない（プルダウンで把握可能）
    }

    // 通知の受信先（Bus）をセット
    DisposableEffect(Unit) {
        PlaybackBus.listener = { action ->
            when (action) {
                PlaybackBus.ACTION_TOGGLE -> {
                    isPlaying = !isPlaying
                    val it = Intent(context, PlaybackService::class.java).apply {
                        this.action = PlaybackService.ACTION_TOGGLE
                    }
                    context.startService(it)
                }
                PlaybackBus.ACTION_STOP -> {
                    isPlaying = false
                    val it = Intent(context, PlaybackService::class.java).apply {
                        this.action = PlaybackService.ACTION_STOP
                    }
                    context.startService(it)
                }
            }
        }
        onDispose {
            PlaybackBus.listener = null
        }
    }
}

private fun showPlaybackNotification(context: android.content.Context, isPlaying: Boolean) {
    val toggleIntent = Intent(context, PlaybackControlReceiver::class.java).apply {
        action = PlaybackBus.ACTION_TOGGLE
    }
    val stopIntent = Intent(context, PlaybackControlReceiver::class.java).apply {
        action = PlaybackBus.ACTION_STOP
    }
    val flags = PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
    val togglePi = PendingIntent.getBroadcast(context, 1, toggleIntent, flags)
    val stopPi = PendingIntent.getBroadcast(context, 2, stopIntent, flags)

    val builder = NotificationCompat.Builder(context, "playback")
        .setSmallIcon(android.R.drawable.ic_media_play)
        .setContentTitle("バクスイ")
        .setContentText(if (isPlaying) "再生中" else "停止中")
        .setOngoing(isPlaying)
        .addAction(
            if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
            if (isPlaying) "一時停止" else "再生",
            togglePi
        )
        .addAction(android.R.drawable.ic_menu_close_clear_cancel, "停止", stopPi)
        .setOnlyAlertOnce(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    with(NotificationManagerCompat.from(context)) {
        notify(1001, builder.build())
    }
}
