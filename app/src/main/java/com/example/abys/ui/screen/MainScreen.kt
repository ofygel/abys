@file:Suppress("MagicNumber")

package com.example.abys.ui.screen

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
import kotlinx.coroutines.delay

private enum class SurfaceStage { Dashboard, CitySheet, CityPicker }

private const val MAIN_OVERLAY_DELAY_MS = 180L
private const val MAIN_OVERLAY_FADE_DURATION_MS = 520
private const val MAIN_OVERLAY_FADE_DURATION_MS = 520L

// Тоны серого стекла и параметры блюра — под эталонный макет
private object GlassDefaults {
    val top: Color
        @Composable get() = Tokens.Colors.overlayTop.copy(alpha = 0.9f)
    val bottom: Color
        @Composable get() = Tokens.Colors.overlayCard.copy(alpha = 0.86f)
    val stroke: Color
        @Composable get() = Color.White.copy(alpha = 0.32f)
    val glow: Color
        @Composable get() = Color.White.copy(alpha = 0.18f)
    val blur: Dp
        @Composable get() = (10f * Dimens.s()).dp
    val elevation: Dp
        @Composable get() = (24f * Dimens.s()).dp
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
        @Composable get() = Tokens.Colors.text.copy(alpha = 0.96f)
    val secondary: Color
        @Composable get() = Tokens.Colors.text.copy(alpha = 0.88f)
    val dim: Color
        @Composable get() = Tokens.Colors.text.copy(alpha = 0.7f)
    val divider: Color
        @Composable get() = Color.White.copy(alpha = 0.16f)
}

private const val TABULAR_FEATURE = "'tnum'"

private val TabularFeatureStyle = TextStyle(fontFeatureSettings = TABULAR_FEATURE)

private const val NIGHT_SECTION_TITLE = "Ночь (3 части)"

private fun Density.dpToPx(value: Float): Float = value * density

@Composable
private fun TabularText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        textAlign = textAlign,
        lineHeight = lineHeight,
        maxLines = maxLines,
        overflow = overflow,
        style = LocalTextStyle.current.merge(TabularFeatureStyle)
    )
}

