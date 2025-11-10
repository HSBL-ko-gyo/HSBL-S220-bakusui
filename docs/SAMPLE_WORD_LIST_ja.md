# サンプル単語リスト（日本語・Markdown）

以下の形式で1行1語を記述します。

- 形式: `category,text`
- カテゴリ例: `object` | `animal` | `place` | `action` | `abstract`
- 空行 / `#` で始まる行 / `---` は無視されます
- `ng` カテゴリの行は読み込み時に除外されます

このファイルは `pc_generator/main.py` の `--from-md` に渡せます。
例:

```bash
python pc_generator/main.py --from-md docs/SAMPLE_WORD_LIST_ja.md --speaker-name 四国めたん --style-name ささやき --pack-name basic_ja --output-dir packs
```

---

# 使い方メモ
- 最低1000語以上にしたい場合は、下の雛形をベースに追記してください
- 重複語はそのまま書いても生成時に上書きされるだけなので、基本は重複を避けてください
- 迷ったらまず `object`（具象名詞）を中心に集めると使い勝手がよいです

---

# 雛形（例・一部）

object,りんご
animal,りす
abstract,リリース
animal,ライオン
object,ラッパ
object,ランプ
object,リンゴ
abstract,ルール
abstract,ループ
abstract,ルーム

# 具象（object）
object,コップ
object,カギ
object,カバン
object,ノート
object,えんぴつ
object,ペン
object,いす
object,つくえ
object,まくら
object,ふとん
object,はさみ
object,ナイフ
object,スプーン
object,フォーク
object,さら
object,ちゃわん
object,なべ
object,かさ
object,めがね
object,スマホ
object,パソコン
object,マウス
object,キーボード
object,テレビ
object,カメラ
object,ボール
object,つみき
object,くつ
object,くつした
object,ぼうし
object,てぶくろ
object,コート
object,シャツ
object,ズボン
object,スカート
object,タオル
object,せっけん
object,はぶらし
object,はみがきこ
object,シャンプー
object,ドライヤー
object,かがみ
object,はな
object,かみ
object,ふうせん
object,でんき
object,でんち
object,めだまやき
object,おにぎり
object,パン
object,バター
object,ジャム
object,ぎゅうにゅう
object,おちゃ
object,みず
object,ジュース
object,コーヒー
object,とうふ
object,みそしる
object,やさい
object,くだもの
object,にんじん
object,きゅうり
object,トマト
object,キャベツ
object,たまねぎ
object,じゃがいも
object,りんご
object,みかん
object,ぶどう
object,いちご
object,もも
object,なし
object,すいか
object,メロン
object,バナナ
object,パイナップル
object,レモン
object,さくらんぼ
object,ナス
object,ピーマン
object,ほうれんそう
object,きのこ
object,しめじ
object,エリンギ
object,しいたけ
object,たまご
object,さかな
object,にく
object,パンケーキ
object,アイスクリーム
object,クッキー
object,ケーキ
object,チョコレート
object,アメ

# 動物（animal）
animal,いぬ
animal,ねこ
animal,うさぎ
animal,りす
animal,さる
animal,とら
animal,くま
animal,しか
animal,ぞう
animal,キリン
animal,ライオン
animal,トラ
animal,オオカミ
animal,キツネ
animal,タヌキ
animal,パンダ
animal,ペンギン
animal,コアラ
animal,カンガルー
animal,ラクダ
animal,ウマ
animal,ウシ
animal,ブタ
animal,ヒツジ
animal,ヤギ
animal,ニワトリ
animal,ハト
animal,スズメ
animal,カモ
animal,ワシ
animal,タカ
animal,フクロウ
animal,ツバメ
animal,イルカ
animal,クジラ
animal,サメ
animal,クラゲ
animal,カメ
animal,ワニ
animal,ヘビ
animal,カエル
animal,トカゲ
animal,カブトムシ
animal,クワガタ
animal,チョウ
animal,ハチ
animal,アリ
animal,テントウムシ

