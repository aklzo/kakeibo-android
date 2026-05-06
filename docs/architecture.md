# アーキテクチャ

## パッケージ構成

```
com.aklzo.kakeiboandroid/
├── auth/
│   ├── AuthManager.kt          # ID Token のグローバル保持（StateFlow）
│   └── SignInScreen.kt         # Google Sign-In 画面
├── network/
│   ├── ApiClient.kt            # Retrofit インスタンスのシングルトン
│   ├── ApiService.kt           # エンドポイント定義（インターフェース）
│   └── model/
│       ├── Transaction.kt      # 取引の request / response モデル
│       ├── Summary.kt          # 集計レスポンスモデル
│       ├── Progress.kt         # 進捗レスポンスモデル
│       └── Budget.kt           # 予算の request / response モデル
├── transactions/
│   ├── Category.kt             # カテゴリ定義（ID と日本語名のリスト）
│   ├── AddTransactionScreen.kt
│   └── TransactionListScreen.kt
├── summary/
│   └── SummaryScreen.kt
├── progress/
│   └── ProgressScreen.kt
├── budget/
│   └── BudgetSettingScreen.kt
├── ui/theme/                   # Material3 テーマ設定（自動生成）
└── MainActivity.kt             # BottomNavigation とルーティング
```

---

## 認証フロー

```
起動
 └─ MainActivity
      └─ AuthManager.idToken を購読（collectAsState）
           ├─ null → SignInScreen を表示
           │    └─ Credential Manager で Google Sign-In
           │         └─ 成功: AuthManager.setToken(idToken)
           └─ 非null → メイン画面（BottomNavigation）を表示
```

ID Token はメモリ上（`MutableStateFlow`）のみに保持する。  
アプリ再起動時は再サインインが必要だが、ID Token の有効期限（約1時間）を考慮するとこれが適切。

---

## 状態管理

### グローバル状態

`AuthManager` オブジェクトが `StateFlow<String?>` で ID Token を保持する。  
`MainActivity` で `collectAsState()` して認証状態の切り替えに使用する。

### 画面ローカル状態

各画面は `remember { mutableStateOf(...) }` で状態を管理する。  
画面間の状態共有は行わず、各画面が独立して API を呼び出す。

```kotlin
var isLoading by remember { mutableStateOf(false) }
var data by remember { mutableStateOf<SomeData?>(null) }
```

### 副作用（API 呼び出し）

条件に応じて自動的に実行する処理は `LaunchedEffect` を使用する。

```kotlin
LaunchedEffect(selectedMonth, refreshKey) {
    // selectedMonth か refreshKey が変わるたびに再実行
    isLoading = true
    // API 呼び出し
    isLoading = false
}
```

ユーザー操作で実行する処理（ボタン押下など）は `rememberCoroutineScope` を使用する。

```kotlin
val scope = rememberCoroutineScope()
Button(onClick = { scope.launch { /* API 呼び出し */ } })
```

---

## 通信層

### ApiClient

`object`（シングルトン）として定義し、`lazy` で初回アクセス時に Retrofit を構築する。  
`BuildConfig.API_BASE_URL` は `local.properties` から注入される。

`GsonBuilder().serializeNulls()` を使用しているのは、PATCH リクエストで `memo: null` を明示的に送る必要があるため。  
デフォルトの Gson は `null` フィールドを JSON に含めない。

### ApiService

Retrofit のインターフェース。全エンドポイントで `@Header("Authorization")` を受け取り、Bearer Token を付与する。  
呼び出し側で毎回 `"Bearer ${AuthManager.idToken.value}"` を渡す。

### モデル

API レスポンスの JSON 構造に合わせた data class。  
スネークケースのキーは `@SerializedName("created_at")` などで対応する。

---

## BottomNavigation

`MainActivity` で `Screen` enum と `mutableStateOf` により画面を切り替える。  
各画面は `innerPadding: PaddingValues` を受け取り、`Scaffold` のコンテンツ領域にフィットする。

```kotlin
private enum class Screen(val label: String) {
    Add("追加"), List("一覧"), Summary("集計"), Progress("進捗"), Budget("予算")
}
```
