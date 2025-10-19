@file:Suppress("MagicNumber")

package com.example.abys.ui.screen

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.abys.data.CityEntry
import com.example.abys.data.EffectId
import com.example.abys.data.FallbackContent
import com.example.abys.logic.CitySheetTab
import com.example.abys.logic.MainViewModel
import com.example.abys.logic.NightIntervals
import com.example.abys.ui.EffectCarousel
import com.example.abys.ui.EffectThumb
import com.example.abys.ui.EffectViewModel
import com.example.abys.ui.background.BackgroundHost
import com.example.abys.ui.rememberCityDirectory
import com.example.abys.ui.rememberEffectCatalogFromRes
import com.example.abys.ui.theme.AbysFonts
import com.example.abys.ui.theme.Dimens
import com.example.abys.ui.theme.Tokens
import com.example.abys.ui.util.backdropBlur
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private const val MAIN_OVERLAY_DELAY_MS = 180L
private const val MAIN_OVERLAY_FADE_DURATION_MS = 520

private object Dur {
    const val X_SHORT = 180
}

@Composable
private fun MutedBackgroundCrossfade(effect: EffectId) {
    Crossfade(
        modifier = Modifier.fillMaxSize(),
        targetState = effect,
        animationSpec = tween(durationMillis = Dur.X_SHORT),
        label = "muted-effect-background"
    ) { target ->
        Box(Modifier.fillMaxSize()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val matrix = remember {
                    ColorMatrix().apply { setSaturation(0.35f) }
                }
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            renderEffect = RenderEffect.createColorFilterEffect(
                                ColorMatrixColorFilter(matrix)
                            ).asComposeRenderEffect()
                        }
                ) {
                    BackgroundHost(effect = target)
                }
            } else {
                BackgroundHost(effect = target)
                Box(
                    Modifier
                        .matchParentSize()
                        .background(GlassDefaults.bgScrim)
                )
            }
        }
    }
}

@Composable
fun MainApp(
    vm: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    effectViewModel: EffectViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    startFaded: Boolean = false
) {
    val city by vm.city.observeAsState("Almaty")
    val times by vm.prayerTimes.observeAsState(emptyMap())
    val thirds by vm.thirds.observeAsState(FallbackContent.nightIntervals)
    val now by vm.clock.observeAsState(
        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
    )
    val showSheet by vm.sheetVisible.observeAsState(false)
    val sheetTab by vm.sheetTab.observeAsState(CitySheetTab.Wheel)
    val hadith by vm.hadithToday.observeAsState("")
    val selectedEffect by effectViewModel.effect.collectAsState()
    val effectThumbs = rememberEffectCatalogFromRes()
    val cityOptions = rememberCityDirectory()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.restorePersisted(context.applicationContext)
    }

    val overlayAlpha = remember { Animatable(if (startFaded) 1f else 0f) }

    LaunchedEffect(startFaded) {
        if (startFaded && overlayAlpha.value > 0f) {
            delay(MAIN_OVERLAY_DELAY_MS)
            overlayAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = MAIN_OVERLAY_FADE_DURATION_MS,
                    easing = FastOutSlowInEasing
                )
            )
        } else if (!startFaded && overlayAlpha.value != 0f) {
            overlayAlpha.snapTo(0f)
        }
    }

    val baseTextStyle = LocalTextStyle.current
    CompositionLocalProvider(LocalTextStyle provides baseTextStyle.copy(fontFamily = AbysFonts.inter)) {
        Box(Modifier.fillMaxSize()) {
            MutedBackgroundCrossfade(selectedEffect)

            MainScreen(
                city = city,
                now = now,
                prayerTimes = times,
                thirds = thirds,
                selectedEffect = selectedEffect,
                effectThumbs = effectThumbs,
                showSheet = showSheet,
                sheetTab = sheetTab,
                hadith = hadith,
                cities = cityOptions,
                onCityPillClick = vm::showSheet,
                onShowWheel = { vm.setSheetTab(CitySheetTab.Wheel) },
                onTabSelected = vm::setSheetTab,
                onSheetDismiss = vm::hideSheet,
                onCityChosen = { vm.setCity(it, context.applicationContext) },
                onEffectSelected = effectViewModel::onEffectSelected
            )

            if (overlayAlpha.value > 0.01f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = overlayAlpha.value))
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    city: String,
    now: String,
    prayerTimes: Map<String, String>,
    thirds: NightIntervals,
    selectedEffect: EffectId,
    effectThumbs: List<EffectThumb>,
    showSheet: Boolean,
    sheetTab: CitySheetTab,
    hadith: String,
    cities: List<CityEntry>,
    onCityPillClick: () -> Unit,
    onShowWheel: () -> Unit,
    onTabSelected: (CitySheetTab) -> Unit,
    onSheetDismiss: () -> Unit,
    onCityChosen: (String) -> Unit,
    onEffectSelected: (EffectId) -> Unit
) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val navPadding = WindowInsets.navigationBars.asPaddingValues()

    BackHandler(enabled = showSheet) {
        if (sheetTab != CitySheetTab.Wheel) {
            onShowWheel()
        } else {
            onSheetDismiss()
        }
    }

    val normalizedCity = remember(city) { city.substringBefore(',').ifBlank { city }.trim() }

    Box(Modifier.fillMaxSize()) {
        PrayerDashboard(
            city = normalizedCity,
            now = now,
            prayerTimes = prayerTimes,
            thirds = thirds,
            onCityClick = onCityPillClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = (162f * sy).dp, start = (48f * sx).dp, end = (48f * sx).dp)
                .widthIn(max = (640f * sx).dp)
        )

        EffectCarousel(
            items = effectThumbs,
            selected = selectedEffect,
            onSelected = onEffectSelected,
            enabled = !showSheet,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = navPadding.calculateBottomPadding() + (48f * sy).dp)
        )

        if (showSheet) {
            Box(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(GlassDefaults.bgScrim)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onSheetDismiss
                        )
                )

                GlassSheetContainer(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = (18f * sx).dp, vertical = (16f * sy).dp)
                        .align(Alignment.Center)
                ) {
                    CitySheet(
                        city = city,
                        hadith = hadith,
                        cities = cities,
                        activeTab = sheetTab,
                        onCityChipTap = onShowWheel,
                        onTabSelected = onTabSelected,
                        onCityChosen = onCityChosen,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = (18f * sx).dp, vertical = (16f * sy).dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassSheetContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(Tokens.Radii.card())
    Box(
        modifier
            .clip(shape)
            .backdropBlur(GlassDefaults.blur)
            .background(Brush.verticalGradient(listOf(GlassDefaults.top, GlassDefaults.bottom)))
            .border(1.dp, GlassDefaults.stroke, shape)
    ) {
        Box(Modifier.matchParentSize(), content = content)
    }
}

