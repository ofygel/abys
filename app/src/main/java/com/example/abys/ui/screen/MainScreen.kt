@file:Suppress("MagicNumber")

package com.example.abys.ui.screen

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.abys.R
import com.example.abys.data.FallbackContent
import com.example.abys.data.CityEntry
import com.example.abys.data.EffectId
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private enum class SurfaceStage { Dashboard, CitySheet, CityPicker }

// Тоны серого стекла и параметры блюра — под эталонный макет
private object GlassDefaults {
    val top = Color.White.copy(alpha = 0.26f)
    val bottom = Color.White.copy(alpha = 0.22f)
    val stroke = Color.White.copy(alpha = 0.18f)
    val blur = 8.dp
    val bgScrim = Color.Black.copy(alpha = 0.25f)
}

// Единая шкала таймингов — чтобы анимации были согласованы
private object Dur {
    const val X_SHORT = 180
    const val SHORT = 200
    const val BASE = 220
    const val MED = 240
    const val LONG = 260
}

// Палитра и типографика под «серый» макет
private object TypeTone {
    val primary: Color
        @Composable get() = Tokens.Colors.text.copy(alpha = 0.92f)
    val secondary: Color
        @Composable get() = Tokens.Colors.text.copy(alpha = 0.78f)
    val dim: Color
        @Composable get() = Tokens.Colors.text.copy(alpha = 0.62f)
    val divider: Color
        @Composable get() = Color.White.copy(alpha = 0.06f)
}

private const val TABULAR_FEATURE = "tnum"

private fun tabularFigures(value: String) = buildAnnotatedString {
    withStyle(SpanStyle(fontFeatureSettings = TABULAR_FEATURE)) {
        append(value)
    }
}

private const val TABULAR_FEATURE = "tnum"

@Composable
private fun ThinDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, color = TypeTone.divider, thickness = 0.75.dp)
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

private fun scaledSp(basePx: Int, scale: Float) = (basePx * scale).roundToInt().sp

private object TypeScale {
    val eyebrow = scaledSp(Tokens.TypographyPx.timeline, 0.6f)
    val city = scaledSp(Tokens.TypographyPx.city, 0.76f)
    val timeNow = scaledSp(Tokens.TypographyPx.timeNow, 0.76f)
    val label = scaledSp(Tokens.TypographyPx.label, 0.7f)
    val subLabel = scaledSp(Tokens.TypographyPx.subLabel, 0.68f)
    val timeline = scaledSp(Tokens.TypographyPx.timeline, 0.66f)
}

