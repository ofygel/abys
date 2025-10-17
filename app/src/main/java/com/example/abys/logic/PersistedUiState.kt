package com.example.abys.logic

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PersistedUiState(
    val city: String,
    val hijri: String?,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asrStd: String,
    val asrHan: String,
    val maghrib: String,
    val isha: String,
    val tz: String
)
