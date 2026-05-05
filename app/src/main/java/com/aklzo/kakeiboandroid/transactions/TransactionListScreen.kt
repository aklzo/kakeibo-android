package com.aklzo.kakeiboandroid.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aklzo.kakeiboandroid.auth.AuthManager
import com.aklzo.kakeiboandroid.network.ApiClient
import com.aklzo.kakeiboandroid.network.model.TransactionData
import kotlinx.coroutines.launch
import java.time.YearMonth

@Composable
fun TransactionListScreen(innerPadding: PaddingValues) {
    val scope = rememberCoroutineScope()
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var transactions by remember { mutableStateOf<List<TransactionData>>(emptyList()) }
    var total by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    fun load() {
        scope.launch {
            isLoading = true
            val token = AuthManager.idToken.value ?: run { isLoading = false; return@launch }
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
    }

    LaunchedEffect(selectedMonth, selectedCategory) { load() }

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

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("取引がありません", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Text(
                text = "${total}件  合計 ¥%,d".format(transactions.sumOf { it.amount }),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(transactions) { tx ->
                    TransactionItem(tx)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(tx: TransactionData) {
    val categoryLabel = CATEGORIES.find { it.first == tx.category }?.second ?: tx.category
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
