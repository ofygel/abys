package com.example.abys.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.abys.R
import com.example.abys.logic.UiTimings
import com.example.abys.ui.PrayerViewModel
import com.example.abys.ui.components.EffectCarousel
import com.example.abys.ui.components.PrayerTable
import com.example.abys.ui.effects.EffectKind
import com.example.abys.ui.effects.EffectLayer
import com.example.abys.ui.effects.windSway
import com.example.abys.ui.screens.background.SlideshowBackground
import com.example.abys.ui.screens.components.GlassCard
import com.example.abys.util.LocationHelper
import java.time.ZoneId
import kotlinx.coroutines.delay
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(viewModel: PrayerViewModel) {

    /* ---------- проверка / запрос разрешения ---------- */
    val ctx = LocalContext.current
    var hasGpsPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permLauncher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        hasGpsPerm = granted
    }

    LaunchedEffect(Unit) {
        if (!hasGpsPerm) {
            permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    /* ---------- геолокация или CityPicker ---------- */
    var needCityPicker by remember { mutableStateOf(false) }

    LaunchedEffect(hasGpsPerm) {
        if (hasGpsPerm) {
            val loc = LocationHelper.getLastBestLocation(ctx)
            if (loc != null) {
                viewModel.load(loc.first, loc.second)
            } else {
                needCityPicker = true
            }
        } else {
            needCityPicker = true
        }
    }

    val prayer = viewModel.state.collectAsState().value

    // ---- состояние темы и карусели
    var selected by remember { mutableStateOf(EffectKind.LEAVES) }
    var carouselCollapsed by remember { mutableStateOf(true) }
    var windT by remember { mutableStateOf(0f) }
    LaunchedEffect(selected) {
        if (selected == EffectKind.WIND || selected == EffectKind.STORM) {
            while (true) {
                windT += 0.05f
                delay(16)
            }
        } else {
            windT = 0f
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        /* ---------- фон-слайдшоу ---------- */
        SlideshowBackground(
            modifier = Modifier.fillMaxSize(),
            images = listOf(
                R.drawable.slide_01, R.drawable.slide_02, R.drawable.slide_03, R.drawable.slide_04,
                R.drawable.slide_05, R.drawable.slide_06, R.drawable.slide_07, R.drawable.slide_08
            )
        )

        /* ---------- слой эффектов ---------- */
        EffectLayer(Modifier.fillMaxSize(), kind = selected)

        /* ---------- карточка с намазами ---------- */
        Box(
            Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .fillMaxHeight(0.60f)
                    .windSway(
                        enabled = selected == EffectKind.WIND || selected == EffectKind.STORM,
                        t = windT,
                        strength = if (selected == EffectKind.STORM) 12f else 6f
                    )
            ) {
                Text(
                    text = LocalContext.current.getString(R.string.today_prayers),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(12.dp))

                if (prayer == null) {
                    CircularProgressIndicator()
                } else {
                    PrayerTable(
                        t = UiTimings(
                            fajr = prayer.fajr,
                            sunrise = "--",
                            dhuhr = prayer.dhuhr,
                            asrStd = prayer.asr,
                            asrHan = prayer.asr,
                            maghrib = prayer.maghrib,
                            isha = prayer.isha,
                            tz = ZoneId.systemDefault()
                        ),
                        selectedSchool = 0
                    )
                }
            }
        }

        /* ---------- нижняя карусель ---------- */
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        ) {
            EffectCarousel(
                collapsed = carouselCollapsed,
                onCollapsedChange = { carouselCollapsed = it },
                onDoubleTapApply = { spec -> selected = spec.kind }
            )
        }

        /* ---------- CityPicker, если координат нет ---------- */
        CityPicker(
            show = needCityPicker,
            onDismiss = { /* остаёмся */ },
            onCityChosen = { _, lat, lon ->
                needCityPicker = false
                viewModel.load(lat, lon)
            }
        )
    }
}
