package com.example.abys.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.abys.R

class SplashActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)          // ← подключаем XML-layout

        playerView = findViewById(R.id.playerView)        // ← из разметки

        player = ExoPlayer.Builder(this).build().apply {
            setMediaItem(MediaItem.fromUri("asset:///greeting.mp4"))
            prepare()
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    }
                }
            })
        }
        playerView.player = player
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }
}
