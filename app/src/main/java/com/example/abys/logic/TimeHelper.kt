package com.example.abys.logic

import java.time.*
import java.time.format.DateTimeFormatter

object TimeHelper {
    private val dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    fun todayHuman(): String = ZonedDateTime.now().format(dateFmt)
    fun now(zoneId: ZoneId): LocalTime = LocalTime.now(zoneId)

    fun parseHHmmLocal(hhmm: String, zone: ZoneId): LocalTime? = runCatching {
        val p = hhmm.trim().split(":")
        LocalTime.of(p[0].toInt(), p[1].toInt())
    }.getOrNull()

    fun untilNowTo(target: String, zone: ZoneId): Duration? {
        val t = parseHHmmLocal(target, zone) ?: return null
        val now = LocalTime.now(zone)
        var targetDt = LocalDateTime.of(LocalDate.now(zone), t)
        if (now.isAfter(t)) targetDt = targetDt.plusDays(1)
        val nowDt = LocalDateTime.of(LocalDate.now(zone), now)
        return Duration.between(nowDt, targetDt)
    }

    data class NightParts(
        val first: Pair<ZonedDateTime, ZonedDateTime>,
        val second: Pair<ZonedDateTime, ZonedDateTime>,
        val third: Pair<ZonedDateTime, ZonedDateTime>
    )

    fun splitNight(maghrib: String, fajr: String, zone: ZoneId): NightParts {
        val m = parseLocalOrThrow(maghrib, zone)
        val f = parseLocalOrThrow(fajr, zone).plusDays(1)
        val dur = Duration.between(m, f)
        val chunk = dur.dividedBy(3)
        val p1e = m.plus(chunk)
        val p2e = p1e.plus(chunk)
        return NightParts(first = m to p1e, second = p1e to p2e, third = p2e to f)
    }

    private fun parseLocalOrThrow(time: String, zone: ZoneId): ZonedDateTime {
        val p = timeFmt.parse(time)
        val lt = LocalTime.from(p)
        return ZonedDateTime.of(LocalDate.now(zone), lt, zone)
    }

    fun formatZ(zdt: ZonedDateTime): String = zdt.toLocalTime().format(timeFmt)
}