# 場所（place）
place,こうえん
place,がっこう
place,えき
place,みせ
place,いえ
place,へや
place,だいどころ
place,ふろば
place,トイレ
place,かいしゃ
place,びょういん
place,としょかん
place,ゆうびんきょく
place,ぎんこう
place,スーパー
place,デパート
place,コンビニ
place,えいがかん
place,びじゅつかん
place,はくぶつかん
place,どうぶつえん
place,すいぞくかん
place,スタジアム
place,うみ
place,やま
place,かわ
place,もり
place,はら
place,さとう
place,みち
place,はし
place,トンネル
place,くうこう
place,くるま
place,でんしゃ
place,バス
place,ふね
place,ひこうき
place,えきまえ
place,ばいてん
place,たいくかん
place,こうどう
place,きょうしつ
place,グラウンド

# 動作（action）
action,あるく
action,はしる
action,とぶ
action,およぐ
action,すわる
action,たつ
action,ねる
action,おきる
action,たべる
action,のむ
action,つくる
action,あらう
action,みがく
action,よむ
action,かく
action,きく
action,はなす
action,うたう
action,ひく
action,たたく
action,おす
action,ひく（引く）
action,もつ
action,あげる
action,もらう
action,ひろう
action,わらう
action,なく
action,おこる
action,おどる
action,つかむ
action,なげる
action,ける
action,まわす
action,ひねる
action,のばす
action,ちぢめる
action,ならべる
action,かぞえる

# 抽象（abstract）
abstract,やすらぎ
abstract,おだやか
abstract,しずけさ
abstract,ひかり
abstract,かぜ
abstract,そら
abstract,ゆめ
abstract,ほし
abstract,つき
abstract,あさ
abstract,よる
abstract,ねむけ
abstract,あんしん
abstract,ゆったり
abstract,ふんわり
abstract,なごみ
abstract,やわらか
abstract,かろやか
abstract,しっとり
abstract,ほのか
abstract,きらめき
abstract,ひだまり
abstract,さざなみ
abstract,せせらぎ
abstract,こだま
abstract,ぬくもり
abstract,ときめき
abstract,しあわせ
abstract,ここちよさ
abstract,めざめ

---

# 補足
- ここに記載したのはあくまで雛形です。1000語以上に増やす際は、この下にどんどん追記してください。
- 語彙の重複や不適切語を避けたい場合は、カテゴリを `ng` にしておけば読み込み時に除外できます。
- 生成時の命名規約は `docs/AUDIO_FILENAME_SPEC.md` を参照してください。

---

# 追加インポート（提供リストそのまま追記・重複は生成時に自動スキップ）

