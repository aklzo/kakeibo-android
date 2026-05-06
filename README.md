# kakeibo-android

家計簿アプリの Android クライアント。[kakeibo-api](../kakeibo-cli) が提供する Cloud Run 上の REST API に接続し、取引の記録・閲覧・集計を行う。

## スクリーン構成

| タブ | 画面 | 概要 |
|------|------|------|
| 追加 | 取引追加 | 収支の記録 |
| 一覧 | 取引一覧 | 月別・カテゴリ別の閲覧、編集・削除 |
| 集計 | 月次集計 | 収入・支出・収支、カテゴリ別内訳 |
| 進捗 | 消費進捗率 | 予算対比・昨月対比のプログレスバー |
| 予算 | 予算設定 | 月全体・カテゴリ別の予算管理 |

## 技術スタック

| 用途 | 選択 |
|------|------|
| 言語 | Kotlin |
| UI | Jetpack Compose (Material3) |
| 認証 | Google Sign-In (Credential Manager) |
| HTTP クライアント | Retrofit2 + Gson |
| 状態管理 | StateFlow + Compose state |

## セットアップ

### 前提条件

- Android Studio Meerkat 以降
- minSdk 26 / targetSdk 35

### 1. local.properties の設定

プロジェクトルートの `local.properties` に以下を追加する。

```properties
API_BASE_URL=https://<service>.run.app/api/v1/
GOOGLE_WEB_CLIENT_ID=<your-web-client-id>.apps.googleusercontent.com
```

- `API_BASE_URL`: kakeibo-api の Cloud Run エンドポイント
- `GOOGLE_WEB_CLIENT_ID`: GCP で発行した **Web クライアント ID**（詳細は [docs/setup.md](docs/setup.md)）

### 2. ビルドと実行

```bash
./gradlew assembleDebug        # APK ビルド
./gradlew installDebug         # 接続済みデバイスにインストール
```

Android Studio から実行する場合は、Run ボタン（Shift+F10）を押すだけでよい。

## ドキュメント

| ファイル | 内容 |
|----------|------|
| [docs/setup.md](docs/setup.md) | Android OAuth クライアントの作成手順 |
| [docs/architecture.md](docs/architecture.md) | アプリのアーキテクチャと設計方針 |
| [docs/screen-spec.md](docs/screen-spec.md) | 各画面の仕様 |
| [docs/api-spec.md](docs/api-spec.md) | API 仕様書 |
| [note/jetpack-compose.md](note/jetpack-compose.md) | Jetpack Compose の学習メモ |
| [note/state-and-flow.md](note/state-and-flow.md) | StateFlow・状態管理の学習メモ |
| [note/coroutines.md](note/coroutines.md) | コルーチンの学習メモ |
| [note/retrofit.md](note/retrofit.md) | Retrofit2 の学習メモ |
| [note/build-config.md](note/build-config.md) | BuildConfig・local.properties の学習メモ |
