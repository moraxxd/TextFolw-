package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class ApiClientManager {

    private var currentBaseUrl = "http://10.0.2.2:8000/"
    private var cachedApi: TextFlowBackendApi? = null

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    @Synchronized
    fun getApi(baseUrl: String): TextFlowBackendApi {
        val cleanUrl = if (!baseUrl.endsWith("/")) "$baseUrl/" else baseUrl
        if (cachedApi != null && currentBaseUrl == cleanUrl) {
            return cachedApi!!
        }

        currentBaseUrl = cleanUrl
        val retrofit = Retrofit.Builder()
            .baseUrl(cleanUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val api = retrofit.create(TextFlowBackendApi::class.java)
        cachedApi = api
        return api
    }
}
