package com.aklzo.kakeiboandroid.network

import com.aklzo.kakeiboandroid.network.model.CreateTransactionRequest
import com.aklzo.kakeiboandroid.network.model.TransactionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("transactions")
    suspend fun createTransaction(
        @Header("Authorization") authorization: String,
        @Body request: CreateTransactionRequest
    ): Response<TransactionResponse>
}
