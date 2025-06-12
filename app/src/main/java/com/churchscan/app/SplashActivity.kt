package com.churchscan.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val gifView = findViewById<ImageView>(R.id.ivSplashGif)

        // res/raw/splash.gif 를 로드합니다.
        Glide.with(this)
            .asGif()
            .load(R.raw.splash)  // 반드시 raw 폴더에 있어야 함
            .into(gifView)

        // 2.5초 후 로그인 화면으로 이동
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3000)
    }
}
