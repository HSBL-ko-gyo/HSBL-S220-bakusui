# VOICEVOX セットアップガイド

## VOICEVOX とは

無料で使える高品質な音声合成ソフトウェア。バクスイでは**VOICEVOX ENGINE**（API版）を使用します。

## インストール手順

### 1. VOICEVOX ダウンロード

公式サイト: https://voicevox.hiroshiba.jp/

1. 「ダウンロード」をクリック
2. お使いのOSを選択（Windows、Mac、Linux）
3. **対応モード**: GPUがあれば「GPU/CPU」、なければ「CPU」を選択
4. **パッケージ**: 「インストーラー」を選択
5. ダウンロード

### 2. インストール

#### Windows
- ダウンロードした `voicevox-x.x.x.exe` を実行
- セキュリティ警告が出たら「詳細情報」→「実行」をクリック
- インストールウィザードに従って完了

#### Mac
- ダウンロードした `.dmg` を開く
- アプリをアプリケーションフォルダにドラッグ＆ドロップ
- 初回起動時に警告が出たら、右クリック→「開く」

### 3. 初回起動

1. VOICEVOXを起動
2. 音声データが自動ダウンロードされる（ネット環境が必要）
3. 初回は少し時間がかかります

## VOICEVOX ENGINE の起動

バクスイのPCツールは**VOICEVOX ENGINE**（API版）を使います。ENGINEはGUI版とは別に起動する必要があります。

### 方法1: vv-engineフォルダから起動

VOICEVOXをインストールしたフォルダ内に`vv-engine`フォルダがあります。その中に`run.exe`があります。

**Windows:**
```powershell
# VOICEVOXのインストールフォルダに移動
cd "$env:LOCALAPPDATA\Programs\VOICEVOX"

# vv-engineフォルダに移動
cd vv-engine

# ENGINEを起動
.\run.exe
```

**Mac/Linux:**
```bash
# VOICEVOXのインストールフォルダに移動
cd ~/.local/share/VOICEVOX
# または
cd /usr/local/share/VOICEVOX

# vv-engineフォルダに移動
cd vv-engine

# ENGINEを起動
./run
```

**注意**: もし`vv-engine`フォルダ内に`run.exe`が見つからない場合は、方法2（GitHubから直接ダウンロード）を使用してください。

### 方法2: GitHubから直接ダウンロード（推奨）

VOICEVOX ENGINEを直接ダウンロードする場合：

1. **GitHub Releases** にアクセス: https://github.com/VOICEVOX/voicevox_engine/releases
2. お使いのOS用の最新リリースをダウンロード
   - Windows: `voicevox_engine-x.x.x-windows-x64.zip`
   - Mac (Intel): `voicevox_engine-x.x.x-macos-x64.zip`
   - Mac (Apple Silicon): `voicevox_engine-x.x.x-macos-arm64.zip`
   - Linux: `voicevox_engine-x.x.x-linux-x64.zip`
3. ZIPを解凍
4. 解凍したフォルダ内の`run.exe`（Windows）または`run`（Mac/Linux）を実行

### 起動の確認

ENGINEが起動すると、コンソールに以下のようなメッセージが表示されます：

```
INFO:     Uvicorn running on http://127.0.0.1:50021 (Press CTRL+C to quit)
```

またはブラウザで `http://localhost:50021` にアクセスして、APIドキュメントが表示されることを確認してください。

## 動作確認

ENGINEが起動していることを確認：

### 方法1: ブラウザで確認
```
http://localhost:50021
```
APIドキュメントが表示されればOKです。

### 方法2: コマンドラインで確認

**PowerShell (Windows):**
```powershell
curl http://localhost:50021/version
```

**コマンドプロンプト (Windows):**
```cmd
curl http://localhost:50021/version
```

**Mac/Linux:**
```bash
curl http://localhost:50021/version
```

バージョン情報が表示されればENGINEは正常に動作しています。

## トラブルシューティング

### ENGINEが起動しない
- インストールフォルダに`run.exe`（Windows）または`run`（Mac/Linux）が存在するか確認
- ファイアウォールがポート50021をブロックしていないか確認
- 他のアプリがポート50021を使っていないか確認（別のポートで起動する場合は`pc_generator/main.py`の`engine_url`を変更）
- コンソールエラーメッセージを確認

### 音声が生成されない
- ENGINEが起動しているか確認（`http://localhost:50021`にアクセス）
- `pc_generator/main.py`の`engine_url`が正しいか確認
- ネットワーク接続が正常か確認
- VOICEVOX ENGINEが最新版か確認

## 参考リンク

- 公式サイト: https://voicevox.hiroshiba.jp/
- GitHub: https://github.com/VOICEVOX/voicevox_engine
- 使い方: https://voicevox.hiroshiba.jp/how_to_use/

## バクスイとの連携

VOICEVOX ENGINEが起動したら、バクスイのPC生成ツールが使えます：

```bash
cd pc_generator
pip install -r requirements.txt
python main.py
```

---

## 将来の計画

将来的には、VOICEVOX ENGINEをプロジェクトに内包する予定です。

- VOICEVOX ENGINEは**LGPL-3.0ライセンス**のため、プロジェクトに含めることが可能です
- ユーザーが手動でENGINEを起動する必要がなくなり、より使いやすくなります
- ライセンス要件を満たすため、プロジェクトのLICENSEファイルにLGPL-3.0の記載を追加します

実装時は、`pc_generator`フォルダ内にENGINEを配置し、自動起動機能を追加する予定です。

## スタイル（例: ささやき）の選び方

VOICEVOXは**話者（speaker）**の中に複数の**スタイル（style）**を持ちます。スタイル名が「ささやき」のものがあれば、指定するだけで“ささやき声”が生成できます。

### 1) 利用可能な話者/スタイルを確認
```
curl http://localhost:50021/speakers | jq ".[0]"
```
出力例（抜粋）:
```json
[
  {
    "name": "四国めたん",
    "speaker_uuid": "...",
    "styles": [
      { "id": 2, "name": "ノーマル" },
      { "id": 6, "name": "ささやき" }
    ]
  }
]
```
→ `name: ささやき` の `id` が **styleId**（= `speaker` パラメータの値）

### 2) audio_query → synthesis（パラメータ微調整）
```bash
# 例: ささやき風（styleId=6）
curl -s -X POST "http://localhost:50021/audio_query?text=りんご&speaker=6" \
 | jq '.speedScale=0.95 | .pitchScale=-0.2 | .intonationScale=0.85 | .volumeScale=0.9' \
 | curl -s -X POST -H "Content-Type: application/json" \
        "http://localhost:50021/synthesis?speaker=6" \
        -d @- --output audio_ringo_whisper.wav
```
- スタイルIDで“ささやき”が無い話者の場合は、`speed/pitch/intonation/volume` を軽く下げると近いニュアンスにできます。

> 注: 実際のスタイル名/IDは環境によります。`/speakers` で都度確認してください。

---

## バクスイ向けのファイル名設計
- 命名規則は `docs/AUDIO_FILENAME_SPEC.md` を参照
- 例: `object__ringo__whisper__spk1__s95__p-2.wav`

---

## 動作確認
```
curl http://localhost:50021/version
```

## トラブルシューティング
- ENGINEが起動しない → フォルダ/ポート/権限を確認
- 生成が遅い → 同時実行を減らす/速度やイントネーションを少し下げる
- スタイルが見つからない → `/speakers` の結果で確認（別話者に切替）

