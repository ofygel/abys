package com.example.abys.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.example.abys.R
import com.example.abys.ui.effects.ThemeSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EffectCarousel(
    themes: List<ThemeSpec>,
    selectedThemeId: String,
    collapsed: Boolean,
    onCollapsedChange: (Boolean) -> Unit,
    onThemeSnapped: (ThemeSpec) -> Unit,
    onDoubleTapApply: (ThemeSpec) -> Unit,
    onTapCenterApply: (ThemeSpec) -> Unit
) {
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    val targetHeight = if (collapsed) 96.dp else 156.dp
    val height by animateDpAsState(targetValue = targetHeight, label = "carHeight")
    val updatedOnSnapped by rememberUpdatedState(onThemeSnapped)
    val updatedOnDoubleTap by rememberUpdatedState(onDoubleTapApply)
    val updatedOnTapApply by rememberUpdatedState(onTapCenterApply)

    if (themes.isEmpty()) {
        return
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        if (collapsed) {
            val selectedTheme = remember(selectedThemeId, themes) {
                themes.firstOrNull { it.id == selectedThemeId } ?: themes.first()
            }
            val expandDescription = stringResource(id = R.string.theme_carousel_expand)

            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth()
                    .height(64.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = expandDescription
                    },
                shape = RoundedCornerShape(28.dp),
                color = Color.Black.copy(alpha = 0.58f),
                tonalElevation = 2.dp,
                onClick = { onCollapsedChange(false) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    width = 2.dp,
                                    brush = Brush.verticalGradient(
                                        listOf(Color.White.copy(alpha = 0.38f), Color.White.copy(alpha = 0.14f))
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .background(Color.Black.copy(alpha = 0.35f))
                        ) {
                            Image(
                                painter = painterResource(id = selectedTheme.thumbRes),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                text = stringResource(id = R.string.theme_carousel_handle),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(id = selectedTheme.titleRes),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                    Text(
                        text = "\u02C4",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        } else {
            var snappedIndex by remember { mutableStateOf(0) }
            var showHint by rememberSaveable { mutableStateOf(true) }

            fun applyAndCollapse(spec: ThemeSpec, callback: (ThemeSpec) -> Unit) {
                showHint = false
                callback(spec)
                onCollapsedChange(true)
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }

            val viewportCenter by remember {
                derivedStateOf {
                    val layoutInfo = state.layoutInfo
                    (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
                }
            }

            CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                LazyRow(
                    state = state,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(horizontal = 32.dp)
                ) {
                    itemsIndexed(
                        items = themes,
                        key = { _, spec -> spec.id }
                    ) { idx, spec ->
                        val itemInfo by remember {
                            derivedStateOf {
                                state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == idx }
                            }
                        }
                        val scale by remember {
                            derivedStateOf {
                                val info = itemInfo
                                val center = viewportCenter
                                if (info == null || center <= 0f) {
                                    0.92f
                                } else {
                                    val itemCenter = info.offset + info.size / 2f
                                    val distancePx = abs(itemCenter - center)
                                    val influence = 1f - (distancePx / (center * 1.2f)).coerceIn(0f, 1f)
                                    0.92f + influence * 0.48f
                                }
                            }
                        }
                        val borderWidth by animateDpAsState(
                            targetValue = if (scale > 1.18f) 3.dp else 1.dp,
                            label = "borderWidth"
                        )
                        val isCentered by remember {
                            derivedStateOf {
                                val info = itemInfo
                                val center = viewportCenter
                                if (info == null) {
                                    false
                                } else {
                                    val itemCenter = info.offset + info.size / 2f
                                    abs(itemCenter - center) < info.size * 0.25f
                                }
                            }
                        }
                        val title = stringResource(id = spec.titleRes)
                        val talkbackDescription = stringResource(
                            id = R.string.theme_apply_talkback,
                            title
                        )
                        val applyActionLabel = stringResource(
                            id = R.string.theme_apply_action,
                            title
                        )
                        val doubleTapLabel = stringResource(
                            id = R.string.theme_apply_double_tap,
                            title
                        )

                        Box(
                            Modifier
                                .size(96.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    shadowElevation = if (scale > 1.15f) 14f else 0f
                                }
                                .clip(RoundedCornerShape(20.dp))
                                .border(
                                    width = borderWidth,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x66FFFFFF),
                                            Color(0x22FFFFFF)
                                        )
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .background(Color.Black.copy(alpha = 0.25f))
                                .pointerInput(idx, isCentered, viewportCenter, itemInfo?.offset ?: 0, itemInfo?.size ?: 0) {
                                    detectTapGestures(
                                        onTap = {
                                            if (!isCentered) {
                                                scope.launch {
                                                    itemInfo?.let { info ->
                                                        val desiredOffset = (viewportCenter - info.size / 2f).roundToInt()
                                                        state.animateScrollToItem(idx, scrollOffset = desiredOffset)
                                                    }
                                                }
                                            } else {
                                                applyAndCollapse(spec, updatedOnTapApply)
                                            }
                                        },
                                        onDoubleTap = {
                                            if (state.isScrollInProgress) {
                                                return@detectTapGestures
                                            }
                                            scope.launch {
                                                val info = itemInfo
                                                val desiredOffset = if (info != null && viewportCenter > 0f) {
                                                    (viewportCenter - info.size / 2f).roundToInt()
                                                } else {
                                                    0
                                                }
                                                state.animateScrollToItem(idx, scrollOffset = desiredOffset)
                                                applyAndCollapse(spec, updatedOnDoubleTap)
                                            }
                                        }
                                    )
                                }
                                .semantics(mergeDescendants = true) {
                                    contentDescription = talkbackDescription
                                    onClick(label = applyActionLabel) {
                                        if (!isCentered) return@onClick false
                                        applyAndCollapse(spec, updatedOnTapApply)
                                        true
                                    }
                                    customActions = listOf(
                                        CustomAccessibilityAction(
                                            label = doubleTapLabel
                                        ) {
                                            if (state.isScrollInProgress) return@CustomAccessibilityAction false
                                            scope.launch {
                                                val info = itemInfo
                                                val desiredOffset = if (info != null && viewportCenter > 0f) {
                                                    (viewportCenter - info.size / 2f).roundToInt()
                                                } else {
                                                    0
                                                }
                                                state.animateScrollToItem(idx, scrollOffset = desiredOffset)
                                                applyAndCollapse(spec, updatedOnDoubleTap)
                                            }
                                            true
                                        }
                                    )
                                    this.role = Role.Button
                                }
                        ) {
                            Image(
                                painter = painterResource(id = spec.thumbRes),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (spec.id == selectedThemeId) {
                                Box(
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(22.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xCC1B5E20)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "\u2713",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontSize = 14.sp
                                    )
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
                                    text = title,
                                    modifier = Modifier.align(Alignment.Center),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                updatedOnSnapped(themes.getOrNull(snappedIndex) ?: themes.first())
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
                snapshotFlow { state.isScrollInProgress }
                    .collectLatest { scrolling ->
                        if (!scrolling) {
                            delay(450)
                            val layoutInfo = state.layoutInfo
                            val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
                            if (center <= 0f) return@collectLatest
                            val currentLayoutInfo = state.layoutInfo
                            val visible = currentLayoutInfo.visibleItemsInfo
                            val closest = visible.minByOrNull { info ->
                                val itemCenterPx = info.offset + info.size / 2f
                                abs(itemCenterPx - center)
                            }
                            if (closest != null && closest.index != snappedIndex) {
                                snappedIndex = closest.index
                                updatedOnSnapped(themes[closest.index])
                            }
                            closest?.let { info ->
                                val itemCenterPx = info.offset + info.size / 2f
                                val delta = itemCenterPx - center
                                if (abs(delta) > 4f) {
                                    val desiredOffset = (center - info.size / 2f).roundToInt()
                                    scope.launch {
                                        state.animateScrollToItem(info.index, scrollOffset = desiredOffset)
                                    }
                                }
                            }
                        }
                    }
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

            val collapseDescription = stringResource(id = R.string.theme_carousel_collapse)

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = collapseDescription
                    },
                shape = RoundedCornerShape(18.dp),
                color = Color.Black.copy(alpha = 0.42f),
                tonalElevation = 1.dp,
                onClick = { onCollapsedChange(true) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.theme_carousel_handle),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                    Text(
                        text = "\u02C5",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            LaunchedEffect(collapsed) {
                if (!collapsed) {
                    showHint = true
                    delay(3200)
                    showHint = false
                } else {
                    showHint = false
                }
            }
        }
    }
}
