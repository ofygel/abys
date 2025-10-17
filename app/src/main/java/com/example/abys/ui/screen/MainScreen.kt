@file:Suppress("MagicNumber")

package com.example.abys.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

private enum class SurfaceStage { Dashboard, CitySheet, CityPicker }

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
    val cityOptions = rememberCitiesFromRes()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.restorePersisted(context.applicationContext)
    }

    CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = AbysFonts.inter)) {
        Box(Modifier.fillMaxSize()) {
            Crossfade(
                modifier = Modifier.fillMaxSize(),
                targetState = selectedEffect,
                animationSpec = tween(durationMillis = 180),
                label = "effect-background"
            ) { effect ->
                BackgroundHost(effect = effect)
            }

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
                onCityChipTap = vm::togglePicker,
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
    val sheetHiddenOffset = with(density) { (236f * sx).dp.toPx() }
    val sheetLift = with(density) { (18f * sy).dp.toPx() }
    val cardLift = with(density) { (42f * sy).dp.toPx() }
    val carouselDrop = with(density) { (36f * sy).dp.toPx() }
    val headerLift = with(density) { (14f * sy).dp.toPx() }

    val prayerAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = if (targetState == SurfaceStage.Dashboard) 200 else 240) },
        label = "prayerAlpha"
    ) { st -> if (st == SurfaceStage.Dashboard) 1f else 0f }
    val prayerScale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 260) },
        label = "prayerScale"
    ) { st -> if (st == SurfaceStage.Dashboard) 1f else 0.94f }
    val prayerTranslation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 240) },
        label = "prayerTranslation"
    ) { st -> if (st == SurfaceStage.Dashboard) 0f else -cardLift }

    val headerAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 220) },
        label = "headerAlpha"
    ) { st -> if (st == SurfaceStage.Dashboard) 1f else 0.82f }
    val headerTranslation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 220) },
        label = "headerTranslation"
    ) { st -> if (st == SurfaceStage.Dashboard) 0f else -headerLift }

    val carouselAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 220) },
        label = "carouselAlpha"
    ) { st -> if (st == SurfaceStage.Dashboard) 1f else 0.45f }
    val carouselScale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 240) },
        label = "carouselScale"
    ) { st -> if (st == SurfaceStage.Dashboard) 1f else 0.92f }
    val carouselTranslation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 240) },
        label = "carouselTranslation"
    ) { st -> if (st == SurfaceStage.Dashboard) 0f else carouselDrop }

    val scrimAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 220) },
        label = "scrimAlpha"
    ) { st -> if (st == SurfaceStage.Dashboard) 0f else 1f }

    val sheetAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 240) },
        label = "sheetAlpha"
    ) { st -> if (st == SurfaceStage.Dashboard) 0f else 1f }
    val sheetTranslationX by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 260) },
        label = "sheetTranslationX"
    ) { st -> if (st == SurfaceStage.Dashboard) sheetHiddenOffset else 0f }
    val sheetTranslationY by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 260) },
        label = "sheetTranslationY"
    ) { st ->
        when (st) {
            SurfaceStage.Dashboard -> sheetLift
            SurfaceStage.CitySheet -> 0f
            SurfaceStage.CityPicker -> -sheetLift / 2f
        }
    }
    val sheetScale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 260) },
        label = "sheetScale"
    ) { st ->
        when (st) {
            SurfaceStage.Dashboard -> 0.9f
            SurfaceStage.CitySheet -> 1f
            SurfaceStage.CityPicker -> 1.04f
        }
    }

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
            onTap = onCityPillClick
        )

        var exploded by remember { mutableStateOf(false) }
        LaunchedEffect(stage) {
            if (stage != SurfaceStage.Dashboard) exploded = false
        }

        val prayerModifier = Modifier
            .padding(
                start = (64f * sx).dp,
                end = (64f * sx).dp,
                top = (226f * sy).dp
            )
            .height((611f * sy).dp)
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
                enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.96f, animationSpec = tween(220)),
                exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.96f, animationSpec = tween(180))
            ) {
                PrayerCard(
                    times = prayerTimes,
                    thirds = thirds,
                    modifier = if (stage == SurfaceStage.Dashboard) {
                        prayerModifier.pointerInput(Unit) {
                            detectTapGestures(onDoubleTap = { exploded = !exploded })
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
            enabled = stage == SurfaceStage.Dashboard,
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
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCityPillClick
                    )
            )
        }

        if (sheetAlpha > 0.01f) {
            AnimatedVisibility(
                visible = showSheet,
                enter = fadeIn(tween(220)) +
                    slideInHorizontally(initialOffsetX = { it / 6 }, animationSpec = tween(220)),
                exit = fadeOut(tween(180)) +
                    slideOutHorizontally(targetOffsetX = { it / 6 }, animationSpec = tween(180))
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
                        .graphicsLayer {
                            alpha = sheetAlpha
                            translationX = sheetTranslationX
                            translationY = sheetTranslationY
                            scaleX = sheetScale
                            scaleY = sheetScale
                        }
                )
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
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val horizontalPadding = Dimens.scaledX(R.dimen.abys_pill_pad_h)
    val verticalPadding = Dimens.scaledY(R.dimen.abys_pill_pad_v)
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
                .backdropBlur(6.dp)
                .background(Tokens.Colors.overlayTop)
        )
        Box(
            Modifier
                .matchParentSize()
                .clip(shape)
                .clickable(onClick = onTap)
                .padding(
                    horizontal = horizontalPadding,
                    vertical = verticalPadding
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = city,
                    fontSize = Tokens.TypographySp.city,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Italic,
                    color = Tokens.Colors.text,
                    textDecoration = TextDecoration.Underline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = now,
                    fontSize = Tokens.TypographySp.timeNow,
                    fontWeight = FontWeight.Bold,
                    color = Tokens.Colors.text,
                    textAlign = TextAlign.Right,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.wrapContentWidth(Alignment.End)
                )
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
    val s = Dimens.s()
    var thirdsExpanded by remember { mutableStateOf(false) }

    val rowStep = Dimens.scaledY(R.dimen.abys_row_step)
    val labelHeight = (Tokens.TypographyPx.label * sy).dp
    val subLabelHeight = (Tokens.TypographyPx.subLabel * sy).dp
    val rowSpacing = (rowStep - labelHeight).coerceAtLeast(0.dp)
    val subSpacing = (rowStep - subLabelHeight).coerceAtLeast(0.dp)

    val shape = RoundedCornerShape(Tokens.Radii.card())
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
                .backdropBlur(6.dp)
                .background(Tokens.Colors.overlayCard)
        )
        Column(
            Modifier
                .matchParentSize()
                .clip(shape)
                .padding(
                    start = Dimens.scaledX(R.dimen.abys_card_pad_h),
                    end = Dimens.scaledX(R.dimen.abys_card_pad_h),
                    top = Dimens.scaledY(R.dimen.abys_card_pad_top),
                    bottom = Dimens.scaledY(R.dimen.abys_card_pad_bottom)
                )
        ) {
            RowItem("Фаджр", times["Fajr"] ?: "--:--")
            Spacer(Modifier.height(rowSpacing))
            RowItem("Восход", times["Sunrise"] ?: "--:--")
            Spacer(Modifier.height(rowSpacing))
            RowItem("Зухр", times["Dhuhr"] ?: "--:--")
            Spacer(Modifier.height(rowSpacing))

            Text(
                text = "Аср:",
                fontSize = Tokens.TypographySp.label,
                fontWeight = FontWeight.Bold,
                color = Tokens.Colors.text
            )
            Spacer(Modifier.height((4f * sy).dp))
            AsrSub(
                label = "стандарт",
                value = times["AsrStd"] ?: "--:--",
                indicatorWidth = (64f * sx).dp,
                indicatorHeight = (4f * sy).dp,
                indicatorRadius = (2f * s).dp
            )
            Spacer(Modifier.height(subSpacing))
            AsrSub(
                label = "Ханафи",
                value = times["AsrHana"] ?: "--:--",
                indicatorWidth = (64f * sx).dp,
                indicatorHeight = (4f * sy).dp,
                indicatorRadius = (2f * s).dp
            )
            Spacer(Modifier.height(rowSpacing))

            RowItem("Магриб", times["Maghrib"] ?: "--:--")
            Spacer(Modifier.height(rowSpacing))
            RowItem("Иша", times["Isha"] ?: "--:--")

            Spacer(Modifier.height((24f * sy).dp))

            val rotation by animateFloatAsState(
                targetValue = if (thirdsExpanded) 180f else 0f,
                animationSpec = tween(durationMillis = 200),
                label = "night-toggle"
            )

            OutlinedButton(
                onClick = { thirdsExpanded = !thirdsExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ночь (3 части)",
                    fontSize = Tokens.TypographySp.label,
                    fontWeight = FontWeight.Bold,
                    color = Tokens.Colors.text,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = Tokens.Colors.text,
                    modifier = Modifier.graphicsLayer { rotationZ = rotation }
                )
            }

            AnimatedVisibility(thirdsExpanded) {
                NightTimeline(thirds)
            }
        }
    }
}

