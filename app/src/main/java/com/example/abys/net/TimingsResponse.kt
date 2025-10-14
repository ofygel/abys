package com.example.abys.net

import com.squareup.moshi.Json

data class TimingsResponse(
    val code: Int,
    val status: String,
    val data: Data
) {
    data class Data(
        val timings: Timings,
        val date: DateInfo,
        val meta: Meta
    )

    data class Timings(
        @Json(name = "Fajr") val fajr: String,
        @Json(name = "Sunrise") val sunrise: String,
        @Json(name = "Dhuhr") val dhuhr: String,
        @Json(name = "Asr") val asr: String,
        @Json(name = "Maghrib") val maghrib: String,
        @Json(name = "Isha") val isha: String,
        @Json(name = "Sunset") val sunset: String? = null,
        @Json(name = "Imsak") val imsak: String? = null,
        @Json(name = "Midnight") val midnight: String? = null,
        @Json(name = "Firstthird") val firstThird: String? = null,
        @Json(name = "Lastthird") val lastThird: String? = null
    )

    data class DateInfo(
        val readable: String,
        val timestamp: String,
        val hijri: Hijri?
    )
    data class Hijri(
        val date: String?,           // "21-04-1447"
        val month: HijriMonth?,      // month.en = "Rabi' al-thani"
        val year: String?
    )
    data class HijriMonth(
        val number: Int?,
        val en: String?
    )

    data class Meta(
        val timezone: String,
        val method: Method
    )
    data class Method(
        val id: Int,
        val name: String
    )
}
