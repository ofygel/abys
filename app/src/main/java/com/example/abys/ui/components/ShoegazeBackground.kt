package com.example.abys.ui.components

import android.os.Build
import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.Shader
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.random.Random
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

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
                    renderEffect = AndroidRenderEffect
                        .createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
                        .asComposeRenderEffect()
                }
            }
            .drawWithContent {
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
        Canvas(Modifier.fillMaxSize()) {
            val step = 7
            val alphaMin = 0.02f
            val alphaMax = 0.06f
            val aR = alphaMax - alphaMin
            val w = size.width.toInt()
            val h = size.height.toInt()
            for (x in 0 until w step step) {
                for (y in 0 until h step step) {
                    val a = alphaMin + Random.nextFloat() * aR
                    drawRect(
                        Color.White.copy(alpha = a),
                        topLeft = Offset(x.toFloat(), y.toFloat()),
                        size = Size(1f, 1f),
                        blendMode = BlendMode.Overlay
                    )
                }
            }
        }
    }
}
