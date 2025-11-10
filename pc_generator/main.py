#!/usr/bin/env python3
"""
バクスイ - PC用音声生成ツール
VOICEVOX ENGINEを使って音声パックを生成する
"""

import json
import os
import sys
import time
import zipfile
import argparse
from pathlib import Path
from typing import List, Dict, Optional

import requests


class VoiceGenerator:
    """VOICEVOX ENGINEを使って音声を生成するクラス"""
    
    def __init__(self, engine_url: str = "http://localhost:50021", speaker_id: int = 1):
        """
        初期化
        
        Args:
            engine_url: VOICEVOX ENGINEのURL
            speaker_id: 話者ID（デフォルトは1）
        """
        self.engine_url = engine_url.rstrip('/')
        self.speaker_id = speaker_id
    
    def generate_audio(self, text: str, query_overrides: Optional[Dict] = None) -> bytes:
        """
        テキストから音声を生成
        
        Args:
            text: 生成するテキスト
            
        Returns:
            WAVファイルのバイナリ
        """
        # audio_queryを取得
        query_url = f"{self.engine_url}/audio_query"
        params = {
            "text": text,
            "speaker": self.speaker_id
        }
        
        try:
            response = requests.post(query_url, params=params)
            response.raise_for_status()
            audio_query = response.json()
            # ささやき近似などのためのパラメータ上書き
            if query_overrides:
                for key, value in query_overrides.items():
                    audio_query[key] = value
            
            # synthesisで音声生成
            synthesis_url = f"{self.engine_url}/synthesis"
            headers = {"Content-Type": "application/json"}
            response = requests.post(
                synthesis_url,
                headers=headers,
                params={"speaker": self.speaker_id},
                json=audio_query
            )
            response.raise_for_status()
            
            return response.content
            
        except requests.exceptions.RequestException as e:
            print(f"音声生成エラー: {text}")
            print(f"エラー詳細: {e}")
            raise


def _hiragana_to_romaji(text: str) -> str:
    """簡易かな→ローマ字（最小実装）。拗音・促音・長音の基本だけ対応。"""
    tr = str.maketrans({chr(0x30A1 + i): chr(0x3041 + i) for i in range(96)})
    s = text.translate(tr)
    base = {
        "あ":"a","い":"i","う":"u","え":"e","お":"o",
        "か":"ka","き":"ki","く":"ku","け":"ke","こ":"ko",
        "さ":"sa","し":"shi","す":"su","せ":"se","そ":"so",
        "た":"ta","ち":"chi","つ":"tsu","て":"te","と":"to",
        "な":"na","に":"ni","ぬ":"nu","ね":"ne","の":"no",
        "は":"ha","ひ":"hi","ふ":"fu","へ":"he","ほ":"ho",
        "ま":"ma","み":"mi","む":"mu","め":"me","も":"mo",
        "や":"ya","ゆ":"yu","よ":"yo",
        "ら":"ra","り":"ri","る":"ru","れ":"re","ろ":"ro",
        "わ":"wa","を":"o","ん":"n",
        "が":"ga","ぎ":"gi","ぐ":"gu","げ":"ge","ご":"go",
        "ざ":"za","じ":"ji","ず":"zu","ぜ":"ze","ぞ":"zo",
        "だ":"da","ぢ":"ji","づ":"zu","で":"de","ど":"do",
        "ば":"ba","び":"bi","ぶ":"bu","べ":"be","ぼ":"bo",
        "ぱ":"pa","ぴ":"pi","ぷ":"pu","ぺ":"pe","ぽ":"po",
        "ゔ":"vu",
    }
    youon = {"き":"ky","し":"sh","ち":"ch","に":"ny","ひ":"hy","み":"my","り":"ry",
             "ぎ":"gy","じ":"j","ぢ":"j","び":"by","ぴ":"py"}
    out = []
    i = 0
    while i < len(s):
        ch = s[i]
        if ch == "ー":
            i += 1
            continue
        if ch == "っ" and i + 1 < len(s):
            nxt = s[i + 1]
            rom = base.get(nxt, "")
            if rom:
                out.append(rom[0])
                i += 1
                continue
        if i + 1 < len(s) and s[i + 1] in ("ゃ","ゅ","ょ"):
            stem = youon.get(ch)
            if stem:
                tail = {"ゃ":"a","ゅ":"u","ょ":"o"}[s[i + 1]]
                out.append(stem + tail)
                i += 2
                continue
        out.append(base.get(ch, ch))
        i += 1
    return "".join(out)


