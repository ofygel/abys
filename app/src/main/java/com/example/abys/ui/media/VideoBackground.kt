package com.example.abys.ui.media

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoBackground(
    modifier: Modifier = Modifier,
    // Передаём имя raw-ресурса без расширения (например, "bg_autumn").
    // Если файла нет — блок тихо не рисуется.
    rawName: String?,
    alpha: Float
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val resId = remember(rawName) { rawName?.let { idByName(context, it) } ?: 0 }
    if (resId == 0) return

    val player = remember(resId) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
            playWhenReady = true
            setMediaItem(MediaItem.fromUri(
                Uri.parse("android.resource://${context.packageName}/$resId")
            ))
            prepare()
        }
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                useController = false
                this.player = player
            }
        },
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha }
    )
}

private fun idByName(ctx: Context, name: String): Int =
    ctx.resources.getIdentifier(name, "raw", ctx.packageName)
