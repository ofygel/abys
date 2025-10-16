@file:Suppress("MagicNumber")

package com.example.abys.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.abys.R
import com.example.abys.logic.MainViewModel
import com.example.abys.ui.background.SlideshowBackground
import com.example.abys.ui.theme.Dimens
import com.example.abys.ui.theme.Tokens
import com.example.abys.ui.util.backdropBlur
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlinx.coroutines.launch

@Composable
fun MainApp(vm: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val city by vm.city.observeAsState("Almaty")
    val times by vm.prayerTimes.observeAsState(emptyMap())
    val thirds by vm.thirds.observeAsState(Triple("21:12", "00:59", "4:50"))
    val now by vm.clock.observeAsState(
        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
    )
    val showSheet by vm.sheetVisible.observeAsState(false)
    val showPicker by vm.pickerVisible.observeAsState(false)
    val hadith by vm.hadithToday.observeAsState("")
    val effects = remember {
        listOf(
            R.drawable.thumb_leaves,
            R.drawable.thumb_lightning,
            R.drawable.thumb_night,
            R.drawable.thumb_rain,
            R.drawable.thumb_snow,
            R.drawable.thumb_storm,
            R.drawable.thumb_sunset_snow,
            R.drawable.thumb_wind
        )
    }

    Box(Modifier.fillMaxSize()) {
        SlideshowBackground()

        MainScreen(
            city = city,
            now = now,
            prayerTimes = times,
            thirds = thirds,
            effects = effects,
            showSheet = showSheet,
            showPicker = showPicker,
            hadith = hadith,
            cities = vm.cities,
            onCityPillClick = vm::toggleSheet,
            onCityChipTap = vm::togglePicker,
            onCityChosen = vm::setCity,
            onEffectClick = vm::setEffect
        )
    }
}

@Composable
fun MainScreen(
    city: String,
    now: String,
    prayerTimes: Map<String, String>,
    thirds: Triple<String, String, String>,
    effects: List<Int>,
    showSheet: Boolean,
    showPicker: Boolean,
    hadith: String,
    cities: List<String>,
    onCityPillClick: () -> Unit,
    onCityChipTap: () -> Unit,
    onCityChosen: (String) -> Unit,
    onEffectClick: (Int) -> Unit
) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()

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
                .height((102f * sy).dp),
            onTap = onCityPillClick
        )

        var exploded by remember { mutableStateOf(false) }
        LaunchedEffect(showSheet) {
            if (showSheet) exploded = false
        }

        AnimatedVisibility(
            visible = !showSheet,
            enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.96f, animationSpec = tween(220)),
            exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.96f, animationSpec = tween(180))
        ) {
            PrayerCard(
                times = prayerTimes,
                thirds = thirds,
                modifier = Modifier
                    .padding(
                        start = (64f * sx).dp,
                        end = (64f * sx).dp,
                        top = (226f * sy).dp
                    )
                    .height((611f * sy).dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = { exploded = !exploded })
                    }
                    .graphicsLayer {
                        alpha = if (exploded) 0f else 1f
                        scaleX = if (exploded) 1.1f else 1f
                        scaleY = if (exploded) 1.1f else 1f
                    }
            )
        }

        EffectCarousel(
            items = effects,
            onTap = onEffectClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = (48f * sy).dp)
        )

        AnimatedVisibility(
            visible = showSheet,
            enter = fadeIn(tween(220)) + slideInHorizontally(initialOffsetX = { it / 6 }, animationSpec = tween(220)),
            exit = fadeOut(tween(180)) + slideOutHorizontally(targetOffsetX = { it / 6 }, animationSpec = tween(180))
        ) {
            CitySheet(
                city = city,
                hadith = hadith,
                cities = cities,
                pickerVisible = showPicker,
                onCityChipTap = onCityChipTap,
                onCityChosen = onCityChosen,
                modifier = Modifier.fillMaxSize()
            )
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
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Tokens.Radii.pill()))
            .background(Tokens.Colors.overlayTop)
            .backdropBlur(6.dp)
            .clickable(onClick = onTap)
            .padding(
                horizontal = (24f * sx).dp,
                vertical = (18f * sy).dp
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
                modifier = Modifier.weight(1f)
            )
            Text(
                text = now,
                fontSize = Tokens.TypographySp.timeNow,
                fontWeight = FontWeight.Bold,
                color = Tokens.Colors.text,
                textAlign = TextAlign.Right,
                maxLines = 1,
                modifier = Modifier.wrapContentWidth(Alignment.End)
            )
        }
    }
}

@Composable
private fun PrayerCard(
    times: Map<String, String>,
    thirds: Triple<String, String, String>,
    modifier: Modifier = Modifier
) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val s = Dimens.s()

    val rowSpacing = ((73f - Tokens.TypographyPx.label) * sy).dp
    val subSpacing = ((73f - Tokens.TypographyPx.subLabel) * sy).dp

    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Tokens.Radii.card()))
            .background(Tokens.Colors.overlayCard)
            .backdropBlur(6.dp)
            .padding(
                start = (44f * sx).dp,
                end = (44f * sx).dp,
                top = (45f * sy).dp,
                bottom = (40f * sy).dp
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
        Timeline(thirds)
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
                .background(Tokens.Colors.text)
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
private fun Timeline(thirds: Triple<String, String, String>) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val long = (57f * sy).dp
    val short = (49f * sy).dp
    val gap = (8f * sx).dp

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(thirds.first, thirds.second, thirds.third).forEach { time ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(gap)
                ) {
                    Tick(height = short)
                    Tick(height = long)
                    Tick(height = short)
                }
                Spacer(Modifier.height((12f * sy).dp))
                Text(
                    text = time,
                    fontSize = Tokens.TypographySp.timeline,
                    fontWeight = FontWeight.Bold,
                    color = Tokens.Colors.text,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun Tick(height: Dp) {
    Box(
        Modifier
            .width(3.dp)
            .height(height)
            .background(Tokens.Colors.text)
    )
}

@Composable
private fun EffectCarousel(
    items: List<Int>,
    onTap: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val spacing = (40f * sx).dp

    Box(modifier) {
        LazyRow(
            state = state,
            horizontalArrangement = Arrangement.spacedBy(spacing),
            contentPadding = PaddingValues(horizontal = spacing)
        ) {
            itemsIndexed(items) { index, res ->
                val info = state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                val center = info?.let { it.offset + it.size / 2f }
                val viewportCenter = state.layoutInfo.viewportSize.width / 2f
                val distance = if (center != null) abs(center - viewportCenter) else Float.MAX_VALUE
                val (scale, alpha) = when {
                    distance < 40f -> 1f to 1f
                    distance < 150f -> 0.85f to 0.7f
                    else -> 0.75f to 0.5f
                }

                Image(
                    painter = painterResource(res),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = (121f * sx).dp, height = (153f * sy).dp)
                        .graphicsLayer {
                            this.scaleX = scale
                            this.scaleY = scale
                            this.alpha = alpha
                        }
                        .clip(RoundedCornerShape(Tokens.Radii.chip()))
                        .clickable {
                            info?.let {
                                val target = center ?: return@let
                                scope.launch {
                                    state.animateScrollBy(viewportCenter - target)
                                }
                            }
                            onTap(res)
                        }
                )
            }
        }
    }
}