@Composable
fun MainApp(
    vm: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    effectViewModel: EffectViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
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

    CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = AbysFonts.inter)) {
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
                onCityPillClick = vm::toggleSheet,
                onShowWheel = { vm.setSheetTab(CitySheetTab.Wheel) },
                onTabSelected = vm::setSheetTab,
                onSheetDismiss = vm::toggleSheet,
                onCityChosen = { vm.setCity(it, context.applicationContext) },
                onEffectSelected = effectViewModel::onEffectSelected
            )
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
    val density = LocalDensity.current
    val navPadding = WindowInsets.navigationBars.asPaddingValues()

    val stage = when {
        !showSheet -> SurfaceStage.Dashboard
        sheetTab == CitySheetTab.Wheel -> SurfaceStage.CityPicker
        else -> SurfaceStage.CitySheet
    }

    val transition = updateTransition(stage, label = "surface")
    // Предвычисляем px-значения, чтобы не трогать layout на каждую рекомпозицию
    val sheetHiddenOffset = remember(density, sx) { with(density) { (236f * sx).dp.toPx() } }
    val sheetLift = remember(density, sy) { with(density) { (18f * sy).dp.toPx() } }
    val cardLift = remember(density, sy) { with(density) { (42f * sy).dp.toPx() } }
    val carouselDrop = remember(density, sy) { with(density) { (36f * sy).dp.toPx() } }
    val headerLift = remember(density, sy) { with(density) { (14f * sy).dp.toPx() } }

    val prayerAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = if (targetState == SurfaceStage.Dashboard) Dur.SHORT else Dur.MED) },
        label = "prayerAlpha"
    ) { st -> if (st == SurfaceStage.Dashboard) 1f else 0f }
    val prayerScale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.LONG) },
        label = "prayerScale"
    ) { st -> if (st == SurfaceStage.Dashboard) 1f else 0.94f }
    val prayerTranslation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.MED) },
        label = "prayerTranslation"
    ) { st -> if (st == SurfaceStage.Dashboard) 0f else -cardLift }

    val headerAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.BASE) },
        label = "headerAlpha"
    ) { st -> if (st == SurfaceStage.Dashboard) 1f else 0.82f }
    val headerTranslation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.BASE) },
        label = "headerTranslation"
    ) { st -> if (st == SurfaceStage.Dashboard) 0f else -headerLift }

    val carouselAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.BASE) },
        label = "carouselAlpha"
    ) { st -> if (st == SurfaceStage.Dashboard) 1f else 0.45f }
    val carouselScale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.MED) },
        label = "carouselScale"
    ) { st -> if (st == SurfaceStage.Dashboard) 1f else 0.92f }
    val carouselTranslation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.MED) },
        label = "carouselTranslation"
    ) { st -> if (st == SurfaceStage.Dashboard) 0f else carouselDrop }

    val scrimAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.BASE) },
        label = "scrimAlpha"
    ) { st -> if (st == SurfaceStage.Dashboard) 0f else 1f }

    val sheetAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.MED) },
        label = "sheetAlpha"
    ) { st -> if (st == SurfaceStage.Dashboard) 0f else 1f }
    val sheetTranslationX by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.LONG) },
        label = "sheetTranslationX"
    ) { st -> if (st == SurfaceStage.Dashboard) sheetHiddenOffset else 0f }
    val sheetTranslationY by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.LONG) },
        label = "sheetTranslationY"
    ) { st ->
        when (st) {
            SurfaceStage.Dashboard -> sheetLift
            SurfaceStage.CitySheet -> 0f
            SurfaceStage.CityPicker -> -sheetLift / 2f
        }
    }
    val sheetScale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.LONG) },
        label = "sheetScale"
    ) { st ->
        when (st) {
            SurfaceStage.Dashboard -> 0.9f
            SurfaceStage.CitySheet -> 1f
            SurfaceStage.CityPicker -> 1.04f
        }
    }

    // Блокируем взаимодействия на время перехода
    val isTransitioning = transition.isRunning

    BackHandler(enabled = showSheet) {
        if (sheetTab != CitySheetTab.Wheel) {
            onShowWheel()
        } else {
            onSheetDismiss()
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val maxH = maxHeight
        val bottomSafe = navPadding.calculateBottomPadding()
        val topPad = maxOf(16.dp, maxH * 0.18f)
        val availableCardHeight = (maxH - topPad - bottomSafe - 96.dp).coerceAtLeast(0.dp)
        val minCardHeight = minOf(maxH * 0.62f, availableCardHeight)

        HeaderPill(
            city = city,
            now = now,
            modifier = Modifier
                .padding(
                    start = (51f * sx).dp,
                    top = (79f * sy).dp,
                    end = (51f * sx).dp
                )
                .height((102f * sy).dp)
                .graphicsLayer {
                    alpha = headerAlpha
                    translationY = headerTranslation
                },
            onTap = {
                if (!isTransitioning) onCityPillClick()
            }
        )

        var exploded by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(stage) {
            if (stage != SurfaceStage.Dashboard) exploded = false
        }

        val prayerModifier = Modifier
            .padding(
                start = (64f * sx).dp,
                end = (64f * sx).dp,
                top = topPad
            )
            .heightIn(min = minCardHeight)
            .graphicsLayer {
                val explodedAlpha = if (exploded) 0f else 1f
                val explodedScale = if (exploded) 1.08f else 1f
                alpha = prayerAlpha * explodedAlpha
                scaleX = prayerScale * explodedScale
                scaleY = prayerScale * explodedScale
                translationY = prayerTranslation
            }

        if (prayerAlpha > 0.01f) {
            AnimatedVisibility(
                visible = !showSheet,
                enter = fadeIn(tween(Dur.BASE)) + scaleIn(initialScale = 0.96f, animationSpec = tween(Dur.BASE)),
                exit = fadeOut(tween(Dur.X_SHORT)) + scaleOut(targetScale = 0.96f, animationSpec = tween(Dur.X_SHORT))
            ) {
                PrayerCard(
                    times = prayerTimes,
                    thirds = thirds,
                    modifier = if (stage == SurfaceStage.Dashboard) {
                        prayerModifier.pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (!isTransitioning) exploded = !exploded
                                }
                            )
                        }
                    } else {
                        prayerModifier
                    }
                )
            }
        }

        EffectCarousel(
            items = effectThumbs,
            selected = selectedEffect,
            onSelected = onEffectSelected,
            enabled = stage == SurfaceStage.Dashboard && !isTransitioning,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = navPadding.calculateBottomPadding() + (56f * sy).dp)
                .graphicsLayer {
                    alpha = carouselAlpha
                    if (stage == SurfaceStage.Dashboard) {
                        scaleX = carouselScale
                        scaleY = carouselScale
                        translationY = carouselTranslation
                    } else {
                        scaleX = 1f
                        scaleY = 1f
                        translationY = 0f
                    }
                }
        )

        if (scrimAlpha > 0.01f || sheetAlpha > 0.01f) {
            Box(Modifier.fillMaxSize()) {
                if (scrimAlpha > 0.01f) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .graphicsLayer { alpha = scrimAlpha }
                            .background(GlassDefaults.bgScrim)
                            .clickable(
                                enabled = !isTransitioning,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onSheetDismiss
                            )
                    )
                }

                if (sheetAlpha > 0.01f) {
                    AnimatedVisibility(
                        visible = showSheet,
                        enter = fadeIn(tween(Dur.BASE)) +
                            slideInHorizontally(initialOffsetX = { it / 6 }, animationSpec = tween(Dur.BASE)),
                        exit = fadeOut(tween(Dur.X_SHORT)) +
                            slideOutHorizontally(targetOffsetX = { it / 6 }, animationSpec = tween(Dur.X_SHORT))
                    ) {
                        GlassSheetContainer(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = sheetAlpha
                                    translationX = sheetTranslationX
                                    translationY = sheetTranslationY
                                    scaleX = sheetScale
                                    scaleY = sheetScale
                                }
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
                                    .padding(horizontal = (18f * sy).dp, vertical = (16f * sy).dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderPill(
    city: String,
    now: String,
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    val sy = Dimens.sy()
    val horizontalPadding = Dimens.scaledX(R.dimen.abys_pill_pad_h)
    val verticalPadding = Dimens.scaledY(R.dimen.abys_pill_pad_v)
    val eyebrowSpacing = (6f * sy).dp
    val shape = RoundedCornerShape(Tokens.Radii.pill())

    Box(
        modifier
            .fillMaxWidth()
            .shadow(elevation = (36f * sy).dp, shape = shape, clip = false)
            .clip(shape)
            .graphicsLayer { compositingStrategy = CompositingStrategy.ModulateAlpha }
    ) {
        Box(
            Modifier
                .matchParentSize()
                .clip(shape)
                .backdropBlur(GlassDefaults.blur)
                .background(
                    Brush.verticalGradient(listOf(GlassDefaults.top, GlassDefaults.bottom))
                )
                .border(width = 1.dp, color = GlassDefaults.stroke, shape = shape)
        )
        Box(
            Modifier
                .matchParentSize()
                .semantics(mergeDescendants = true) {
                    contentDescription = "Открыть выбор города"
                    role = Role.Button
                }
                .clickable(onClick = onTap)
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(eyebrowSpacing)
            ) {
                Text(
                    text = "Город",
                    fontSize = TypeScale.eyebrow,
                    fontWeight = FontWeight.Medium,
                    color = TypeTone.dim
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = city,
                        fontSize = TypeScale.city,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic,
                        textDecoration = TextDecoration.Underline,
                        color = TypeTone.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width((12f * Dimens.sx()).dp))
                    Text(
                        text = tabularFigures(now),
                        fontSize = TypeScale.timeNow,
                        fontWeight = FontWeight.SemiBold,
                        color = TypeTone.secondary,
                        textAlign = TextAlign.Right,
                        fontFeatureSettings = TABULAR_FEATURE,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.wrapContentWidth(Alignment.End),
                        style = LocalTextStyle.current.merge(TextStyle(fontFeatureSettings = TABULAR_FEATURE))
                    )
                }
            }
        }
    }
}

@Composable
private fun PrayerCard(
    times: Map<String, String>,
    thirds: NightIntervals,
    modifier: Modifier = Modifier
) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val shape = RoundedCornerShape(Tokens.Radii.card())
    val rowSpacing = (12f * sy).dp
    val sectionSpacing = (22f * sy).dp
    val asrSpacing = (8f * sy).dp
    val asrLineHeight = (1.2f * sy).dp
    val asrGap = (10f * sx).dp
    Box(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .graphicsLayer { compositingStrategy = CompositingStrategy.ModulateAlpha }
    ) {
        Box(
            Modifier
                .matchParentSize()
                .clip(shape)
                .backdropBlur(GlassDefaults.blur)
                .background(
                    Brush.verticalGradient(listOf(GlassDefaults.top, GlassDefaults.bottom))
                )
                .border(width = 1.dp, color = GlassDefaults.stroke, shape = shape)
        )
        Column(
            Modifier
                .matchParentSize()
                .padding(
                    start = Dimens.scaledX(R.dimen.abys_card_pad_h),
                    end = Dimens.scaledX(R.dimen.abys_card_pad_h),
                    top = Dimens.scaledY(R.dimen.abys_card_pad_top),
                    bottom = Dimens.scaledY(R.dimen.abys_card_pad_bottom)
                )
                .animateContentSize(animationSpec = tween(Dur.BASE))
        ) {
            val ordered = listOf(
                "Фаджр" to (times["Fajr"] ?: "--:--"),
                "Восход" to (times["Sunrise"] ?: "--:--"),
                "Зухр" to (times["Dhuhr"] ?: "--:--")
            )
            ordered.forEachIndexed { index, (label, value) ->
                PrayerRow(label, value)
                if (index != ordered.lastIndex) {
                    Spacer(Modifier.height(rowSpacing))
                    ThinDivider()
                    Spacer(Modifier.height(rowSpacing))
                }
            }

            Spacer(Modifier.height(sectionSpacing))
            SectionHeading("Аср")
            Spacer(Modifier.height(asrSpacing))
            AsrVariantRow(
                label = "стандарт",
                value = times["AsrStd"] ?: "--:--",
                gap = asrGap,
                lineHeight = asrLineHeight
            )
            Spacer(Modifier.height(asrSpacing))
            AsrVariantRow(
                label = "ханафи",
                value = times["AsrHana"] ?: "--:--",
                gap = asrGap,
                lineHeight = asrLineHeight
            )

            Spacer(Modifier.height(sectionSpacing))
            ThinDivider()
            Spacer(Modifier.height(sectionSpacing))

            val evening = listOf(
                "Магриб" to (times["Maghrib"] ?: "--:--"),
                "Иша" to (times["Isha"] ?: "--:--")
            )
            evening.forEachIndexed { index, (label, value) ->
                PrayerRow(label, value)
                if (index != evening.lastIndex) {
                    Spacer(Modifier.height(rowSpacing))
                    ThinDivider()
                    Spacer(Modifier.height(rowSpacing))
                }
            }

            Spacer(Modifier.height(sectionSpacing))
            PrayerTimeline(times, thirds)
        }
    }
}

