package com.example.abys.ui.components

import android.os.Build
import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun ShoegazeBackground(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "bg")
    val t by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "t"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                if (Build.VERSION.SDK_INT >= 31) {
                    renderEffect = RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
                }
            }
            .drawWithContent {
                // фон-градиент: циан ↔ персик
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0EA5A8),
                            Color(0xFF2A3A3A),
                            Color(0xFFF39A6B)
                        ),
                        start = center.copy(x = size.width * t),
                        end = center.copy(y = size.height * (1f - t))
                    )
                )
                drawContent()
            }
    ) {
        // зерно/шум сверху
        Canvas(Modifier.fillMaxSize()) {
            val step = 5
            for (x in 0 until size.width.toInt() step step) {
                for (y in 0 until size.height.toInt() step step) {
                    val a = (0.02f..0.06f).random()
                    drawRect(
                        Color.White.copy(alpha = a),
                        topLeft = androidx.compose.ui.geometry.Offset(x.toFloat(), y.toFloat()),
                        size = androidx.compose.ui.geometry.Size(1f, 1f),
                        blendMode = BlendMode.Overlay
                    )
                }
            }
        }
    }
}
