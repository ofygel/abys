package com.example.abys.ui.effects

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.imageResource
import kotlinx.coroutines.isActive
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random


// ---------- Параметры и модель ----------

private data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var depth: Float,     // 0.0 (близко) .. 1.0 (даль)
    var rot: Float,
    var rotSpeed: Float,
    var flutterPhase: Float,
    var flutterFreq: Float,
    var scale: Float,     // базовый масштаб спрайта
    var alpha: Float,
    var spriteIndex: Int  // индекс в массиве спрайтов
)

private data class SpriteSet(
    val sprites: List<ImageBitmap>,      // leaf_*.png / snow_*.png / petal_*.png
    val fallbackShape: FallbackShape     // если спрайтов нет — рисуем простую форму
)

private enum class FallbackShape { LEAF, SNOW, PETAL, NONE }

// ---------- Публичный API компонента ----------

@Composable
fun SeasonalParticles(
    season: Season,
    heavyIntro: Boolean,
    modifier: Modifier = Modifier
) {
    // грузим спрайты, но не падаем, если их нет
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val sprites by remember(season) {
        mutableStateOf(loadSpriteSet(ctx, season))
    }

    // настройки сезона
    val cfg = remember(season to heavyIntro) {
        when (season) {
            Season.AUTUMN -> SeasonCfg(
                // жёлтые/оранжевые листья
                colors = listOf(Color(0xFFF5D76E), Color(0xFFF0A35E), Color(0xFFB8713C)),
                introRate = 120f,    // шт/сек в заставке
                idleBurst = true,    // после заставки — группами 3-4 листа
                idleBurstEverySec = 5f,
                idleBurstCount = 3..4,
                vy = 120f..220f,     // пикс/сек
                vx = -40f..40f,
                rotSpeed = -60f..60f,
                scale = 0.6f..1.2f,
                alpha = 0.85f..1.0f,
                flutterFreq = 1.2f..2.2f, // «флаттер» (махание)
            )
            Season.WINTER -> SeasonCfg(
                colors = listOf(Color.White.copy(0.92f)),
                introRate = 140f,
                idleBurst = false,   // ровный лёгкий снег
                idleRate = 12f,
                vy = 70f..150f,
                vx = -20f..20f,
                rotSpeed = -10f..10f,
                scale = 0.6f..1.1f,
                alpha = 0.75f..1.0f,
                flutterFreq = 0.6f..1.2f
            )
            Season.SPRING -> SeasonCfg(
                colors = listOf(Color(0xFFFFAEC0), Color(0xFFFFD0E1)),
                introRate = 120f,
                idleBurst = false,
                idleRate = 8f,
                vy = 90f..170f,
                vx = -30f..45f,
                rotSpeed = -30f..30f,
                scale = 0.7f..1.2f,
                alpha = 0.7f..1.0f,
                flutterFreq = 1.0f..1.8f
            )
            Season.SUMMER -> SeasonCfg.empty()
        }
    }

    // частицы и размеры холста
    val particles = remember { mutableStateListOf<Particle>() }
    var W by remember { mutableStateOf(1080f) }
    var H by remember { mutableStateOf(1920f) }

    // планировщик «бурстов» для осени
    var nextBurstAt by remember { mutableStateOf(0f) }
    var time by remember { mutableStateOf(0f) }

    // симуляция
    LaunchedEffect(season, heavyIntro) {
        val rnd = Random(System.currentTimeMillis())
        var t0 = 0L
        while (isActive) {
            withFrameNanos { t ->
                if (t0 == 0L) { t0 = t; return@withFrameNanos }
                val dt = (t - t0) / 1_000_000_000f
                t0 = t
                time += dt

                // спавн (заставка)
                if (heavyIntro && cfg.introRate > 0f) {
                    spawnMany(particles, cfg.introRate * dt, rnd, W, sprites, cfg)
                }

                // фон
                if (!heavyIntro) {
                    if (cfg.idleBurst) {
                        if (time >= nextBurstAt) {
                            val count = rnd.nextInt(cfg.idleBurstCount.first, cfg.idleBurstCount.last + 1)
                            repeat(count) { particles += newParticle(rnd, W, sprites, cfg, burst = true) }
                            nextBurstAt = time + cfg.idleBurstEverySec
                        }
                    } else if (cfg.idleRate > 0f) {
                        spawnMany(particles, cfg.idleRate * dt, rnd, W, sprites, cfg)
                    }
                }

                // апдейт частиц
                val it = particles.iterator()
                while (it.hasNext()) {
                    val p = it.next()
                    // флаттер (волн. смещение по X)
                    val flutter = 18f * sin((time + p.flutterPhase) * p.flutterFreq) * (1f - p.depth)
                    p.x += (p.vx + flutter) * dt
                    p.y += (p.vy) * dt
                    p.rot += p.rotSpeed * dt

                    // удаление, когда вышли за экран
                    if (p.y - 64f > H || abs(p.x) > W * 1.5f) it.remove()
                }
            }
        }
    }

    // отрисовка
    Canvas(modifier.fillMaxSize()) {
        W = size.width; H = size.height
        particles.forEach { p ->
            val img = sprites.sprites.getOrNull(p.spriteIndex)
            // псевдо-глубина: дальние — меньше, прозрачнее и медленнее (задано при спауне)
            val k = (1f - 0.55f * p.depth) * p.scale
            val a = p.alpha * (1f - 0.35f * p.depth)

            withTransform({
                translate(p.x, p.y)
                rotate(p.rot)
                scale(k, k)
            }) {
                if (img != null) {
                    // рисуем спрайт, центрируя
                    drawImage(img, topLeft = Offset(-img.width / 2f, -img.height / 2f), alpha = a)
                } else {
                    // Фолбэк (если не положили png-ассеты)
                    when (sprites.fallbackShape) {
                        FallbackShape.SNOW -> drawCircle(Color.White.copy(alpha = a), radius = 3f)
                        FallbackShape.PETAL -> drawRoundRect(
                            color = Color(0xFFFFD0E1).copy(alpha = a),
                            topLeft = Offset(-6f, -3f),
                            size = androidx.compose.ui.geometry.Size(12f, 6f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
                        )
                        FallbackShape.LEAF -> drawRoundRect(
                            color = Color(0xFFF5D76E).copy(alpha = a),
                            topLeft = Offset(-7f, -4f),
                            size = androidx.compose.ui.geometry.Size(14f, 8f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                        )
                        else -> {}
                    }
                }
            }
        }
    }
}

// ---------- Внутренняя логика ----------

private data class SeasonCfg(
    val colors: List<Color> = emptyList(),
    val introRate: Float = 0f,             // в заставке (шт/сек)
    val idleRate: Float = 0f,              // после заставки (шт/сек)
    val idleBurst: Boolean = false,        // режим «пачками»
    val idleBurstEverySec: Float = 4f,
    val idleBurstCount: IntRange = 3..4,
    val vy: ClosedFloatingPointRange<Float> = 0f..0f,
    val vx: ClosedFloatingPointRange<Float> = 0f..0f,
    val rotSpeed: ClosedFloatingPointRange<Float> = 0f..0f,
    val scale: ClosedFloatingPointRange<Float> = 1f..1f,
    val alpha: ClosedFloatingPointRange<Float> = 1f..1f,
    val flutterFreq: ClosedFloatingPointRange<Float> = 1f..1f
) {
    companion object { fun empty() = SeasonCfg() }
}

private fun spawnMany(
    particles: MutableList<Particle>,
    amount: Float,
    rnd: Random,
    W: Float,
    sprites: SpriteSet,
    cfg: SeasonCfg
) {
    var acc = amount
    while (acc >= 1f) { particles += newParticle(rnd, W, sprites, cfg, burst = false); acc -= 1f }
    if (acc > rnd.nextFloat()) particles += newParticle(rnd, W, sprites, cfg, burst = false)
}

private fun newParticle(
    rnd: Random,
    W: Float,
    sprites: SpriteSet,
    cfg: SeasonCfg,
    burst: Boolean
): Particle {
    val depth = rnd.nextFloat()   // 0..1
    val baseScale = rnd.lerp(cfg.scale.start, cfg.scale.endInclusive)
    val scale = baseScale * (0.65f + 0.35f * (1f - depth)) // дальние меньше
    val x = rnd.nextFloat() * W
    val y = -rnd.nextFloat() * 120f - (if (burst) 0f else 60f)
    val vy = rnd.lerp(cfg.vy.start, cfg.vy.endInclusive) * (0.55f + 0.45f * (1f - depth))
    val vx = rnd.lerp(cfg.vx.start, cfg.vx.endInclusive) * (0.55f + 0.45f * (1f - depth))
    val rot = rnd.nextFloat() * 360f
    val rotSpeed = rnd.lerp(cfg.rotSpeed.start, cfg.rotSpeed.endInclusive) * (1f - 0.4f * depth)
    val flutterFreq = rnd.lerp(cfg.flutterFreq.start, cfg.flutterFreq.endInclusive)
    val alpha = rnd.lerp(cfg.alpha.start, cfg.alpha.endInclusive)
    val idx = if (sprites.sprites.isNotEmpty()) rnd.nextInt(sprites.sprites.size) else 0
    return Particle(
        x = x, y = y, vx = vx, vy = vy, depth = depth, rot = rot, rotSpeed = rotSpeed,
        flutterPhase = rnd.nextFloat() * 6.283f, flutterFreq = flutterFreq,
        scale = scale, alpha = alpha, spriteIndex = idx
    )
}

private fun loadSpriteSet(ctx: android.content.Context, season: Season): SpriteSet {
    val ids: List<Int>
    val fallback: FallbackShape
    when (season) {
        Season.AUTUMN -> {
            ids = listOf("leaf_1","leaf_2","leaf_3","leaf_4","leaf_5","leaf_6").map { idByName(ctx, it) }.filter { it != 0 }
            fallback = FallbackShape.LEAF
        }
        Season.WINTER -> {
            ids = listOf("snow_1","snow_2","snow_3","snow_4").map { idByName(ctx, it) }.filter { it != 0 }
            fallback = FallbackShape.SNOW
        }
        Season.SPRING -> {
            ids = listOf("petal_1","petal_2","petal_3","petal_4").map { idByName(ctx, it) }.filter { it != 0 }
            fallback = FallbackShape.PETAL
        }
        Season.SUMMER -> {
            ids = emptyList(); fallback = FallbackShape.NONE
        }
    }
    val imgs = ids.map { ImageBitmap.imageResource(ctx.resources, it) }
    return SpriteSet(imgs, fallback)
}

@DrawableRes
private fun idByName(ctx: android.content.Context, name: String): Int =
    ctx.resources.getIdentifier(name, "drawable", ctx.packageName)

private fun Random.lerp(a: Float, b: Float) = a + (b - a) * nextFloat()
