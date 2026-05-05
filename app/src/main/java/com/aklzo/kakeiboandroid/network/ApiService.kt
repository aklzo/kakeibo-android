package com.aklzo.kakeiboandroid.network

import com.aklzo.kakeiboandroid.network.model.CreateTransactionRequest
import com.aklzo.kakeiboandroid.network.model.TransactionResponse
import com.aklzo.kakeiboandroid.network.model.TransactionsListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("transactions")
    suspend fun createTransaction(
        @Header("Authorization") authorization: String,
        @Body request: CreateTransactionRequest
    ): Response<TransactionResponse>

    @GET("transactions")
    suspend fun getTransactions(
        @Header("Authorization") authorization: String,
        @Query("month") month: String?,
        @Query("category") category: String?,
        @Query("limit") limit: Int = 50
    ): Response<TransactionsListResponse>
}
