package com.example.abys.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.animation.Interpolator
import android.view.animation.PathInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import com.example.abys.databinding.ActivitySplashBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private var player: ExoPlayer? = null
    private var hasNavigated = false
    private var hasFadedInVideo = false
    private var isPlayerReleased = false
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

                Player.STATE_ENDED -> navigateToMain(ExitReason.PLAYBACK_ENDED)
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            @Player.DiscontinuityReason reason: Int
        ) {
            if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION ||
                reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION
            ) {
                navigateToMain(ExitReason.PLAYBACK_DISCONTINUITY)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.w(TAG, "Splash video playback error", error)
            showPlaceholderFallback()
            cancelFallback()
            navigateToMain(ExitReason.PLAYER_ERROR)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupWindowInsetsController()
        setupViews()
        runNavigationFlow()
    }

    override fun onResume() {
        super.onResume()
        ensureSystemBarsHidden()
    }

    override fun onStop() {
        if (!hasNavigated && !isChangingConfigurations) {
            navigateToMain(ExitReason.USER_BACKGROUND)
        }
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
        placeholderView.alpha = PLACEHOLDER_INTRO_ALPHA
        placeholderView.scaleX = PLACEHOLDER_INTRO_SCALE
        placeholderView.scaleY = PLACEHOLDER_INTRO_SCALE
        placeholderView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(PLACEHOLDER_INTRO_DURATION_MS)
            .setInterpolator(FADE_INTERPOLATOR)
            .start()
    }

    private fun setupWindowInsetsController() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        windowInsetsController = WindowInsetsControllerCompat(window, binding.root).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun runNavigationFlow() {
        lifecycleScope.launch {
            val assetAvailable = withContext(Dispatchers.IO) {
                assetExists(ASSET_GREETING_VIDEO)
            }

            if (!assetAvailable) {
                Log.w(TAG, "Greeting asset missing; falling back to main")
                showPlaceholderFallback()
                scheduleFallback(ExitReason.ASSET_MISSING)
                return@launch
            }

            initializePlayer()
        }
    }

    private fun initializePlayer() {
        val mediaItem = MediaItem.Builder()
            .setUri("asset:///$ASSET_GREETING_VIDEO")
            .build()

        isPlayerReleased = false

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
            post { setShutterBackgroundColor(Color.TRANSPARENT) }
            keepScreenOn = true
        }

        scheduleFallback(ExitReason.FALLBACK_TIMEOUT)

        lifecycleScope.launch {
            runCatching { player?.prepare() }
                .onFailure {
                    Log.w(TAG, "Preparing splash video failed", it)
                    showPlaceholderFallback()
                    navigateToMain(ExitReason.PREPARE_FAILED)
                }
        }
    }

    private fun fadeInVideo() {
        if (hasFadedInVideo) return
        hasFadedInVideo = true

        binding.playerView.animate()
            .alpha(1f)
            .setDuration(VIDEO_FADE_IN_DURATION_MS)
            .setInterpolator(FADE_INTERPOLATOR)
            .start()

        binding.placeholderView.animate().setListener(null)
        binding.placeholderView.animate()
            .alpha(0f)
            .setDuration(PLACEHOLDER_FADE_OUT_DURATION_MS)
            .setInterpolator(FADE_INTERPOLATOR)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.placeholderView.animate().setListener(null)
                    if (binding.placeholderView.alpha <= 0.01f) {
                        binding.placeholderView.isVisible = false
                    }
                }
            })
            .start()

        ensureSystemBarsHidden()
    }

    private fun showPlaceholderFallback() {
        binding.placeholderView.apply {
            if (!isVisible) {
                alpha = 0f
                scaleX = PLACEHOLDER_INTRO_SCALE
                scaleY = PLACEHOLDER_INTRO_SCALE
                isVisible = true
            } else {
                scaleX = PLACEHOLDER_INTRO_SCALE
                scaleY = PLACEHOLDER_INTRO_SCALE
            }

            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(PLACEHOLDER_RECOVER_FADE_IN_MS)
                .setInterpolator(FADE_INTERPOLATOR)
                .start()
        }
    }

    private fun scheduleFallback(reason: ExitReason) {
        cancelFallback()
        fallbackJob = lifecycleScope.launch {
            delay(FALLBACK_DELAY_MS)
            navigateToMain(reason)
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
        if (isPlayerReleased) return
        isPlayerReleased = true

        player?.let { exoPlayer ->
            exoPlayer.removeListener(playerListener)
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.release()
        }
        binding.playerView.apply {
            keepScreenOn = false
            player = null
        }
        player = null
    }

    private fun navigateToMain(reason: ExitReason) {
        if (hasNavigated) return
        hasNavigated = true
        cancelFallback()
        Log.d(TAG, "Leaving splash: ${reason.logValue}")
        binding.playerView.keepScreenOn = false
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    companion object {
        private val FADE_INTERPOLATOR: Interpolator = PathInterpolator(0.22f, 1f, 0.36f, 1f)
        private const val VIDEO_FADE_IN_DURATION_MS = 360L
        private const val PLACEHOLDER_FADE_OUT_DURATION_MS = 360L
        private const val PLACEHOLDER_INTRO_DURATION_MS = 320L
        private const val PLACEHOLDER_RECOVER_FADE_IN_MS = 240L
        private const val FALLBACK_DELAY_MS = 2_000L
        private const val ASSET_GREETING_VIDEO = "greeting.mp4"
        private const val PLACEHOLDER_INTRO_ALPHA = 0.9f
        private const val PLACEHOLDER_INTRO_SCALE = 0.98f
        private const val TAG = "SplashActivity"
    }

    private enum class ExitReason(val logValue: String) {
        FALLBACK_TIMEOUT("fallback_timeout"),
        PLAYER_ERROR("player_error"),
        PREPARE_FAILED("prepare_failed"),
        ASSET_MISSING("asset_missing"),
        PLAYBACK_ENDED("playback_ended"),
        PLAYBACK_DISCONTINUITY("playback_discontinuity"),
        USER_BACKGROUND("user_background")
    }
}