def _to_safe_token(text: str) -> str:
    import re
    rom = _hiragana_to_romaji(text.lower())
    rom = re.sub(r"[^a-z0-9_-]+", "-", rom)
    rom = re.sub(r"-+", "-", rom).strip("-")
    return rom or "x"


def build_spec_filename(category: str, text: str, tone: str, speaker_id: int,
                        speed_scale: Optional[float], pitch_scale: Optional[float]) -> str:
    text_roman = _to_safe_token(text)
    tone_token = _to_safe_token(tone) if tone else "normal"
    s = 100 if speed_scale is None else int(round(speed_scale * 100))
    p = 0 if pitch_scale is None else int(round(pitch_scale * 10))
    return f"{category}__{text_roman}__{tone_token}__spk{speaker_id}__s{s}__p{p}.wav"

def find_style_id(engine_url: str, speaker_name: str, style_name: Optional[str]) -> Optional[int]:
    """/speakers から話者名とスタイル名で styleId を検索する。

    Args:
        engine_url: ENGINE の URL
        speaker_name: 例) "ずんだもん"
        style_name: 例) "ささやき"（None の場合は最初のスタイル）

    Returns:
        見つかった style の id（見つからなければ None）
    """
    try:
        resp = requests.get(f"{engine_url.rstrip('/')}/speakers", timeout=5)
        resp.raise_for_status()
        speakers = resp.json()
    except requests.RequestException:
        return None

    for sp in speakers:
        if sp.get("name") == speaker_name:
            styles = sp.get("styles") or []
            if style_name:
                for st in styles:
                    if st.get("name") == style_name:
                        return st.get("id")
            # スタイル名未指定 or 見つからない場合は最初のスタイル
            if styles:
                return styles[0].get("id")

    # 完全一致しなければ部分一致も試す
    for sp in speakers:
        if speaker_name in str(sp.get("name")):
            styles = sp.get("styles") or []
            if style_name:
                for st in styles:
                    if style_name in str(st.get("name")):
                        return st.get("id")
            if styles:
                return styles[0].get("id")
    return None


def load_items_from_markdown(md_path: Path, category_filter: Optional[str] = None) -> List[Dict[str, str]]:
    """docs/SAMPLE_WORD_LIST_ja.md のような Markdown から CSV ラインを抽出する。

    - 形式: `category,text`
    - 余計な見出し行はスキップ
    - category_filter があれば一致する行のみ返す
    """
    import re
    items: List[Dict[str, str]] = []
    seen = set()
    allowed = {"object", "animal", "place", "action", "abstract"}

    # ひらがな→カタカナ（「ー」を含むひらがな語はカタカナへ寄せるため）
    hiragana_to_katakana = str.maketrans({chr(0x3041 + i): chr(0x30A1 + i) for i in range(96)})

    text = md_path.read_text(encoding="utf-8")
    for raw_line in text.splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or line.startswith("---"):
            continue
        # シンプルな CSV ラインだけを拾う（カンマ1つ以上を含み、先頭に英字カテゴリっぽいもの）
        if "," in line and not line.startswith("```"):
            parts = [p.strip() for p in line.split(",", 1)]
            if len(parts) == 2:
                category, word = parts[0], parts[1]
                # カテゴリ限定
                if category_filter and category != category_filter:
                    continue
                # 許容カテゴリのみ
                if category not in allowed:
                    continue
                # NGカテゴリは除外（互換）
                if category == "ng":
                    continue
                # 正規化: 注釈や矢印以降、括弧内、記号尾部の除去
                # 例: "フォーク（ふぉーく）→ふぉーく" → "ふぉーく"
                if "→" in word:
                    word = word.split("→")[-1].strip()
                # 丸括弧/全角括弧は中身ごと除去
                word = re.sub(r"（[^）]*）", "", word)
                word = re.sub(r"\([^)]*\)", "", word)
                # '※' 以降は注釈として除去
                word = word.split("※")[0].strip()
                # 余白・連続スペース削除
                word = re.sub(r"\s+", "", word)
                # 表記調整:
                # - ひらがなで「ー」を含む場合 → カタカナに変換（例: すぷーん → スプーン）
                # - それ以外は原表記を尊重（カタカナ語はそのまま）
                if "ー" in word:
                    has_katakana = bool(re.search(r"[ァ-ヴー]", word))
                    has_hiragana = bool(re.search(r"[ぁ-ゖ]", word))
                    if has_hiragana and not has_katakana:
                        word = word.translate(hiragana_to_katakana)
                # 空は除外
                if not word:
                    continue
                # 重複除去（カテゴリ+語）
                key = (category, word)
                if key in seen:
                    continue
                seen.add(key)
                items.append({"text": word, "category": category})
    return items


