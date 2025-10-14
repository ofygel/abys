package com.example.abys.logic

import java.time.*
import java.time.format.DateTimeFormatter
import kotlin.math.max

object TimeHelper {
    private val dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    fun todayHuman(zone: ZoneId = ZoneId.systemDefault()): String =
        ZonedDateTime.now(zone).format(dateFmt)

    fun now(zoneId: ZoneId): LocalTime = LocalTime.now(zoneId)

    /** Парсинг "HH:mm" в LocalTime (без бросков исключений). */
    fun parseHHmmLocal(hhmm: String, zone: ZoneId): LocalTime? = runCatching {
        val p = hhmm.trim().split(":")
        LocalTime.of(p[0].toInt(), p[1].toInt())
    }.getOrNull()

    /** Формат "HH:mm" из ZonedDateTime. */
    fun formatZ(zdt: ZonedDateTime): String = zdt.toLocalTime().format(timeFmt)

    /**
     * Сколько осталось до времени `targetHHmm` (Duration).
     * Если target уже прошёл сегодня — считаем до времени завтрашнего дня.
     */
    fun untilNowTo(targetHHmm: String, zone: ZoneId): Duration? {
        val lt = parseHHmmLocal(targetHHmm, zone) ?: return null
        val nowZ = ZonedDateTime.now(zone)
        var target = ZonedDateTime.of(LocalDate.now(zone), lt, zone)
        if (!nowZ.isBefore(target)) target = target.plusDays(1)
        return Duration.between(nowZ, target)
    }

    data class NightParts(
        val first: Pair<ZonedDateTime, ZonedDateTime>,
        val second: Pair<ZonedDateTime, ZonedDateTime>,
        val third: Pair<ZonedDateTime, ZonedDateTime>
    )

    /**
     * Делим отрезок [Maghrib сегодня → Fajr завтра] на 3 равные части.
     */
    fun splitNight(maghrib: String, fajr: String, zone: ZoneId): NightParts {
        val m = ZonedDateTime.of(LocalDate.now(zone), parseHHmmLocal(maghrib, zone) ?: LocalTime.of(18, 0), zone)
        var f = ZonedDateTime.of(LocalDate.now(zone), parseHHmmLocal(fajr, zone) ?: LocalTime.of(6, 0), zone)
        if (!f.isAfter(m)) f = f.plusDays(1)

        val dur = max(1L, Duration.between(m, f).toMinutes()) // защита от 0
        val chunk = Duration.ofMinutes(dur / 3)
        val p1e = m.plus(chunk)
        val p2e = p1e.plus(chunk)
        return NightParts(first = m to p1e, second = p1e to p2e, third = p2e to f)
    }
}
