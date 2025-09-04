package com.example.abys.ui.effects

import java.time.LocalDate

enum class Season { AUTUMN, WINTER, SPRING, SUMMER }

fun detectSeason(today: LocalDate = LocalDate.now(), northernHemisphere: Boolean = true): Season {
    // Для Казахстана true
    val m = today.monthValue
    return if (northernHemisphere) when (m) {
        in 3..5 -> Season.SPRING
        in 6..8 -> Season.SUMMER
        in 9..11 -> Season.AUTUMN
        else -> Season.WINTER
    } else when (m) {
        in 3..5 -> Season.AUTUMN
        in 6..8 -> Season.WINTER
        in 9..11 -> Season.SPRING
        else -> Season.SUMMER
    }
}