class PackGenerator:
    """音声パックを生成するクラス"""
    
    def __init__(self, voice_gen: VoiceGenerator):
        """
        初期化
        
        Args:
            voice_gen: VoiceGeneratorインスタンス
        """
        self.voice_gen = voice_gen
    
    def generate_pack(
        self,
        items: List[Dict[str, str]],
        output_dir: Path,
        pack_name: str,
        create_zip: bool = True,
        filename_mode: str = "spec",
        tone: Optional[str] = None,
        speed_scale: Optional[float] = None,
        pitch_scale: Optional[float] = None,
        include_pack_json: bool = False,
        subdir_name: str = "audio",
    ) -> Path:
        """
        音声パックを生成
        
        Args:
            items: アイテムのリスト [{"text": "りんご", "category": "object"}, ...]
            output_dir: 出力先ディレクトリ
            pack_name: パック名
            create_zip: ZIPファイルを作成するか
            subdir_name: 出力サブディレクトリ名（例: "audio" | "intro"）
            
        Returns:
            生成されたパックのパス
        """
        # 出力ディレクトリを作成
        pack_dir = output_dir / pack_name
        subdir = pack_dir / subdir_name
        subdir.mkdir(parents=True, exist_ok=True)
        
        print(f"パック生成開始: {pack_name}")
        print(f"出力先: {pack_dir}")
        
        # アイテムにIDを割り当てて音声生成
        pack_items = []
        total = len(items)
        
        for idx, item in enumerate(items, 1):
            item_id = f"w{idx:04d}"
            text = item.get("text", "")
            category = item.get("category", "unknown")
            if filename_mode == "spec":
                filename = build_spec_filename(
                    category=category,
                    text=text,
                    tone=tone or "normal",
                    speaker_id=self.voice_gen.speaker_id,
                    speed_scale=speed_scale,
                    pitch_scale=pitch_scale,
                )
            else:
                filename = f"{item_id}.wav"
            
            print(f"[{idx}/{total}] {text} - {category}")
            
            # 音声生成
            try:
                wav_data = self.voice_gen.generate_audio(text)
                
                # WAVファイル保存
                wav_path = subdir / filename
                with open(wav_path, "wb") as f:
                    f.write(wav_data)
                
                if include_pack_json:
                    pack_items.append({
                        "id": item_id,
                        "text": text,
                        "category": category,
                        "file": filename,
                    })
                
                # APIの負荷軽減のため少し待機
                time.sleep(0.5)
                
            except Exception as e:
                print(f"  スキップ: {e}")
                continue
        
        # pack.jsonを生成（必要な場合のみ）
        if include_pack_json:
            pack_json = {
                "version": 1,
                "language": "ja",
                "name": pack_name,
                "audio_dir": subdir_name,
                "items": pack_items
            }
            json_path = pack_dir / "pack.json"
            with open(json_path, "w", encoding="utf-8") as f:
                json.dump(pack_json, f, ensure_ascii=False, indent=2)
        
        print(f"\nパック生成完了: {len(pack_items)}個のアイテム")
        
        # ZIPファイルを作成
        if create_zip:
            zip_path = output_dir / f"sleep_pack_{pack_name}.zip"
            self._create_zip(pack_dir, zip_path)
            print(f"ZIPファイル作成: {zip_path}")
            return zip_path
        
        return pack_dir
    
    def _create_zip(self, source_dir: Path, zip_path: Path):
        """
        ZIPファイルを作成
        
        Args:
            source_dir: 圧縮するディレクトリ
            zip_path: 出力ZIPファイルのパス
        """
        with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zf:
            for root, dirs, files in os.walk(source_dir):
                for file in files:
                    file_path = Path(root) / file
                    arc_name = file_path.relative_to(source_dir)
                    zf.write(file_path, arc_name)


