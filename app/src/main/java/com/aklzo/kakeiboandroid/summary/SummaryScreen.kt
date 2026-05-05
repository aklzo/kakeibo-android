package com.aklzo.kakeiboandroid.summary

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aklzo.kakeiboandroid.auth.AuthManager
import com.aklzo.kakeiboandroid.network.ApiClient
import com.aklzo.kakeiboandroid.network.model.SummaryData
import com.aklzo.kakeiboandroid.transactions.CATEGORIES
import java.time.YearMonth

private val IncomeColor = Color(0xFF388E3C)
private val ExpenseColor = Color(0xFFD32F2F)

@Composable
fun SummaryScreen(innerPadding: PaddingValues) {
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var summaryData by remember { mutableStateOf<SummaryData?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(selectedMonth) {
        isLoading = true
        summaryData = null
        val token = AuthManager.idToken.value ?: run { isLoading = false; return@LaunchedEffect }
        try {
            val response = ApiClient.service.getSummary(
                authorization = "Bearer $token",
                month = selectedMonth.toString()
            )
            if (response.isSuccessful) {
                summaryData = response.body()?.data
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

        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            summaryData == null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("データがありません", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> SummaryContent(summaryData!!)
        }
    }
}

@Composable
private fun SummaryContent(data: SummaryData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Income / Expense / Balance cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                label = "収入",
                amount = data.income,
                amountColor = IncomeColor,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "支出",
                amount = data.expense,
                amountColor = ExpenseColor,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "収支",
                amount = data.balance,
                amountColor = if (data.balance >= 0) IncomeColor else ExpenseColor,
                showSign = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))

        if (data.byCategory.isNotEmpty()) {
            Text(
                "カテゴリ別内訳",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()

            val maxAmount = data.byCategory.maxOf { it.amount }.coerceAtLeast(1)

            data.byCategory.forEach { item ->
                val label = CATEGORIES.find { it.first == item.category }?.second ?: item.category
                val progress = item.amount.toFloat() / maxAmount

                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "¥%,d".format(item.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    amount: Int,
    amountColor: Color,
    modifier: Modifier = Modifier,
    showSign: Boolean = false
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            val sign = if (showSign && amount > 0) "+" else if (showSign && amount < 0) "" else ""
            Text(
                "$sign¥%,d".format(amount),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = amountColor,
                maxLines = 1
            )
        }
    }
}
