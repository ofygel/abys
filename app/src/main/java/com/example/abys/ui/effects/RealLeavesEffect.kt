package com.example.abys.ui.effects

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

private data class Leaf(
    var x: Float,
    var y: Float,
    var vx: Float,         // собственная горизонтальная скорость
    var vy: Float,         // собственная вертикальная скорость
    var angle: Float,      // текущий угол
    var angVel: Float,     // угловая скорость (в т.ч. «флаттер»)
    var scale: Float,      // масштаб (задаёт «глубину»)
    var swayPhase: Float,  // фаза колебаний
    val sprite: Int        // индекс спрайта
)

/**
 * Реалистичный листопад:
 *  - спрайты листьев (если положите leaf1/leaf2/leaf3.png в drawable[-nodpi])
 *  - редкий и хаотичный поток
 *  - ветер с «порывами», лёгкий флаттер и параллакс
 */
@Composable
fun LeavesEffect(
    modifier: Modifier = Modifier,
    density: Float = 0.06f     // чем меньше, тем реже листья
) {
    val ctx = LocalContext.current

    // Пытаемся загрузить 3 спрайта по именам (без compile-time ссылок на R)
    val sprites by remember {
        mutableStateOf(loadLeafSprites(ctx, listOf("leaf1", "leaf2", "leaf3")))
    }

    var leaves by remember { mutableStateOf(emptyList<Leaf>()) }
    var w by remember { mutableStateOf(0f) }
    var h by remember { mutableStateOf(0f) }

    // Глобальные параметры ветра / порывов
    var t by remember { mutableStateOf(0f) }           // время
    var gust by remember { mutableStateOf(0f) }        // мгновенный порыв
    var nextGust in remember { mutableStateOf(60) }    // тики до следующего порыва

    Canvas(modifier) {
        if (w != size.width || h != size.height || leaves.isEmpty()) {
            w = size.width; h = size.height
            val area = w * h
            val target = (area / 26000f * density).toInt().coerceIn(12, 48) // редкий поток
            leaves = List(target) {
                val rnd = Random(it + System.nanoTime().toInt())
                val z = rnd.nextFloat()         // «глубина» 0..1
                val sc = 0.6f + z * 0.9f        // 0.6..1.5
                Leaf(
                    x = rnd.nextFloat() * w,
                    y = rnd.nextFloat() * h,
                    vx = (rnd.nextFloat() - 0.5f) * 0.4f,          // базовый разброс
                    vy = 0.7f + z * 1.0f + rnd.nextFloat() * 0.4f, // чем «ближе», тем быстрее
                    angle = rnd.nextFloat() * 360f,
                    angVel = (rnd.nextFloat() - 0.5f) * 1.4f,      // град/кадр
                    scale = sc,
                    swayPhase = rnd.nextFloat() * 6.28f,
                    sprite = rnd.nextInt(maxOf(1, sprites.size))
                )
            }
        }

        // Рисуем
        leaves.forEach { leaf ->
            val bmp = sprites.getOrNull(leaf.sprite)
            val sz = if (bmp != null) {
                // реальный размер с учётом масштаба
                IntSize(
                    (bmp.width * leaf.scale).toInt().coerceAtLeast(6),
                    (bmp.height * leaf.scale).toInt().coerceAtLeast(6)
                )
            } else {
                IntSize((18 * leaf.scale).toInt(), (12 * leaf.scale).toInt())
            }

            // Поворачиваем вокруг центра и отрисовываем
            rotate(degrees = leaf.angle, pivot = Offset(leaf.x, leaf.y)) {
                if (bmp != null) {
                    // Рисуем спрайт как картинку
                    drawImage(
                        image = bmp,
                        topLeft = Offset(leaf.x - sz.width / 2f, leaf.y - sz.height / 2f),
                        dstSize = sz
                    )
                } else {
                    // Фолбэк: вытянутый «лист»
                    drawRoundRect(
                        color = Color(0xFFE1B567),
                        topLeft = Offset(leaf.x - sz.width / 2f, leaf.y - sz.height / 2f),
                        size = androidx.compose.ui.geometry.Size(sz.width.toFloat(), sz.height.toFloat()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius( sz.height / 2f, sz.height / 2f)
                    )
                }
            }
        }
    }

    // Физика (16мс шаг, лёгкий sin-ветер + случайные порывы)
    LaunchedEffect(w, h) {
        val rnd = Random(System.currentTimeMillis())
        while (true) {
            t += 0.016f

            // Базовый ветер: сумма двух медленных синусоид
            val baseWind = (sin(t * 0.35f) * 0.6f) + (sin(t * 0.17f + 1.3f) * 0.8f)

            // Порывы
            nextGust--
            if (nextGust <= 0) {
                gust = rnd.nextFloat() * 3.5f * if (rnd.nextBoolean()) 1f else -1f
                nextGust = rnd.nextInt(180, 480) // следующий через 3–8 секунд
            }
            // Плавное затухание порыва
            gust *= 0.98f

            val windX = baseWind + gust

            leaves = leaves.map { p ->
                val sway = sin(t * (0.8f + p.scale * 0.2f) + p.swayPhase) * (0.7f + p.scale * 0.3f)
                var x = p.x + p.vx + windX * (0.8f + p.scale * 0.6f) + sway * 0.5f
                var y = p.y + p.vy
                var ang = p.angle + p.angVel + sway * 0.3f

                // респавн сверху/с боков, когда лист вышел за границы
                if (y > h + 24f || x < -32f || x > w + 32f) {
                    x = rnd.nextFloat() * w
                    y = -16f
                    ang = rnd.nextFloat() * 360f
                }
                p.copy(x = x, y = y, angle = ang)
            }

            delay(16L)
        }
    }
}

private fun loadLeafSprites(
    ctx: android.content.Context,
    names: List<String>
): List<ImageBitmap> {
    val res = ctx.resources
    val pkg = ctx.packageName
    val bitmaps = mutableListOf<ImageBitmap>()
    names.forEach { n ->
        val id = res.getIdentifier(n, "drawable", pkg)
        if (id != 0) {
            BitmapFactory.decodeResource(res, id)?.let { bitmaps.add(it.asImageBitmap()) }
        }
    }
    return bitmaps
}
