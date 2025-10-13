package com.example.abys.ui.components

import androidx.compose.animation.animateContentSize
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.example.abys.ui.effects.EFFECTS
import com.example.abys.ui.effects.EffectSpec
import kotlinx.coroutines.launch

@Composable
fun EffectCarousel(
    collapsed: Boolean,
    onCollapsedChange: (Boolean) -> Unit,
    onDoubleTapApply: (EffectSpec) -> Unit
) {
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val alpha by animateFloatAsState(if (collapsed) 0.35f else 1f, label = "carAlpha")
    val height = if (collapsed) 72.dp else 132.dp

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
                itemsIndexed(EFFECTS) { idx, spec ->
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
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        onDoubleTapApply(spec)
                                        onCollapsedChange(true)
                                        scope.launch {
                                            // возвращаемся к выбранному элементу после сворачивания
                                            state.animateScrollToItem(idx)
                                        }
                                    }
                                )
                            }
                    ) {
                        Image(
                            painter = painterResource(id = spec.preview),
                            contentDescription = spec.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
