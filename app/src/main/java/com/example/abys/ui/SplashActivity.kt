package com.example.abys.ui

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.abys.R

class SplashActivity : AppCompatActivity() {

    private val delayMs = 5000L
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var videoView: VideoView

    private val goNext = Runnable {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        videoView = findViewById(R.id.videoView)
        val uri = Uri.parse("android.resource://${packageName}/${R.raw.greeting}")
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { mp: MediaPlayer ->
            // выключим звук
            mp.setVolume(0f, 0f)
            videoView.start()
        }
        // На случай, если видео короче/дольше — принудительно уходим через 5 сек
        handler.postDelayed(goNext, delayMs)
        videoView.setOnCompletionListener {
            // если закончилось раньше — уйдём сразу
            if (!isFinishing) {
                handler.removeCallbacks(goNext)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(goNext)
        super.onDestroy()
    }
}
