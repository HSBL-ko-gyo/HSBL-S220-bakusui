# シャッフルロジック設計（修正版）

## 認知シャッフル睡眠法（Cognitive Shuffle / Serial Diverse Imagining）

- 考案者: Luc P. Beaudoin（Simon Fraser University）
- 背景理論: SIP（Somnolent Information Processing）
- 目的: **insomnolent**（眠りを遠ざける）思考（心配・計画・反すう等）を、**counter‑insomnolent** な多様イメージで上書きし、入眠直前のバラバラな心象（hypnagogic）を意図的に先取りする
- 別名: SDI（Serial Diverse Imagining）

### 実践の要点
- 1アイテムの滞在時間は 5〜10秒（最大15秒）
- アイテム条件: 想像しやすい／感情的に中性〜やや楽しい／脅威や仕事は除外
- 用途: 就寝時＋夜間覚醒後の再入眠
- 前提: 1語音声は短い（1〜2秒）。→ 個々の語に対する「スキップ」操作は前提としない（無限シーケンスに身を任せる）

---

## 実装パターン

### 1) 文字ベース（自力SDI / Cue Word）
- cue語（日本語では「2〜5モーラ」程度）を選ぶ
- 各文字で始まる単語を1つずつ、5〜10秒で次へ（“複数を長く列挙”ではない）

### 2) 完全ランダム（技術支援型）
- アプリが“安全で想像しやすい”単語をランダム提示
- 無限に吐き続け、途中で寝落ちても良い設計（1パスで終わらない）

### 3) カテゴリ混合
- 人・場所・行動・物などの“関係づけにくい”カテゴリを交互に織り交ぜる

---

## バクスイの方針（プロトタイプ）

### 先頭プリロール（イントロ一回再生）
- セッション開始時に `kind='intro'` の音声からランダムで1件を再生
- 再生は一度だけ。以降は通常シーケンスへ遷移
- 配置規約: `assets/intro/*.wav`（DBでは `kind='intro'`, `category='system'`）

```kotlin
suspend fun playIntroIfAvailable(dao: Dao, player: PlayerFacade) {
  val intros = dao.getIntroPhrases()
  if (intros.isNotEmpty()) {
    val intro = intros.random()
    player.prepareNext(listOf(intro))
    player.play(intro)
  }
}
```

### モードA: 完全ランダム（無限シーケンス）
```kotlin
fun randomSequence(base: List<AudioWord>): Sequence<AudioWord> = sequence {
    val pool = base.toMutableList()
    while (true) {
        pool.shuffle()
        for (w in pool) yield(w)
    }
}
```

### モードB: 寝やすい順序（SDI最適化）
- 音声は1〜2秒程度を優先し、ユーザの想像時間（5〜10秒）を確保
- 並べ替え基準：短い/想像しやすい/中性/カテゴリ連続回避

#### カテゴリバランス（局所シャッフル）
```kotlin
fun balancedBlocks(words: List<AudioWord>, blockSize: Int = 3): List<AudioWord> {
    val byCat = words.groupBy { it.category }
    val catOrder = byCat.keys.shuffled()
    val interleaved = mutableListOf<AudioWord>()
    var i = 0
    while (interleaved.size < words.size) {
        for (cat in catOrder) {
            val list = byCat[cat] ?: emptyList()
            if (i < list.size) interleaved += list[i]
        }
        i++
    }
    return interleaved.windowed(blockSize, blockSize, partialWindows = true)
        .flatMap { it.shuffled() }
}
```

### プリロードと動的差し替え
- **プリロード**: 次の1〜2アイテムを事前`prepare`して切替のポップ音を抑制
- **動的差し替え**: 再生中にカテゴリ偏り検知/再入眠切替があれば、次ブロックを差し替え（Playerへ更新通知）

---

## データ取得と順序付けの責務
- DBは集合まで、順序はアプリ側（重複した順序付けを避ける）

## SDI向けシーケンス長
- 再入眠用は 10〜30語（約1〜3分）

## セッション開始フロー（例）
1. イントロ候補取得 → ランダム1件を再生（任意）
2. 安全語集合を取得（`getSafeWords()`）
3. モードに応じてブロック生成（ランダム/寝やすい順序）
4. Playerに次の1〜2件を `prepare` → 1件ずつ再生
5. 各件後に5〜10秒の無音待機（Engine側タイマ）

## 参考の整理
- 理論: SIP / SDI（Beaudoin）
- 実装例: mySleepButton（Beaudoinの会社）

## 補足
- 初期スキャンのduration取得は非同期で（WorkManager等）
- isImaginable / isEmotionallyNeutral はメタCSV/サイドカーで上書き可能

---

## 関連ドキュメント
- シャッフルの型（頭文字／カテゴリ／場面）の整理と実装指針は `docs/SHUFFLE_VARIATIONS.md` を参照
