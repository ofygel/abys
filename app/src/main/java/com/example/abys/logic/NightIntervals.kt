package com.example.abys.logic

data class NightIntervals(
    val first: Pair<String, String>,
    val second: Pair<String, String>,
    val third: Pair<String, String>
) {
    fun asList(): List<Pair<String, String>> = listOf(first, second, third)
}
