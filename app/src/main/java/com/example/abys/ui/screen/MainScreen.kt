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
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
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
    val top = Color(0x59131618)
    val bottom = Color(0x66131618)
    val stroke = Color(0x14FFFFFF)
    val blur = 18.dp
    val bgScrim = Color(0x33101518)
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
        @Composable get() = Color.White.copy(alpha = 0.08f)
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

    Box(Modifier.fillMaxSize()) {
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
                top = (226f * sy).dp
            )
            .heightIn(min = (611f * sy).dp)
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
                .padding(bottom = navPadding.calculateBottomPadding() + (48f * sy).dp)
                .graphicsLayer {
                    alpha = carouselAlpha
                    scaleX = carouselScale
                    scaleY = carouselScale
                    translationY = carouselTranslation
                }
        )

        if (scrimAlpha > 0.01f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = scrimAlpha }
                    .background(Tokens.Colors.tickDark.copy(alpha = 0.5f))
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
                    text = now,
                    fontSize = TypeScale.timeNow,
                    fontWeight = FontWeight.SemiBold,
                    color = TypeTone.secondary,
                    textAlign = TextAlign.Right,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.wrapContentWidth(Alignment.End)
                    text = "Город",
                    fontSize = TypeScale.eyebrow,
                    fontWeight = FontWeight.Medium,
                    color = TypeTone.dim
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = city,
                        fontSize = TypeScale.city,
                        fontWeight = FontWeight.Medium,
                        color = TypeTone.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = now,
                        fontSize = TypeScale.timeNow,
                        fontWeight = FontWeight.SemiBold,
                        color = TypeTone.secondary,
                        textAlign = TextAlign.Right,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.wrapContentWidth(Alignment.End)
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
    val rowSpacing = (10f * sy).dp
    val subSpacing = (6f * sy).dp
    val dividerPadding = (5f * sy).dp
    val blockSpacing = (18f * sy).dp
    val togglePadding = PaddingValues(
        vertical = (10f * sy).dp,
        horizontal = (18f * sx).dp
    )
    var thirdsExpanded by rememberSaveable { mutableStateOf(true) }
    val rotation by animateFloatAsState(
        targetValue = if (thirdsExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = Dur.SHORT),
        label = "night-toggle"
    )

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
                indicatorWidth = (60f * sx).dp,
                indicatorHeight = (4f * sy).dp,
                indicatorRadius = (2f * s).dp,
                spacing = (10f * sx).dp
            )
            Spacer(Modifier.height(subSpacing))
            AsrSub(
                label = "ханафи",
                value = times["AsrHana"] ?: "--:--",
                indicatorWidth = (60f * sx).dp,
                indicatorHeight = (4f * sy).dp,
                indicatorRadius = (2f * s).dp,
                spacing = (10f * sx).dp
            )
            Spacer(Modifier.height(rowSpacing))
            ThinDivider(Modifier.padding(vertical = dividerPadding))
            Spacer(Modifier.height(rowSpacing))

            RowItem("Магриб", times["Maghrib"] ?: "--:--")
            Spacer(Modifier.height(rowSpacing))
            ThinDivider(Modifier.padding(vertical = dividerPadding))
            Spacer(Modifier.height(rowSpacing))

            RowItem("Иша", times["Isha"] ?: "--:--")

            Spacer(Modifier.height(blockSpacing))

            OutlinedButton(
                onClick = { thirdsExpanded = !thirdsExpanded },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = togglePadding,
                border = BorderStroke(1.dp, TypeTone.divider),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = TypeTone.primary
                )
            ) {
                Text(
                    text = "Ночь (3 части)",
                    fontSize = TypeScale.label,
                    fontWeight = FontWeight.Medium,
                    color = TypeTone.primary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = TypeTone.primary,
                    modifier = Modifier.graphicsLayer { rotationZ = rotation }
                )
            }
            AnimatedVisibility(
                visible = thirdsExpanded,
                enter = expandVertically(animationSpec = tween(Dur.BASE)) + fadeIn(tween(Dur.X_SHORT)),
                exit = shrinkVertically(animationSpec = tween(Dur.BASE)) + fadeOut(tween(Dur.X_SHORT))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height((16f * sy).dp))
                    NightThirdsRow(thirds)
                }
            }
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
            lineHeight = TypeScale.subLabel,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        Row(
            modifier = Modifier.wrapContentWidth(Alignment.End),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            repeat(2) {
                Box(
                    Modifier
                        .width(indicatorWidth)
                        .height(indicatorHeight)
                        .clip(RoundedCornerShape(indicatorRadius))
                        .background(Tokens.Colors.tickFull)
                )
            }
        }
        Spacer(Modifier.width(spacing))
        Text(
            text = value,
            fontSize = TypeScale.subLabel,
            fontWeight = FontWeight.SemiBold,
            color = TypeTone.secondary,
            textAlign = TextAlign.Right,
            maxLines = 1,
            modifier = Modifier.wrapContentWidth(Alignment.End)
            lineHeight = TypeScale.subLabel,
            modifier = Modifier.wrapContentWidth(Alignment.End),
            maxLines = 1
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
private fun NightThirdsRow(thirds: NightIntervals) {
    val sy = Dimens.sy()
    val numerals = listOf("I", "II", "III")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy((16f * sy).dp)
    ) {
        thirds.asList().forEachIndexed { index, (start, end) ->
            NightThirdCard(
                numeral = numerals.getOrElse(index) { "" },
                start = start,
                end = end,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NightThirdCard(
    numeral: String,
    start: String,
    end: String,
    modifier: Modifier = Modifier
) {
    val sy = Dimens.sy()
    val shape = RoundedCornerShape(Tokens.Radii.card())
    val timeStyle = MaterialTheme.typography.bodyMedium.copy(
        fontFamily = AbysFonts.inter,
        fontWeight = FontWeight.Medium,
        fontSize = TypeScale.timeline,
        color = TypeTone.primary,
        textAlign = TextAlign.Center
    )
    val numeralStyle = MaterialTheme.typography.titleMedium.copy(
        fontFamily = AbysFonts.inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = TypeScale.label,
        color = TypeTone.secondary
    )

    Box(
        modifier
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
            modifier = Modifier
                .matchParentSize()
                .padding(vertical = (16f * sy).dp)
                .padding(horizontal = (12f * sy).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = start, style = timeStyle, maxLines = 1)
            Spacer(Modifier.height((10f * sy).dp))
            Text(text = numeral, style = numeralStyle)
            Spacer(Modifier.height((10f * sy).dp))
            Text(text = end, style = timeStyle, maxLines = 1)
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

