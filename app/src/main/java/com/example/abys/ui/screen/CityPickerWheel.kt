package com.example.abys.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.rememberSplineBasedDecay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consume
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.abys.ui.theme.Dimens
import com.example.abys.ui.theme.Tokens
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private const val VISIBLE_AROUND = 4
private val BASE_STEP: Dp = 92.dp

@Composable
fun CityPickerWheel(
    cities:       List<String>,
    currentCity:  String,
    onChosen:     (String) -> Unit,
    modifier:     Modifier = Modifier
) {
    if (cities.isEmpty()) return

    val density = LocalDensity.current
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val s = Dimens.s()
    val scope = rememberCoroutineScope()
    val stepPx = with(density) { (BASE_STEP.value * sy).dp.toPx() }

    var centerIndex by remember(cities, currentCity) {
        mutableStateOf(cities.indexOf(currentCity).takeIf { it >= 0 } ?: 0)
    }
    val offsetY = remember { Animatable(0f) }
    val decay = rememberSplineBasedDecay<Float>()
    val velocityTracker = remember { VelocityTracker() }

    LaunchedEffect(currentCity, cities) {
        val idx = cities.indexOf(currentCity).takeIf { it >= 0 } ?: 0
        centerIndex = idx
        offsetY.snapTo(0f)
    }

    suspend fun accumulate(delta: Float) {
        val newOffset = offsetY.value + delta
        val steps = (newOffset / stepPx).toInt()
        if (steps != 0) {
            centerIndex = (centerIndex - steps).floorMod(cities.size)
        }
        offsetY.snapTo(newOffset - steps * stepPx)
    }

    suspend fun snap() {
        val deltaSteps = (offsetY.value / stepPx).roundToInt()
        if (deltaSteps != 0) {
            centerIndex = (centerIndex - deltaSteps).floorMod(cities.size)
        }
        offsetY.animateTo(0f)
        onChosen(cities[centerIndex])
    }

    fun settle(velocity: Float) {
        scope.launch {
            offsetY.stop()
            if (abs(velocity) > 50f) {
                var lastValue = 0f
                AnimationState(initialValue = 0f, initialVelocity = velocity).animateDecay(decay) {
                    val delta = value - lastValue
                    lastValue = value
                    accumulate(delta)
                }
            }
            snap()
        }
    }

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .clipToBounds()
            .pointerInput(cities, centerIndex) {
                detectVerticalDragGestures(
                    onDragStart = {
                        velocityTracker.reset()
                        scope.launch { offsetY.stop() }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        accumulate(dragAmount)
                    },
                    onDragEnd = {
                        val velocity = velocityTracker.calculateVelocity().y
                        settle(velocity)
                    },
                    onDragCancel = { settle(0f) }
                )
            }
            .drawWithContent {
                drawContent()
                if (size.height <= 0f) return@drawWithContent
                val fadeHeight = with(density) { (160f * sy).dp.toPx() }
                val fraction = (fadeHeight / size.height).coerceIn(0f, 0.49f)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White,
                            Color.White,
                            Color.Transparent
                        ),
                        stops = listOf(0f, fraction, 1f - fraction, 1f)
                    ),
                    size = size,
                    blendMode = BlendMode.DstIn
                )
            }
    ) {
        val centerY = constraints.maxHeight / 2f

        Canvas(Modifier.fillMaxSize()) {
            val y = size.height / 2f
            val stroke = 6.dp.toPx()
            val leftStart = Offset(with(density) { (129f * sx).dp.toPx() }, y)
            val leftEnd = Offset(with(density) { (239f * sx).dp.toPx() }, y)
            val rightStart = Offset(with(density) { (403f * sx).dp.toPx() }, y)
            val rightEnd = Offset(with(density) { (513f * sx).dp.toPx() }, y)

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
            val pxSize = (42f * scaleFactor).coerceIn(22f, 42f)
            val textSize = (pxSize * s).sp
            val alpha = exp(-(distance / 1.15f).pow(2)).toFloat().coerceIn(0.2f, 1f)

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
                    color = Tokens.Colors.text,
                    lineHeight = (if (distance == 0) 1.10f else 1.05f).em,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.35f),
                        offset = Offset(0f, 2f),
                        blurRadius = 6f
                    )
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
