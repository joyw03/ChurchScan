package com.churchscan.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivLogo: ImageView
    private lateinit var tvGreeting: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvUserBirth: TextView
    private lateinit var btnGoToEdit: Button
    private lateinit var btnLogout: Button

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // ✅ XML과 1:1 매칭된 뷰 바인딩
        ivLogo = findViewById(R.id.ivLogo)
        tvGreeting = findViewById(R.id.tvGreeting)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvUserName = findViewById(R.id.tvUserName)
        tvUserPhone = findViewById(R.id.tvUserPhone)
        tvUserBirth = findViewById(R.id.tvUserBirth)
        btnGoToEdit = findViewById(R.id.btnGoToEdit)
        btnLogout = findViewById(R.id.btnLogout)
        val bottom = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // ✅ 하단 네비: 현재 탭 표시
        bottom.selectedItemId = R.id.nav_profile

        // ✅ 하단 네비: 탭 이동
        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    true
                }
                R.id.nav_profile -> true // 이미 현재 화면
                else -> false
            }
        }
        bottom.setOnItemReselectedListener { /* no-op */ }

        // ✅ Firebase 사용자 정보 표시 (알 수 없는 값은 기본 텍스트 유지/대시 처리)
        val user = auth.currentUser
        if (user != null) {
            val name = user.displayName ?: "사용자"
            tvGreeting.text = "ChurchScan에 오신 것을 환영합니다!"
            // 이름/이메일 표시
            tvUserName.text = "이름 : $name"
            tvUserEmail.text = "이메일 : ${user.email ?: "-"}"
            // 전화/생년월일은 별도 저장소(예: Firestore)를 쓰지 않았다면 기본값 유지
            if (tvUserPhone.text.isNullOrBlank()) tvUserPhone.text = "전화번호 : -"
            if (tvUserBirth.text.isNullOrBlank()) tvUserBirth.text = "생년월일 : -"
        }

        // ✅ 정보 수정 화면으로 이동
        btnGoToEdit.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // ✅ 로그아웃
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // 프로필 수정 후 돌아왔을 때 갱신 로직 (필요 시 Firestore에서 다시 로드)
        val user = auth.currentUser
        if (user != null) {
            tvUserName.text = "이름 : ${user.displayName ?: "사용자"}"
            tvUserEmail.text = "이메일 : ${user.email ?: "-"}"
        }
    }
}
