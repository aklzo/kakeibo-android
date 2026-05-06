# BuildConfig と local.properties メモ

## なぜ BuildConfig を使うのか

API の URL やクライアント ID などの設定値をコードにハードコードすると、環境ごとの切り替えが困難になりセキュリティリスクにもなる。  
`local.properties` に書いてビルド時に `BuildConfig` フィールドとして注入することで、コードを変えずに値を差し替えられる。  
`local.properties` は `.gitignore` に含まれるため、秘密情報をリポジトリに含めずに済む。

---

## 設定手順

### 1. local.properties に値を追記

`local.properties` はプロジェクトルートに自動生成される SDK パス設定ファイル。  
このファイルに任意のキーを追記できる。

```properties
# SDK パス（自動生成）
sdk.dir=/Users/username/Library/Android/sdk

# 追加した設定
API_BASE_URL=https://my-service.run.app/api/v1/
GOOGLE_WEB_CLIENT_ID=123456789.apps.googleusercontent.com
```

### 2. app/build.gradle.kts で読み込んで BuildConfig フィールドに設定

```kotlin
import java.util.Properties

android {
    buildFeatures {
        buildConfig = true   // BuildConfig の生成を有効化（デフォルト無効）
    }

    defaultConfig {
        val props = Properties().apply {
            load(rootProject.file("local.properties").inputStream())
        }
        buildConfigField("String", "API_BASE_URL", "\"${props["API_BASE_URL"]}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${props["GOOGLE_WEB_CLIENT_ID"]}\"")
    }
}
```

`buildConfigField` の第3引数は Kotlin/Java のリテラルとして解釈されるため、文字列は `\"...\"` でクォートが必要。

### 3. コードから参照

ビルド後に `BuildConfig` クラスが自動生成され、フィールドとしてアクセスできる。

```kotlin
val baseUrl = BuildConfig.API_BASE_URL       // "https://my-service.run.app/api/v1/"
val clientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
```

---

## BuildConfig が生成される場所

```
app/build/generated/source/buildConfig/debug/
  └── com/aklzo/kakeiboandroid/BuildConfig.kt
```

ビルドするまでこのファイルは存在しないため、初回は Android Studio がエラーを出すことがある。  
一度ビルドすれば解決する。

---

## 注意事項

- `local.properties` は `.gitignore` に含まれており、リポジトリに含まれない
- 新しいメンバーが参加したときは、値を別途共有する必要がある
- `buildConfigField` はビルドバリアント（debug / release）ごとに異なる値を設定することもできる
