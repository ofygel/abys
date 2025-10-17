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
import com.example.abys.R
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var placeholderView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)          // ← подключаем XML-layout

        playerView = findViewById(R.id.playerView)        // ← из разметки
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
                                if (placeholderView.isVisible) {
                                    placeholderView.animate()
                                        .alpha(0f)
                                        .setDuration(220L)
                                        .withEndAction { placeholderView.isVisible = false }
                                        .start()
                                }
                                if (!isPlaying) play()
                            }

                            Player.STATE_ENDED -> {
                                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                finish()
                            }
                        }
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
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }
}