@Composable
private fun ThinDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, color = TypeTone.divider, thickness = 1.dp)
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
                    BackgroundHost()
                }
            } else {
                BackgroundHost()
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
    val eyebrow = scaledSp(Tokens.TypographyPx.timeline, 0.52f)
    val city = scaledSp(Tokens.TypographyPx.city, 0.68f)
    val timeNow = scaledSp(Tokens.TypographyPx.timeNow, 0.68f)
    val label = scaledSp(Tokens.TypographyPx.label, 0.56f)
    val subLabel = scaledSp(Tokens.TypographyPx.subLabel, 0.54f)
    val timeline = scaledSp(Tokens.TypographyPx.timeline, 0.54f)
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
@OptIn(ExperimentalFoundationApi::class)
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

    val transition = updateTransition(targetState = stage, label = "surface")
    // Предвычисляем px-значения, чтобы не трогать layout на каждую рекомпозицию
    val sheetHiddenOffset: Float = remember(density, sx) { density.dpToPx(236f * sx) }
    val sheetLift: Float = remember(density, sy) { density.dpToPx(18f * sy) }
    val cardLift: Float = remember(density, sy) { density.dpToPx(42f * sy) }
    val carouselDrop: Float = remember(density, sy) { density.dpToPx(36f * sy) }
    val headerLift: Float = remember(density, sy) { density.dpToPx(14f * sy) }

    val prayerAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = if (targetState == SurfaceStage.Dashboard) Dur.SHORT else Dur.MED) },
        label = "prayerAlpha"
    ) { st: SurfaceStage -> if (st == SurfaceStage.Dashboard) 1f else 0f }
    val prayerScale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.LONG) },
        label = "prayerScale"
    ) { st: SurfaceStage -> if (st == SurfaceStage.Dashboard) 1f else 0.94f }
    val prayerTranslation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.MED) },
        label = "prayerTranslation"
    ) { st: SurfaceStage -> if (st == SurfaceStage.Dashboard) 0f else -cardLift }

    val headerAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.BASE) },
        label = "headerAlpha"
    ) { st: SurfaceStage -> if (st == SurfaceStage.Dashboard) 1f else 0.82f }
    val headerTranslation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.BASE) },
        label = "headerTranslation"
    ) { st: SurfaceStage -> if (st == SurfaceStage.Dashboard) 0f else -headerLift }

    val carouselAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.BASE) },
        label = "carouselAlpha"
    ) { st: SurfaceStage -> if (st == SurfaceStage.Dashboard) 1f else 0.45f }
    val carouselScale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.MED) },
        label = "carouselScale"
    ) { st: SurfaceStage -> if (st == SurfaceStage.Dashboard) 1f else 0.92f }
    val carouselTranslation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.MED) },
        label = "carouselTranslation"
    ) { st: SurfaceStage -> if (st == SurfaceStage.Dashboard) 0f else carouselDrop }

    val scrimAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.BASE) },
        label = "scrimAlpha"
    ) { st: SurfaceStage -> if (st == SurfaceStage.Dashboard) 0f else 1f }

    val sheetAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.MED) },
        label = "sheetAlpha"
    ) { st: SurfaceStage -> if (st == SurfaceStage.Dashboard) 0f else 1f }
    val sheetTranslationX by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.LONG) },
        label = "sheetTranslationX"
    ) { st: SurfaceStage -> if (st == SurfaceStage.Dashboard) sheetHiddenOffset else 0f }
    val sheetTranslationY by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.LONG) },
        label = "sheetTranslationY"
    ) { st: SurfaceStage ->
        when (st) {
            SurfaceStage.Dashboard -> sheetLift
            SurfaceStage.CitySheet -> 0f
            SurfaceStage.CityPicker -> -sheetLift / 2f
        }
    }
    val sheetScale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = Dur.LONG) },
        label = "sheetScale"
    ) { st: SurfaceStage ->
        when (st) {
            SurfaceStage.Dashboard -> 0.9f
            SurfaceStage.CitySheet -> 1f
            SurfaceStage.CityPicker -> 1.04f
        }
    }

    // Блокируем взаимодействия на время перехода
    val isTransitioning = transition.isRunning

    var showCityHint by rememberSaveable { mutableStateOf(true) }
    var showExplodeHint by rememberSaveable { mutableStateOf(true) }

    BackHandler(enabled = showSheet) {
        if (sheetTab != CitySheetTab.Wheel) {
            onShowWheel()
        } else {
            onSheetDismiss()
        }
    }

    Box(Modifier.fillMaxSize()) {
        val headerOffsetY = (79f * sy).dp
        val headerHorizontal = (67f * sx).dp
        val headerWidth = (533f * sx).dp
        val cardOffsetY = (226f * sy).dp
        val cardHorizontal = (52f * sx).dp
        val cardMaxWidth = (540f * sx).dp
        val carouselBottomOffset = navPadding.calculateBottomPadding() + (48f * sy).dp

        val normalizedCity = remember(city) { city.substringBefore(',').ifBlank { city }.trim() }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = headerOffsetY, start = headerHorizontal, end = headerHorizontal)
                .widthIn(max = headerWidth)
                .zIndex(1f)
                .graphicsLayer {
                    alpha = headerAlpha
                    translationY = headerTranslation
                },
            verticalArrangement = Arrangement.spacedBy((12f * sy).dp)
        ) {
            HeaderPill(
                city = normalizedCity,
                now = now,
                modifier = Modifier.fillMaxWidth(),
                onTap = {
                    showCityHint = false
                    if (!isTransitioning) onCityPillClick()
                }
            )

            AnimatedVisibility(
                visible = showCityHint && stage == SurfaceStage.Dashboard,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                enter = fadeIn(tween(Dur.BASE)) +
                        slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut(tween(Dur.SHORT)) +
                        slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                HintBubble(
                    text = "Нажмите на плашку, чтобы выбрать город",
                    onDismiss = { showCityHint = false }
                )
            }
        }

        var exploded by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(stage) {
            if (stage != SurfaceStage.Dashboard) {
                exploded = false
                showExplodeHint = false
                showCityHint = false
            }
        }

        val prayerModifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = cardOffsetY, start = cardHorizontal, end = cardHorizontal)
            .widthIn(max = cardMaxWidth)
            .graphicsLayer {
                val explodedScale = if (exploded) 1.08f else 1f
                alpha = prayerAlpha
                scaleX = prayerScale * explodedScale
                scaleY = prayerScale * explodedScale
                translationY = prayerTranslation
            }

        val toggleExploded = {
            if (!isTransitioning) {
                exploded = !exploded
                showExplodeHint = false
            }
        }

        PrayerCard(
            times = prayerTimes,
            thirds = thirds,
            exploded = exploded,
            showExplodeHint = showExplodeHint && stage == SurfaceStage.Dashboard,
            onToggleExploded = toggleExploded,
            onExplodeHintDismiss = { showExplodeHint = false },
            modifier = if (stage == SurfaceStage.Dashboard) {
                prayerModifier.combinedClickable(
                    enabled = !isTransitioning,
                    onClick = {},
                    onDoubleClick = { toggleExploded() },
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                )
            } else {
                prayerModifier
            }
        )

        EffectCarousel(
            items = effectThumbs,
            selected = selectedEffect,
            onSelected = onEffectSelected,
            enabled = stage == SurfaceStage.Dashboard && !isTransitioning,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = carouselBottomOffset)
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
    val sx = Dimens.sx()
    val horizontalPadding = Dimens.scaledX(R.dimen.abys_pill_pad_h)
    val verticalPadding = Dimens.scaledY(R.dimen.abys_pill_pad_v)
    val headlineSpacing = (10f * sy).dp
    val chevronSize = (14f * sx).dp
    val shape = RoundedCornerShape(Tokens.Radii.pill())

    Box(
        modifier
            .fillMaxWidth()
            .shadow(elevation = GlassDefaults.elevation, shape = shape, clip = false)
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
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        0f to GlassDefaults.stroke,
                        1f to GlassDefaults.glow
                    ),
                    shape = shape
                )
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = (4f * sy).dp),
                verticalArrangement = Arrangement.spacedBy(headlineSpacing)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((12f * sx).dp)
                ) {
                    Text(
                        text = city.ifBlank { "—" },
                        fontSize = TypeScale.city,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = TypeTone.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        textDecoration = TextDecoration.Underline
                    )
                    TabularText(
                        text = now.ifBlank { "--:--" },
                        fontSize = TypeScale.timeNow,
                        fontWeight = FontWeight.SemiBold,
                        color = TypeTone.secondary,
                        textAlign = TextAlign.Right,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.wrapContentWidth(Alignment.End)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((8f * sx).dp)
                ) {
                    Box(
                        Modifier
                            .size(chevronSize)
                            .border(
                                width = 1.dp,
                                color = TypeTone.divider,
                                shape = RoundedCornerShape(chevronSize / 2)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "›",
                            fontSize = TypeScale.eyebrow,
                            color = TypeTone.secondary
                        )
                    }
                    Text(
                        text = "Открыть выбор города",
                        fontSize = TypeScale.eyebrow,
                        fontWeight = FontWeight.Medium,
                        color = TypeTone.dim
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
    exploded: Boolean,
    showExplodeHint: Boolean,
    onToggleExploded: () -> Unit,
    onExplodeHintDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sy = Dimens.sy()
    val shape = RoundedCornerShape(Tokens.Radii.card())
    val rowSpacing = (12f * sy).dp
    val sectionSpacing = (24f * sy).dp
    val nightHeadingSpacing = (8f * sy).dp
    val cardHorizontalPad = Dimens.scaledX(R.dimen.abys_card_pad_h)
    val cardTopPad = Dimens.scaledY(R.dimen.abys_card_pad_top)
    val cardBottomPad = Dimens.scaledY(R.dimen.abys_card_pad_bottom)
    val toggleTopOffset = cardTopPad + (4f * sy).dp
    val toggleEndOffset = cardHorizontalPad + (2f * Dimens.sx()).dp
    val hintTopOffset = toggleTopOffset + (34f * sy).dp
    Box(
        modifier
            .fillMaxWidth()
            .shadow(elevation = GlassDefaults.elevation, shape = shape, clip = false)
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
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        0f to GlassDefaults.stroke,
                        1f to GlassDefaults.glow
                    ),
                    shape = shape
                )
        )
        Column(
            Modifier
                .fillMaxWidth()
                .padding(
                    start = cardHorizontalPad,
                    end = cardHorizontalPad,
                    top = cardTopPad,
                    bottom = cardBottomPad
                )
                .animateContentSize(animationSpec = tween(Dur.BASE))
        ) {
            val schedule = listOf(
                "Фаджр" to (times["Fajr"] ?: "--:--"),
                "Восход" to (times["Sunrise"] ?: "--:--"),
                "Зухр" to (times["Dhuhr"] ?: "--:--"),
                "Аср (Стандарт)" to (times["AsrStd"] ?: "--:--"),
                "Аср (Ханафитский)" to (times["AsrHana"] ?: "--:--"),
                "Магриб" to (times["Maghrib"] ?: "--:--"),
                "Иша" to (times["Isha"] ?: "--:--")
            )
            schedule.forEachIndexed { index, (label, value) ->
                PrayerRow(label, value)
                if (index != schedule.lastIndex) {
                    Spacer(Modifier.height(rowSpacing))
                    ThinDivider()
                    Spacer(Modifier.height(rowSpacing))
                }
            }

            Spacer(Modifier.height(sectionSpacing))
            NightSectionHeading()
            Spacer(Modifier.height(nightHeadingSpacing))
            NightThirdsRow(thirds)
        }

        ExplodeToggle(
            expanded = exploded,
            onToggle = onToggleExploded,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = toggleTopOffset, end = toggleEndOffset)
        )

        AnimatedVisibility(
            visible = showExplodeHint,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = hintTopOffset, end = toggleEndOffset),
            enter = fadeIn(tween(Dur.BASE)) + slideInVertically(initialOffsetY = { -it / 2 }),
            exit = fadeOut(tween(Dur.SHORT)) + slideOutVertically(targetOffsetY = { -it / 2 })
        ) {
            HintBubble(
                text = "Двойной тап или «Фокус» увеличит расписание",
                onDismiss = onExplodeHintDismiss
            )
        }
    }
}