@Composable
private fun PrayerRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = TypeScale.label,
            fontWeight = FontWeight.Medium,
            color = TypeTone.secondary,
            lineHeight = TypeScale.label,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        Text(
            text = tabularFigures(value),
            fontSize = TypeScale.label,
            fontWeight = FontWeight.SemiBold,
            color = TypeTone.primary,
            textAlign = TextAlign.Right,
            lineHeight = TypeScale.label,
            fontFeatureSettings = TABULAR_FEATURE,
            modifier = Modifier.wrapContentWidth(Alignment.End),
            maxLines = 1,
            style = LocalTextStyle.current.merge(TextStyle(fontFeatureSettings = TABULAR_FEATURE))
        )
    }
}

@Composable
private fun AsrVariantRow(
    label: String,
    value: String,
    gap: Dp,
    lineHeight: Dp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = TypeScale.subLabel,
            fontWeight = FontWeight.Medium,
            color = TypeTone.dim,
            maxLines = 1,
            modifier = Modifier.wrapContentWidth(Alignment.Start)
        )
        Spacer(Modifier.width(gap))
        Box(
            Modifier
                .weight(1f)
                .height(lineHeight)
                .clip(RoundedCornerShape(lineHeight / 2))
                .background(TypeTone.divider)
        )
        Spacer(Modifier.width(gap))
        val indicatorSize = (8f * Dimens.sx()).dp
        Box(
            Modifier
                .size(indicatorSize)
                .clip(CircleShape)
                .background(Tokens.Colors.tickFull.copy(alpha = 0.75f))
        )
        Spacer(Modifier.width(gap))
        Text(
            text = tabularFigures(value),
            fontSize = TypeScale.subLabel,
            fontWeight = FontWeight.SemiBold,
            color = TypeTone.secondary,
            textAlign = TextAlign.Right,
            maxLines = 1,
            modifier = Modifier.wrapContentWidth(Alignment.End),
            style = LocalTextStyle.current.merge(TextStyle(fontFeatureSettings = TABULAR_FEATURE))
            fontFeatureSettings = TABULAR_FEATURE,
            modifier = Modifier.wrapContentWidth(Alignment.End)
        )
    }
}

