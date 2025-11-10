## PC 音声生成ツールの使い方（VOICEVOX）

この手順で `docs/SAMPLE_WORD_LIST_ja.md`（category,text 形式）から音声パックを生成します。

### 0) 前提
- VOICEVOX ENGINE が起動している（デフォルト: `http://localhost:50021`）
- Python 3.x が使える環境

VOICEVOX ENGINE の起動手順は `docs/VOICEVOX_SETUP.md` を参照。

### 1) 単語リストを用意
- `docs/SAMPLE_WORD_LIST_ja.md` に最低1000行以上を記載（形式: `category,text`）
- 例: `object,りんご`

### 2) 生成コマンド（Windows PowerShell 例）
```powershell
# ずんだもん／ささやき で生成する例
python pc_generator/main.py `
  --from-md docs/SAMPLE_WORD_LIST_ja.md `
  --speaker-name "ずんだもん" `
  --style-name "ささやき" `
  --pack-name "basic_ja" `
  --output-dir "packs" `
  --filename "spec" `
  --out-subdir "audio" `
  --zip
```

主なオプション
- `--speaker-id` または `--speaker-name` + `--style-name` で声色指定
- `--speed-scale 0.95` や `--pitch-scale -0.2` で落ち着いたトーンに調整可
- `--filename spec` にすると `docs/AUDIO_FILENAME_SPEC.md` に準拠した命名になります

出力
- `packs/<pack_name>/audio/*.wav`
- `packs/<pack_name>/pack.json`（`--no-pack-json` を付けなければ生成）
- `packs/<pack_name>.zip`（`--zip` の場合）

### 3) 所要時間の目安
- デフォルト実装は1項目ごとに API 負荷軽減のため約0.5秒の待機あり
- 1000語でおおよそ 8〜10 分程度（マシンやネットワークに依存）

### 4) コツ・運用メモ
- 語彙は `object`（具象名詞）中心に増やすとイメージしやすく効果的
- 重複行は避ける（同一ファイル名になり上書きされることがあります）
- 生成失敗が出た語は、後から該当行だけ抽出して再実行すると効率的

### 5) トラブルシューティング
- ENGINE に接続できない → `http://localhost:50021/version` をブラウザで開き応答確認
- 話者/スタイルが見つからない → `--speaker-id` で直接 styleId を指定
- 日本語ファイル名の不具合 → 既定の `spec` 命名を利用（ローマ字化されます）


