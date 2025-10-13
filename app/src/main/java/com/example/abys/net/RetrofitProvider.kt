package com.example.abys.net

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitProvider {

    /* ---------- общий OkHttpClient (без лог-интерсептора) ---------- */
    private val okHttp: OkHttpClient = OkHttpClient.Builder()
        .build()

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