def parse_input(text: str) -> List[Dict[str, str]]:
    """
    入力テキストをパースしてアイテムリストに変換
    
    Args:
        text: 入力テキスト（1行1単語、またはJSON）
        
    Returns:
        アイテムリスト
    """
    # JSON形式か判定
    text = text.strip()
    if text.startswith('[') or text.startswith('{'):
        try:
            data = json.loads(text)
            if isinstance(data, list):
                return data
        except json.JSONDecodeError:
            pass
    
    # 1行1単語形式として処理
    items = []
    for line in text.splitlines():
        line = line.strip()
        if line:
            # "りんご" または "りんご,object" 形式を想定
            parts = line.split(',')
            text_part = parts[0].strip().strip('"\'')
            category = parts[1].strip() if len(parts) > 1 else "unknown"
            items.append({"text": text_part, "category": category})
    
    return items


def main():
    """メイン処理"""
    parser = argparse.ArgumentParser(description="バクスイ - PC用音声生成ツール")
    parser.add_argument("--engine-url", default="http://localhost:50021")
    parser.add_argument("--speaker-id", type=int, default=None, help="直接 styleId を指定（/speakers の style.id）")
    parser.add_argument("--speaker-name", default=None, help="例: ずんだもん")
    parser.add_argument("--style-name", default=None, help="例: ささやき")
    parser.add_argument("--from-md", type=str, default=None, help="Markdown ファイルから読み込むパス")
    parser.add_argument("--category", type=str, default=None, help="カテゴリでフィルタ（例: object）")
    parser.add_argument("--pack-name", type=str, default=None, help="パック名（未指定時は自動生成）")
    parser.add_argument("--output-dir", type=str, default="packs")
    parser.add_argument("--zip", dest="create_zip", action="store_true", default=True)
    parser.add_argument("--no-zip", dest="create_zip", action="store_false")
    parser.add_argument("--filename", choices=["id","spec"], default="spec", help="ファイル名方式。spec=docs/AUDIO_FILENAME_SPEC に準拠")
    parser.add_argument("--no-pack-json", action="store_true", help="pack.json を出力しない")
    parser.add_argument("--yes", "-y", action="store_true", help="確認なしで実行")
    parser.add_argument("--approx-whisper-if-missing", action="store_true", default=True,
                        help="ささやきスタイルが無い場合に近似パラメータで生成")
    parser.add_argument("--out-subdir", type=str, default="audio", help="出力先サブディレクトリ名（audio/intro など）")
    # 追加: 落ち着いた声にするための任意パラメータ指定
    parser.add_argument("--speed-scale", type=float, default=None, help="速度倍率（例: 0.95）")
    parser.add_argument("--pitch-scale", type=float, default=None, help="ピッチ（半音相当の相対指標・例: -0.2）")
    parser.add_argument("--intonation-scale", type=float, default=None, help="抑揚（例: 0.85）")
    parser.add_argument("--volume-scale", type=float, default=None, help="音量（例: 0.9）")

    args = parser.parse_args()

    print("=" * 60)
    print("バクスイ - PC用音声生成ツール")
    print("=" * 60)

    engine_url = args.engine_url
    output_dir = Path(args.output_dir)

    # 入力データの決定
    items: List[Dict[str, str]]
    if args.from_md:
        md_path = Path(args.from_md)
        if not md_path.exists():
            print(f"エラー: Markdown が見つかりません: {md_path}")
            sys.exit(1)
        items = load_items_from_markdown(md_path, category_filter=args.category)
        if not items:
            print("エラー: 条件に一致する項目がありませんでした")
            sys.exit(1)
    else:
        # 互換: 既存のサンプル
        items = [
            {"text": "りんご", "category": "object"},
            {"text": "りす", "category": "animal"},
            {"text": "リリース", "category": "abstract"},
        ]

    # speaker 決定
    speaker_id: Optional[int] = args.speaker_id
    # 任意パラメータ上書き（指定があれば常に適用）
    query_overrides: Dict = {}
    if args.speed_scale is not None:
        query_overrides["speedScale"] = args.speed_scale
    if args.pitch_scale is not None:
        query_overrides["pitchScale"] = args.pitch_scale
    if args.intonation_scale is not None:
        query_overrides["intonationScale"] = args.intonation_scale
    if args.volume_scale is not None:
        query_overrides["volumeScale"] = args.volume_scale
    used_name = args.speaker_name
    used_style = args.style_name
    if speaker_id is None:
        if used_name:
            speaker_id = find_style_id(engine_url, used_name, used_style)
        # ささやき指定だが見つからない場合の近似
        if speaker_id is None and used_name and used_style and "ささやき" in used_style and args.approx_whisper_if_missing:
            # 話者名だけで styleId を取得（最初のスタイル）
            speaker_id = find_style_id(engine_url, used_name, None)
            # 近似用の軽い調整（環境に応じて微調整可）
            query_overrides.setdefault("speedScale", 0.95)
            query_overrides.setdefault("pitchScale", -0.2)
            query_overrides.setdefault("intonationScale", 0.85)
            query_overrides.setdefault("volumeScale", 0.9)
    if speaker_id is None:
        # 最後の手段: 1 を使う
        speaker_id = 1

    # パック名
    pack_name = args.pack_name
    if not pack_name:
        parts = []
        if args.category:
            parts.append(args.category)
        if used_name:
            parts.append(used_name)
        if used_style:
            parts.append(used_style)
        pack_name = "_".join(parts) if parts else "sample_pack"

    print("\n設定:")
    print(f"  ENGINE URL: {engine_url}")
    print(f"  話者ID(styleId): {speaker_id}")
    if used_name:
        print(f"  話者名: {used_name}")
    if used_style:
        print(f"  スタイル: {used_style}")
    print(f"  出力先: {output_dir}")
    print(f"  パック名: {pack_name}")
    print(f"  生成アイテム数: {len(items)}")
    print(f"  ファイル名方式: {args.filename}")
    print(f"  pack.json: {'出力しない' if args.no_pack_json else '出力する'}")

    if not args.yes and not args.from_md:
        user_input = input("\nこの設定で生成しますか？ (y/n): ").strip().lower()
        if user_input != 'y':
            print("キャンセルされました")
            return
    
    # VOICEVOX ENGINEの接続確認
    try:
        voice_gen = VoiceGenerator(engine_url, speaker_id)
        response = requests.get(f"{engine_url}/version", timeout=3)
        print(f"\nVOICEVOX ENGINE 接続確認: OK (バージョン: {response.text})")
    except requests.exceptions.RequestException as e:
        print(f"\nエラー: VOICEVOX ENGINEに接続できません")
        print(f"  URL: {engine_url}")
        print(f"  エラー: {e}")
        print("\nVOICEVOX ENGINEが起動しているか確認してください")
        sys.exit(1)
    
    # パック生成
    pack_gen = PackGenerator(voice_gen)
    
    try:
        # query_overrides は generate_pack 内で扱わないため、逐次生成のところで使う
        # 簡易対応として、VoiceGenerator の speaker_id と overrides を使うため、
        # generate_pack を軽くラップする（ここでは既存 generate_pack をそのまま使用し、
        # VoiceGenerator 側で overrides を効かせるために monkey patch 的に関数を差し替える）

        original_generate_audio = voice_gen.generate_audio

        def generate_audio_with_overrides(text: str) -> bytes:
            return original_generate_audio(text, query_overrides=query_overrides)

        voice_gen.generate_audio = generate_audio_with_overrides  # type: ignore

        # tone 表記（style名→トーン）
        def style_to_tone(name: Optional[str]) -> str:
            if not name:
                return "normal"
            return "whisper" if "ささやき" in name else _to_safe_token(name)

        speed_scale = query_overrides.get("speedScale") if query_overrides else None
        pitch_scale = query_overrides.get("pitchScale") if query_overrides else None

        result_path = pack_gen.generate_pack(
            items,
            output_dir,
            pack_name,
            create_zip=args.create_zip,
            filename_mode=args.filename,
            tone=style_to_tone(used_style),
            speed_scale=speed_scale,
            pitch_scale=pitch_scale,
            include_pack_json=not args.no_pack_json,
            subdir_name=args.out_subdir,
        )
        print(f"\n✅ 完成！")
        print(f"パック: {result_path}")
        print("\n次は Android アプリでインポートして使ってください")
    except Exception as e:
        print(f"\nエラー: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()

