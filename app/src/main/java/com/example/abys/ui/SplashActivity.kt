package com.example.abys.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.PlaybackException
import com.example.abys.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var placeholderView: ImageView
    private var navigationJob: Job? = null
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContentView(R.layout.activity_splash)          // ← подключаем XML-layout

        playerView = findViewById(R.id.playerView)        // ← из разметки
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        placeholderView = findViewById<ImageView>(R.id.placeholderView)

        val mediaItem = MediaItem.Builder()
            .setUri("asset:///greeting.mp4")
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setEndPositionMs(2000)
                    .build()
            )
            .build()

        player = ExoPlayer.Builder(this)
            .setUseLazyPreparation(true)
            .build().apply {
                setMediaItem(mediaItem)
                playWhenReady = false
                repeatMode = Player.REPEAT_MODE_OFF
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_READY -> {
                                placeholderView.isVisible = false
                                if (!isPlaying) play()
                            }

                            Player.STATE_ENDED -> {
                                navigateToMain()
                            }
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        placeholderView.isVisible = true
                        navigateToMain()
                    }
                })
            }

        playerView.apply {
            player = this@SplashActivity.player
            setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        lifecycleScope.launch {
            player.prepare()
        }

        navigationJob = lifecycleScope.launch {
            delay(SPLASH_TIMEOUT_MS)
            navigateToMain()
        }
    }

    override fun onStop() {
        super.onStop()
        player.release()
        navigationJob?.cancel()
    }

    private fun navigateToMain() {
        if (hasNavigated) return
        hasNavigated = true
        navigationJob?.cancel()
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    companion object {
        private const val SPLASH_TIMEOUT_MS = 2_500L
    }
}