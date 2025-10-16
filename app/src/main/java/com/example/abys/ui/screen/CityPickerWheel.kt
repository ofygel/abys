package com.example.abys.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicText
import com.example.abys.ui.theme.Tokens
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private const val VISIBLE_AROUND = 6
private val STEP: Dp = 92.dp

@Composable
fun CityPickerWheel(
    cities:       List<String>,
    currentCity:  String,
    onChosen:     (String) -> Unit,
    modifier:     Modifier = Modifier
) {
    if (cities.isEmpty()) return

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val stepPx = with(density) { STEP.toPx() }

    var centerIndex by remember(cities, currentCity) {
        mutableStateOf(cities.indexOf(currentCity).takeIf { it >= 0 } ?: 0)
    }
    val offsetY = remember { Animatable(0f) }

    LaunchedEffect(currentCity, cities) {
        val idx = cities.indexOf(currentCity).takeIf { it >= 0 } ?: 0
        centerIndex = idx
        offsetY.snapTo(0f)
    }

    fun snap() {
        scope.launch {
            val deltaSteps = (offsetY.value / stepPx).roundToInt()
            if (deltaSteps != 0) {
                centerIndex = (centerIndex - deltaSteps).floorMod(cities.size)
            }
            offsetY.animateTo(0f)
            onChosen(cities[centerIndex])
        }
    }

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .clipToBounds()
            .pointerInput(cities, centerIndex) {
                detectVerticalDragGestures(
                    onDragEnd = { snap() },
                    onDragCancel = { snap() }
                ) { _, dragAmount ->
                    offsetY.snapTo(offsetY.value + dragAmount)
                }
            }
    ) {
        val centerY = constraints.maxHeight / 2f

        Canvas(Modifier.fillMaxSize()) {
            val y = size.height / 2f
            val stroke = 6.dp.toPx()
            val leftStart = Offset(129.dp.toPx(), y)
            val leftEnd = Offset(239.dp.toPx(), y)
            val rightStart = Offset(403.dp.toPx(), y)
            val rightEnd = Offset(513.dp.toPx(), y)

            drawLine(
                color = Tokens.Colors.tickDark,
                start = leftStart,
                end = leftEnd,
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Tokens.Colors.tickDark,
                start = rightStart,
                end = rightEnd,
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }

        val range = -VISIBLE_AROUND..VISIBLE_AROUND
        for (relative in range) {
            val actualIndex = (centerIndex + relative).floorMod(cities.size)
            val distance = abs(relative)
            val name = cities[actualIndex]

            val scaleFactor = 0.60f + 0.40f * exp(-(distance / 1.2f).pow(2))
            val textSize = (42f * scaleFactor).sp
            val alpha = exp(-(distance / 1.15f).pow(2)).toFloat()

            val yPosition = centerY + relative * stepPx + offsetY.value
            val heightPx = with(density) { textSize.toPx() }
            val yOffset = (yPosition - heightPx / 2f).roundToInt()

            BasicText(
                text = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { this.alpha = alpha }
                    .offset { IntOffset(0, yOffset) },
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = textSize,
                    fontWeight = if (distance == 0) FontWeight.ExtraBold else FontWeight.Bold,
                    color = Tokens.Colors.text
                ),
                maxLines = 1
            )
        }
    }
}

private fun Int.floorMod(mod: Int): Int {
    if (mod == 0) return 0
    val r = this % mod
    return if (r >= 0) r else r + mod
}
