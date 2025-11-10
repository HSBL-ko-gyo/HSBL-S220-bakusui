package jp.hsbl.bakusui.player

import android.content.Context
import android.media.MediaPlayer
import android.media.AudioAttributes
import android.content.res.AssetFileDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.random.Random

class AudioPlayer(
    private val appContext: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) {
    private var loopJob: Job? = null
    private var currentPlayer: MediaPlayer? = null
    @Volatile private var stopRequested: Boolean = false
    @Volatile private var appVolume: Float = 1.0f
    @Volatile private var introPlayed: Boolean = false
    @Volatile private var dwellSecondsCurrent: Int = 7

    fun isPlaying(): Boolean = loopJob?.isActive == true

    fun setVolume(volume: Float) {
        val v = volume.coerceIn(0f, 1f)
        appVolume = v
        currentPlayer?.setVolume(v, v)
    }

    // --- シーケンス生成用の簡易メタパース（ファイル名ルール: cat__text_roman__... .wav を想定） ---
    private data class Meta(val category: String?, val textRoman: String?, val head: Char?)

    private fun parseMeta(fileName: String): Meta {
        val base = fileName.removeSuffix(".wav")
        val parts = base.split("__")
        val category = parts.getOrNull(0)
        val textRoman = parts.getOrNull(1)
        val head = textRoman?.firstOrNull()
        // 形式に合わない場合はnull
        val valid = parts.size >= 2 && category?.isNotBlank() == true && textRoman?.isNotBlank() == true
        return if (valid) Meta(category, textRoman, head) else Meta(null, null, null)
    }

    private fun buildSequence(mode: String, files: List<String>, seed: Long): List<String> {
        val wavs = files.filter { it.endsWith(".wav") }
        if (wavs.isEmpty()) return emptyList()
        val rnd = Random(seed)
        return when (mode) {
            // やわらかミックス: 隣接で同カテゴリ/同頭文字を避ける貪欲法
            "sleep" -> {
                val pool = wavs.shuffled(rnd).toMutableList()
                val out = ArrayList<String>(pool.size)
                var lastCat: String? = null
                var lastHead: Char? = null
                while (pool.isNotEmpty()) {
                    // 避け条件に合う候補を優先
                    val idx = pool.indexOfFirst {
                        val m = parseMeta(it)
                        (m.category != null && m.category != lastCat) && (m.head != null && m.head != lastHead)
                    }.let { i -> if (i >= 0) i else pool.indexOfFirst {
                        val m = parseMeta(it); m.category != lastCat
                    }.let { j -> if (j >= 0) j else 0 } }
                    val pick = pool.removeAt(idx)
                    val meta = parseMeta(pick)
                    if (meta.category != null) lastCat = meta.category
                    if (meta.head != null) lastHead = meta.head
                    out += pick
                }
                out
            }
            // 文字ベース: 頭文字ごとに巡回（尽きたら次の頭文字へ）。開始頭文字はランダム。
            "letter" -> {
                val byHead = wavs.map { it to parseMeta(it) }
                    .groupBy { it.second.head }
                    .filterKeys { it != null }
                    .mapKeys { it.key!! }
                    .mapValues { (_, list) -> list.map { it.first }.shuffled(rnd).toMutableList() }
                    .toMutableMap()
                if (byHead.isEmpty()) {
                    wavs.shuffled(rnd)
                } else {
                    val headOrder = byHead.keys.toList().shuffled(rnd)
                    val out = ArrayList<String>(wavs.size)
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
            // カテゴリミックス: カテゴリごとに1件ずつラウンドロビン
            "category" -> {
                val byCat = wavs.groupBy { parseMeta(it).category ?: "unknown" }.toMutableMap()
                val catOrder = byCat.keys.shuffled(rnd)
                val out = ArrayList<String>(wavs.size)
                var added = true
                while (added) {
                    added = false
                    for (c in catOrder) {
                        val list = byCat[c] ?: emptyList()
                        if (list.isNotEmpty()) {
                            // 先頭をランダムに選んで消費
                            val pick = list.shuffled(rnd).first()
                            out += pick
                            byCat[c] = list.filterNot { it == pick }
                            added = true
                        }
                    }
                }
                out
            }
            else -> wavs.shuffled(rnd) // ミックス（完全ランダム）
        }
    }

    fun start(mode: String = "random", dwellSeconds: Int = 7, seed: Long? = null) {
        if (isPlaying()) return
        loopJob = scope.launch {
            stopRequested = false
            withContext(Dispatchers.IO) {
                val assets = appContext.assets
                dwellSecondsCurrent = dwellSeconds
                // 先頭プリロール（intro からランダム1件）※アプリ起動中は一度だけ
                val introList = assets.list("intro")?.filter { it.endsWith(".wav") } ?: emptyList()
                if (!introPlayed && introList.isNotEmpty()) {
                    val intro = introList.random()
                    playOnceFromAssets(intro, folder = "intro")
                    // 一拍（約1〜3秒）待ってから本編へ
                    if (!isActive) return@withContext
                    if (stopRequested) return@withContext
                    // 端末遷移直後に「音が消えた」と感じにくいよう、待機を短縮
                    delay(1000)
                    if (stopRequested) return@withContext
                    introPlayed = true
                }

                val all = assets.list("audio")?.filter { it.endsWith(".wav") }?.toMutableList() ?: mutableListOf()
                if (all.isEmpty()) return@withContext
                while (isActive) {
                    val baseSeed = seed ?: System.currentTimeMillis()
                    val sequence = buildSequence(mode, all, baseSeed)
                    for (name in sequence) {
                        if (!isActive) break
                        playOnceFromAssets(name, folder = "audio")
                        if (stopRequested) return@withContext
                        // 想像時間のための無音待機
                        val dwell = dwellSecondsCurrent.coerceAtLeast(0)
                        repeat(dwell) {
                            if (!isActive) return@withContext
                            if (stopRequested) return@withContext
                            delay(1000)
                        }
                    }
                }
            }
        }
    }

    fun setDwellSeconds(seconds: Int) {
        dwellSecondsCurrent = seconds.coerceAtLeast(0)
    }

    /** 次回startでイントロ再生を有効化（必要時のみ使用） */
    fun enableIntroNextStart() {
        introPlayed = false
    }

    fun stop() {
        loopJob?.cancel()
        loopJob = null
        currentPlayer?.run {
            try { stop() } catch (_: Throwable) {}
            try { release() } catch (_: Throwable) {}
        }
        currentPlayer = null
    }

    /** 現在の音声が終わったら停止する要求を出す（即停止はしない） */
    fun requestStopAfterCurrent() {
        stopRequested = true
    }

    private fun playOnceFromAssets(fileName: String, folder: String = "audio") {
        val afd: AssetFileDescriptor = try {
            appContext.assets.openFd("$folder/$fileName")
        } catch (e: IOException) {
            return
        }
        val mp = MediaPlayer()
        currentPlayer = mp
        try {
            // 他アプリとの同時再生を許容するため、通常のUSAGE_MEDIA/CONTENT_TYPE_SPEECHで再生
            // （本アプリ側ではAudioFocusを取得しない＝フォーカス変化に反応しない）
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mp.setOnCompletionListener {
                try { it.release() } catch (_: Throwable) {}
            }
            mp.prepare()
            mp.setVolume(appVolume, appVolume)
            mp.start()
            // ブロッキングで待つ（短尺前提）
            while (mp.isPlaying) {
                Thread.sleep(50)
            }
        } catch (_: Throwable) {
            try { mp.release() } catch (_: Throwable) {}
        } finally {
            currentPlayer = null
        }
    }
}
