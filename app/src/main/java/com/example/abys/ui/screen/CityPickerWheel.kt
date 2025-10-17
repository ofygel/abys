package com.example.abys.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import com.example.abys.ui.theme.AbysFonts
import com.example.abys.ui.theme.Dimens
import com.example.abys.ui.theme.Tokens
import kotlin.math.abs
import kotlinx.coroutines.flow.filter

private const val VISIBLE_AROUND = 4

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CityPickerWheel(
    cities: List<String>,
    currentCity: String,
    onChosen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (cities.isEmpty()) return

    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val s = Dimens.s()
    val density = LocalDensity.current

    val initialIndex = remember(cities, currentCity) {
        cities.indexOf(currentCity).takeIf { it >= 0 } ?: 0
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    var hasAligned by remember { mutableStateOf(false) }
    LaunchedEffect(cities) { hasAligned = false }

    val flingBehavior = rememberSnapFlingBehavior(
        lazyListState = listState
    )

    val haptics = LocalHapticFeedback.current

    LaunchedEffect(cities, currentCity) {
        val index = cities.indexOf(currentCity).takeIf { it >= 0 } ?: 0
        if (!hasAligned) {
            listState.scrollToItem(index)
            hasAligned = true
        } else if (!listState.isScrollInProgress) {
            listState.animateScrollToItem(index)
        }
    }

    var lastSnapped by remember { mutableStateOf(initialIndex.coerceIn(cities.indices)) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .filter { !it }
            .collect {
                val centerIndex = listState.closestCenterItem()
                if (centerIndex in cities.indices && centerIndex != lastSnapped) {
                    lastSnapped = centerIndex
                    onChosen(cities[centerIndex])
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
    }

    BoxWithConstraints(modifier.clipToBounds()) {
        val itemHeight = with(density) { (92f * sy).dp.toPx() }
        val viewportHeight = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val verticalPadding = ((viewportHeight - itemHeight) / 2f).coerceAtLeast(0f)
        val paddingDp = with(density) { verticalPadding.toDp() }
        val centerIndex by remember { derivedStateOf { listState.closestCenterItem() } }

        Box(
            Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    if (size.height <= 0f) return@drawWithContent
                    val fadeHeight = with(density) { (140f * sy).dp.toPx() }
                    val fraction = (fadeHeight / size.height).coerceIn(0f, 0.42f)
                    drawRect(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color.Transparent,
                                fraction to Color.Black,
                                (1f - fraction) to Color.Black,
                                1f to Color.Transparent
                            )
                        ),
                        size = size,
                        blendMode = BlendMode.DstIn
                    )
                }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = (32f * sx).dp),
            state = listState,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = paddingDp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy((12f * sy).dp),
            flingBehavior = flingBehavior
        ) {
            itemsIndexed(cities) { index, city ->
                val distance = abs(centerIndex - index)
                val scale = 0.60f + 0.40f * (1f - (distance / (VISIBLE_AROUND + 1f))).coerceIn(0f, 1f)
                val alpha = (1f - distance / (VISIBLE_AROUND + 1f)).coerceIn(0.35f, 1f)
                val textSize = (42f * scale).coerceIn(22f, 42f)

                BasicText(
                    text = city,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            this.alpha = alpha
                        },
                    style = TextStyle(
                        fontFamily = AbysFonts.inter,
                        textAlign = TextAlign.Center,
                        fontSize = (textSize * s).sp,
                        fontWeight = if (distance == 0) FontWeight.ExtraBold else FontWeight.Bold,
                        color = Tokens.Colors.text,
                        lineHeight = (if (distance == 0) 1.10f else 1.05f).em,
                        shadow = Shadow(
                            color = Tokens.Colors.tickDark.copy(alpha = 0.35f),
                            offset = Offset(0f, 2f),
                            blurRadius = 6f
                        )
                    ),
                    maxLines = 1
                )
            }
        }

        val highlightShape = RoundedCornerShape((18f * s).dp)
        val slotHeightDp = with(density) { itemHeight.toDp() }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(slotHeightDp)
                .clip(highlightShape)
                .background(Tokens.Colors.tickDark.copy(alpha = 0.08f))
                .border(2.dp, Tokens.Colors.chipStroke, highlightShape)
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListState.closestCenterItem(): Int {
    val layout = layoutInfo
    if (layout.visibleItemsInfo.isEmpty()) return firstVisibleItemIndex
    val center = (layout.viewportStartOffset + layout.viewportEndOffset) / 2f
    return layout.visibleItemsInfo.minByOrNull { info ->
        val itemCenter = info.offset + info.size / 2f
        abs(itemCenter - center)
    }?.index ?: firstVisibleItemIndex
}
