# 音声管理DB設計（追加入力に強い）

## 目的
- assets直置きの初期音声に加えて、**あとから追加された音声（USER/LOCAL/DOWNLOAD）** を拾える設計にする
- メタ情報（カテゴリ・感情・再生時間・再生履歴）をDBで一元管理

---

## エンティティ
```kotlin
@Entity(tableName = "audio_words")
data class AudioWord(
    @PrimaryKey val id: String,              // 論理ID（拡張子なし）
    val fileName: String,                    // 実ファイル名（word_001.wav）
    val filePath: String,                    // 再生に使うパス（assets/.. or storage/..）
    val source: String,                      // "asset" | "user" | "local" | "download"
    val kind: String = "word",               // "word" | "intro"（冒頭アナウンス等）
    val text: String,                        // 表示テキスト（りんご）
    val category: String,                    // object/animal/abstract/...（自由）
    val duration: Long,                      // ms
    val isImaginable: Boolean = true,
    val isEmotionallyNeutral: Boolean = true,
    val playCount: Int = 0,
    val lastPlayedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

### インデックス例
- `kind, category`
- `source, category`
- `playCount, lastPlayedAt`

---

## 取り込みポリシー

### 1) 初期取り込み（assets → 一度きり）
- 単語群（通常再生用）: `assets/audio/*.wav` を列挙
- ID生成: `fileName.removeSuffix(".wav")`
- filePath: `assets://audio/<fileName>` の擬似スキームで表現（プレーヤー層で解決）
- duration取得は**バックグラウンド**で順次

#### イントロ音声（冒頭アナウンス）
- `assets/intro/*.wav` を列挙
- `kind="intro"`, `category="system"` として投入（textは表示用に任意）
- filePath: `assets://intro/<fileName>`

### 2) 追加入力（USER/LOCAL/DOWNLOAD → 随時）
- 監視対象フォルダ（例）:
  - `Android/data/<app>/files/packs/`
  - `Music/Bakusui/` など
- `rescanAudioFolders(ctx)` を随時呼べるAPIとして実装
  - 単語: `.../audio/` 配下、イントロ: `.../intro/` 配下を対象
  - 取り込み時にフォルダ名（audio/intro）から `kind` を推定

```kotlin
suspend fun rescanAudioFolders(ctx: Context) {
    // 1. 対象フォルダ列挙
    // 2. 新規ファイルのみDBにinsert（既存idはスキップ or 更新ポリシー）
    // 3. durationはWorkManagerで非同期採取
}
```

---

## メタ情報の付与方法

### a) ファイル名エンコード方式
- 例: `object_apple_neutral.wav`
- パース例:
  - category=object, text=apple, isEmotionallyNeutral=true

### b) サイドカーJSON方式
- `apple.wav` と同じ場所に `apple.json`
```json
{
  "text": "りんご",
  "category": "object",
  "isImaginable": true,
  "isEmotionallyNeutral": true
}
```
- JSONがあればDB項目を上書き

### c) 初期CSV方式（開発時）
- `audio_meta.csv` に行追加で一括投入

---

## クエリ設計（例）
```kotlin
// 安全語の集合（順序は付けない＝アプリ側で整列）※ イントロは除外
@Query("""
  SELECT * FROM audio_words
  WHERE kind = 'word' AND isImaginable = 1 AND isEmotionallyNeutral = 1
""")
suspend fun getSafeWords(): List<AudioWord>

// カテゴリ別
@Query("SELECT * FROM audio_words WHERE category = :cat")
suspend fun byCategory(cat: String): List<AudioWord>

// セッション冒頭のアナウンス候補（ランダムで1つ再生）
@Query("SELECT * FROM audio_words WHERE kind = 'intro'")
suspend fun getIntroPhrases(): List<AudioWord>
```

> 並べ替えは**アプリ側**（シャッフルロジック）に統一。DBでは集合のみ返し、二重の順序付けを避ける。

---

## 起動パフォーマンス
- 初期描画をブロックしない
- WorkManagerでバックグラウンド取り込み＋duration採取
- UIは“取り込み中”で逐次更新

---

## 再入眠シーケンス
- 10〜30語（約1〜3分）を目安に **短尺** を出せるAPIを用意

---

## マイグレーション備忘
- 将来の列追加（例: `language`, `tags`）に備えて `Room.migration` を用意
- sourceやfilePathの既存行はデフォルトを埋める（asset推定）
