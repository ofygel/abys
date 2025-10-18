package com.example.abys.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.animation.PathInterpolator
import android.widget.ImageView
import androidx.annotation.OptIn
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
import androidx.media3.ui.PlayerView
import com.example.abys.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

@Suppress("OptInUsage", "DEPRECATION", "SameParameterValue")
class SplashActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var placeholderView: ImageView
    private var fallbackJob: Job? = null
    private var videoRevealJob: Job? = null
    private var hasNavigated = false
    private var hasStartedVideoReveal = false
    private var isPlayerReleased = false
    private var isVideoReady = false
    private var placeholderVisibleDeadlineMs = 0L
    private var windowInsetsController: WindowInsetsControllerCompat? = null

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        playerView = findViewById(R.id.playerView)
        placeholderView = findViewById(R.id.placeholderView)
        setupSystemBars()
        setupViews()
        runNavigationFlow()
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
        // fallbackJob отменяется сам при завершении lifecycleScope
    }

    @UnstableApi
    private fun setupViews() {
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        playerView.alpha = 0f
        placeholderView.isVisible = true
        placeholderView.alpha = PLACEHOLDER_INTRO_ALPHA
        placeholderView.scaleX = PLACEHOLDER_INTRO_SCALE
        placeholderView.scaleY = PLACEHOLDER_INTRO_SCALE
        placeholderVisibleDeadlineMs = SystemClock.uptimeMillis() + PLACEHOLDER_MIN_VISIBLE_MS
        placeholderView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(PLACEHOLDER_INTRO_DURATION_MS)
            .setInterpolator(FADE_INTERPOLATOR)
            .start()
    }

    private fun setupSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        windowInsetsController = WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun ensureSystemBarsHidden() {
        windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
    }

    @OptIn(UnstableApi::class)
    private fun runNavigationFlow() {
        lifecycleScope.launch {
            val assetAvailable = withContext(Dispatchers.IO) { assetExists() }
            if (!assetAvailable) {
                Log.w(TAG, "Greeting asset missing; falling back to main")
                showPlaceholderFallback()
                scheduleFallback()
                return@launch
            }
            initializePlayer()
        }
    }

    @UnstableApi
    private fun initializePlayer() {
        isPlayerReleased = false
        isVideoReady = false
        hasStartedVideoReveal = false
        cancelVideoRevealJob()
        player = ExoPlayer.Builder(this)
            .setUseLazyPreparation(true)
            .build().apply {
                repeatMode = Player.REPEAT_MODE_OFF
                playWhenReady = false
                setMediaItem(MediaItem.fromUri("asset:///$ASSET_GREETING_VIDEO"))
                addListener(playerListener)
            }
        playerView.player = player
        playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        playerView.keepScreenOn = true
        scheduleFallback()
        player.prepare()
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    cancelFallback()
                    isVideoReady = true
                    scheduleVideoRevealIfReady()
                    ensureSystemBarsHidden()
                }
                Player.STATE_ENDED -> {
                    cancelFallback()
                    handleVideoEnded()
                }
                Player.STATE_BUFFERING, Player.STATE_IDLE -> { /* no-op */ }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.w(TAG, "Splash video playback error", error)
            showPlaceholderFallback()
            cancelFallback()
            cancelVideoRevealJob()
            navigateToMain()
        }
    }

    private fun scheduleVideoRevealIfReady() {
        if (hasStartedVideoReveal || isPlayerReleased || !isVideoReady) return
        val now = SystemClock.uptimeMillis()
        val delayMs = max(placeholderVisibleDeadlineMs - now, 0L) + VIDEO_REVEAL_LEAD_IN_MS
        if (delayMs <= 0L) {
            startVideoReveal()
        } else {
            videoRevealJob?.cancel()
            videoRevealJob = lifecycleScope.launch {
                delay(delayMs)
                startVideoReveal()
            }
        }
    }

    private fun startVideoReveal() {
        if (hasStartedVideoReveal || isPlayerReleased || !isVideoReady) return
        hasStartedVideoReveal = true
        cancelVideoRevealJob()

        placeholderView.animate().setListener(null)
        placeholderView.animate()
            .alpha(0f)
            .scaleX(PLACEHOLDER_OUTRO_SCALE)
            .scaleY(PLACEHOLDER_OUTRO_SCALE)
            .setDuration(PLACEHOLDER_FADE_OUT_DURATION_MS)
            .setInterpolator(FADE_INTERPOLATOR)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    placeholderView.animate().setListener(null)
                    if (placeholderView.alpha <= 0.01f) {
                        placeholderView.isVisible = false
                    }
                }
            })
            .start()

        player.pause()
        player.seekTo(0)

        playerView.animate().setListener(null)
        playerView.animate()
            .alpha(1f)
            .setDuration(VIDEO_FADE_IN_DURATION_MS)
            .setInterpolator(FADE_INTERPOLATOR)
            .withEndAction {
                if (!isPlayerReleased) {
                    player.play()
                }
            }
            .start()
    }

    private fun showPlaceholderFallback() {
        placeholderView.apply {
            cancelVideoRevealJob()
            placeholderVisibleDeadlineMs = SystemClock.uptimeMillis() + PLACEHOLDER_MIN_VISIBLE_MS
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

    private fun scheduleFallback() {
        fallbackJob?.cancel()
        fallbackJob = lifecycleScope.launch {
            delay(FALLBACK_DELAY_MS)
            navigateToMain()
        }
    }

    private fun cancelFallback() {
        fallbackJob?.cancel()
        fallbackJob = null
    }

    private fun assetExists(): Boolean = runCatching {
        assets.open(ASSET_GREETING_VIDEO).close()
    }.isSuccess

    private fun releasePlayer() {
        if (isPlayerReleased || !::player.isInitialized) return
        isPlayerReleased = true
        isVideoReady = false
        cancelVideoRevealJob()
        player.removeListener(playerListener)
        player.release()
        playerView.keepScreenOn = false
        playerView.player = null
    }

    private fun navigateToMain() {
        if (hasNavigated) return
        hasNavigated = true
        cancelFallback()
        cancelVideoRevealJob()
        Log.d(TAG, "Leaving splash")
        playerView.keepScreenOn = false
        val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_START_FADED, true)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun handleVideoEnded() {
        if (hasNavigated) return
        playerView.animate().setListener(null)
        playerView.animate()
            .alpha(0f)
            .setDuration(VIDEO_OUTRO_FADE_DURATION_MS)
            .setInterpolator(FADE_INTERPOLATOR)
            .withEndAction { navigateToMain() }
            .start()
    }

    private fun cancelVideoRevealJob() {
        videoRevealJob?.cancel()
        videoRevealJob = null
    }

    companion object {
        private val FADE_INTERPOLATOR = PathInterpolator(0.22f, 1f, 0.36f, 1f)
        private const val VIDEO_FADE_IN_DURATION_MS = 420L
        private const val VIDEO_OUTRO_FADE_DURATION_MS = 320L
        private const val VIDEO_REVEAL_LEAD_IN_MS = 120L
        private const val PLACEHOLDER_FADE_OUT_DURATION_MS = 420L
        private const val PLACEHOLDER_INTRO_DURATION_MS = 320L
        private const val PLACEHOLDER_RECOVER_FADE_IN_MS = 240L
        private const val PLACEHOLDER_MIN_VISIBLE_MS = 920L
        private const val FALLBACK_DELAY_MS = 2000L
        private const val ASSET_GREETING_VIDEO = "greeting.mp4"
        private const val PLACEHOLDER_INTRO_ALPHA = 0.9f
        private const val PLACEHOLDER_INTRO_SCALE = 0.98f
        private const val PLACEHOLDER_OUTRO_SCALE = 1.02f
        private const val TAG = "SplashActivity"
    }
}
