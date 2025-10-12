package com.example.abys.net

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AladhanApi {
    // https://api.aladhan.com/v1/timings?latitude=..&longitude=..&method=2&school=0
    @GET("v1/timings")
    suspend fun timings(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 2,
        @Query("school") school: Int = 0
    ): Response<TimingsResponse>

    // https://api.aladhan.com/v1/timingsByCity?city=Almaty&country=Kazakhstan&method=2&school=0
    @GET("v1/timingsByCity")
    suspend fun timingsByCity(
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int = 2,
        @Query("school") school: Int = 0
    ): Response<TimingsResponse>
}
