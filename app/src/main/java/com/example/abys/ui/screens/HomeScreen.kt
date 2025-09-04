package com.example.abys.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.abys.MainViewModel
import com.example.abys.data.prefs.SlideAnchor
import com.example.abys.data.prefs.SlideStore
import com.example.abys.ui.background.Slides
import com.example.abys.ui.background.SlideshowBackground
import com.example.abys.ui.components.GlassCard
import com.example.abys.ui.components.GlassTopBar
import com.example.abys.ui.components.NextPrayerChip
import com.example.abys.ui.components.NightTimeline
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(vm: MainViewModel, onRequestLocation: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val slides = Slides.all

    // якорь слайда из DataStore
    val anchor by remember { SlideStore.flow(ctx) }
        .collectAsState(initial = SlideAnchor(0, 0L))
    var bgIndex by remember(anchor) { mutableStateOf(anchor.index.coerceIn(0, slides.lastIndex)) }

    // времена намаза
    val state by vm.timings.collectAsStateWithLifecycle()

    // модалка выбора города
    var showPicker by remember { mutableStateOf(false) }

    Box {
        // ФОН — бесшовное слайд-шоу 20с, продолжает идти между запусками
        SlideshowBackground(
            images = slides,
            anchorIndex = anchor.index,
            anchorEpoch = anchor.epoch,
            intervalMs = 20_000,
            fadeMs = 800
        ) { newIdx ->
            bgIndex = newIdx
            scope.launch { SlideStore.save(ctx, newIdx) }
        }

        // Контент
        Column(
            Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues())
                .padding(16.dp)
        ) {
            GlassTopBar(
                currentBgRes = slides[bgIndex],
                title = if (state.cityLabel == "—") "Выбрать город" else state.cityLabel,
                onClick = { showPicker = true },
                onGps = onRequestLocation
            )
            Spacer(Modifier.height(12.dp))

            NextPrayerChip(state)

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(600)) +
                        slideInVertically(
                            initialOffsetY = { fullHeight: Int -> fullHeight / 6 },
                            animationSpec = tween(700)
                        )
            ) {
                GlassCard(currentBgRes = slides[bgIndex]) {
                    Column {
                        Row(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                            androidx.compose.material3.Text(
                                "Город: ${state.cityLabel}",
                                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                            )
                        }
                        PrayerRow("Фаджр", state.fajr)
                        PrayerRow("Восход", state.sunrise)
                        PrayerRow("Зухр", state.dhuhr)
                        PrayerRow("Аср (Стандарт)", state.asrStandard)
                        PrayerRow("Аср (Ханафитский)", state.asrHanafi)
                        PrayerRow("Магриб", state.maghrib)
                        PrayerRow("Иша", state.isha)
                        Spacer(Modifier.height(10.dp))
                        NightTimeline(
                            first = state.nightParts.first,
                            second = state.nightParts.second,
                            third = state.nightParts.third
                        )
                    }
                }
            }
        }

        CityPickerModal(
            vm = vm,
            bgRes = slides[bgIndex],
            visible = showPicker,
            onDismiss = { showPicker = false },
            onGps = onRequestLocation,
            onChosen = { /* выбрано — закрываем */ }
        )
    }
}

@Composable
private fun PrayerRow(label: String, time: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        androidx.compose.material3.Text(label, modifier = Modifier.weight(1f))
        androidx.compose.material3.Text(
            time,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
        )
    }
}
