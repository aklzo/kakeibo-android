package com.aklzo.kakeiboandroid.network.model

import com.google.gson.annotations.SerializedName

data class ProgressTotal(
    val base: Int,
    val current: Int,
    val percentage: Float
)

data class CategoryProgress(
    val category: String,
    val base: Int,
    val current: Int,
    val percentage: Float
)

data class ProgressData(
    val month: String,
    val mode: String,
    val total: ProgressTotal?,
    @SerializedName("by_category") val byCategory: List<CategoryProgress>?
)

data class ProgressResponse(val data: ProgressData)
