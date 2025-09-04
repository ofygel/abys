package com.example.abys.data.aladhan

import retrofit2.http.GET
import retrofit2.http.Query

interface AladhanService {

    // По городу
    @GET("v1/timingsByCity")
    suspend fun timingsByCity(
        @Query("city") city: String,
        @Query("country") country: String = "",
        @Query("method") method: Int = 2,   // MWL по умолчанию
        @Query("school") school: Int = 0,   // 0 - Shafi (Стандарт), 1 - Hanafi
        @Query("iso8601") iso: Boolean = true
    ): AladhanResponse

    // По координатам (для GPS)
    @GET("v1/timings")
    suspend fun timingsByCoords(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("method") method: Int = 2,
        @Query("school") school: Int = 0,
        @Query("iso8601") iso: Boolean = true
    ): AladhanResponse

    companion object {
        fun create(): AladhanService {
            val logging = okhttp3.logging.HttpLoggingInterceptor()
                .apply { level = okhttp3.logging.HttpLoggingInterceptor.Level.BASIC }
            val ok = okhttp3.OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            return retrofit2.Retrofit.Builder()
                .baseUrl("https://api.aladhan.com/")
                .client(ok)
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()
                .create(AladhanService::class.java)
        }
    }
}
