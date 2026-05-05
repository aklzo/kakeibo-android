package com.aklzo.kakeiboandroid.network.model

import com.google.gson.annotations.SerializedName

data class CreateTransactionRequest(
    val name: String,
    val amount: Int,
    val date: String,
    val category: String,
    val memo: String?
)

data class UpdateTransactionRequest(
    val name: String,
    val amount: Int,
    val date: String,
    val category: String,
    val memo: String?
)

data class TransactionData(
    val id: Int,
    val name: String,
    val amount: Int,
    val date: String,
    val category: String,
    val memo: String?,
    @SerializedName("created_at") val createdAt: String
)

data class TransactionResponse(val data: TransactionData)

data class TransactionsListData(
    val transactions: List<TransactionData>,
    val total: Int
)

data class TransactionsListResponse(val data: TransactionsListData)
