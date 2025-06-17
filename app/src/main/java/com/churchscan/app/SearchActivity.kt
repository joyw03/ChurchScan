package com.churchscan.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.churchscan.app.adapter.SearchHistoryAdapter
import com.churchscan.app.util.SharedPreferencesHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class SearchActivity : AppCompatActivity() {

    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var adapter: SearchHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // ✅ Firebase 초기화
        FirebaseApp.initializeApp(this)

        // ✅ SharedPreferences 도우미 초기화
        prefsHelper = SharedPreferencesHelper(this)

        // 🔍 뷰 연결
        val editText = findViewById<EditText>(R.id.etSearchText)
        val searchButton = findViewById<Button>(R.id.btnSearchText)
        val btnClearAll = findViewById<Button>(R.id.btnClearAll)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewSearchHistory)

        // ✅ RecyclerView 설정
        val historyList = prefsHelper.getRecentSearches().toMutableList()
        adapter = SearchHistoryAdapter(historyList) { itemToDelete ->
            prefsHelper.removeSearch(itemToDelete)
            adapter.removeItem(itemToDelete)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ✅ MainActivity에서 전달된 검색어 처리
        val passedQuery = intent.getStringExtra("search_query")
        if (!passedQuery.isNullOrEmpty()) {
            editText.setText(passedQuery)
            prefsHelper.saveRecentSearch(passedQuery)
            if (historyList.contains(passedQuery)) {
                adapter.removeItem(passedQuery)
            }
            adapter.notifyDataSetChanged()
            searchHeresyByChurchName(passedQuery)
        }

        // 🔍 검색 버튼 클릭
        searchButton.setOnClickListener {
            val keyword = editText.text.toString().trim()
            if (keyword.isNotEmpty()) {
                prefsHelper.saveRecentSearch(keyword)
                adapter.removeItem(keyword)
                adapter.notifyDataSetChanged()
                searchHeresyByChurchName(keyword)
            } else {
                showAlert("입력 오류", "교회 이름을 입력해주세요.")
            }
        }

        // 🔍 키보드 엔터로 검색
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                val keyword = editText.text.toString().trim()
                if (keyword.isNotEmpty()) {
                    prefsHelper.saveRecentSearch(keyword)
                    adapter.removeItem(keyword)
                    adapter.notifyDataSetChanged()
                    searchHeresyByChurchName(keyword)
                } else {
                    showAlert("입력 오류", "교회 이름을 입력해주세요.")
                }
                true
            } else {
                false
            }
        }

        // ❌ 전체 삭제 버튼 클릭
        btnClearAll.setOnClickListener {
            prefsHelper.clearAllSearches()
            adapter.clearAll()
        }

        // 🔻 하단 네비게이션 바
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
        db.collection("jesus114_decisions")
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

    // 📢 AlertDialog 표시
    private fun showAlert(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("확인", null)
            .show()
    }
}
