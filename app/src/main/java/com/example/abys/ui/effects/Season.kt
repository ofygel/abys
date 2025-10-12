package com.example.abys.ui.effects

import java.time.LocalDate
import java.time.Month

enum class Season { Winter, Spring, Summer, Autumn }

// Простейшее разбиение по месяцам для северного полушария
fun currentSeason(d: LocalDate = LocalDate.now()): Season = when (d.month) {
    Month.DECEMBER, Month.JANUARY, Month.FEBRUARY -> Season.Winter
    Month.MARCH, Month.APRIL, Month.MAY -> Season.Spring
    Month.JUNE, Month.JULY, Month.AUGUST -> Season.Summer
    else -> Season.Autumn
}
