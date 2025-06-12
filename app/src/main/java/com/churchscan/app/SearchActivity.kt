package com.churchscan.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // ✅ Firebase 초기화
        FirebaseApp.initializeApp(this)

        // ✅ Firestore 인스턴스
        val db = FirebaseFirestore.getInstance()

        // 🔍 검색 버튼 연결
        val editText = findViewById<EditText>(R.id.etSearchText)
        val searchButton = findViewById<Button>(R.id.btnSearchText)

        searchButton.setOnClickListener {
            val keyword = editText.text.toString().trim()
            if (keyword.isNotEmpty()) {
                searchHeresyByChurchName(keyword)
            } else {
                showAlert("입력 오류", "교회 이름을 입력해주세요.")
            }
        }

        // ✅ 엔터키(IME Action)로도 검색 가능하게 처리
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                val keyword = editText.text.toString().trim()
                if (keyword.isNotEmpty()) {
                    searchHeresyByChurchName(keyword)
                } else {
                    showAlert("입력 오류", "교회 이름을 입력해주세요.")
                }
                true
            } else {
                false
            }
        }

        // 🔻 하단 네비게이션 바 설정
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavView)
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.menu_search -> true
                R.id.menu_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
        bottomNavView.selectedItemId = R.id.menu_search
    }

    // 🔍 Firestore에서 이단 여부 확인
    private fun searchHeresyByChurchName(keyword: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("jesus114_decisions")  // ✅ 컬렉션명 확인
            .get()
            .addOnSuccessListener { documents ->
                var found = false
                for (doc in documents) {
                    val title = doc.get("title")?.toString() ?: ""
                    val reason = doc.get("reason")?.toString() ?: ""
                    if (title.contains(keyword) || reason.contains(keyword)) {
                        found = true
                        break
                    }
                }

                if (found) {
                    showAlert("⚠️ 이단 주의", "$keyword 관련 이단 정보가 존재합니다.")
                } else {
                    showAlert("✅ 정상", "$keyword 관련 이단 정보는 없습니다.")
                }
            }
            .addOnFailureListener {
                showAlert("❌ 오류", "Firestore 조회 중 오류 발생: ${it.message}")
            }
    }

    // 📢 결과 표시용 AlertDialog
    private fun showAlert(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("확인", null)
            .show()
    }
}