@Composable
private fun RowItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = Tokens.TypographySp.label,
            fontWeight = FontWeight.Bold,
            color = Tokens.Colors.text,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        Text(
            text = value,
            fontSize = Tokens.TypographySp.label,
            fontWeight = FontWeight.Bold,
            color = Tokens.Colors.text,
            textAlign = TextAlign.Right,
            modifier = Modifier.wrapContentWidth(Alignment.End),
            maxLines = 1
        )
    }
}

@Composable
private fun AsrSub(
    label: String,
    value: String,
    indicatorWidth: Dp,
    indicatorHeight: Dp,
    indicatorRadius: Dp
) {
    val sx = Dimens.sx()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = Tokens.TypographySp.subLabel,
            fontWeight = FontWeight.Bold,
            color = Tokens.Colors.text,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        Box(
            Modifier
                .width(indicatorWidth)
                .height(indicatorHeight)
                .clip(RoundedCornerShape(indicatorRadius))
                .background(Tokens.Colors.tickFull)
        )
        Spacer(Modifier.width((12f * sx).dp))
        Text(
            text = value,
            fontSize = Tokens.TypographySp.subLabel,
            fontWeight = FontWeight.Bold,
            color = Tokens.Colors.text,
            textAlign = TextAlign.Right,
            modifier = Modifier.wrapContentWidth(Alignment.End),
            maxLines = 1
        )
    }
}

@Composable
private fun NightTimeline(thirds: NightIntervals) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val labelStyle = MaterialTheme.typography.bodyMedium.copy(
        fontFamily = AbysFonts.inter,
        fontWeight = FontWeight.SemiBold,
        color = Tokens.Colors.text
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = (16f * sy).dp),
        verticalArrangement = Arrangement.spacedBy((12f * sy).dp)
    ) {
        thirds.asList().forEachIndexed { index, (start, end) ->
            val label = when (index) {
                0 -> "Первая треть"
                1 -> "Вторая треть"
                else -> "Последняя треть"
            }
            Column {
                Text(text = label, style = labelStyle)
                Spacer(Modifier.height((4f * sy).dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = start,
                        fontSize = Tokens.TypographySp.timeline,
                        fontWeight = FontWeight.Bold,
                        color = Tokens.Colors.text
                    )
                    Text(
                        text = end,
                        fontSize = Tokens.TypographySp.timeline,
                        fontWeight = FontWeight.Bold,
                        color = Tokens.Colors.text
                    )
                }
            }
        }
    }
}