# from: Downloads/___________________.csv（ヘッダー行は除外）
place,えんてい
object,せんぷうき
animal,りす
object,ちりとり
object,ひこうき
place,うみ
abstract,いかり
abstract,ふあん
abstract,きぼう
abstract,まんぞく
animal,ちょうちょ
object,かぎ
animal,ひよこ
action,きく
place,ばいてん
object,おにぎり
animal,なまず
action,まつ
animal,いぬ
object,でんわ
place,えいがかん
object,てぶくろ
action,かく
place,へや
abstract,たいくつ
place,おしいれ
abstract,じゆう
action,きる
animal,おおかみ
place,びょういん
abstract,かなしい
abstract,おそれ
animal,とら
place,たいいくかん
action,およぐ
place,ゆうびんきょく
object,てーぶる
abstract,まじめ
place,ぱんや
place,はくぶつかん
place,じてんしゃや
object,おさら
animal,あり
place,じむしょ
abstract,じしん
place,ばすてい
abstract,あい
action,いく
abstract,うぬぼれ
action,あそぶ
animal,くわがた
abstract,うれしさ
object,いす
place,もり
place,しょくどう
object,かさ
animal,くま
action,たつ
object,せんたくばさみ
object,さとう
place,ちゅうしゃじょう
place,たんぼ
object,でんち
abstract,やるき
abstract,もやもや
object,てーぷ
animal,らいおん
object,ほうき
object,じしょ
object,はぶらし
abstract,なみだ
object,でんとう
abstract,かんどう
animal,さる
animal,うさぎ
abstract,よわさ
object,こおり
animal,ばった
object,おかし
place,じんじゃ
place,いけ
object,れいぞうこ
animal,しか
abstract,つらさ
animal,さめ
action,ならう
place,くうしゃ
object,えんぴつ
action,しめる
object,しんぶん
action,ぬぐ
animal,もぐら
object,ぼうし
animal,かに
action,つくる
place,としょかん
action,おこる
place,びよういん
action,ひく
action,あるく
object,ひげそり
abstract,げんき
animal,とり
animal,あしか
place,いえ
place,てら
place,どうぶつえん
object,おさら
object,ふく
place,くうこう
action,わらう
action,まもる
action,みる
animal,ぺんぎん
animal,かもめ
action,さがす
place,おてあらい
animal,かぶとむし
object,ごみばこ
action,もつ
place,こうえん
place,まち
object,せっけん
animal,ねこ
abstract,がまん
abstract,やさしさ
animal,きりん
action,つたえる
abstract,おもいで
place,ふろば
place,きょうしつ
place,かいがん
action,とぶ
place,かわ
object,くつ
place,やま
place,ろてんぶろ
animal,めだか
action,すわる
animal,うし
animal,たこ
abstract,いたみ
animal,かめ
object,はし
place,だいどころ
place,がっこう
abstract,ねがい
place,のはら
action,つかう
action,なでる
abstract,どきどき
place,こうさてん
place,みせ
object,かがみ
abstract,せいちょう
action,かえる
place,すいぞくかん
action,おす
action,わける
action,おしえる
animal,すずめ
abstract,いじ
object,ちゃわん
abstract,つかれ
object,じてんしゃ
object,すいとう
object,こっぷ
animal,こうもり
action,はしる
object,ばんそうこう
action,おく
animal,とんぼ
object,ふでばこ
action,なく
place,ほんや
place,ろうか
object,こうちゃ
abstract,たのしみ
place,くすりや
animal,はと
animal,いか
action,なでる
abstract,ふまん
abstract,やすらぎ
abstract,きんちょう
abstract,あこがれ
animal,やぎ
abstract,のんびり
object,のり
abstract,しんぱい
action,のむ
object,つくえ
animal,おにやんま
animal,さかな
action,ねる
action,たべる
animal,いるか
action,ひく
object,はんがー
action,なげる
abstract,きもち
animal,せみ
abstract,しあわせ
animal,ひつじ
place,でんしゃ
abstract,しつぼう
action,ふく
abstract,しんらい
object,まくら
abstract,ほこり
action,ひろう
abstract,こわい
object,ぼーるぺん
place,えき
place,しょくば
action,とじる
place,げんかん
object,けしごむ
animal,ぞう
action,はこぶ
animal,しゃち
animal,おっとせい
action,あける
animal,つばめ
abstract,おもいやり
action,おどる
action,さけぶ
action,のる
object,かめら
object,かばん
object,かいだん
action,うたう
action,はなす
animal,わに
animal,くらげ
abstract,こうふん
animal,にわとり
animal,ぶた
abstract,ときめき
abstract,いじわる
animal,たぬき
abstract,ねむけ
abstract,まごころ
abstract,ゆめ
place,びじゅつかん
object,ほん
abstract,つよさ
action,とる
action,けす
animal,えび

# 追加貼り付け分（整形前でもOK。生成時に正規化・重複除外）
object,みかん

object,りんご

object,ばなな

object,おにぎり

object,たまご

object,さかな

object,やさい

object,ごはん

object,みず

object,おちゃ

object,ぎゅうにゅう

object,くつ

object,かさ

object,ぼうし

object,かばん

object,ふく

object,えんぴつ

object,けしごむ

object,とけい

object,かみ

object,ほん

object,つくえ

object,いす

object,まど

object,でんわ

object,てがみ

object,しゃぼん

object,おもちゃ

object,ふとん

object,まくら

object,せんたくき

object,れいぞうこ

object,でんき

object,せっけん

object,てぬぐい

object,さいふ

object,はさみ

object,くすり

object,はがき

object,とびら

object,ねんど

object,えのぐ

object,たいこ

object,まめ

object,こめ

object,さとう

object,しお

object,きって

object,ふうせん

object,はなび

object,ろうそく

object,てぶくろ

object,めがね

object,かぎ

object,ゆびわ

object,ゆきだるま

object,おさら

object,コップ

object,おはし

object,なべ

object,フライパン

object,びん

object,かん

object,くぎ

object,ひも

object,ふね

object,くるま

