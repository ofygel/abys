package com.example.abys.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter

object TimeUtils {
    private val fmt = DateTimeFormatter.ofPattern("HH:mm")

    /** "05:28" -> 19680 сек */
    fun hmsToSec(hms: String): Int =
        LocalTime.parse(hms, fmt).toSecondOfDay()

    /** "05:28" –> LocalTime */
    fun parse(hms: String): LocalTime = LocalTime.parse(hms, fmt)
}
