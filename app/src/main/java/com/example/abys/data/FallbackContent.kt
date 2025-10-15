package com.example.abys.data

import com.example.abys.data.model.PrayerTimes
import com.example.abys.logic.UiTimings
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Набор демонстрационных данных, чтобы интерфейс выглядел живым до загрузки реального расписания.
 */
object FallbackContent {
    private val zone: ZoneId = ZoneId.of("Asia/Almaty")
    private val locale = Locale("ru", "KZ")
    private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", locale)

    val cityLabel: String = "Алматы, Казахстан"
    val hijriLabel: String = "10 Зуль-када 1445"

    val uiTimings: UiTimings = UiTimings(
        fajr = "04:12",
        sunrise = "05:48",
        dhuhr = "13:26",
        asrStd = "17:16",
        asrHan = "18:20",
        maghrib = "20:34",
        isha = "22:02",
        tz = zone
    )

    fun nextPrayer(selectedSchool: Int): Pair<String, String>? {
        return uiTimings.nextPrayer(selectedSchool) ?: ("Dhuhr" to uiTimings.dhuhr)
    }

    val prayerTimes: PrayerTimes = PrayerTimes(
        fajr = uiTimings.fajr,
        sunrise = uiTimings.sunrise,
        dhuhr = uiTimings.dhuhr,
        asrStandard = uiTimings.asrStd,
        asrHanafi = uiTimings.asrHan,
        maghrib = uiTimings.maghrib,
        isha = uiTimings.isha,
        imsak = "04:02",
        sunset = "20:36",
        midnight = "00:24",
        firstThird = "22:31",
        lastThird = "02:17",
        timezone = zone,
        methodName = "Muslim World League",
        readableDate = LocalDate.now(zone).format(dateFormatter),
        hijriDate = "10 Dhul Qa'dah 1445",
        hijriMonth = "Dhul Qa'dah",
        hijriYear = "1445"
    )

    val actionTips: List<ActionTip> = listOf(
        ActionTip(
            title = "Включите геолокацию",
            description = "Мы подберём точные времена намазов по вашему текущему местоположению.",
            cta = "Разрешить доступ",
            action = TipAction.LOCATION
        ),
        ActionTip(
            title = "Выберите любимый город",
            description = "Сохраните избранные города, чтобы переключаться между расписаниями в один тап.",
            cta = "Открыть список",
            action = TipAction.CITY
        ),
        ActionTip(
            title = "Настройте напоминания",
            description = "Получайте уведомления за 15 минут до начала каждого намаза.",
            cta = "Включить напоминания",
            action = TipAction.REMINDER
        )
    )

    val inspiration: List<String> = listOf(
        "Заранее выделите время на тихий зикр после каждого намаза.",
        "Последняя треть ночи — лучшее время для искренних ду'а.",
        "Запланируйте благие дела между Магрибом и Иша для духовного роста."
    )

    data class ActionTip(
        val title: String,
        val description: String,
        val cta: String,
        val action: TipAction
    )

    enum class TipAction { LOCATION, CITY, REMINDER }
}
