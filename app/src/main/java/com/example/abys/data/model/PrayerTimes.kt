package com.example.abys.data.model

import com.example.abys.util.TimeUtils
import java.time.ZoneId
import java.time.ZonedDateTime

data class PrayerTimes(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asrStandard: String,
    val asrHanafi: String,
    val maghrib: String,
    val isha: String,
    val imsak: String?,
    val sunset: String?,
    val midnight: String?,
    val firstThird: String?,
    val lastThird: String?,
    val timezone: ZoneId,
    val methodName: String?,
    val readableDate: String?,
    val hijriDate: String?,
    val hijriMonth: String?,
    val hijriYear: String?
) {

    fun asr(school: Int): String = if (school == 1) asrHanafi else asrStandard

    fun schedule(school: Int): List<Pair<String, String>> = listOfNotNull(
        imsak?.let { "Imsak" to it },
        "Fajr" to fajr,
        "Sunrise" to sunrise,
        "Dhuhr" to dhuhr,
        "Asr" to asr(school),
        sunset?.let { "Sunset" to it },
        "Maghrib" to maghrib,
        "Isha" to isha
    )

    fun next(now: ZonedDateTime = ZonedDateTime.now(timezone), school: Int): Pair<String, String>? {
        val day = now.toLocalDate()
        val entries = listOfNotNull(
            imsak?.let { "Imsak" to it },
            "Fajr" to fajr,
            "Sunrise" to sunrise,
            "Dhuhr" to dhuhr,
            "Asr" to asr(school),
            "Maghrib" to maghrib,
            "Isha" to isha
        )
        val upcoming = entries.firstOrNull { (_, raw) ->
            val time = runCatching { TimeUtils.parse(clean(raw)) }.getOrNull() ?: return@firstOrNull false
            val zoned = time.atDate(day).atZone(timezone)
            zoned.isAfter(now)
        }
        return upcoming ?: entries.firstOrNull()
    }

    private fun clean(value: String): String = value.substringBefore(" ").trim()
}
