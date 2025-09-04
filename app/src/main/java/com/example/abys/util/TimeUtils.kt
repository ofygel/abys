package com.example.abys.util

import java.time.*

object TimeUtils {
    private val hm = java.time.format.DateTimeFormatter.ofPattern("HH:mm")

    fun parseLocal(time: String, zone: ZoneId): ZonedDateTime {
        val today = LocalDate.now(zone)
        val lt = LocalTime.parse(time.take(5), hm)
        return ZonedDateTime.of(today, lt, zone)
    }

    data class NightParts(
        val first: Pair<ZonedDateTime, ZonedDateTime>,
        val second: Pair<ZonedDateTime, ZonedDateTime>,
        val third: Pair<ZonedDateTime, ZonedDateTime>
    )

    // ночь: от магриба до фаджра следующего дня
    fun splitNight(maghrib: String, fajr: String, zone: ZoneId): NightParts {
        val m = parseLocal(maghrib, zone)
        val f = parseLocal(fajr, zone).plusDays(1)
        val dur = Duration.between(m, f)
        val chunk = dur.dividedBy(3)
        val p1e = m.plus(chunk)
        val p2e = p1e.plus(chunk)
        return NightParts(
            first = m to p1e,
            second = p1e to p2e,
            third = p2e to f
        )
    }

    fun format(zdt: ZonedDateTime): String = zdt.toLocalTime().format(hm)
}
