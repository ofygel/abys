package com.example.abys.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import com.example.abys.databinding.ActivitySplashBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@UnstableApi
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private var player: ExoPlayer? = null
    private var hasNavigated = false
    private var hasFadedInVideo = false
    private var fallbackJob: Job? = null
    private var windowInsetsController: WindowInsetsControllerCompat? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    cancelFallback()
                    fadeInVideo()
                    ensureSystemBarsHidden()
                    player?.takeIf { !it.isPlaying }?.play()
                }

                Player.STATE_ENDED -> navigateToMain()
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            @Player.DiscontinuityReason reason: Int
        ) {
            if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                navigateToMain()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            binding.placeholderView.isVisible = true
            cancelFallback()
            navigateToMain()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        windowInsetsController = WindowInsetsControllerCompat(window, binding.root)

        setupViews()

        if (!assetExists(ASSET_GREETING_VIDEO)) {
            scheduleFallback()
            return
        }

        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        ensureSystemBarsHidden()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
        cancelFallback()
    }

    private fun setupViews() = with(binding) {
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        playerView.alpha = 0f
        placeholderView.isVisible = true
        placeholderView.alpha = 1f
    }

    private fun initializePlayer() {
        val mediaItem = MediaItem.Builder()
            .setUri("asset:///$ASSET_GREETING_VIDEO")
            .build()

        player = ExoPlayer.Builder(this)
            .setUseLazyPreparation(true)
            .build()
            .also { exoPlayer ->
                exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
                exoPlayer.playWhenReady = false
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.addListener(playerListener)
            }

        binding.playerView.apply {
            player = this@SplashActivity.player
            setShutterBackgroundColor(Color.TRANSPARENT)
            keepScreenOn = true
        }

        scheduleFallback()

        lifecycleScope.launch {
            runCatching { player?.prepare() }
                .onFailure { navigateToMain() }
        }
    }

    private fun fadeInVideo() {
        if (hasFadedInVideo) return
        hasFadedInVideo = true

        binding.playerView.animate()
            .alpha(1f)
            .setDuration(FADE_DURATION_MS)
            .start()

        binding.placeholderView.animate()
            .alpha(0f)
            .setDuration(FADE_DURATION_MS)
            .withEndAction {
                binding.placeholderView.isVisible = false
            }
            .start()

        ensureSystemBarsHidden()
    }

    private fun scheduleFallback() {
        cancelFallback()
        fallbackJob = lifecycleScope.launch {
            delay(FALLBACK_DELAY_MS)
            navigateToMain()
        }
    }

    private fun cancelFallback() {
        fallbackJob?.cancel()
        fallbackJob = null
    }

    private fun ensureSystemBarsHidden() {
        windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun assetExists(assetName: String): Boolean = runCatching {
        assets.open(assetName).close()
    }.isSuccess

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            exoPlayer.removeListener(playerListener)
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.release()
        }
        binding.playerView.player = null
        player = null
    }

    private fun navigateToMain() {
        if (hasNavigated) return
        hasNavigated = true
        cancelFallback()
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    companion object {
        private const val FADE_DURATION_MS = 300L
        private const val FALLBACK_DELAY_MS = 2_000L
        private const val ASSET_GREETING_VIDEO = "greeting.mp4"
    }
}