object,でんしゃ

object,ひこうき

object,じてんしゃ

object,ふうとう

object,おさら

object,はけ

object,すな

object,いし

object,きのこ

object,おかし

object,あめ

object,ガム

object,ジュース

object,バター

object,ちーず

object,みそ

object,しょうゆ

object,おもち

object,おさら

object,タオル

object,てんぷら

animal,いぬ

animal,ねこ

animal,うさぎ

animal,ぞう

animal,きりん

animal,らいおん

animal,くま

animal,しか

animal,さる

animal,うま

animal,ぶた

animal,とり

animal,ひよこ

animal,すずめ

animal,かも

animal,あり

animal,はち

animal,ちょう

animal,とんぼ

animal,かえる

animal,へび

animal,かめ

animal,えび

animal,かに

animal,さけ

animal,まぐろ

animal,たい

animal,いるか

animal,くじら

animal,ぺんぎん

animal,ねずみ

animal,もぐら

animal,こうもり

animal,たつ

animal,りゅう

animal,むし

animal,ほたる

animal,こい

animal,かたつむり

animal,くも

animal,ひつじ

animal,やぎ

animal,きつね

animal,たぬき

animal,ひょう

animal,わに

animal,あひる

animal,ふくろう

animal,ぺりかん

place,いえ

place,まち

place,みち

place,やま

place,かわ

place,うみ

place,そら

place,くも

place,つき

place,ほし

place,たいよう

place,こうえん

place,がっこう

place,びょういん

place,えき

place,みせ

place,うち

place,へや

place,トイレ

place,おふろ

place,だいどころ

place,にわ

place,たうえ

place,はたけ

place,もり

place,かいしゃ

place,こうじょう

place,くうこう

place,みなと

place,どうぶつえん

place,としょかん

place,はくぶつかん

place,ぎんこう

place,ゆうびんきょく

place,じんじゃ

place,おてら

place,ちゅうしゃじょう

place,ひろば

place,たに

place,いけ

place,みさき

place,さかみち

place,すいぞくかん

place,しやくしょ

place,けいさつしょ

action,たべる

action,のむ

action,ねる

action,あるく

action,はしる

action,とぶ

action,およぐ

action,わらう

action,なく

action,おこる

action,あそぶ

action,うたう

action,おどる

action,よむ

action,かく

action,みる

action,きく

action,はなす

action,さわる

action,もつ

action,なげる

action,ひろう

action,まげる

action,のばす

action,おきる

action,すわる

action,たつ

action,はいる

action,でる

action,あける

action,しめる

action,つける

action,けす

action,ならぶ

action,まがる

action,わたる

action,くる

action,いく

action,まつ

action,つくる

action,なおす

action,あらう

action,ふく

action,きる

action,ぬぐ

action,かくす

action,おしえる

action,まなぶ

action,わすれる

action,おぼえる

action,つかれる

action,いそぐ

action,はたらく

action,やすむ

action,ほめる

action,しかる

action,さわる

action,さけぶ

action,おちる

action,たてる

action,あつめる

action,くばる

action,たべる

action,あまえる

action,ふりむく

abstract,きもち

abstract,こころ

abstract,あい

abstract,ゆめ

abstract,きぼう

abstract,かなしみ

abstract,よろこび

abstract,おもい

abstract,ことば

abstract,おと

abstract,におい

abstract,あじ

abstract,いろ

abstract,かたち

abstract,ちから

abstract,じかん

abstract,かぜ

abstract,ひかり

abstract,おんど

abstract,しつど

abstract,いのり

abstract,しんらい

abstract,ゆうき

abstract,どりょく

abstract,へいわ

abstract,せいぎ

abstract,うそ

abstract,まこと

abstract,げんき

abstract,あんしん

abstract,ふあん

abstract,かな

abstract,じゅんび

abstract,うん

abstract,そんざい

abstract,くうき

abstract,じゆう

abstract,へいき

abstract,ちえ

abstract,りゆう

abstract,かんしゃ

abstract,たいせつ

abstract,いみ

abstract,あきらめ

abstract,しんか

abstract,へんか

abstract,くふう

abstract,そくど

abstract,しゅうかん

abstract,さいご

abstract,げんかい

abstract,きおく

object,カメラ



