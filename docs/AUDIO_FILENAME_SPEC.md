# 音声ファイル名仕様（生成ツール用・アプリ連携）

目的: 生成側とアプリ側の解釈を揃えるため、ファイル名に最小限のメタを埋める。

## 命名ルール（推奨・現在の実装に整合）
```
<category>__<text_roman>__[<tone>__spk<speakerId>__s<speed>__p<pitch>].wav
```
- 区切り: `__`（ダブルアンダースコア）
- ファイル拡張子: `.wav`
- 文字種: 半角英数とハイフン（`-`）のみ推奨

### 必須
- `category`: 例）`object` | `animal` | `place` | `action` | `abstract` など
- `text_roman`: 単語のローマ字（空白は `-` で表現）

### 任意（あれば保持・UIには表示しない）
- `tone`: 例）`whisper` | `soft` | `normal`
- `speakerId`: VOICEVOX等のstyleId（`spk2` のように表記）
- `s<speed>`: 速度倍率 ×100（例：`s95` = 0.95）
- `p<pitch>`: ピッチ相対（10分の1半音相当。例：`p-2` = -0.2）

## 例
```
object__ringo__whisper__spk2__s95__p-2.wav
animal__risu.wav
place__kouen__soft__spk1.wav
```

## アプリ側の利用（現在の`AudioPlayer`/UIプレビュー）
- カテゴリ推定: 先頭トークン（`category`）
- 表示名（簡易）: 2番目のトークン（`text_roman`）を抽出し、`-` を空白に変換
- 頭文字（Letter-based用）: `text_roman` の先頭1文字
- 場面シャッフル: `category == "place"` を優先
- やわらかミックス: 直前と「同一カテゴリ/同一頭文字」を避ける並びを貪欲に生成

> 先頭2トークン（`category` と `text_roman`）が揃っていれば、すべてのシャッフルモードが有効に働きます。任意トークンはあってもなくても構いません。

## 注意
- 日本語ファイル名は端末・ZIPで差異が出やすいので、`text_roman`（ローマ字）推奨
- `__` の数が足りない場合、アプリはフォールバック（カテゴリ/文字が不明扱い）します
- 可能なら生成ツール側で規約に沿った命名を徹底してください
