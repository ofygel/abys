package com.example.abys.ui.util

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies a backdrop style blur to the current layer. On Android S and above we use a real
 * [RenderEffect]. On older devices we gracefully fall back to drawing without blur so the
 * component still renders correctly albeit without the glass effect described in the spec.
 */
fun Modifier.backdropBlur(radius: Dp): Modifier = composed {
    if (radius <= 0.dp) return@composed this
    val density = LocalDensity.current
    val radiusPx = with(density) { radius.toPx() }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.graphicsLayer {
            renderEffect = RenderEffect.createBlurEffect(radiusPx, radiusPx, Shader.TileMode.CLAMP)
            compositingStrategy = CompositingStrategy.Offscreen
        }
    } else {
        this
    }
}

