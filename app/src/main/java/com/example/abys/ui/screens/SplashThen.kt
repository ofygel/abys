package com.example.abys.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun SplashThen(content: @Composable () -> Unit) {
    var show by remember { mutableStateOf(true) }
    AnimatedContent(
        targetState = show,
        transitionSpec = { fadeIn(tween(600)) togetherWith fadeOut(tween(600)) },
        label = "splash",
    ) { s ->
        if (s) {
            LaunchedEffect(Unit) {
                delay(5_000)
                show = false
            }
            Box(Modifier.fillMaxSize())
        } else {
            content()
        }
    }
}
