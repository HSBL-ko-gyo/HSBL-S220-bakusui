このフォルダには、出荷時に同梱する「基本パック」を配置します。

推奨は ZIP 1ファイルにまとめて `default_pack.zip` として配置し、
アプリ初回起動時に内部領域へ展開します（実装予定）。

作り方：
1) PCで VOICEVOX ENGINE を起動
2) ルートの `pc_generator/main.py` を実行
3) 生成される `packs/sleep_pack_<name>.zip` を本フォルダへコピーし、
   `default_pack.zip` にリネーム
