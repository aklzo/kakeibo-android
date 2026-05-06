# Jetpack Compose メモ

## @Composable の基本

`@Composable` アノテーションを付けた関数が UI のビルディングブロックになる。  
通常の関数と違い、状態が変わると自動的に再実行（再コンポーズ）される。

```kotlin
@Composable
fun Greeting(name: String) {
    Text("Hello, $name")
}
```

---

## 状態の保持: remember と mutableStateOf

`remember` はコンポーズをまたいで値を保持する。`mutableStateOf` と組み合わせて状態を作る。  
状態が変わると、その値を参照しているコンポーザブルが再コンポーズされる。

```kotlin
var count by remember { mutableStateOf(0) }
Button(onClick = { count++ }) { Text("Count: $count") }
```

`by` デリゲートを使うと `.value` を省略できる（`getValue`/`setValue` の委譲）。

---

## 副作用: LaunchedEffect

コンポーザブルの外側（API 呼び出し、DB アクセスなど）で実行する処理を `LaunchedEffect` に書く。  
`key` が変わるたびに再実行される。`Unit` を渡すと初回のみ実行。

```kotlin
LaunchedEffect(selectedMonth) {
    // selectedMonth が変わるたびに呼ばれる
    data = api.getTransactions(month = selectedMonth.toString())
}
```

複数の key を渡すと、いずれかが変わったときに再実行される。

```kotlin
LaunchedEffect(selectedMonth, refreshKey) { ... }
```

---

## Scaffold と BottomNavigation

`Scaffold` はアプリの基本レイアウト（TopAppBar / BottomBar / FAB / Snackbar）を提供する。  
`innerPadding` を受け取ってコンテンツに適用しないと、BottomBar の裏にコンテンツが隠れる。

```kotlin
Scaffold(
    bottomBar = { NavigationBar { /* ... */ } },
    snackbarHost = { SnackbarHost(snackbarHostState) }
) { innerPadding ->
    Content(modifier = Modifier.padding(innerPadding))
}
```

---

## ModalBottomSheet

`rememberModalBottomSheetState()` で状態を管理する。  
`sheetState.hide()` は suspend 関数なので、コルーチン内で呼び出す。

```kotlin
val sheetState = rememberModalBottomSheetState()
val scope = rememberCoroutineScope()

ModalBottomSheet(
    onDismissRequest = { showSheet = false },
    sheetState = sheetState
) {
    Button(onClick = {
        scope.launch {
            sheetState.hide()       // アニメーション完了まで待機
            showSheet = false
            showDialog = true       // シートが閉じてからダイアログを開く
        }
    }) { Text("削除") }
}
```

---

## AlertDialog

```kotlin
AlertDialog(
    onDismissRequest = { showDialog = false },
    title = { Text("削除の確認") },
    text = { Text("削除しますか？") },
    confirmButton = {
        TextButton(onClick = { /* 削除処理 */ }) { Text("削除") }
    },
    dismissButton = {
        TextButton(onClick = { showDialog = false }) { Text("キャンセル") }
    }
)
```

---

## ExposedDropdownMenuBox（ドロップダウン）

```kotlin
var expanded by remember { mutableStateOf(false) }
var selected by remember { mutableStateOf(options.first()) }

ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = it }
) {
    OutlinedTextField(
        value = selected,
        onValueChange = {},
        readOnly = true,
        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
    )
    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.forEach { option ->
            DropdownMenuItem(
                text = { Text(option) },
                onClick = { selected = option; expanded = false }
            )
        }
    }
}
```

---

## LinearProgressIndicator

```kotlin
LinearProgressIndicator(
    progress = { 0.8f },           // ラムダで渡す（直接 Float は deprecated）
    modifier = Modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.primary,
    trackColor = MaterialTheme.colorScheme.surfaceVariant
)
```