@Composable
private fun PrayerRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
        TabularText(
            text = value,
            fontSize = TypeScale.label,
            fontWeight = FontWeight.SemiBold,
            color = TypeTone.primary,
            textAlign = TextAlign.Right,
            lineHeight = TypeScale.label,
            modifier = Modifier.wrapContentWidth(Alignment.End),
            maxLines = 1
        )
    }
}

@Composable
private fun ExplodeToggle(expanded: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val shape = RoundedCornerShape((18f * sy).dp)
    val label = if (expanded) "Обычный вид" else "Фокус"
    Box(
        modifier
            .clip(shape)
            .background(Color.White.copy(alpha = 0.14f))
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.28f), shape = shape)
            .clickable(onClick = onToggle)
            .padding(horizontal = (16f * sx).dp, vertical = (6f * sy).dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = TypeScale.eyebrow,
            fontWeight = FontWeight.SemiBold,
            color = TypeTone.primary
        )
    }
}

@Composable
private fun HintBubble(
    text: String,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val shape = RoundedCornerShape((18f * sy).dp)
    Row(
        modifier
            .clip(shape)
            .background(Color.Black.copy(alpha = 0.62f))
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.28f), shape = shape)
            .padding(horizontal = (16f * sx).dp, vertical = (8f * sy).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy((10f * sx).dp)
    ) {
        Text(
            text = text,
            fontSize = TypeScale.eyebrow,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            lineHeight = TypeScale.eyebrow
        )
        if (onDismiss != null) {
            Text(
                text = "×",
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = (6f * sx).dp, vertical = (2f * sy).dp),
                fontSize = TypeScale.eyebrow,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun NightSectionHeading() {
    Text(
        text = NIGHT_SECTION_TITLE,
        fontSize = TypeScale.label,
        fontWeight = FontWeight.SemiBold,
        color = TypeTone.primary
    )
}

@Composable
private fun NightThirdsRow(thirds: NightIntervals) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val spacing = (10f * sx).dp
    val cardHeight = (72f * sy).dp
    val shape = RoundedCornerShape((16f * sy).dp)
    val borderColor = Color.White.copy(alpha = 0.26f)
    val background = Brush.verticalGradient(
        0f to Color.White.copy(alpha = 0.22f),
        1f to Color.White.copy(alpha = 0.08f)
    )
    val romans = listOf("I", "II", "III")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        thirds.asList().forEachIndexed { index, (start, end) ->
            NightThirdCard(
                title = romans.getOrElse(index) { "" },
                start = start.ifBlank { "--:--" },
                end = end.ifBlank { "--:--" },
                modifier = Modifier.weight(1f),
                height = cardHeight,
                shape = shape,
                borderColor = borderColor,
                background = background
            )
        }
    }
}

@Composable
private fun NightThirdCard(
    title: String,
    start: String,
    end: String,
    modifier: Modifier = Modifier,
    height: Dp,
    shape: RoundedCornerShape,
    borderColor: Color,
    background: Brush
) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val spacing = (6f * sy).dp
    val titleSize = TypeScale.subLabel
    val timeSize = TypeScale.timeline

    Box(
        modifier
            .height(height)
            .clip(shape)
            .background(background)
            .border(width = 1.dp, color = borderColor, shape = shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = (10f * sx).dp, vertical = (12f * sy).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                color = TypeTone.secondary
            )
            Spacer(Modifier.height(spacing))
            TabularText(
                text = start,
                fontSize = timeSize,
                fontWeight = FontWeight.SemiBold,
                color = TypeTone.primary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            ThinDivider(Modifier.padding(vertical = (4f * sy).dp))
            TabularText(
                text = end,
                fontSize = timeSize,
                fontWeight = FontWeight.SemiBold,
                color = TypeTone.primary,
                textAlign = TextAlign.Center,
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


