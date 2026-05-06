# コルーチンメモ

## suspend 関数

`suspend` キーワードを付けた関数は、他の `suspend` 関数またはコルーチンスコープ内でのみ呼び出せる。  
内部で非同期処理（IO 待機など）を行っても、スレッドをブロックしない。

```kotlin
suspend fun fetchData(): List<Item> {
    return api.getItems()   // Retrofit の suspend 対応関数
}
```

---

## LaunchedEffect vs rememberCoroutineScope

### LaunchedEffect: 宣言的な副作用

コンポーザブルの**ライフサイクルに紐付いた**処理に使う。  
画面表示時やキー変更時に自動で実行したい処理が対象。

```kotlin
LaunchedEffect(selectedMonth) {
    isLoading = true
    data = api.getData(selectedMonth)
    isLoading = false
}
```

コンポーザブルが画面から外れると自動でキャンセルされる。

### rememberCoroutineScope: ユーザー操作起点の処理

ボタン押下など**ユーザー操作をトリガーとする**処理に使う。  
`scope.launch { }` で任意のタイミングでコルーチンを起動できる。

```kotlin
val scope = rememberCoroutineScope()

Button(onClick = {
    scope.launch {
        api.saveData(formData)
        snackbarHostState.showSnackbar("保存しました")
    }
})
```

---

## 使い分けのまとめ

| 処理の種類 | 使うもの |
|-----------|---------|
| 画面表示時・キー変更時の自動実行 | `LaunchedEffect` |
| ボタン押下などユーザー操作起点 | `rememberCoroutineScope` + `scope.launch` |

---

## エラーハンドリング

このプロジェクトでは最低限のエラーハンドリングを採用している。  
例外は catch して握り潰し、UI 側で null チェックをするか Snackbar を表示する。

```kotlin
try {
    val response = api.createTransaction(token, request)
    if (response.isSuccessful) {
        snackbarHostState.showSnackbar("登録しました")
    } else {
        snackbarHostState.showSnackbar("登録に失敗しました")
    }
} catch (_: Exception) {
    snackbarHostState.showSnackbar("通信エラーが発生しました")
}
```

---

## Retrofit と suspend

Retrofit は `suspend fun` に対応しており、コルーチン内でそのまま呼び出せる。  
返り値を `Response<T>` にすると HTTP ステータスコードを確認できる。

```kotlin
// ApiService 定義
@GET("transactions")
suspend fun getTransactions(
    @Header("Authorization") authorization: String,
    @Query("month") month: String?
): Response<TransactionsListResponse>

// 呼び出し側
val response = ApiClient.service.getTransactions("Bearer $token", month)
if (response.isSuccessful) {
    val data = response.body()?.data
}
```
