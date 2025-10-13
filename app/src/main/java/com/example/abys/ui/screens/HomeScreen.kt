package com.example.abys.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.abys.R
import com.example.abys.ui.PrayerViewModel
import com.example.abys.ui.screens.background.SlideshowBackground
import com.example.abys.ui.screens.components.GlassCard
import com.example.abys.util.LocationHelper
import com.example.abys.util.TimeUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import java.time.LocalTime

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(viewModel: PrayerViewModel) {
    /* ---------- разрешение на гео ---------- */
    val locPerm = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    LaunchedEffect(Unit) {
        if (!locPerm.status.isGranted) locPerm.launchPermissionRequest()
    }

    /* ---------- геолокация или CityPicker ---------- */
    val ctx = LocalContext.current
    var needCityPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val loc = LocationHelper.getLastBestLocation(ctx)
        if (loc != null) {
            viewModel.load(loc.first, loc.second)
        } else {
            needCityPicker = true
        }
    }

    val prayer = viewModel.state.collectAsState().value
    val now = LocalTime.now().toSecondOfDay()

    Box(modifier = Modifier.fillMaxSize()) {
        /* ---------- фон-слайдшоу ---------- */
        SlideshowBackground(
            modifier = Modifier.fillMaxSize(),
            images = listOf(
                R.drawable.slide_01, R.drawable.slide_02, R.drawable.slide_03, R.drawable.slide_04,
                R.drawable.slide_05, R.drawable.slide_06, R.drawable.slide_07, R.drawable.slide_08
            )
        )

        /* ---------- карточка с намазами ---------- */
        Column(
            Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlassCard(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = LocalContext.current.getString(R.string.today_prayers),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(12.dp))
                prayer?.let {
                    listOf(
                        "Fajr"    to it.fajr,
                        "Dhuhr"   to it.dhuhr,
                        "Asr"     to it.asr,
                        "Maghrib" to it.maghrib,
                        "Isha"    to it.isha
                    ).forEach { (name, time) -> TimeRow(name, time) }

                    /* следующий намаз */
                    val (nName, nTime) = it.next(now)
                    Spacer(Modifier.height(8.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text("Next: $nName at $nTime") }
                    )
                } ?: CircularProgressIndicator()
            }
        }

        /* ---------- выбор города при отсутствии GPS ---------- */
        CityPicker(
            show = needCityPicker,
            onDismiss = { /* stay */ },
            onCityChosen = { _, lat, lon -> viewModel.load(lat, lon) }
        )
    }
}

@Composable
private fun TimeRow(label: String, time: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(time)
    }
}
