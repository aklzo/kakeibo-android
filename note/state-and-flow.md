# StateFlow と状態管理メモ

## StateFlow の基本

`StateFlow` は常に最新の値を保持するリアクティブなストリーム。  
`MutableStateFlow` で書き込み、`StateFlow` として公開するのが標準パターン。

```kotlin
object AuthManager {
    private val _idToken = MutableStateFlow<String?>(null)
    val idToken: StateFlow<String?> = _idToken.asStateFlow()  // 外部には読み取り専用で公開

    fun setToken(token: String) { _idToken.value = token }
    fun clearToken() { _idToken.value = null }
}
```

---

## Compose で StateFlow を購読する: collectAsState

`collectAsState()` で `StateFlow` を Compose の `State` に変換する。  
値が変わると自動で再コンポーズされる。

```kotlin
val idToken by AuthManager.idToken.collectAsState()

if (idToken == null) {
    SignInScreen()
} else {
    MainScreen()
}
```

---

## 画面ローカルの状態: mutableStateOf

各画面内で完結する状態は `remember { mutableStateOf(...) }` を使う。

```kotlin
var isLoading by remember { mutableStateOf(false) }
var transactions by remember { mutableStateOf<List<TransactionData>>(emptyList()) }
```

---

## Map 形式の状態: mutableStateMapOf

キー・バリュー形式の状態は `mutableStateMapOf` を使う。  
`remember { mutableStateMapOf<K, V>() }` で初期化する。  
通常の `mutableStateOf(mutableMapOf(...))` では Map 内部の変更が検知されない。

```kotlin
// 予算設定画面: カテゴリ ID -> 入力金額文字列
val amounts = remember { mutableStateMapOf<String?, String>() }

amounts[null] = "150000"      // 月全体の予算
amounts["food"] = "40000"     // 食費の予算
```

---

## リスト再読み込みパターン: refreshKey

`LaunchedEffect` の key に整数カウンターを追加することで、任意のタイミングで再読み込みをトリガーできる。

```kotlin
var refreshKey by remember { mutableIntStateOf(0) }

LaunchedEffect(selectedMonth, refreshKey) {
    // selectedMonth か refreshKey が変わるたびに再実行
    transactions = fetchTransactions(selectedMonth)
}

// 削除・編集後にリストを更新したいとき
fun onDeleteSuccess() {
    refreshKey++   // key が変わり LaunchedEffect が再実行される
}
```

`mutableIntStateOf` は `mutableStateOf<Int>` の最適化版。プリミティブ型には専用の関数がある（`mutableLongStateOf`, `mutableFloatStateOf` など）。
