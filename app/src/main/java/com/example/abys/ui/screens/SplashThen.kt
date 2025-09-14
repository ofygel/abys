package com.example.abys.ui.screens

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.abys.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashThen(content: @Composable () -> Unit) {
    var show by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    AnimatedContent(
        targetState = show,
        transitionSpec = { fadeIn(tween(600)) togetherWith fadeOut(tween(600)) },
        label = "splash",
    ) { s ->
        if (s) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoURI(
                            Uri.parse("android.resource://${ctx.packageName}/${R.raw.greeting_intro}")
                        )
                        setOnPreparedListener { mp ->
                            mp.isLooping = false
                            start()
                            scope.launch {
                                delay(5_000)
                                show = false
                            }
                        }
                    }
                }
            )
            // Place greeting_intro.mp4 (5s) in app/src/main/res/raw/
        } else {
            content()
        }
    }
}
