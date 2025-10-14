package com.example.abys.net

import com.example.abys.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitProvider {

    /* ---------- общий OkHttpClient (с optional-логированием) ---------- */
    private val okHttp: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(logging)
        }

        builder.build()
    }

    /* ---------- AlAdhan API ---------- */
    val aladhan: AladhanApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(okHttp)
            .build()
            .create(AladhanApi::class.java)
    }

    /* ---------- Nominatim (OpenStreetMap) ---------- */
    val nominatim: NominatimApi by lazy {
        /* добавляем кастомный User-Agent, как требует Nominatim */
        val ua: Interceptor = Interceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", "PrayerTimesApp/1.0 (Android)")
                    .header("Accept-Language", "ru")
                    .build()
            )
        }

        val okNominatim = okHttp.newBuilder()
            .addInterceptor(ua)
            .build()

        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(okNominatim)
            .build()
            .create(NominatimApi::class.java)
    }
}
