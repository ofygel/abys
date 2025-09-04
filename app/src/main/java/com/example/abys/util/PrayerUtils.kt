package com.example.abys.util

import com.example.abys.TimingsUi
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

object PrayerUtils {
    data class Next(val name: String, val inDur: Duration)

    fun nextPrayer(t: TimingsUi): Next? {
        val tz = runCatching { ZoneId.of(t.tzId) }.getOrDefault(ZoneId.systemDefault())
        val now = ZonedDateTime.now(tz)
        val times = listOf(
            "Фаджр" to TimeUtils.parseLocal(t.fajr, tz),
            "Зухр"  to TimeUtils.parseLocal(t.dhuhr, tz),
            "Аср"   to TimeUtils.parseLocal(t.asrStandard, tz),
            "Магриб" to TimeUtils.parseLocal(t.maghrib, tz),
            "Иша"   to TimeUtils.parseLocal(t.isha, tz)
        )
        val next = times.firstOrNull { it.second.isAfter(now) }
            ?: ("Фаджр" to TimeUtils.parseLocal(t.fajr, tz).plusDays(1))
        return Next(next.first, Duration.between(now, next.second))
    }

    fun formatDur(d: Duration): String {
        val h = d.toHours()
        val m = d.minusHours(h).toMinutes()
        val s = d.minusHours(h).minusMinutes(m).seconds
        return "%02d:%02d:%02d".format(h, m, s)
    }
}
