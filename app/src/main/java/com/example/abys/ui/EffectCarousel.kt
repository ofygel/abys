package com.example.abys.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.abys.R
import com.example.abys.ui.theme.Dimens
import com.example.abys.ui.theme.Tokens
import com.example.abys.data.EffectId
import kotlin.math.abs
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

// Data class representing the thumbnail and its corresponding effect id.
data class EffectThumb(val id: EffectId, @DrawableRes val resId: Int)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EffectCarousel(
    modifier: Modifier = Modifier,
    items: List<EffectThumb>,
    selected: EffectId,
    onSelected: (EffectId) -> Unit,
    enabled: Boolean = true
) {
    val cardWidth: Dp = Dimens.scaledX(R.dimen.abys_thumb_w)
    val cardHeight: Dp = Dimens.scaledY(R.dimen.abys_thumb_h)
    val cardRadius = Tokens.Radii.chip()
    val itemSpacing = 40.dp

    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val scope = rememberCoroutineScope()

    var viewportWidthPx by remember { mutableStateOf(0) }

    val sidePadding = cardWidth / 2 + itemSpacing

    LaunchedEffect(items, selected, viewportWidthPx) {
        if (viewportWidthPx == 0) return@LaunchedEffect
        val targetIndex = items.indexOfFirst { it.id == selected }
        if (targetIndex < 0) return@LaunchedEffect
        val currentIndex = nearestCenterIndex(listState, viewportWidthPx)
        if (currentIndex == targetIndex) {
            val delta = listState.calculateCenteringDelta(viewportWidthPx)
            if (delta != null && abs(delta) > 0.5f) {
                listState.scrollByCompat(delta)
            }
        } else {
            listState.animateScrollToItem(targetIndex)
        }
    }

    LaunchedEffect(listState, viewportWidthPx, items, enabled) {
        if (viewportWidthPx == 0) return@LaunchedEffect
        snapshotFlow { listState.isScrollInProgress }
            .filter { !it }
            .collectLatest {
                val delta = listState.calculateCenteringDelta(viewportWidthPx)
                if (delta != null && abs(delta) > 0.5f) {
                    listState.animateScrollByCompat(delta)
                    return@collectLatest
                }
                if (!enabled) return@collectLatest
                val index = nearestCenterIndex(listState, viewportWidthPx)
                val effect = items.getOrNull(index)?.id ?: return@collectLatest
                if (effect != selected) {
                    onSelected(effect)
                }
            }
    }

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        LazyRow(
            state = listState,
            flingBehavior = flingBehavior,
            userScrollEnabled = enabled,
            contentPadding = PaddingValues(horizontal = sidePadding),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(itemSpacing),
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { viewportWidthPx = it.size.width }
        ) {
            itemsIndexed(items) { index, item ->
                val distance = distanceToCenter(index, listState, viewportWidthPx)
                val scale = scaleForDistance(distance)
                val alpha = alphaForDistance(distance)

                Card(
                    modifier = Modifier
                        .size(cardWidth, cardHeight)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .alpha(alpha),
                    shape = RoundedCornerShape(cardRadius),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    onClick = {
                        if (!enabled) return@Card
                        scope.launch {
                            listState.animateScrollToItem(index)
                            if (selected != item.id) {
                                onSelected(item.id)
                            }
                        }
                    }
                ) {
                    Image(
                        painter = painterResource(item.resId),
                        contentDescription = item.id.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (item.id == selected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = 2.dp,
                                    color = Tokens.Colors.separator,
                                    shape = RoundedCornerShape(cardRadius)
                                )
                        )
                    }
                }
            }
        }
    }
}

private fun distanceToCenter(
    index: Int,
    listState: LazyListState,
    viewportWidthPx: Int
): Float {
    if (viewportWidthPx == 0) return Float.MAX_VALUE
    val layoutInfo = listState.layoutInfo
    val visible = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
        ?: return Float.MAX_VALUE
    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
    val itemCenter = visible.offset + visible.size / 2f
    val itemExtent = visible.size.takeIf { it > 0 } ?: return Float.MAX_VALUE
    return abs(itemCenter - viewportCenter) / itemExtent.toFloat()
}

private fun nearestCenterIndex(listState: LazyListState, viewportWidthPx: Int): Int {
    val layout = listState.layoutInfo
    if (layout.totalItemsCount == 0) return 0
    val viewportCenter = (layout.viewportStartOffset + layout.viewportEndOffset) / 2f
    return layout.visibleItemsInfo.minByOrNull { info ->
        val itemCenter = info.offset + info.size / 2f
        abs(itemCenter - viewportCenter)
    }?.index ?: listState.firstVisibleItemIndex
}

private fun LazyListState.calculateCenteringDelta(viewportWidthPx: Int): Float? {
    val layout = layoutInfo
    if (layout.visibleItemsInfo.isEmpty()) return null
    val viewportCenter = (layout.viewportStartOffset + layout.viewportEndOffset) / 2f
    val closest = layout.visibleItemsInfo.minByOrNull { info ->
        val itemCenter = info.offset + info.size / 2f
        abs(itemCenter - viewportCenter)
    } ?: return null
    val itemCenter = closest.offset + closest.size / 2f
    return viewportCenter - itemCenter
}

private fun scaleForDistance(distance: Float): Float {
    if (distance == Float.MAX_VALUE) return 0.75f
    return when {
        distance <= 1f -> lerp(1f, 0.85f, distance.coerceIn(0f, 1f))
        else -> lerp(0.85f, 0.75f, (distance - 1f).coerceIn(0f, 1f))
    }
}

private fun alphaForDistance(distance: Float): Float {
    if (distance == Float.MAX_VALUE) return 0.5f
    return when {
        distance <= 1f -> lerp(1f, 0.7f, distance.coerceIn(0f, 1f))
        else -> lerp(0.7f, 0.5f, (distance - 1f).coerceIn(0f, 1f))
    }
}

private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}

private suspend fun LazyListState.scrollByCompat(distance: Float) {
    if (distance == 0f) return
    scrollBy(distance)
}

private suspend fun LazyListState.animateScrollByCompat(distance: Float) {
    if (distance == 0f) return
    animateScrollBy(distance)
}