object,でんわ

object,ラジオ

object,テレビ

object,とけい

object,かーど

object,コード

object,スイッチ

object,スマホ

object,タブレット

object,イヤホン

object,リモコン

object,ひーたー

object,せんぷうき

object,アイロン

object,ドライヤー

object,オーブン

object,こんろ

object,でんたく

object,コピーき

object,ミシン

object,きかい

object,でんち

object,でんげん

object,ろくおんき

object,でんきゅう

object,でんせん

object,はかり

object,スピーカー

object,マイク

object,レコーダー

object,てれび

object,プリンター

object,スキャナー

object,ちゅういでん

object,かちでん

object,おんき

object,じゅうでん

object,えんぴつ



object,けしごむ

object,じてんしゃ

object,つくえ

object,いす

object,はさみ

object,のり

object,かばん

object,ほん

object,でんわ

object,めがね

object,かさ

object,てぶくろ

object,ぼうし

object,まくら

object,ふとん

object,はぶらし

object,しゃしん

object,とけい

object,かがみ

object,さら

object,はし

object,コップ

object,なべ

object,れいぞうこ

object,てちょう

object,でんき

object,パソコン

object,カメラ

object,てがみ

object,かみ

object,ボール

object,けいたい

object,スプーン

object,フォーク

object,ランドセル

object,くつ

object,くつした

object,シャツ

object,ズボン

object,コート

object,セーター

object,マスク

object,ティッシュ

object,けいたいでんわ

object,でんしゃ

object,くるま

object,ひこうき

object,ふね

object,じどうしゃ

object,バス

object,エレベーター

object,エスカレーター

object,ラジオ

object,テレビ

object,れいとうこ

object,でんきスタンド

object,けいさんき

object,ドライヤー

object,ホッチキス

object,シャープペン

object,ふうせん

object,おさら

object,おちゃわん

object,おはし

object,まど

object,ドア

object,かぎ

object,さいふ

object,きっぷ

object,カギ

object,しんぶん

object,ノート

object,ペン

object,マウス

object,イヤホン

object,スリッパ

object,でんきゅう

object,がっこうようひん

object,バケツ

object,ほうき

object,ちりとり

object,そうじき

object,せっけん

object,タオル

object,ハンカチ

object,しんぶんし

object,ポスター

object,でんりょく

object,おかね

object,きんこ

object,きって

animal,いぬ

animal,ねこ

animal,うさぎ

animal,とり

animal,すずめ

animal,からす

animal,はと

animal,にわとり

animal,ぶた

animal,うし

animal,ひつじ

animal,やぎ

animal,うま

animal,さる

animal,くま

animal,きつね

animal,たぬき

animal,ぞう

animal,きりん

animal,らいおん

animal,とら

animal,しか

animal,かめ

animal,へび

animal,かえる

animal,さかな

animal,たい

animal,いわし

animal,さめ

animal,くじら

animal,いるか

animal,かに

animal,えび

animal,くらげ

animal,たこ

animal,いか

animal,あり

animal,はち

animal,ちょう

animal,か

animal,とんぼ

animal,せみ

animal,くわがた

animal,かぶとむし

animal,ありくい

animal,ねずみ

place,がっこう

place,こうえん

place,いえ

place,おふろ

place,だいどころ

place,トイレ

place,きょうしつ

place,うみ

place,やま

place,かわ

place,もり

place,そら

place,まち

place,えき

place,スーパー

place,デパート

place,びょういん

place,くうこう

place,ホテル

place,レストラン

place,みせ

place,コンビニ

place,しょくどう

place,びじゅつかん

place,どうぶつえん

place,すいぞくかん

place,えいがかん

place,ゆうえんち

place,じんじゃ

place,おてら

place,はなばたけ

place,ゆきやま

place,みなと

place,バスてい

place,こうさてん

place,しょうてんがい

place,くすりや

place,ほんや

place,パンや

place,にくや

place,さかなや

place,やおや

place,びょういん

place,がっこうこう

place,えきまえ

action,たべる

action,のむ

action,ねる

action,おきる

action,あるく

action,はしる

action,とぶ

action,みる

action,きく

action,いう

action,はなす

action,わらう

action,なく

action,おこる

action,よむ

action,かく

