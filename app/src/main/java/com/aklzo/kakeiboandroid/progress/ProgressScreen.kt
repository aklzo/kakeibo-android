package com.aklzo.kakeiboandroid.progress

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.aklzo.kakeiboandroid.network.model.CategoryProgress
import com.aklzo.kakeiboandroid.network.model.ProgressData
import com.aklzo.kakeiboandroid.network.model.ProgressTotal
import com.aklzo.kakeiboandroid.transactions.CATEGORIES
import java.time.YearMonth

private const val MODE_BUDGET = "budget"
private const val MODE_LAST_MONTH = "last_month"

private val ColorWarning = Color(0xFFF57C00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(innerPadding: PaddingValues = PaddingValues(0.dp)) {
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var mode by remember { mutableStateOf(MODE_BUDGET) }
    var progressData by remember { mutableStateOf<ProgressData?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(selectedMonth, mode) {
        isLoading = true
        progressData = null
        val token = AuthManager.idToken.value ?: run { isLoading = false; return@LaunchedEffect }
        try {
            val response = ApiClient.service.getProgress(
                authorization = "Bearer $token",
                month = selectedMonth.toString(),
                mode = mode
            )
            if (response.isSuccessful) {
                progressData = response.body()?.data
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

        // Mode toggle
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            SegmentedButton(
                selected = mode == MODE_BUDGET,
                onClick = { mode = MODE_BUDGET },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text("予算比") }
            SegmentedButton(
                selected = mode == MODE_LAST_MONTH,
                onClick = { mode = MODE_LAST_MONTH },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("昨月比") }
        }

        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            progressData == null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("データがありません", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> ProgressContent(progressData!!)
        }
    }
}

@Composable
private fun ProgressContent(data: ProgressData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Total section
        data.total?.let { total ->
            SectionHeader("月全体")
            Spacer(Modifier.height(8.dp))
            ProgressRow(
                label = null,
                current = total.current,
                base = total.base,
                percentage = total.percentage
            )
        }

        // By-category section
        val byCategory = data.byCategory
        if (!byCategory.isNullOrEmpty()) {
            Spacer(Modifier.height(20.dp))
            SectionHeader("カテゴリ別")
            Spacer(Modifier.height(8.dp))
            byCategory.forEach { item ->
                ProgressRow(
                    label = CATEGORIES.find { it.first == item.category }?.second ?: item.category,
                    current = item.current,
                    base = item.base,
                    percentage = item.percentage
                )
                Spacer(Modifier.height(4.dp))
            }
        } else if (data.total != null) {
            Spacer(Modifier.height(20.dp))
            SectionHeader("カテゴリ別")
            Spacer(Modifier.height(8.dp))
            Text(
                "カテゴリ別データがありません",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(4.dp))
    HorizontalDivider()
}

@Composable
private fun ProgressRow(
    label: String?,
    current: Int,
    base: Int,
    percentage: Float
) {
    val barColor = when {
        percentage > 100f -> MaterialTheme.colorScheme.error
        percentage > 80f -> ColorWarning
        else -> MaterialTheme.colorScheme.primary
    }
    val clampedProgress = (percentage / 100f).coerceIn(0f, 1f)
    val percentText = "%.1f%%".format(percentage)

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        if (label != null) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "¥%,d / ¥%,d".format(current, base),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                percentText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { clampedProgress },
            modifier = Modifier.fillMaxWidth(),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
