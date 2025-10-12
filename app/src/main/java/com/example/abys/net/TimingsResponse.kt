package com.example.abys.net

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
        val Fajr: String,
        val Sunrise: String,
        val Dhuhr: String,
        val Asr: String,
        val Maghrib: String,
        val Isha: String
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
