# UI設計（音楽プレーヤー風デザイン）

## コンセプト
バクスイは睡眠向けアプリだが、UIは**音楽プレーヤー風**に。  
ユーザーが慣れ親しんだ操作感で、リラックスした状態で使える。

---

## 技術選定

### 推奨: Jetpack Compose + Material Design 3

**選定理由**
1. **Jetpack Compose**: Android公式の最新UIツールキット
   - 宣言的UI（ReactやSwiftUI風）
   - ホットリロードで高速開発
   - Material Design 3との統合が完璧
   
2. **Material Design 3 (Material You)**
   - Dynamic Color（システムカラーに自動適応）
   - Elevated Card / Bottom Sheet など音楽アプリ向けコンポーネント
   - アクセシビリティ対応

**代替案（将来検討）**
- Flutter: クロスプラットフォーム（iOS対応が必要な場合）
- React Native: Web版も作る場合

---

## 画面レイアウト（多機能トップ → 機能別画面）

### 1) トップ（機能セレクタ） → 2) ホーム（認知シャッフル） → 3) 再生画面

```
トップ（機能セレクタ）
┌────────────────────────────┐
│   Top App Bar              │ ← タイトル「バクスイ」
├────────────────────────────┤
│  [ 認知シャッフル睡眠法 ]  [ 視聴アラート ]                │ ← 2列の大きなカード
└────────────────────────────┘

ホーム（タイル：デカ目・Cardベース）
┌────────────────────────────┐
│   Top App Bar              │ ← タイトル「バクスイ」
├────────────────────────────┤
│  [ ランダム ]  [ 寝やすい順 ]                             │ ← 3列グリッド（ElevatedCard + Label、96dp程度の高さ。準備中は非表示）
│  [ 物だけ ]   [ 動物だけ ]      [ 場所だけ ]              │ ← カテゴリ別のタイル（将来ON/OFF）
│                                                        │
│  下部: 「最近の設定: 単語の間隔 7秒」などの軽いサマリ         │
└────────────────────────────┘
- 再生画面上部に「モード: ○○」を表示して、選択内容を明示

再生画面（現行）
┌────────────────────────────┐
│   Top App Bar              │
├────────────────────────────┤
│   【メインカード：現在再生中】                                 │
│   【再生コントロール：▶/||】                                   │
│   【設定：単語の間隔（プルダウン）】                           │
└────────────────────────────┘
```

---

## 視聴アラート（YouTube/TikTok等の継続視聴アラート）

### 画面構成
- タイトル: 「視聴アラート」
- 監視対象アプリ（複数選択）
  - YouTube / TikTok / Instagram / X(Twitter) / Facebook / LINE
  - YouTube行の下に「YouTubeショートのみ（高精度）」スイッチ＋「設定」ボタン（アクセシビリティ設定を開く）
- しきい値
  - 「1回目: N分」（初回通知）
  - 「スヌーズ: M分」（以降の通知間隔）
- 開始/停止トグル（▶/⏸）
- 権限ボタン（使用状況へのアクセス）

### 仕様
- 名称統一: 機能名/通知タイトルともに「視聴アラート」
- 通知チャネル
  - 視聴アラート（監視）: 重要度MIN（常時表示、静音）
  - 視聴アラート（アラート）: 重要度HIGH（到達時のみ表示）
- 前面アプリ検出: UsageStats（RESUMED優先）、必要時アクセシビリティでYouTubeショートを高精度判定
- YouTubeショート限定（ON時）
  - 優先: アクセシビリティが直近（≤120秒）で「Shorts/ショート」を示していればショート扱い
  - 次点: クラス名に"Short/Shorts"を含めばショート扱い
  - 最後: 直近のUsageEventsを走査してYouTubeのShorts系クラスがあればショート扱い
- 計測
  - 対象一致（選択アプリ、かつショート条件）の間だけ「累積時間」を加算
  - 対象外に出ても累積は保持し、再度対象に戻れば加算再開
  - 初回N分で通知、以降はスヌーズ間隔ごとに通知
  - 設定変更（対象アプリ/ショートのみ）で累積と通知状態をリセット
  - 最後にショート対象だった時刻から6時間連続で非対象なら累積を破棄

### 開発/端末注意
- アクセシビリティ許可は端末や再インストールで無効化される場合があるため、必要時は「設定」から再有効化
- 省電力の最適化解除、自動起動許可を推奨

---

## デザイン要素

### 1. Top App Bar
```kotlin
@Composable
fun BakusuiTopBar() {
    TopAppBar(
        title = { Text("バクスイ") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
```

### 2. メインカード（現在再生中）
- **Elevated Card** を使用（Material 3）
- 中央に大きく単語表示
- Dynamic Colorでアクセント

