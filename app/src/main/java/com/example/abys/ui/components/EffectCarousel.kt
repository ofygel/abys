package com.example.abys.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.abys.R
import com.example.abys.ui.effects.ThemeSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.snapshotFlow

@Composable
fun EffectCarousel(
    themes: List<ThemeSpec>,
    selectedThemeId: String,
    collapsed: Boolean,
    onCollapsedChange: (Boolean) -> Unit,
    onThemeSnapped: (ThemeSpec) -> Unit,
    onDoubleTapApply: (ThemeSpec) -> Unit
) {
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val alpha by animateFloatAsState(if (collapsed) 0.35f else 1f, label = "carAlpha")
    val targetHeight = if (collapsed) 72.dp else 132.dp
    val height by animateDpAsState(targetHeight, label = "carHeight")
    val updatedOnSnapped by rememberUpdatedState(onThemeSnapped)

    Box(
        Modifier
            .fillMaxWidth()
            .height(height)
            .alpha(alpha)
            .animateContentSize()
            .pointerInput(collapsed) {
                detectTapGestures(onTap = { if (collapsed) onCollapsedChange(false) })
            }
    ) {
        if (collapsed) {
            // маленькая «спящая» версия
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x44000000))
            )
        } else {
            var listCenter by remember { mutableStateOf(0f) }
            var snappedIndex by remember { mutableStateOf(0) }
            var showHint by remember { mutableStateOf(true) }

            LazyRow(
                state = state,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp)
                    .onGloballyPositioned { coords ->
                        listCenter = coords.size.width / 2f
                    },
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(horizontal = 32.dp)
            ) {
                itemsIndexed(themes) { idx, spec ->
                    var itemCenter by remember { mutableStateOf(0f) }

                    val scale = remember(itemCenter, listCenter) {
                        if (listCenter <= 0f) 0.92f else {
                            val distancePx = kotlin.math.abs(itemCenter - listCenter)
                            val influence = 1f - (distancePx / (listCenter * 1.2f)).coerceIn(0f, 1f)
                            0.92f + influence * 0.48f
                        }
                    }

                    Box(
                        Modifier
                            .size(96.dp)
                            .onGloballyPositioned { coords ->
                                val width = coords.size.width
                                itemCenter = coords.positionInParent().x + width / 2f
                            }
                            .scale(scale)
                            .clip(RoundedCornerShape(20.dp))
                            .border(
                                width = if (scale > 1.25f) 3.dp else 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x55FFFFFF),
                                        Color(0x11FFFFFF)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .background(Color.Black.copy(alpha = 0.25f))
                            .pointerInput(showHint) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        onDoubleTapApply(spec)
                                        onCollapsedChange(true)
                                        showHint = false
                                        scope.launch {
                                            // возвращаемся к выбранному элементу после сворачивания
                                            state.animateScrollToItem(idx)
                                        }
                                    }
                                )
                            }
                    ) {
                        Image(
                            painter = painterResource(id = spec.thumbRes),
                            contentDescription = stringResource(id = spec.titleRes),
                            modifier = Modifier.fillMaxSize()
                        )
                        if (spec.id == selectedThemeId) {
                            Box(
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xAA1B5E20)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓", color = Color.White, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Box(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.35f))
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(id = spec.titleRes),
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                snapshotFlow { state.isScrollInProgress }
                    .collectLatest { scrolling ->
                        if (!scrolling) {
                            delay(450)
                            val layoutInfo = state.layoutInfo
                            val center = listCenter.takeIf { it > 0f } ?: return@collectLatest
                            val visible = layoutInfo.visibleItemsInfo
                            val closest = visible.minByOrNull { info ->
                                val itemCenterPx = info.offset + info.size / 2f
                                kotlin.math.abs(itemCenterPx - center)
                            }
                            if (closest != null && closest.index != snappedIndex) {
                                snappedIndex = closest.index
                                updatedOnSnapped(themes[closest.index])
                            }
                            closest?.let { info ->
                                val itemCenterPx = info.offset + info.size / 2f
                                val delta = itemCenterPx - center
                                if (kotlin.math.abs(delta) > 4f) {
                                    scope.launch {
                                        state.animateScrollBy(delta)
                                    }
                                }
                            }
                        }
                    }
            }

            LaunchedEffect(selectedThemeId, collapsed) {
                if (!collapsed) {
                    val index = themes.indexOfFirst { it.id == selectedThemeId }.takeIf { it >= 0 } ?: 0
                    snappedIndex = index
                    state.scrollToItem(index)
                    updatedOnSnapped(themes[index])
                }
            }

            LaunchedEffect(Unit) {
                updatedOnSnapped(themes.getOrNull(snappedIndex) ?: themes.first())
            }

            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp),
                visible = showHint
            ) {
                Surface(
                    tonalElevation = 2.dp,
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = stringResource(id = R.string.theme_apply_hint),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }

            LaunchedEffect(collapsed) {
                if (!collapsed) {
                    delay(3200)
                    showHint = false
                }
            }
        }
    }
}
