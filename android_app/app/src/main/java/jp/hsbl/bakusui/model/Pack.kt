package jp.hsbl.bakusui.model

import com.google.gson.annotations.SerializedName

/**
 * 音声パックのデータクラス
 */
data class Pack(
    val version: Int,
    val language: String,
    val name: String,
    @SerializedName("audio_dir")
    val audioDir: String,
    val items: List<PackItem>
)

