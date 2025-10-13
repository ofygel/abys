package com.example.abys.data.model

import com.squareup.moshi.Json

data class PrayerTimes(
    val fajr: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
) {
    companion object {
        /** конвертация из json-структуры AlAdhan */
        fun fromApi(t: Timings): PrayerTimes = PrayerTimes(
            fajr = t.fajr,
            dhuhr = t.dhuhr,
            asr = t.asr,
            maghrib = t.maghrib,
            isha = t.isha
        )
    }

    /** Возвращает название и время ближайшего намаза */
    fun next(nowSec: Int): Pair<String, String> =
        listOf("Fajr" to fajr, "Dhuhr" to dhuhr, "Asr" to asr, "Maghrib" to maghrib, "Isha" to isha)
            .first { TimeUtils.hmsToSec(it.second) > nowSec }
}

/** ========== retrofit DTO ======== **/
data class ApiResponse(
    val data: Data?
) {
    data class Data(
        val timings: Timings
    )
}

data class Timings(
    @Json(name = "Fajr")    val fajr: String,
    @Json(name = "Dhuhr")   val dhuhr: String,
    @Json(name = "Asr")     val asr: String,
    @Json(name = "Maghrib") val maghrib: String,
    @Json(name = "Isha")    val isha: String
)
