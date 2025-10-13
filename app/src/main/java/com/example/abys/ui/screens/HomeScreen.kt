package com.example.abys.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.hapticfeedback.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.abys.R
import com.example.abys.logic.UiTimings
import com.example.abys.logic.SettingsStore
import com.example.abys.ui.PrayerViewModel
import com.example.abys.ui.components.EffectCarousel
import com.example.abys.ui.components.PrayerTable
import com.example.abys.ui.effects.EffectLayer
import com.example.abys.ui.effects.EffectKind
import com.example.abys.ui.effects.ThemeSpec
import com.example.abys.ui.effects.THEMES
import com.example.abys.ui.effects.themeById
import com.example.abys.ui.effects.StormParams
import com.example.abys.ui.effects.WindParams
import com.example.abys.ui.effects.windSway
import com.example.abys.ui.screens.background.SlideshowBackground
import com.example.abys.ui.screens.components.GlassCard
import com.example.abys.util.LocationHelper
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    val themes = remember { THEMES }
    var appliedTheme by remember { mutableStateOf(themeById("leaves")) }
    var focusedTheme by remember { mutableStateOf<ThemeSpec>(appliedTheme) }
    var carouselCollapsed by remember { mutableStateOf(false) }
    var windPhase by remember { mutableStateOf(0f) }
    var applyFlash by remember { mutableStateOf(false) }
    val flashAlpha by animateFloatAsState(if (applyFlash) 0.3f else 0f, label = "applyFlash")
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val ctxState = LocalContext.current
    LaunchedEffect(Unit) {
        val saved = SettingsStore.getThemeId(ctxState)
        if (saved != null) {
            val restored = themeById(saved)
            appliedTheme = restored
            focusedTheme = restored
        }
    }

    LaunchedEffect(appliedTheme.id) {
        windPhase = 0f
        val windParams = appliedTheme.windParams()
        if (windParams != null) {
            while (true) {
                windPhase += windParams.speed * 16f
                delay(16)
            }
        }
    }

    LaunchedEffect(applyFlash) {
        if (applyFlash) {
            delay(240)
            applyFlash = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        /* ---------- фон-слайдшоу ---------- */
        val backgrounds = appliedTheme.backgrounds.ifEmpty {
            listOf(
                R.drawable.slide_01, R.drawable.slide_02, R.drawable.slide_03, R.drawable.slide_04,
                R.drawable.slide_05, R.drawable.slide_06, R.drawable.slide_07, R.drawable.slide_08
            )
        }
        SlideshowBackground(
            modifier = Modifier.fillMaxSize(),
            images = backgrounds
        )

        /* ---------- слой эффектов ---------- */
        EffectLayer(Modifier.fillMaxSize(), theme = appliedTheme)

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
                        enabled = appliedTheme.supportsWindSway,
                        t = windPhase,
                        params = appliedTheme.windParams(),
                        intensity = appliedTheme.defaultIntensity / 100f
                    )
            ) {
                Text(
                    text = stringResource(id = R.string.today_prayers),
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
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.theme_active_label, stringResource(id = appliedTheme.titleRes)),
                    style = MaterialTheme.typography.labelLarge
                )
                if (!carouselCollapsed && focusedTheme.id != appliedTheme.id) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(id = R.string.theme_preview_label, stringResource(id = focusedTheme.titleRes)),
                        style = MaterialTheme.typography.bodySmall
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
                themes = themes,
                selectedThemeId = appliedTheme.id,
                collapsed = carouselCollapsed,
                onCollapsedChange = { carouselCollapsed = it },
                onThemeSnapped = { focusedTheme = it },
                onDoubleTapApply = { spec ->
                    appliedTheme = spec
                    carouselCollapsed = true
                    focusedTheme = spec
                    applyFlash = true
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch { SettingsStore.setThemeId(ctxState, spec.id) }
                }
            )
        }

        if (flashAlpha > 0f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = flashAlpha))
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

private fun ThemeSpec.windParams(): WindParams? = when (effect) {
    EffectKind.WIND -> params as? WindParams
    EffectKind.STORM -> (params as? StormParams)?.wind
    else -> null
}
