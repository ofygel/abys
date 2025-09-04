package com.example.abys.data.aladhan

data class AladhanResponse(val code: Int, val status: String, val data: AladhanData)
data class AladhanData(val timings: Timings, val date: DateInfo, val meta: Meta)
data class Timings(
    val Fajr: String,
    val Sunrise: String,
    val Dhuhr: String,
    val Asr: String,
    val Sunset: String?,
    val Maghrib: String,
    val Isha: String,
    val Imsak: String?,
    val Midnight: String?
)
data class DateInfo(val readable: String)
data class Meta(val timezone: String)
