package com.example.abys.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.math.roundToInt
import kotlin.random.Random

private val leafRandom = Random(System.currentTimeMillis())

private class Leaf {
    var x: Float = 0f
    var y: Float = 0f
    var size: Float = 0f
    var bend: Float = 0f
    var phase: Float = 0f
    var rotation: Float = 0f

    var baseVx: Float = 0f
    var baseVy: Float = 0f
    var baseSway: Float = 0f
    var baseSpinSpeed: Float = 0f
    var basePhaseSpeed: Float = 0f

    var bodyColor: Color = Color.Transparent
    var veinColorMain: Color = Color.Transparent
    var veinColorBranch: Color = Color.Transparent

    val path: Path = Path()
}

private data class LeafLayer(
    val sizeMultiplier: Float,
    val speedMultiplier: Float,
    val swayMultiplier: Float,
    val alphaMultiplier: Float
)

private val leafLayers = arrayOf(
    LeafLayer(sizeMultiplier = 1.25f, speedMultiplier = 1.3f, swayMultiplier = 1.2f, alphaMultiplier = 1f),
    LeafLayer(sizeMultiplier = 1f, speedMultiplier = 1f, swayMultiplier = 1f, alphaMultiplier = 0.85f),
    LeafLayer(sizeMultiplier = 0.8f, speedMultiplier = 0.75f, swayMultiplier = 0.7f, alphaMultiplier = 0.7f)
)

private const val TAU = 6.2831855f
private const val FRAME_DELAY_MS = 16L
private const val FRAME_DT = 1f / 60f
private const val BASELINE_AREA = 1080f * 1920f
private const val DEFAULT_DENSITY = 0.12f
private const val MIN_LEAF_COUNT = 12f
private const val MAX_LEAF_COUNT = 80f

@Composable
fun LeavesEffect(
    modifier: Modifier = Modifier,
    params: LeavesParams,
    intensity: Float
) {
    val leaves = remember { mutableStateListOf<Leaf>() }
    var canvasWidth by remember { mutableStateOf(0f) }
    var canvasHeight by remember { mutableStateOf(0f) }
    var frameTick by remember { mutableStateOf(0) }
    var currentIntensity by remember { mutableStateOf(intensity) }
    var previousParams by remember { mutableStateOf(params) }

    LaunchedEffect(intensity) {
        currentIntensity = intensity.coerceIn(0f, 1f)
    }

    LaunchedEffect(canvasWidth, canvasHeight, params, currentIntensity) {
        if (canvasWidth == 0f || canvasHeight == 0f) return@LaunchedEffect
        val desiredCount = computeLeafCount(canvasWidth, canvasHeight, params, currentIntensity)
        adjustLeafCount(leaves, desiredCount, canvasWidth, canvasHeight, params)

        if (previousParams != params) {
            leaves.forEach { leaf ->
                leaf.reset(canvasWidth, canvasHeight, params)
            }
            previousParams = params
        }
    }

    LaunchedEffect(canvasWidth, canvasHeight, params, currentIntensity) {
        var windPhase = leafRandom.nextFloat() * TAU
        var gustTimer = 0f
        var gustDuration = randomRange(0.8f, 1.2f)
        var gustStrength = 0f
        var gustTarget = 0f
        val windPeriod = randomRange(6f, 10f)

        while (true) {
            if (canvasWidth == 0f || canvasHeight == 0f || leaves.isEmpty()) {
                delay(FRAME_DELAY_MS)
                continue
            }

            val speedScale = 0.7f + 0.6f * currentIntensity
            val swayScale = 0.8f + 0.5f * currentIntensity
            val driftScale = 0.6f + 0.5f * currentIntensity
            val spinScale = 0.5f + 0.7f * currentIntensity

            windPhase += FRAME_DT * TAU / windPeriod
            if (windPhase > TAU) windPhase -= TAU

            gustTimer += FRAME_DT
            if (gustTimer >= gustDuration) {
                gustTimer = 0f
                gustDuration = randomRange(0.8f, 1.2f)
                gustTarget = randomRange(-1f, 1f) * params.driftX * 120f
            }
            gustStrength += (gustTarget - gustStrength) * 0.08f

            val baseWind = sin(windPhase) * params.driftX * 42f
            val windX = (baseWind + gustStrength) * (0.7f + 0.6f * currentIntensity)

            leaves.forEach { leaf ->
                val vx = leaf.baseVx * driftScale
                val vy = leaf.baseVy * speedScale
                val swayVelocity = leaf.baseSway * swayScale
                val spinSpeed = leaf.baseSpinSpeed * spinScale

                leaf.phase += leaf.basePhaseSpeed * FRAME_DT
                if (leaf.phase > TAU) leaf.phase -= TAU

                val swayOffset = sin(leaf.phase) * swayVelocity

                leaf.x += (vx + swayOffset + windX) * FRAME_DT
                leaf.y += vy * FRAME_DT
                leaf.rotation += spinSpeed * FRAME_DT

                if (leaf.rotation > 360f) {
                    leaf.rotation -= 360f
                } else if (leaf.rotation < -360f) {
                    leaf.rotation += 360f
                }

                if (leaf.y > canvasHeight + 40f || leaf.x < -40f || leaf.x > canvasWidth + 40f) {
                    leaf.reset(canvasWidth, canvasHeight, params, spawnFromTop = true)
                }
            }

            frameTick++
            delay(FRAME_DELAY_MS)
        }
    }

    Canvas(modifier) {
        if (size.width == 0f || size.height == 0f) {
            canvasWidth = 0f
            canvasHeight = 0f
            return@Canvas
        }

        if (canvasWidth != size.width || canvasHeight != size.height) {
            canvasWidth = size.width
            canvasHeight = size.height
        }

        // читать frameTick, чтобы Canvas перерисовывался
        frameTick

        leaves.forEach { leaf ->
            withTransform({
                translate(leaf.x, leaf.y)
                rotate(leaf.rotation)
            }) {
                drawPath(path = leaf.path, color = leaf.bodyColor)
                drawLine(
                    color = leaf.veinColorMain,
                    start = Offset(0f, -leaf.size * 1.2f),
                    end = Offset(0f, leaf.size * 1.1f),
                    strokeWidth = leaf.size * 0.12f
                )
                val branch = leaf.size * 0.7f
                val branchStroke = leaf.size * 0.08f
                drawLine(
                    color = leaf.veinColorBranch,
                    start = Offset(0f, -leaf.size * 0.2f),
                    end = Offset(branch, leaf.size * 0.3f),
                    strokeWidth = branchStroke
                )
                drawLine(
                    color = leaf.veinColorBranch,
                    start = Offset(0f, leaf.size * 0.2f),
                    end = Offset(-branch, leaf.size * 0.5f),
                    strokeWidth = branchStroke
                )
            }
        }
    }
}

