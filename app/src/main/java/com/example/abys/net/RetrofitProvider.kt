package com.example.abys.net

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitProvider {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
    private val ok = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val aladhan: AladhanApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(ok)
            .build()
            .create(AladhanApi::class.java)
    }

    val nominatim: NominatimApi by lazy {
        val ua = okhttp3.Interceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", "PrayerTimesApp/1.0 (Android)")
                    .header("Accept-Language", "ru")
                    .build()
            )
        }
        val okNominatim = ok.newBuilder().addInterceptor(ua).build()
        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okNominatim)
            .build()
            .create(NominatimApi::class.java)
    }
}
