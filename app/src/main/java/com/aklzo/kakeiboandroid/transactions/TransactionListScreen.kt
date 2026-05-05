package com.aklzo.kakeiboandroid.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aklzo.kakeiboandroid.auth.AuthManager
import com.aklzo.kakeiboandroid.network.ApiClient
import com.aklzo.kakeiboandroid.network.model.TransactionData
import com.aklzo.kakeiboandroid.network.model.UpdateTransactionRequest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(innerPadding: PaddingValues) {
    val scope = rememberCoroutineScope()
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var transactions by remember { mutableStateOf<List<TransactionData>>(emptyList()) }
    var total by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }

    var editingTransaction by remember { mutableStateOf<TransactionData?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingTransaction by remember { mutableStateOf<TransactionData?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun reload() { refreshKey++ }

    LaunchedEffect(selectedMonth, selectedCategory, refreshKey) {
        isLoading = true
        val token = AuthManager.idToken.value ?: run { isLoading = false; return@LaunchedEffect }
        try {
            val response = ApiClient.service.getTransactions(
                authorization = "Bearer $token",
                month = selectedMonth.toString(),
                category = selectedCategory
            )
            if (response.isSuccessful) {
                response.body()?.data?.let {
                    transactions = it.transactions
                    total = it.total
                }
            }
        } catch (_: Exception) {
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        // Month selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { selectedMonth = selectedMonth.minusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "前月")
            }
            Text(
                text = "${selectedMonth.year}年${selectedMonth.monthValue}月",
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(
                onClick = { selectedMonth = selectedMonth.plusMonths(1) },
                enabled = selectedMonth < YearMonth.now()
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "翌月")
            }
        }

        // Category filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("すべて") }
                )
            }
            items(CATEGORIES) { (id, label) ->
                FilterChip(
                    selected = selectedCategory == id,
                    onClick = { selectedCategory = if (selectedCategory == id) null else id },
                    label = { Text(label) }
                )
            }
        }

        HorizontalDivider()

        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            transactions.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("取引がありません", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> {
                Text(
                    text = "${total}件  合計 ¥%,d".format(transactions.sumOf { it.amount }),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionItem(tx, onClick = { editingTransaction = tx })
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    // Edit bottom sheet
    if (editingTransaction != null) {
        ModalBottomSheet(
            onDismissRequest = { editingTransaction = null },
            sheetState = sheetState
        ) {
            EditTransactionSheet(
                transaction = editingTransaction!!,
                onSave = { request ->
                    val token = AuthManager.idToken.value ?: return@EditTransactionSheet
                    scope.launch {
                        try {
                            val response = ApiClient.service.updateTransaction(
                                authorization = "Bearer $token",
                                id = editingTransaction!!.id,
                                request = request
                            )
                            if (response.isSuccessful) {
                                sheetState.hide()
                                editingTransaction = null
                                reload()
                            }
                        } catch (_: Exception) {
                        }
                    }
                },
                onDeleteRequest = {
                    scope.launch {
                        deletingTransaction = editingTransaction
                        sheetState.hide()
                        editingTransaction = null
                        showDeleteDialog = true
                    }
                }
            )
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && deletingTransaction != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                deletingTransaction = null
            },
            title = { Text("削除の確認") },
            text = { Text("「${deletingTransaction!!.name}」を削除しますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val tx = deletingTransaction ?: return@TextButton
                        scope.launch {
                            val token = AuthManager.idToken.value ?: return@launch
                            try {
                                val response = ApiClient.service.deleteTransaction(
                                    authorization = "Bearer $token",
                                    id = tx.id
                                )
                                if (response.isSuccessful) {
                                    showDeleteDialog = false
                                    deletingTransaction = null
                                    reload()
                                }
                            } catch (_: Exception) {
                            }
                        }
                    }
                ) {
                    Text("削除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    deletingTransaction = null
                }) { Text("キャンセル") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTransactionSheet(
    transaction: TransactionData,
    onSave: (UpdateTransactionRequest) -> Unit,
    onDeleteRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(transaction.name) }
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var selectedCategory by remember {
        mutableStateOf(CATEGORIES.find { it.first == transaction.category } ?: CATEGORIES.first())
    }
    var date by remember { mutableStateOf(LocalDate.parse(transaction.date)) }
    var memo by remember { mutableStateOf(transaction.memo ?: "") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = LocalDate.parse(transaction.date)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "取引を編集",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDeleteRequest) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "削除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("名称") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() } },
            label = { Text("金額") },
            suffix = { Text("円") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedCategory.second,
                onValueChange = {},
                readOnly = true,
                label = { Text("カテゴリ") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                CATEGORIES.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat.second) },
                        onClick = {
                            selectedCategory = cat
                            categoryExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = date.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text("日付") },
            trailingIcon = {
                TextButton(onClick = { showDatePicker = true }) { Text("変更") }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = memo,
            onValueChange = { memo = it },
            label = { Text("メモ（任意）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val amountInt = amount.toIntOrNull() ?: return@Button
                if (name.isBlank() || amountInt <= 0) return@Button
                isLoading = true
                scope.launch {
                    onSave(
                        UpdateTransactionRequest(
                            name = name,
                            amount = amountInt,
                            date = date.toString(),
                            category = selectedCategory.first,
                            memo = memo.ifBlank { null }
                        )
                    )
                    isLoading = false
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text("保存")
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("キャンセル") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun TransactionItem(tx: TransactionData, onClick: () -> Unit) {
    val categoryLabel = CATEGORIES.find { it.first == tx.category }?.second ?: tx.category
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(tx.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                "${tx.date}  $categoryLabel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            "¥%,d".format(tx.amount),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