private fun computeLeafCount(
    width: Float,
    height: Float,
    params: LeavesParams,
    intensity: Float
): Int {
    val areaScale = (width * height) / BASELINE_AREA
    val densityScale = (params.density.coerceAtLeast(0.01f) / DEFAULT_DENSITY).coerceAtLeast(0.2f)
    val minCount = (MIN_LEAF_COUNT * densityScale).coerceAtLeast(4f)
    val maxCount = (MAX_LEAF_COUNT * densityScale)
    val baseCount = lerp(minCount, maxCount, intensity.coerceIn(0f, 1f))
    val scaled = (baseCount * areaScale).roundToInt()
    val scaledMax = (maxCount * areaScale).roundToInt().coerceAtLeast(4)
    return scaled.coerceIn(4, scaledMax)
}

private fun adjustLeafCount(
    leaves: MutableList<Leaf>,
    desiredCount: Int,
    width: Float,
    height: Float,
    params: LeavesParams
) {
    if (leaves.size > desiredCount) {
        while (leaves.size > desiredCount) {
            leaves.removeAt(leaves.lastIndex)
        }
    } else if (leaves.size < desiredCount) {
        repeat(desiredCount - leaves.size) {
            leaves += Leaf().apply {
                reset(width, height, params)
            }
        }
    }
}

private fun Leaf.reset(
    width: Float,
    height: Float,
    params: LeavesParams,
    spawnFromTop: Boolean = false
) {
    val layerIndex = leafRandom.nextInt(leafLayers.size)
    val layer = leafLayers[layerIndex]

    val baseSize = 6f + leafRandom.nextFloat() * 9f
    size = baseSize * layer.sizeMultiplier
    bend = (leafRandom.nextFloat() - 0.5f) * 1.2f
    path.configureLeaf(size, bend)

    val hue = 30f + leafRandom.nextFloat() * 25f
    val saturation = 0.65f + leafRandom.nextFloat() * 0.25f
    val value = 0.7f + leafRandom.nextFloat() * 0.2f
    val baseColor = Color.hsv(hue, saturation, value)
    val veinBase = Color.hsv(hue, saturation * 0.5f, (value * 0.8f).coerceAtMost(1f))
    bodyColor = baseColor.copy(alpha = baseColor.alpha * layer.alphaMultiplier)
    veinColorMain = veinBase.copy(alpha = veinBase.alpha * layer.alphaMultiplier)
    veinColorBranch = veinColorMain.copy(alpha = veinColorMain.alpha * 0.75f)

    x = leafRandom.nextFloat() * width
    y = if (spawnFromTop) -leafRandom.nextFloat() * height * 0.3f - 40f else leafRandom.nextFloat() * height

    baseVx = (leafRandom.nextFloat() - 0.5f) * params.driftX * 1.2f * 60f * layer.speedMultiplier
    baseVy = ((params.speedY * 0.8f) + leafRandom.nextFloat() * params.speedY) * 60f * layer.speedMultiplier
    baseSway = ((0.15f + leafRandom.nextFloat() * 0.35f) * size) * 60f * layer.swayMultiplier
    baseSpinSpeed = ((leafRandom.nextFloat() - 0.5f) * 1.2f) * 60f
    basePhaseSpeed = (1.5f + leafRandom.nextFloat() * 2.2f)

    phase = leafRandom.nextFloat() * TAU
    rotation = leafRandom.nextFloat() * 360f
}

private fun Path.configureLeaf(size: Float, bend: Float) {
    reset()
    val halfLength = size * 1.4f
    val halfWidth = size * 0.6f
    val bendOffset = bend * size * 0.5f
    moveTo(0f, -halfLength)
    cubicTo(
        halfWidth,
        -halfLength * 0.25f,
        halfWidth + bendOffset,
        halfLength * 0.2f,
        0f,
        halfLength
    )
    cubicTo(
        -halfWidth + bendOffset,
        halfLength * 0.2f,
        -halfWidth,
        -halfLength * 0.25f,
        0f,
        -halfLength
    )
    close()
}

private fun randomRange(start: Float, end: Float): Float {
    return start + leafRandom.nextFloat() * (end - start)
}

private fun lerp(start: Float, stop: Float, amount: Float): Float {
    return start + (stop - start) * amount
}