action,あそぶ

action,まつ

action,のる

action,およぐ

action,はたらく

action,つくる

action,かう

action,うる

action,あらう

action,きる

action,ぬぐ

action,たつ

action,すわる

action,おす

action,ひく

action,もつ

action,なげる

action,ける

action,あげる

action,もらう

action,のぞく

action,さがす

action,おしえる

action,ならう

action,はこぶ

action,でかける

action,いく

action,かえる

action,でんわする

action,べんきょうする

action,りょうりする

action,うたう

abstract,うれしい

abstract,たのしい

abstract,かなしい

abstract,いたい

abstract,つかれる

abstract,きもち

abstract,こころ

abstract,ゆめ

abstract,きぼう

abstract,じゆう

abstract,まもり

abstract,あい

abstract,ゆるし

abstract,ひみつ

abstract,おもいで

abstract,じかん

abstract,せかい

abstract,やすみ

abstract,ねがい

abstract,なやみ

abstract,おそれ

abstract,くやしさ

abstract,つよさ

abstract,よわさ

abstract,げんき

abstract,へいわ

abstract,こうふく

abstract,じしん

abstract,まごころ

abstract,しんらい

abstract,なかよし

abstract,やさしさ

abstract,おちつき

abstract,どりょく

abstract,きずな

abstract,ゆうき

abstract,しあわせ

abstract,おもいやり

object,するめ

object,くつした

object,おさら

object,はさみ

object,つくえ

object,いす

object,ほん

object,えんぴつ

object,かばん

object,くるま

object,じてんしゃ

object,めがね

object,けいたい

object,とけい

object,でんわ

object,まくら

object,こっぷ

object,フォーク（ふぉーく）→ふぉーく

object,ナイフ（ないふ）→ないふ

object,スプーン（すぷーん）→すぷーん

object,まど

object,ドア（どあ）→どあ

object,かさ

object,ぼうし

object,びん

object,ざっし

object,しょっき

object,おさら

object,ねんど

object,えほん

object,てちょう

object,めいし

object,ノート→のーと

object,はんこ

object,ふでばこ

object,くれよん

object,マスク→ますく

object,せっけん

object,タオル→たおる

object,みずぎ

object,バッグ→ばっぐ

object,リュック→りゅっく

animal,いぬ

animal,ねこ

animal,うさぎ

animal,ぞう

animal,とり

animal,さる

animal,きりん

animal,しか

animal,くま

animal,きつね

animal,たぬき

animal,ねずみ

animal,うま

animal,うし

animal,ぶた

animal,ひつじ

animal,にわとり

animal,かえる

animal,さかな

animal,ちょう

animal,あり

animal,はち

animal,むし

animal,へび

animal,ペンギン→ぺんぎん

place,こうえん

place,がっこう

place,えき

place,まち

place,やま

place,うみ

place,かわ

place,おてら

place,じんじゃ

place,びょういん

place,やおや

place,ほんや

place,スーパー

place,コンビニ→こんびに

place,おみせ

place,きっさてん

place,しょくどう

place,ゆうえんち

place,どうぶつえん

place,プール→ぷーる

place,ゆうびんきょく→ゆうびんきょく

action,たべる

action,のむ

action,ねる

action,おきる

action,はしる

action,あるく

action,およぐ

action,かく

action,よむ

action,きく

action,みる

action,はなす

action,かう

action,うる

action,つくる

action,あそぶ

action,わらう

action,なく

action,おどる

action,うたう

action,はたらく

action,まつ

action,てつだう

action,そだてる

action,でんわする→でんわする

abstract,こころ

abstract,ゆめ

abstract,あい

abstract,しあわせ

abstract,くらし

abstract,ともだち

abstract,ほし

abstract,えがお

abstract,やすみ

abstract,きもち

abstract,みらい

abstract,じゆう

abstract,へいわ

abstract,おもいで

abstract,ちえ

abstract,がんばり

abstract,あんしん

abstract,ふしぎ

abstract,せいしん

abstract,ねがい

abstract,りそう

abstract,せいかつ

abstract,しんらい

abstract,けんこう

abstract,せいちょう

abstract,そうぞう

abstract,かんしゃ

abstract,けつい

abstract,じしん

abstract,しんせつ
 