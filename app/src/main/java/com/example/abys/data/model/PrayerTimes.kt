package com.example.abys.data.model

import com.example.abys.util.TimeUtils

data class PrayerTimes(
    val fajr: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
) {

    /** Возвращает название и время ближайшего намаза */
    fun next(nowSec: Int): Pair<String, String> =
        listOf(
            "Fajr" to fajr,
            "Dhuhr" to dhuhr,
            "Asr" to asr,
            "Maghrib" to maghrib,
            "Isha" to isha
        ).firstOrNull { TimeUtils.hmsToSec(it.second) > nowSec }
            ?: ("Fajr" to fajr)
}
