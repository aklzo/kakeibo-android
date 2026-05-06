# セットアップ手順

kakeibo-android を動かすために必要な Android 固有の設定手順。  
GCP プロジェクト・Cloud Run・Turso の設定は [kakeibo-cli/note/gcp-setup.md](../../kakeibo-cli/note/gcp-setup.md) を参照。

---

## 1. Web クライアント ID の確認

Google Sign-In では、Android アプリが Google ID Token を取得する際に **Web クライアント ID** を使用する。  
Android クライアント ID ではないため注意。

1. [GCP Console](https://console.cloud.google.com/) → 「APIとサービス」→「認証情報」を開く
2. 「OAuth 2.0 クライアント ID」の一覧から種類が **ウェブ アプリケーション** のものを選択
3. 「クライアント ID」の値をコピーする（`xxxxx.apps.googleusercontent.com` 形式）

---

## 2. Android OAuth クライアントの登録

Google Sign-In を機能させるには、GCP に Android クライアントを登録し、アプリの SHA-1 フィンガープリントを紐付ける必要がある。

### 2-1. SHA-1 フィンガープリントの取得

デバッグビルド用のキーストアから取得する。

```bash
keytool -list -v \
  -keystore ~/.android/debug.keystore \
  -alias androiddebugkey \
  -storepass android \
  -keypass android
```

出力の `SHA1:` 行の値（例: `AA:BB:CC:...`）をコピーする。

### 2-2. Android クライアントの作成

1. GCP Console →「APIとサービス」→「認証情報」→「認証情報を作成」→「OAuth クライアント ID」
2. アプリケーションの種類: **Android**
3. パッケージ名: `com.aklzo.kakeiboandroid`
4. SHA-1 証明書フィンガープリント: 上で取得した値を入力
5. 「作成」をクリック

> Android クライアント ID 自体をコードで使う必要はない。  
> 登録することで、同じ GCP プロジェクトの Web クライアント ID に対して ID Token を発行できるようになる。

---

## 3. local.properties の設定

プロジェクトルートの `local.properties` に以下を追記する。

```properties
API_BASE_URL=https://<service>.run.app/api/v1/
GOOGLE_WEB_CLIENT_ID=<web-client-id>.apps.googleusercontent.com
```

| キー | 値 |
|------|-----|
| `API_BASE_URL` | kakeibo-api の Cloud Run URL（末尾 `/` 付き） |
| `GOOGLE_WEB_CLIENT_ID` | 手順 1 で確認した **Web** クライアント ID |

`local.properties` は `.gitignore` に含まれており、リポジトリにはコミットされない。

---

## 4. 動作確認

設定完了後、アプリを起動してサインイン画面が表示され、Google アカウントでログインできれば設定は正しい。  
ログイン後に取引追加画面が表示されれば API 接続も成功している。
