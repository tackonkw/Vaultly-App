package com.project.vaultly.core.network

import com.project.vaultly.BuildConfig
import com.project.vaultly.feature.auth.data.UserRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")

            UserRepository.getAccessToken()?.takeIf { it.isNotBlank() }?.let { token ->
                requestBuilder.header("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }
        .addInterceptor(loggingInterceptor)
        .build()

    val service: VaultlyApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(VaultlyApiService::class.java)
}
