package com.example.abys.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.abys.MainViewModel
import com.example.abys.ui.components.CitySearchBar
import com.example.abys.ui.components.NightTimeline
import com.example.abys.ui.components.ShoegazeBackground

@Composable
fun HomeScreen(vm: MainViewModel, onRequestLocation: () -> Unit) {
    val state by vm.timings.collectAsStateWithLifecycle()

    Box {
        ShoegazeBackground(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            CitySearchBar(
                vm,
                onSearch = { city -> vm.fetchByCity(city) },
                onGps = onRequestLocation
            )
            Spacer(Modifier.height(16.dp))

            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Город: ${state.cityLabel}", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    PrayerRow("Фаджр", state.fajr)
                    PrayerRow("Восход", state.sunrise)
                    PrayerRow("Зухр", state.dhuhr)
                    PrayerRow("Аср (Стандарт)", state.asrStandard)
                    PrayerRow("Аср (Ханафитский)", state.asrHanafi)
                    PrayerRow("Магриб", state.maghrib)
                    PrayerRow("Иша", state.isha)
                    Spacer(Modifier.height(12.dp))
                    NightTimeline(
                        first = state.nightParts.first,
                        second = state.nightParts.second,
                        third = state.nightParts.third
                    )
                }
            }
        }
    }
}

@Composable
private fun PrayerRow(label: String, time: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, modifier = Modifier.weight(1f))
        Text(time, style = MaterialTheme.typography.titleMedium)
    }
}
