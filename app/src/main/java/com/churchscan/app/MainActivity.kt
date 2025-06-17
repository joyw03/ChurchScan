package com.churchscan.app

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.churchscan.app.util.SharedPreferencesHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var etMainSearch: EditText
    private lateinit var btnMainSearch: Button
    private lateinit var btnUploadImage: Button
    private lateinit var recentSearchLayout: LinearLayout
    private lateinit var prefsHelper: SharedPreferencesHelper

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etMainSearch = findViewById(R.id.etMainSearch)
        btnMainSearch = findViewById(R.id.btnMainSearch)
        btnUploadImage = findViewById(R.id.btnUploadImage)
        recentSearchLayout = findViewById(R.id.recentSearchList)
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavView)

        prefsHelper = SharedPreferencesHelper(this)

        // 🔍 검색 버튼 클릭
        btnMainSearch.setOnClickListener {
            val query = etMainSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                prefsHelper.saveRecentSearch(query)
                navigateToSearch(query)
            } else {
                Toast.makeText(this, "검색어를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        // 🔍 엔터 키 처리
        etMainSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                btnMainSearch.performClick() // 버튼 클릭 효과
                true
            } else {
                false
            }
        }

        btnUploadImage.setOnClickListener {
            Toast.makeText(this, "이미지 업로드 기능은 추후 지원됩니다", Toast.LENGTH_SHORT).show()
        }

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> true
                R.id.menu_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    finish()
                    true
                }
                R.id.menu_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        bottomNavView.selectedItemId = R.id.menu_home
        updateRecentSearches()
    }

    private fun updateRecentSearches() {
        recentSearchLayout.removeAllViews()
        val recentSearches = prefsHelper.getRecentSearches()
        for (search in recentSearches) {
            val textView = TextView(this).apply {
                text = "- $search"
                textSize = 16f
                setPadding(8, 8, 8, 8)
                setOnClickListener {
                    navigateToSearch(search)
                }
            }
            recentSearchLayout.addView(textView)
        }
    }

    private fun navigateToSearch(query: String) {
        val intent = Intent(this, SearchActivity::class.java)
        intent.putExtra("search_query", query)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        updateRecentSearches()
    }
}
