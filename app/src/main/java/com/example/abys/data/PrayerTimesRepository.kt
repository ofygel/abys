package com.example.abys.data

import com.example.abys.data.model.PrayerTimes
import com.example.abys.data.model.ApiResponse     // ← добавили import
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class PrayerTimesRepository {

    private val api = Retrofit.Builder()
        .baseUrl("https://api.aladhan.com/v1/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(AlAdhanApi::class.java)

    suspend fun fetch(lat: Double, lon: Double): PrayerTimes? =
        api.today(lat, lon).data              // ← data виден
            ?.timings
            ?.let { PrayerTimes.fromApi(it) }

    /* ---- Retrofit interface ---- */
    interface AlAdhanApi {
        @GET("timings")
        suspend fun today(
            @Query("latitude") lat: Double,
            @Query("longitude") lon: Double,
            @Query("method") method: Int = 2
        ): ApiResponse
    }
}
