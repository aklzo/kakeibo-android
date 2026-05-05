package com.aklzo.kakeiboandroid.transactions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aklzo.kakeiboandroid.auth.AuthManager
import com.aklzo.kakeiboandroid.network.ApiClient
import com.aklzo.kakeiboandroid.network.model.CreateTransactionRequest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val CATEGORIES = listOf(
    "food" to "食費",
    "fixed" to "固定費",
    "leisure" to "娯楽",
    "transport" to "交通費",
    "medical" to "医療費",
    "clothing" to "衣服・美容",
    "daily" to "日用品",
    "income" to "収入",
    "other" to "その他"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(innerPadding: PaddingValues, snackbarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CATEGORIES.first()) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var memo by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = LocalDate.now()
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
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
                TextButton(onClick = { showDatePicker = true }) {
                    Text("変更")
                }
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
                scope.launch {
                    isLoading = true
                    val message = submitTransaction(
                        name = name,
                        amount = amount,
                        category = selectedCategory.first,
                        date = date,
                        memo = memo,
                        onSuccess = {
                            name = ""
                            amount = ""
                            selectedCategory = CATEGORIES.first()
                            date = LocalDate.now()
                            memo = ""
                        }
                    )
                    snackbarHostState.showSnackbar(message)
                    isLoading = false
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text("追加")
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
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

private suspend fun submitTransaction(
    name: String,
    amount: String,
    category: String,
    date: LocalDate,
    memo: String,
    onSuccess: () -> Unit
): String {
    val token = AuthManager.idToken.value ?: return "サインインが必要です"
    val amountInt = amount.toIntOrNull()
    if (name.isBlank() || amountInt == null || amountInt <= 0) {
        return "名称と金額（正の整数）を入力してください"
    }
    return try {
        val response = ApiClient.service.createTransaction(
            authorization = "Bearer $token",
            request = CreateTransactionRequest(
                name = name,
                amount = amountInt,
                date = date.toString(),
                category = category,
                memo = memo.ifBlank { null }
            )
        )
        if (response.isSuccessful) {
            onSuccess()
            "取引を追加しました"
        } else {
            "エラー: ${response.code()}"
        }
    } catch (e: Exception) {
        "通信エラー: ${e.message}"
    }
}
