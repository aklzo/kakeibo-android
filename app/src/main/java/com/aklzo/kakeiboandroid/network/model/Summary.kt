package com.aklzo.kakeiboandroid.network.model

import com.google.gson.annotations.SerializedName

data class CategorySummary(
    val category: String,
    val amount: Int
)

data class SummaryData(
    val month: String,
    val income: Int,
    val expense: Int,
    val balance: Int,
    @SerializedName("by_category") val byCategory: List<CategorySummary>
)

data class SummaryResponse(val data: SummaryData)
