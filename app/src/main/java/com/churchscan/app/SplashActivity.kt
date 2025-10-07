package com.churchscan.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide


class SplashActivity : AppCompatActivity() {

    private val splashDelayMs = 2000L
    private val handler = Handler(Looper.getMainLooper())
    private var navigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // GIF는 Glide로 로드(기기/에뮬레이터에서 src만 쓰면 멈추는 경우 방지)
        val iv = findViewById<ImageView>(R.id.ivSplashGif)
        Glide.with(this)
            .asGif()
            .load(R.raw.splash)
            .into(iv)

        // 메인 스레드 블로킹 금지: postDelayed로 화면 전환
        handler.postDelayed({
            if (!navigated) {
                navigated = true
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, splashDelayMs)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
