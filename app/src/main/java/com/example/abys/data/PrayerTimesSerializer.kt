package com.example.abys.data

import com.example.abys.data.model.PrayerTimes
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.ZoneId

private data class PrayerTimesSnapshot(
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
    val timezone: String,
    val methodName: String?,
    val readableDate: String?,
    val hijriDate: String?,
    val hijriMonth: String?,
    val hijriYear: String?
)

object PrayerTimesSerializer {
    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val adapter = moshi.adapter(PrayerTimesSnapshot::class.java)

    fun encode(value: PrayerTimes): String? {
        val snapshot = value.toSnapshot()
        return runCatching { adapter.toJson(snapshot) }.getOrNull()
    }

    fun decode(json: String): PrayerTimes? {
        val snapshot = runCatching { adapter.fromJson(json) }.getOrNull() ?: return null
        return snapshot.toModel()
    }
}

private fun PrayerTimes.toSnapshot(): PrayerTimesSnapshot = PrayerTimesSnapshot(
    fajr = fajr,
    sunrise = sunrise,
    dhuhr = dhuhr,
    asrStandard = asrStandard,
    asrHanafi = asrHanafi,
    maghrib = maghrib,
    isha = isha,
    imsak = imsak,
    sunset = sunset,
    midnight = midnight,
    firstThird = firstThird,
    lastThird = lastThird,
    timezone = timezone.id,
    methodName = methodName,
    readableDate = readableDate,
    hijriDate = hijriDate,
    hijriMonth = hijriMonth,
    hijriYear = hijriYear
)

private fun PrayerTimesSnapshot.toModel(): PrayerTimes = PrayerTimes(
    fajr = fajr,
    sunrise = sunrise,
    dhuhr = dhuhr,
    asrStandard = asrStandard,
    asrHanafi = asrHanafi,
    maghrib = maghrib,
    isha = isha,
    imsak = imsak,
    sunset = sunset,
    midnight = midnight,
    firstThird = firstThird,
    lastThird = lastThird,
    timezone = runCatching { ZoneId.of(timezone) }.getOrDefault(ZoneId.systemDefault()),
    methodName = methodName,
    readableDate = readableDate,
    hijriDate = hijriDate,
    hijriMonth = hijriMonth,
    hijriYear = hijriYear
)
