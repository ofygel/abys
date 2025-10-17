package com.example.abys.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import com.example.abys.data.CityEntry
import com.example.abys.data.CityRepository

@Composable
fun rememberCityDirectory(): List<CityEntry> {
    val configuration = LocalConfiguration.current
    return remember(configuration) { CityRepository.cities }
}
