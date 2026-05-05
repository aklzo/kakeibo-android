package com.aklzo.kakeiboandroid.network

import com.aklzo.kakeiboandroid.network.model.CreateTransactionRequest
import com.aklzo.kakeiboandroid.network.model.ProgressResponse
import com.aklzo.kakeiboandroid.network.model.SummaryResponse
import com.aklzo.kakeiboandroid.network.model.TransactionResponse
import com.aklzo.kakeiboandroid.network.model.TransactionsListResponse
import com.aklzo.kakeiboandroid.network.model.UpdateTransactionRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
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

    @PATCH("transactions/{id}")
    suspend fun updateTransaction(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
        @Body request: UpdateTransactionRequest
    ): Response<TransactionResponse>

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Response<Unit>

    @GET("summary")
    suspend fun getSummary(
        @Header("Authorization") authorization: String,
        @Query("month") month: String?,
        @Query("by_category") byCategory: Boolean = true
    ): Response<SummaryResponse>

    @GET("progress")
    suspend fun getProgress(
        @Header("Authorization") authorization: String,
        @Query("month") month: String?,
        @Query("mode") mode: String,
        @Query("scope") scope: String = "both"
    ): Response<ProgressResponse>
}
