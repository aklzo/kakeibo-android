package com.aklzo.kakeiboandroid.network

import com.aklzo.kakeiboandroid.BuildConfig
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    val service: ApiService by lazy {
        val baseUrl = BuildConfig.API_BASE_URL.trimEnd('/') + "/"
        val gson = GsonBuilder().serializeNulls().create()
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
