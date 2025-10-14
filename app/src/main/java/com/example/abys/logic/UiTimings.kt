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
    /** Выбранный Asr по мазхабу: 0 = Standard, 1 = Hanafi */
    fun asr(selectedSchool: Int): String = if (selectedSchool == 1) asrHan else asrStd

    /**
     * Ближайший намаз от «сейчас».
     * Возвращает Pair<название, время в "HH:mm"> или null (крайне маловероятно).
     */
    fun nextPrayer(selectedSchool: Int): Pair<String, String>? {
        val order = listOf(
            "Fajr"    to fajr,
            "Shuruq"  to sunrise,
            "Dhuhr"   to dhuhr,
            "Asr"     to asr(selectedSchool),
            "Maghrib" to maghrib,
            "Isha"    to isha
        )
        val now = TimeHelper.now(tz)
        order.firstOrNull { (_, t) ->
            val tt = TimeHelper.parseHHmmLocal(t, tz) ?: return@firstOrNull false
            now.isBefore(tt)
        }?.let { return it }
        // Если все времена уже прошли — следующий будет Fajr (следующего дня)
        return order.firstOrNull()
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