```kotlin
@Composable
fun CurrentWordCard(word: String, category: String) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = word,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "カテゴリ: $category",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

### 3. 再生コントロール
- **FilledIconButton** (Material 3)
- アイコンはMaterial Icons

```kotlin
@Composable
fun PlayPauseButton(isPlaying: Boolean, onToggle: () -> Unit) {
    FilledIconButton(
        onClick = onToggle,
        modifier = Modifier.size(80.dp)
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "一時停止" else "再生",
            modifier = Modifier.size(48.dp)
        )
    }
}
```

補足:
- 先頭に「イントロアナウンス（assets/intro）」を1件ランダムで再生し、その後に本編を開始。
- イントロ直後に一拍（既定3秒）の無音ポーズを挿入してから最初のワードへ遷移。

### 4. インターバル調整スライダー
```kotlin
@Composable
fun IntervalSlider(
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "インターバル: ${value.toInt()}ms",
            style = MaterialTheme.typography.titleMedium
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 500f..3000f,
            steps = 24
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("500ms", style = MaterialTheme.typography.bodySmall)
            Text("3000ms", style = MaterialTheme.typography.bodySmall)
        }
    }
}
```

### 5. シャッフルモード選択
```kotlin
enum class ShuffleMode(val displayName: String) {
    RANDOM("ランダム"),
    SLEEP_OPTIMIZED("寝やすい順序"),
    LETTER_BASED("文字ベース（準備中）")
}
```

### 6. ホーム（タイル）
```kotlin
data class ShuffleTile(val id: String, val title: String, val subtitle: String)

val defaultTiles = listOf(
  ShuffleTile("random", "ランダム", "安全語から無限シーケンス"),
  ShuffleTile("sleep", "寝やすい順序", "短い/想像しやすい優先"),
  ShuffleTile("letter", "文字ベース", "準備中"),
)
```
- タイルは3列グリッド（大きめElevatedCard、Label中心／96dp程度、高コントラスト）
- タップで`PlayerScreen`へ遷移し、選択モードをパラメータで渡す

### 7. ナビゲーション（2画面）
- ルート: `start`, `home`, `player?mode={mode}`, `shorts`
- 遷移: トップで機能選択 → 認知シャッフルなら`home` → `player?mode=...`

```kotlin
NavHost(navController, startDestination = "start") {
  composable("start") { StartScreen(onSelectShuffle = { nav.navigate("home") }, onSelectShorts = { nav.navigate("shorts") }) }
  composable("home") { HomeScreen(onSelect = { mode ->
    navController.navigate("player?mode=$mode")
  }) }
  composable("player?mode={mode}") { backStackEntry ->
    val mode = backStackEntry.arguments?.getString("mode") ?: "random"
    PlayerScreen(mode = mode) // 画面上部に「モード: ○○」を表示。下にプレビューも表示
  }
  composable("shorts") { ShortsGuardScreen() }
}
```

### 8. 再生予定プレビュー（デバッグ補助）
- 再生ボタン上に、現在モードに基づく先頭12件の「再生予定」をテキストで表示
- 並びはモード変更時に自動更新（ボタンはなし）

---

## カラーテーマ

### Sleep Purple（落ち着いた紫のブランド色）
- Light:
  - primary: `#6A5AAE`
  - secondary: `#5F5970`
  - tertiary: `#8A79A7`
- Dark:
  - primary: `#CAB8FF`
  - secondary: `#BEB3D4`
  - tertiary: `#E0C9F2`

指針:
- 低彩度・やや暗めの紫を基調にして眩しさを抑える
- 強いアクセント色は避け、面はprimaryContainer/onSurfaceVariantで穏やかに
- ダークモードを既定、Dynamic Colorは端末テーマに追従（未対応端末は上記にフォールバック）

### Dynamic Color（Material You）対応
```kotlin
@Composable
fun BakusuiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) 
            else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = Color(0xFF6750A4),
            secondary = Color(0xFF625B71),
            tertiary = Color(0xFF7D5260)
        )
        else -> lightColorScheme(
            primary = Color(0xFF6750A4),
            secondary = Color(0xFF625B71),
            tertiary = Color(0xFF7D5260)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

### 睡眠向けの配慮
- **ダークモード優先**: 夜間使用が前提
- **低彩度**: 目に優しい色味
- **Dynamic Color**: ユーザーの好みの色に自動適応

---

## アニメーション

### 再生中の視覚フィードバック
```kotlin
@Composable
fun PulsingWordCard(word: String, isPlaying: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    ElevatedCard(
        modifier = Modifier.scale(scale)
    ) {
        Text(text = word)
    }
}
```

### スライドトランジション
```kotlin
AnimatedContent(
    targetState = currentWord,
    transitionSpec = {
        slideInHorizontally { width -> width } + fadeIn() with
        slideOutHorizontally { width -> -width } + fadeOut()
    }
) { word ->
    CurrentWordCard(word, category)
}
```

---

## 参考デザイン

### 参考にする音楽プレーヤー
1. **YouTube Music**: シンプルで直感的
2. **Spotify**: カード主体のレイアウト
3. **Apple Music**: 大きなアルバムアート風表示

### Material Design 3 公式リソース
- [Material Design 3 Components](https://m3.material.io/components)
- [Media controls guidelines](https://m3.material.io/foundations/interaction/states)
- [Color system](https://m3.material.io/styles/color/overview)

---

## 実装優先順位

**Phase 1（プロトタイプ）**
1. ホーム（タイル）画面の追加（3列グリッド＋ナビゲーション）
2. 再生画面（現行UIに追従）
3. インターバル（単語の間隔）プルダウン＋保存/復元
4. イントロ一回再生＋一拍ポーズ

**Phase 2（拡張）**
1. アニメーション追加
2. ダークモード最適化
3. アクセシビリティ対応

**Phase 3（最適化）**
1. 音声追加UI実装
2. 統計表示（再生回数等）
3. カスタムテーマ

