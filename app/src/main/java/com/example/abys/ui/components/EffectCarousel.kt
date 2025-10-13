package com.example.abys.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.abys.ui.effects.EFFECTS
import com.example.abys.ui.effects.EffectSpec
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.alpha

@Composable
fun EffectCarousel(
    collapsed: Boolean,
    onCollapsedChange: (Boolean) -> Unit,
    onDoubleTapApply: (EffectSpec) -> Unit
) {
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val alpha by animateFloatAsState(if (collapsed) 0.35f else 1f, label = "carAlpha")
    val height = if (collapsed) 72.dp else 120.dp

    Box(
        Modifier
            .fillMaxWidth()
            .height(height)
            .alpha(alpha)
            .animateContentSize()
            .pointerInput(collapsed) {
                detectTapGestures(
                    onTap = { if (collapsed) onCollapsedChange(false) },
                )
            }
    ) {
        if (!collapsed) {
            LazyRow(
                state = state,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                itemsIndexed(EFFECTS) { idx, spec ->
                    var itemCenter by remember { mutableStateOf(0f) }
                    var listCenter by remember { mutableStateOf(0f) }

                    val scale = remember(itemCenter, listCenter) {
                        val d = kotlin.math.abs(itemCenter - listCenter)
                        val s = 1f - (d / 500f).coerceIn(0f, 0.4f)
                        s
                    }

                    Box(
                        Modifier
                            .size(86.dp)
                            .onGloballyPositioned { coords ->
                                val parent = coords.parentLayoutCoordinates ?: return@onGloballyPositioned
                                val itemBounds = coords.boundsInRoot()
                                val listBounds = parent.boundsInRoot()
                                itemCenter = itemBounds.center.x
                                listCenter = listBounds.center.x
                            }
                            .scale(scale)
                            .clip(RoundedCornerShape(16.dp))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        onDoubleTapApply(spec)
                                        onCollapsedChange(true)
                                    }
                                )
                            }
                    ) {
                        Image(
                            painter = painterResource(id = spec.preview),
                            contentDescription = spec.title,
                            modifier = Modifier.fillMaxSize()
                        )
                        // тёмная подложка для читаемости можно добавить при желании
                    }
                }
            }
        }
    }
}
