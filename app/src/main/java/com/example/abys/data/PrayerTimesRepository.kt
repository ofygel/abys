package com.example.abys.data

import com.example.abys.data.model.PrayerTimes
import com.example.abys.net.AladhanApi
import com.example.abys.net.RetrofitProvider

class PrayerTimesRepository(
    private val api: AladhanApi = RetrofitProvider.aladhan
) {

    suspend fun fetch(lat: Double, lon: Double): PrayerTimes? {
        val response = api.timings(latitude = lat, longitude = lon)
        if (!response.isSuccessful) return null

        val timings = response.body()?.data?.timings ?: return null

        return PrayerTimes(
            fajr = timings.Fajr,
            dhuhr = timings.Dhuhr,
            asr = timings.Asr,
            maghrib = timings.Maghrib,
            isha = timings.Isha
        )
    }
}
