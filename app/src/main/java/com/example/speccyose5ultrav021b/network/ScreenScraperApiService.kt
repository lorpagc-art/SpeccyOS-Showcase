package com.example.speccyose5ultrav021b.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ScreenScraperApiService {

    @GET("jeuInfos.php")
    suspend fun getGameInfo(
        // Developer credentials
        @Query("devid") devId: String,
        @Query("devpassword") devPassword: String,
        @Query("softname") softwareName: String,

        // User credentials
        @Query("ssid") user: String,
        @Query("sspassword") pass: String,

        // Game query
        @Query("romnom") romName: String,
        @Query("systemeid") systemId: Int? = null,

        // Response format
        @Query("output") output: String = "json"
    ): Response<ScreenScraperResponse>
}
