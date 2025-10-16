package com.example.abys.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.abys.ui.theme.AbysFonts
import com.example.abys.ui.theme.Dimens
import com.example.abys.ui.theme.Tokens
import kotlin.math.abs

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

    LaunchedEffect(cities, currentCity) {
        val index = cities.indexOf(currentCity).takeIf { it >= 0 } ?: 0
        listState.animateScrollToItem(index)
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { inProgress ->
                if (!inProgress) {
                    val centerIndex = listState.closestCenterItem()
                    if (centerIndex in cities.indices) {
                        onChosen(cities[centerIndex])
                    }
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
                .matchParentSize()
                .drawWithContent {
                    drawContent()
                    if (size.height <= 0f) return@drawWithContent
                    val fadeHeight = with(density) { (160f * sy).dp.toPx() }
                    val fraction = (fadeHeight / size.height).coerceIn(0f, 0.49f)
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
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy((12f * sy).dp)
        ) {
            itemsIndexed(cities) { index, city ->
                val distance = abs(centerIndex - index)
                val scale = 0.60f + 0.40f * (1f - (distance / (VISIBLE_AROUND + 1f))).coerceIn(0f, 1f)
                val alpha = (1f - distance / (VISIBLE_AROUND + 1f)).coerceIn(0.2f, 1f)
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

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .clip(RoundedCornerShape((18f * s).dp))
                .background(Tokens.Colors.tickDark.copy(alpha = 0.06f))
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
