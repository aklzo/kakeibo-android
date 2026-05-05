package com.aklzo.kakeiboandroid.budget

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aklzo.kakeiboandroid.auth.AuthManager
import com.aklzo.kakeiboandroid.network.ApiClient
import com.aklzo.kakeiboandroid.network.model.CreateBudgetRequest
import com.aklzo.kakeiboandroid.transactions.CATEGORIES
import kotlinx.coroutines.launch

@Composable
fun BudgetSettingScreen(
    snackbarHostState: SnackbarHostState,
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    val scope = rememberCoroutineScope()
    val amounts = remember { mutableStateMapOf<String?, String>() }
    var isSaving by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    fun load() {
        scope.launch {
            isLoading = true
            val token = AuthManager.idToken.value ?: run { isLoading = false; return@launch }
            try {
                val response = ApiClient.service.getBudgets("Bearer $token")
                if (response.isSuccessful) {
                    val budgets = response.body()?.data?.budgets ?: emptyList()
                    // Initialize keys that haven't been typed in yet
                    (listOf<String?>(null) + CATEGORIES.map { it.first }).forEach { cat ->
                        if (!amounts.containsKey(cat)) amounts[cat] = ""
                    }
                    // Overwrite with server values (month=null entries only)
                    budgets.filter { it.month == null }.forEach { b ->
                        amounts[b.category] = b.amount.toString()
                    }
                }
            } catch (_: Exception) {
            }
            isLoading = false
        }
    }

    fun save(category: String?) {
        val amountStr = amounts[category]?.trim() ?: return
        val amount = amountStr.toIntOrNull()?.takeIf { it > 0 } ?: run {
            scope.launch { snackbarHostState.showSnackbar("金額は正の整数で入力してください") }
            return
        }
        isSaving = true
        scope.launch {
            val token = AuthManager.idToken.value ?: run { isSaving = false; return@launch }
            try {
                val response = ApiClient.service.createBudget(
                    "Bearer $token",
                    CreateBudgetRequest(month = null, category = category, amount = amount)
                )
                if (response.isSuccessful) {
                    response.body()?.data?.let { saved ->
                        amounts[saved.category] = saved.amount.toString()
                    }
                    snackbarHostState.showSnackbar("保存しました")
                } else {
                    snackbarHostState.showSnackbar("保存に失敗しました")
                }
            } catch (_: Exception) {
                snackbarHostState.showSnackbar("通信エラーが発生しました")
            }
            isSaving = false
        }
    }

    LaunchedEffect(Unit) { load() }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SectionLabel("月全体")
        BudgetRow(
            label = "月全体予算",
            value = amounts[null] ?: "",
            onValueChange = { amounts[null] = it },
            isSaving = isSaving,
            onSave = { save(null) }
        )

        Spacer(Modifier.height(24.dp))
        SectionLabel("カテゴリ別")

        CATEGORIES.forEach { (id, label) ->
            BudgetRow(
                label = label,
                value = amounts[id] ?: "",
                onValueChange = { amounts[id] = it },
                isSaving = isSaving,
                onSave = { save(id) }
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(4.dp))
    HorizontalDivider()
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun BudgetRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isSaving: Boolean,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it.filter { c -> c.isDigit() }) },
            label = { Text(label) },
            suffix = { Text("円") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = onSave,
            enabled = !isSaving && value.isNotBlank()
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
                Text("設定")
            }
        }
    }
}