@Composable
private fun PrayerTimeline(times: Map<String, String>, thirds: NightIntervals) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val rowSpacing = (10f * sy).dp
    val dividerPadding = (5f * sy).dp
    val indicatorWidth = (60f * sx).dp
    val indicatorHeight = (4f * sy).dp
    val indicatorRadius = indicatorHeight / 2
    val indicatorSpacing = (10f * sx).dp

    Column(modifier = Modifier.fillMaxWidth()) {
        RowItem("Фаджр", times["Fajr"] ?: "--:--")
        Spacer(Modifier.height(rowSpacing))
        ThinDivider(Modifier.padding(vertical = dividerPadding))
        Spacer(Modifier.height(rowSpacing))

        RowItem("Восход", times["Sunrise"] ?: "--:--")
        Spacer(Modifier.height(rowSpacing))
        ThinDivider(Modifier.padding(vertical = dividerPadding))
        Spacer(Modifier.height(rowSpacing))

        RowItem("Зухр", times["Dhuhr"] ?: "--:--")
        Spacer(Modifier.height(rowSpacing))
        ThinDivider(Modifier.padding(vertical = dividerPadding))
        Spacer(Modifier.height(rowSpacing))

        SectionHeading("Аср")
        Spacer(Modifier.height((2f * sy).dp))
        AsrSub(
            label = "стандарт",
            value = times["AsrStd"] ?: "--:--",
            indicatorWidth = indicatorWidth,
            indicatorHeight = indicatorHeight,
            indicatorRadius = indicatorRadius,
            spacing = indicatorSpacing
        )
        Spacer(Modifier.height((6f * sy).dp))
        AsrSub(
            label = "ханафи",
            value = times["AsrHana"] ?: "--:--",
            indicatorWidth = indicatorWidth,
            indicatorHeight = indicatorHeight,
            indicatorRadius = indicatorRadius,
            spacing = indicatorSpacing
        )
        Spacer(Modifier.height(rowSpacing))
        ThinDivider(Modifier.padding(vertical = dividerPadding))
        Spacer(Modifier.height(rowSpacing))

        RowItem("Магриб", times["Maghrib"] ?: "--:--")
        Spacer(Modifier.height(rowSpacing))
        ThinDivider(Modifier.padding(vertical = dividerPadding))
        Spacer(Modifier.height(rowSpacing))

        RowItem("Иша", times["Isha"] ?: "--:--")
        Spacer(Modifier.height(rowSpacing))
        NightThirdsTimeline(thirds)
    }
}

