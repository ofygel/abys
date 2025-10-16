package com.example.abys.data

import com.example.abys.data.FallbackContent
import com.example.abys.data.model.PrayerTimes
import com.example.abys.net.AladhanApi
import com.example.abys.net.RetrofitProvider
import java.time.ZoneId
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class PrayerTimesRepository(
    private val api: AladhanApi = RetrofitProvider.aladhan
) {

    private var cache: PrayerTimes = FallbackContent.prayerTimes

    suspend fun fetch(lat: Double, lon: Double): PrayerTimes = coroutineScope {
        val stdDeferred = async { api.timings(latitude = lat, longitude = lon, school = 0) }
        val hanDeferred = async { api.timings(latitude = lat, longitude = lon, school = 1) }

        val stdResp = runCatching { stdDeferred.await() }.getOrNull()
        val stdBody = if (stdResp?.isSuccessful == true) stdResp.body() else null

        if (stdBody == null) {
            return@coroutineScope cache
        }

        val hanResp = runCatching { hanDeferred.await() }.getOrNull()
        val hanBody = if (hanResp?.isSuccessful == true) hanResp.body() else null

        val stdTimings = stdBody.data.timings
        val hanTimings = hanBody?.data?.timings

        val zone = runCatching { ZoneId.of(stdBody.data.meta.timezone) }.getOrElse { ZoneId.systemDefault() }

        cache = PrayerTimes(
            fajr = stdTimings.fajr.clean(),
            sunrise = stdTimings.sunrise.clean(),
            dhuhr = stdTimings.dhuhr.clean(),
            asrStandard = stdTimings.asr.clean(),
            asrHanafi = hanTimings?.asr?.clean() ?: stdTimings.asr.clean(),
            maghrib = stdTimings.maghrib.clean(),
            isha = stdTimings.isha.clean(),
            imsak = stdTimings.imsak.cleanNullable(),
            sunset = stdTimings.sunset.cleanNullable(),
            midnight = stdTimings.midnight.cleanNullable(),
            firstThird = stdTimings.firstThird.cleanNullable(),
            lastThird = stdTimings.lastThird.cleanNullable(),
            timezone = zone,
            methodName = stdBody.data.meta.method.name,
            readableDate = stdBody.data.date.readable,
            hijriDate = stdBody.data.date.hijri?.date,
            hijriMonth = stdBody.data.date.hijri?.month?.en,
            hijriYear = stdBody.data.date.hijri?.year
        )

        cache
    }
}

private fun String.clean(): String = substringBefore(" ").trim()
private fun String?.cleanNullable(): String? = this?.let { it.substringBefore(" ").trim().ifBlank { null } }
