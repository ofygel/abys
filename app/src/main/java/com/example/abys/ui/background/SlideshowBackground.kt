package com.example.abys.ui.background

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.floor
import kotlin.random.Random

/**
 * Детерминированное слайд-шоу:
 * - visibleMs = 15_000 по умолчанию
 * - fadeInMs = 900, fadeOutMs = 500
 * - индекс кадра считается от (nowMs - ANCHOR_MS) / periodMs
 *   => при повторном запуске продолжает «с того места», где должен быть по времени.
 */
@Composable
fun SlideshowBackground(
    modifier: Modifier = Modifier,
    images: List<Int> = Slides.all,
    visibleMs: Long = 15_000L,
    fadeInMs: Long = 900L,
    fadeOutMs: Long = 500L,
    // фиксированная опорная точка (UTC). Можно поменять на «момент первого запуска», если захочешь.
    anchorMs: Long = 0L
) {
    val slides = remember(images) {
        images.takeIf { it.isNotEmpty() } ?: Slides.all
    }
    val n = slides.size.coerceAtLeast(1)
    val period = visibleMs + fadeInMs + fadeOutMs

    // Тикер времени (≈30 FPS). Нет «накопления» — просто читает системное время.
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(33L)
        }
    }

    // Сколько прошло с опорной точки
    val elapsed = (nowMs - anchorMs).mod(Long.MAX_VALUE)
    val slideFloat = elapsed.toDouble() / period.toDouble()
    val idx = floor(slideFloat).toLong().mod(n.toLong()).toInt()
    val within = (elapsed % period).toDouble()

    // Алфы для cross-fade:
    // - в начале периода: prev -> current (fade in)
    // - в конце периода: current -> next (fade out)
    val (prevIdx, currIdx, nextIdx) = Triple(
        ((idx - 1 + n) % n),
        idx,
        ((idx + 1) % n)
    )

    var alphaPrev = 0f
    var alphaCurr = 1f
    var alphaNext = 0f

    when {
        within < fadeInMs -> {
            // Входим в кадр
            val a = (within / fadeInMs).toFloat().coerceIn(0f, 1f)
            alphaPrev = 1f - a
            alphaCurr = a
        }
        within > (fadeInMs + visibleMs) -> {
            // Выходим из кадра
            val out = ((within - fadeInMs - visibleMs) / fadeOutMs).toFloat().coerceIn(0f, 1f)
            alphaCurr = 1f - out
            alphaNext = out
        }
        else -> {
            alphaPrev = 0f
            alphaCurr = 1f
            alphaNext = 0f
        }
    }

    val grayscaleFilter = remember {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    }

    Box(modifier.fillMaxSize()) {
        // Рисуем до трёх слоёв для аккуратного кросс-фейда
        val imageModifier = Modifier
            .fillMaxSize()
            .blur(2.dp)

        if (alphaPrev > 0f) {
            Image(
                painter = painterResource(id = slides[prevIdx]),
                contentDescription = null,
                modifier = imageModifier.graphicsLayer {
                    alpha = alphaPrev
                },
                colorFilter = grayscaleFilter,
                contentScale = ContentScale.Crop
            )
        }
        Image(
            painter = painterResource(id = slides[currIdx]),
            contentDescription = null,
            modifier = imageModifier.graphicsLayer {
                alpha = alphaCurr
            },
            colorFilter = grayscaleFilter,
            contentScale = ContentScale.Crop
        )
        if (alphaNext > 0f) {
            Image(
                painter = painterResource(id = slides[nextIdx]),
                contentDescription = null,
                modifier = imageModifier.graphicsLayer {
                    alpha = alphaNext
                },
                colorFilter = grayscaleFilter,
                contentScale = ContentScale.Crop
            )
        }

        // Лёгкий тёмный слой для читаемости текста
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0x80000000))
        )

        Canvas(Modifier.fillMaxSize()) {
            val rnd = Random(42)
            val step = 4.dp.toPx().coerceAtLeast(1f)
            var x = 0f
            while (x < size.width) {
                var y = 0f
                while (y < size.height) {
                    drawRect(
                        color = Color.White.copy(alpha = rnd.nextFloat() * 0.02f),
                        topLeft = Offset(x, y),
                        size = Size(step, step)
                    )
                    y += step
                }
                x += step
            }
        }
    }
}
