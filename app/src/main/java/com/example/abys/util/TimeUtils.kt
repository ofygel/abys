package com.example.abys.util

import java.time.*

object TimeUtils {
    private val hm = java.time.format.DateTimeFormatter.ofPattern("HH:mm")

    class InvalidTimeException(message: String) : Exception(message)

    fun parseLocal(time: String, zone: ZoneId): ZonedDateTime {
        if (time.startsWith("--")) throw InvalidTimeException("Time not available")
        return if (time.contains('T')) {
            val odt = OffsetDateTime.parse(time)
            odt.atZoneSameInstant(zone)
        } else {
            val today = LocalDate.now(zone)
            val lt = LocalTime.parse(time.take(5), hm)
            ZonedDateTime.of(today, lt, zone)
        }
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
