package com.example.abys.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.hapticfeedback.performHapticFeedback
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.abys.R
import com.example.abys.data.EffectId
import com.example.abys.ui.theme.Dimens
import com.example.abys.ui.theme.Tokens
import kotlin.math.abs
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
    if (items.isEmpty()) {
        return
    }

    val cardWidth: Dp = Dimens.scaledX(R.dimen.abys_thumb_w)
    val cardHeight: Dp = Dimens.scaledY(R.dimen.abys_thumb_h)
    val cardRadius = Tokens.Radii.chip()
    val itemSpacing = 40.dp

    val repeatedItems = remember(items) {
        val source = items.ifEmpty { return@remember emptyList() }
        val chunk = source.size
        List(chunk * 3) { index -> source[index % chunk] }
    }
    val baseCount = items.size
    val centerOffset = baseCount

    val listState = rememberLazyListState()
    var hasAligned by remember { mutableStateOf(false) }
    LaunchedEffect(items) { hasAligned = false }

    val baseFling = rememberSnapFlingBehavior(
        lazyListState = listState
    )
    val flingBehavior = remember(baseFling) {
        ScaledFlingBehavior(baseFling, frictionScale = 0.88f)
    }
    val scope = rememberCoroutineScope()

    var viewportWidthPx by remember { mutableStateOf(0) }

    val sidePadding = cardWidth / 2 + itemSpacing

    val targetBaseIndex = items.indexOfFirst { it.id == selected }.takeIf { it >= 0 } ?: 0
    val globalTargetIndex = (centerOffset + targetBaseIndex)
        .coerceIn(0, repeatedItems.lastIndex)

    LaunchedEffect(repeatedItems) {
        hasAligned = false
    }

    LaunchedEffect(viewportWidthPx, items, selected, repeatedItems) {
        if (viewportWidthPx == 0 || repeatedItems.isEmpty()) return@LaunchedEffect
        if (!hasAligned) {
            listState.scrollToItem(globalTargetIndex)
            hasAligned = true
            return@LaunchedEffect
        }
        if (listState.isScrollInProgress) return@LaunchedEffect
        val currentIndex = nearestCenterIndex(listState, viewportWidthPx)
        val targetIndex = nearestMiddleIndex(
            currentIndex = currentIndex,
            baseCount = baseCount,
            targetBaseIndex = targetBaseIndex
        )
        if (targetIndex != null && targetIndex != currentIndex) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    LaunchedEffect(listState, items) {
        if (repeatedItems.isEmpty()) return@LaunchedEffect
        if (baseCount < 2) return@LaunchedEffect
        val threshold = (baseCount / 2).coerceAtLeast(1)
        val total = repeatedItems.size
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collectLatest { (index, offset) ->
                when {
                    index < threshold -> listState.scrollToItem(index + baseCount, offset)
                    index > total - baseCount - threshold -> listState.scrollToItem(index - baseCount, offset)
                }
            }
    }

    val haptics = LocalHapticFeedback.current
    var lastAnnounced by remember { mutableStateOf(selected) }

    LaunchedEffect(selected) {
        lastAnnounced = selected
    }

    LaunchedEffect(listState, viewportWidthPx, repeatedItems, enabled) {
        if (viewportWidthPx == 0 || repeatedItems.isEmpty()) return@LaunchedEffect
        snapshotFlow { listState.isScrollInProgress }
            .filter { !it }
            .collectLatest {
                if (!enabled) return@collectLatest
                val index = nearestCenterIndex(listState, viewportWidthPx)
                val effect = repeatedItems.getOrNull(index)?.id ?: return@collectLatest
                if (effect != selected) {
                    onSelected(effect)
                }
                if (effect != lastAnnounced) {
                    lastAnnounced = effect
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
            itemsIndexed(
                items = repeatedItems,
                key = { index, item -> "${item.id.name}-$index" }
            ) { index, item ->
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

private fun nearestMiddleIndex(
    currentIndex: Int,
    baseCount: Int,
    targetBaseIndex: Int
): Int? {
    if (baseCount == 0) return null
    val middleStart = baseCount
    val middleEnd = baseCount * 2 - 1
    var candidate = currentIndex
    val normalized = ((candidate % baseCount) + baseCount) % baseCount
    val delta = targetBaseIndex - normalized
    candidate += delta
    val total = baseCount * 3
    while (candidate < middleStart) candidate += baseCount
    while (candidate > middleEnd) candidate -= baseCount
    return candidate.coerceIn(0, total - 1)
}

private class ScaledFlingBehavior(
    private val delegate: FlingBehavior,
    private val frictionScale: Float
) : FlingBehavior {
    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        val scaled = initialVelocity * frictionScale
        return with(delegate) { this@performFling.performFling(scaled) }
    }
}