@Composable
private fun RowItem(label: String, value: String) {
    val sx = Dimens.sx()
    val indicatorSize = (6f * sx).dp
    val spacing = (12f * sx).dp

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(indicatorSize)
                .clip(CircleShape)
                .background(TypeTone.divider)
        )
        Spacer(Modifier.width(spacing))
        Text(
            text = label,
            fontSize = TypeScale.label,
            fontWeight = FontWeight.Medium,
            color = TypeTone.secondary,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        Spacer(Modifier.width(spacing))
        Text(
            text = tabularFigures(value),
            fontSize = TypeScale.label,
            fontWeight = FontWeight.SemiBold,
            color = TypeTone.primary,
            textAlign = TextAlign.Right,
            fontFeatureSettings = TABULAR_FEATURE,
            modifier = Modifier.wrapContentWidth(Alignment.End),
            maxLines = 1,
            style = LocalTextStyle.current.merge(TextStyle(fontFeatureSettings = TABULAR_FEATURE))
        )
    }
}

@Composable
private fun AsrSub(
    label: String,
    value: String,
    indicatorWidth: Dp,
    indicatorHeight: Dp,
    indicatorRadius: Dp,
    spacing: Dp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = TypeScale.subLabel,
            fontWeight = FontWeight.Medium,
            color = TypeTone.dim,
            maxLines = 1,
            modifier = Modifier.wrapContentWidth(Alignment.Start)
        )
        Spacer(Modifier.width(spacing))
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(2) { index ->
                val alpha = if (index == 0) 0.9f else 0.4f
                Box(
                    Modifier
                        .width(indicatorWidth)
                        .height(indicatorHeight)
                        .clip(RoundedCornerShape(indicatorRadius))
                        .background(Tokens.Colors.tickFull.copy(alpha = alpha))
                )
            }
        }
        Spacer(Modifier.width(spacing))
        Text(
            text = tabularFigures(value),
            fontSize = TypeScale.subLabel,
            fontWeight = FontWeight.SemiBold,
            color = TypeTone.secondary,
            textAlign = TextAlign.Right,
            maxLines = 1,
            modifier = Modifier.wrapContentWidth(Alignment.End),
            style = LocalTextStyle.current.merge(TextStyle(fontFeatureSettings = TABULAR_FEATURE))
            fontFeatureSettings = TABULAR_FEATURE,
            modifier = Modifier.wrapContentWidth(Alignment.End)
        )
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text,
        fontSize = TypeScale.label,
        fontWeight = FontWeight.SemiBold,
        color = TypeTone.primary
    )
}

