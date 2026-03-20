package com.example.speccyose5ultrav021b.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object ScreenScraperRetrofitInstance {

    private const val BASE_URL = "https://www.screenscraper.fr/api2/"

    // Developer credentials for ScreenScraper API. These are not the user's credentials.
    // It's recommended to register your own developer account on their website.
    // For now, we can use some generic ones, but they are rate-limited.
    const val DEV_ID = "SpeccyOS"
    const val DEV_PASSWORD = "" // Often not needed for basic calls
    const val SOFTWARE_NAME = "SpeccyFrontier"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Adding a logger to see request details, very useful for debugging
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient) // Using the client with the logger
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val api: ScreenScraperApiService by lazy {
        retrofit.create(ScreenScraperApiService::class.java)
    }
}
