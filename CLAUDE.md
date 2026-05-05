# kakeibo-android 実装ガイドライン

## 概要
kakeibo-api（Cloud Run）に接続する Android アプリ。
入力 UI の提供が目的のため、ビジネスロジックはサーバー側に持つ。

## 技術スタック
- 言語: Kotlin
- UI: Jetpack Compose
- 認証: Google Sign-In
- HTTP クライアント: Retrofit2
- API 仕様: docs/api-spec.md を参照すること

## API ベース URL
- コードにハードコードしないこと
- local.properties の API_BASE_URL を BuildConfig 経由で参照すること

## 実装優先順位
1. Google Sign-In
2. 取引追加画面
3. 一覧表示画面
4. 集計・進捗表示画面
5. 予算設定画面

## 方針
- 動作することを最優先とする
- エラーハンドリングは最低限で問題ない
- 画面遷移は Bottom Navigation で管理する