@Composable
private fun NightThirdsTimeline(thirds: NightIntervals) {
    val sy = Dimens.sy()
    val density = LocalDensity.current
    val lineHeight = (3f * sy).dp
    val tickHeight = (18f * sy).dp
    val spacing = (12f * sy).dp
    val timelineColor = TypeTone.divider.copy(alpha = 0.9f)
    val tickColor = Tokens.Colors.tickFull.copy(alpha = 0.75f)
    val thicknessPx = with(density) { lineHeight.toPx() }
    val tickStrokePx = with(density) { 1.5.dp.toPx() }
    val fractions = listOf(0f, 1f / 3f, 2f / 3f, 1f)
    val labels = listOf(
        thirds.first.first.ifBlank { "--:--" },
        thirds.first.second.ifBlank { "--:--" },
        thirds.second.second.ifBlank { "--:--" },
        thirds.third.second.ifBlank { "--:--" }
    )

    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { value ->
                Text(
                    text = tabularFigures(value),
                    fontSize = TypeScale.timeline,
                    fontWeight = FontWeight.SemiBold,
                    color = TypeTone.primary,
                    text = value,
                    fontSize = TypeScale.timeline,
                    fontWeight = FontWeight.SemiBold,
                    color = TypeTone.primary,
                    maxLines = 1,
                    style = LocalTextStyle.current.merge(TextStyle(fontFeatureSettings = TABULAR_FEATURE))
                    fontFeatureSettings = TABULAR_FEATURE,
                    maxLines = 1
                )
            }
        }

        Spacer(Modifier.height(spacing / 2))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(tickHeight)
        ) {
            val centerY = size.height / 2f
            drawLine(
                color = timelineColor,
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = thicknessPx
            )
            fractions.forEach { fraction ->
                val x = size.width * fraction
                drawLine(
                    color = tickColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = tickStrokePx
                )
            }
        }

        Spacer(Modifier.height(spacing / 2))

        Row(Modifier.fillMaxWidth()) {
            Text(
                text = "начало ночи",
                fontSize = TypeScale.subLabel,
                fontWeight = FontWeight.Medium,
                color = TypeTone.dim,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Text(
                text = "середина ночи",
                fontSize = TypeScale.subLabel,
                fontWeight = FontWeight.Medium,
                color = TypeTone.dim,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Text(
                text = "конец ночи",
                fontSize = TypeScale.subLabel,
                fontWeight = FontWeight.Medium,
                color = TypeTone.dim,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun GlassSheetContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(Tokens.Radii.card())
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

