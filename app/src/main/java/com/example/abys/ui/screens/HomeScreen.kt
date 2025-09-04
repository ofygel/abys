package com.example.abys.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.abys.MainViewModel
import com.example.abys.ui.components.CitySearchBar
import com.example.abys.ui.components.NightTimeline
import com.example.abys.ui.components.ShoegazeBackground
import com.example.abys.ui.effects.*
import com.example.abys.ui.media.VideoBackground
import java.time.LocalDate
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(vm: MainViewModel, onRequestLocation: () -> Unit) {
    val state by vm.timings.collectAsStateWithLifecycle()

    // 1) определяем сезон
    val season = remember { detectSeason(LocalDate.now(), northernHemisphere = true) }

    // 2) оркестровка шагов
    var intro by remember { mutableStateOf(true) }        // 0..3 сек — "много листьев/снега/цветов"
    var showVideo by remember { mutableStateOf(false) }   // плавное появление видео
    var showTable by remember { mutableStateOf(false) }   // таблица после видео-паузы
    var videoAlpha by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        intro = true
        delay(3000)                // 3 секунды плотной анимации
        intro = false
        showVideo = true
        // 0.0 -> 1.0 за 1200мс
        val steps = 12
        repeat(steps) {
            videoAlpha = (it + 1) / steps.toFloat()
            delay(100)
        }
        delay(2000)                // видео «постоять» 2 секунды
        showTable = true
    }

    Box {
        // фоновый градиент (shoegaze)
        ShoegazeBackground(Modifier.fillMaxSize())

        // видео по сезону, мягко появляющееся
        val rawName = when (season) {
            Season.AUTUMN -> "bg_autumn"
            Season.WINTER -> "bg_winter"
            Season.SPRING -> "bg_spring"
            Season.SUMMER -> "bg_summer"
        }
        if (showVideo) {
            VideoBackground(
                rawName = rawName,     // если файла нет — просто не покажется
                alpha = videoAlpha,
                modifier = Modifier.fillMaxSize()
            )
        }

        // частицы: intro = плотный «дождь», затем — лёгкий фон (3–4 за интервал)
        SeasonalParticles(
            season = season,
            heavyIntro = intro,
            modifier = Modifier.fillMaxSize()
        )

        // контент
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            CitySearchBar(
                vm,
                onSearch = { city -> vm.fetchByCity(city) },
                onGps = onRequestLocation
            )
            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(
                visible = showTable,
                enter = fadeIn(animationSpec = tween(600)) +
                        slideInVertically(initialOffsetY = { it / 6 }, animationSpec = tween(700))
            ) {
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
}

@Composable
private fun PrayerRow(label: String, time: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, modifier = Modifier.weight(1f))
        Text(time, style = MaterialTheme.typography.titleMedium)
    }
}
