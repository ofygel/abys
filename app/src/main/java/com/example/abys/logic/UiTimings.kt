package com.example.abys.logic

import java.time.ZoneId

/**
 * Модель времен намазов + часовой пояс.
 * asrStd — Asr по стандарту, asrHan — по ханафитскому мазхабу.
 */
data class UiTimings(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asrStd: String,
    val asrHan: String,
    val maghrib: String,
    val isha: String,
    val tz: ZoneId
) {
    /** Возвращает Asr согласно выбранной школе: 0 — Standard, 1 — Hanafi */
    fun asr(selectedSchool: Int): String =
        if (selectedSchool == 1) asrHan else asrStd

    /**
     * Находит ближайший будущий намаз.
     * @return Pair(название, время HH:mm) или первый намаз дня, если все прошли.
     */
    fun nextPrayer(selectedSchool: Int): Pair<String, String>? {
        val ordered = listOf(
            "Fajr"    to fajr,
            "Shuruq"  to sunrise,
            "Dhuhr"   to dhuhr,
            "Asr"     to asr(selectedSchool),
            "Maghrib" to maghrib,
            "Isha"    to isha
        )

        val now = TimeHelper.now(tz)                     // ZonedDateTime «сейчас»
        for ((name, timeStr) in ordered) {
            val tt = TimeHelper.parseHHmmLocal(timeStr, tz) ?: continue
            if (now.isBefore(tt)) return name to timeStr // первый ещё не наступивший
        }
        return ordered.firstOrNull()                     // день уже прошёл ⇒ Fajr
    }

    /** Список пар “название – время” для отображения */
    fun toDisplayList(selectedSchool: Int): List<Pair<String, String>> = listOf(
        "Fajr"    to fajr,
        "Shuruq"  to sunrise,
        "Dhuhr"   to dhuhr,
        "Asr"     to asr(selectedSchool),
        "Maghrib" to maghrib,
        "Isha"    to isha
    )
}
