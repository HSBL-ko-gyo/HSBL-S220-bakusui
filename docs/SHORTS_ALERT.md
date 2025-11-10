# 視聴アラート（YouTube/TikTok等の継続視聴アラート）

本ドキュメントは、視聴アラート機能（旧: ショート検知）の仕様をまとめる。

## 目的
- YouTube/TikTok等の「短時間動画の連続視聴」を検知し、一定時間でユーザーに通知する。
- 睡眠前の視聴過多を抑制する補助。

## 名称
- 機能名: 視聴アラート
- 通知チャネル:
  - 視聴アラート（監視）: 最小重要度（常時表示・静音）
  - 視聴アラート（アラート）: 高重要度（到達時）

## UI
- 監視対象アプリ選択（複数）
  - YouTube / TikTok / Instagram / X(Twitter) / Facebook / LINE
  - YouTube行に「YouTubeショートのみ（高精度）」スイッチ＋「設定」ボタン（アクセシビリティ設定）
- しきい値
  - 1回目: N分（初回通知）
  - スヌーズ: M分（以降の通知間隔）
- 開始/停止（▶/⏸）、使用状況へのアクセス権限ボタン

## 実装概要
- Foreground Service: `ShortsGuardService`
  - 常時「監視」通知（静音・最小）で安定動作
  - アラート時のみ高重要度通知
- 前面アプリ検出（UsageStats）
  - `UsageEvents`から直近120秒の`ACTIVITY_RESUMED`（優先）/`MOVE_TO_FOREGROUND`で前面アプリを推定
  - 必要に応じて`queryUsageStats`で補完
- YouTubeショート限定（ON時）
  - 1) アクセシビリティ（`YoutubeAccessibilityService`）の結果（直近≤120秒）があれば最優先
  - 2) クラス名に"Short/Shorts"を含む場合はショート扱い
  - 3) 直近のUsageEvents走査でShorts系クラスが見つかればショート扱い

## 計測ロジック
- 対象一致（選択アプリ、かつ必要ならショート条件）中のみ「累積時間」を加算
- 対象外に出ても累積は保持し、再度対象に戻れば加算再開（中断耐性）
- 初回しきい値到達で通知。以降はスヌーズ間隔ごとに再通知
- 設定変更（対象アプリ/ショートのみ）時は累積・通知状態をリセット
- 最後に対象だった時刻から6時間連続で非対象なら累積を破棄（日跨ぎなどで自然リセット）

## 設定保存
- `SharedPreferences: bakusui_prefs`
  - `shorts_targets`: 監視対象パッケージの集合
  - `shorts_min1`, `shorts_min2`: 初回/スヌーズ（分）
  - `shorts_youtube_only`: YouTubeショートのみ（高精度）
  - アクセシビリティ補助:
    - `yt_is_shorts`（Bool）, `yt_last_event`（Long）, `yt_last_class`（String）

## 権限/Manifest
- `PACKAGE_USAGE_STATS`（使用状況へのアクセス）
- Foreground Service（メディア再生種別は不要）
- アクセシビリティサービス（任意・有効化はユーザー操作）
  - `YoutubeAccessibilityService`（`res/xml/yt_accessibility_service.xml`）
  - パッケージ限定: `com.google.android.youtube`

## 既知の注意点
- 一部端末ではアクセシビリティが再インストール等で無効化されることがある
  - その際はUIの「設定」から再度オンにする
- 省電力/自動起動許可が厳しい端末では、サービス維持のため個別設定が必要な場合がある


