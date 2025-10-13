package com.churchscan.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.churchscan.app.adapter.SearchHistoryAdapter
import com.churchscan.app.util.SharedPreferencesHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentSnapshot

class SearchActivity : AppCompatActivity() {

    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var historyAdapter: SearchHistoryAdapter

    private lateinit var editText: EditText
    private lateinit var searchButton: Button
    private lateinit var rvHistory: RecyclerView

    // ✅ 추가: 검색 결과 표시용
    private lateinit var rvResults: RecyclerView
    private lateinit var resultsAdapter: ChurchResultAdapter
    private var progressBar: ProgressBar? = null
    private var tvResultsTitle: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // ✅ Firebase 초기화
        FirebaseApp.initializeApp(this)

        // ✅ SharedPreferences 초기화
        prefsHelper = SharedPreferencesHelper(this)

        // 🔍 뷰 연결
        editText = findViewById(R.id.etSearchText)
        searchButton = findViewById(R.id.btnSearchText)
        val btnClearAll = findViewById<Button>(R.id.btnClearAll)
        rvHistory = findViewById(R.id.recyclerViewSearchHistory)

        // ✅ 추가된 뷰
        rvResults = findViewById(R.id.rvResults)
        progressBar = findViewById(R.id.progressSearch)
        tvResultsTitle = findViewById(R.id.tvResultsTitle)

        // ✅ 검색 기록 어댑터 연결
        val historyList = prefsHelper.getRecentSearches().toMutableList()
        historyAdapter = SearchHistoryAdapter(
            historyList,
            onDelete = { itemToDelete ->
                prefsHelper.removeSearch(itemToDelete)
                historyAdapter.removeItem(itemToDelete)
            },
            onItemClick = { selectedItem ->
                editText.setText(selectedItem)
            }
        )
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = historyAdapter

        // ✅ 결과 리스트 어댑터 연결
        resultsAdapter = ChurchResultAdapter()
        rvResults.layoutManager = LinearLayoutManager(this)
        rvResults.adapter = resultsAdapter

        // ✅ 전달된 검색어 처리
        val passedQuery = intent.getStringExtra("search_query")
        if (!passedQuery.isNullOrEmpty()) {
            editText.setText(passedQuery)
            prefsHelper.saveRecentSearch(passedQuery)
            if (historyList.contains(passedQuery)) {
                historyAdapter.removeItem(passedQuery)
            }
            historyAdapter.notifyDataSetChanged()
            doSearch(passedQuery)
        }

        // 🔍 검색 버튼 클릭
        searchButton.setOnClickListener {
            val keyword = editText.text.toString().trim()
            if (keyword.isNotEmpty()) {
                prefsHelper.saveRecentSearch(keyword)
                historyAdapter.removeItem(keyword)
                historyAdapter.notifyDataSetChanged()
                doSearch(keyword)
            } else {
                showAlert("입력 오류", "교회 이름을 입력해주세요.")
            }
        }

        // ✅ 엔터키 → 검색 버튼
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchButton.performClick()
                true
            } else false
        }

        // ❌ 전체 삭제 버튼
        btnClearAll.setOnClickListener {
            prefsHelper.clearAllSearches()
            historyAdapter.clearAll()
        }

        // 🔻 하단 네비게이션
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

    /** 🔤 띄어쓰기/대소문자 무시 */
    private fun normalizeForSearch(input: String): String =
        input.trim().lowercase().replace("\\s+".toRegex(), "")


    /** 🔎 교회 검색 */
    private fun doSearch(rawKeyword: String) {
        val q = normalizeForSearch(rawKeyword)
        if (q.isEmpty()) {
            resultsAdapter.submit(emptyList())
            return
        }

        progressBar?.visibility = android.view.View.VISIBLE
        tvResultsTitle?.text = "검색 결과"

        val db = FirebaseFirestore.getInstance()
        db.collection("churches")
            .orderBy("name_norm", Query.Direction.ASCENDING)
            .startAt(q)
            .endAt(q + "\uf8ff")
            .limit(50)
            .get()
            .addOnSuccessListener { snap ->
                val items: List<Church> = snap.documents.mapNotNull { it.toChurch() }
                resultsAdapter.submit(items)
                progressBar?.visibility = android.view.View.GONE
            }
            .addOnFailureListener { e ->
                progressBar?.visibility = android.view.View.GONE
                if (e.message?.contains("FAILED_PRECONDITION") == true) {
                    showAlert("인덱스 필요",
                        "Firestore에서 name_norm 인덱스를 생성해 주세요.\n에러 메시지의 'Create index' 링크를 눌러 생성하면 됩니다.")
                } else {
                    showAlert("오류", "검색 중 오류가 발생했습니다: ${e.message}")
                }
            }

        // ✅ 이단 여부 체크
        searchHeresyByChurchName(rawKeyword)

        // ✅ 검색 후 입력창 초기화
        editText.setText("")
    }

    // 🔍 이단 여부 조회
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

    /** DocumentSnapshot → Church 변환 */
    private fun DocumentSnapshot.toChurch(): Church? {
        val name = getString("name") ?: return null
        return Church(
            name = name,
            address = getString("address") ?: "",
            denomination = getString("denomination") ?: "",
            pastor = getString("pastor") ?: "",
            website = getString("website") ?: ""
        )
    }

    private fun showAlert(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("확인", null)
            .show()
    }
}

/* ---------- 결과 리스트용 간단 모델/어댑터 ---------- */
data class Church(
    val name: String,
    val address: String,
    val denomination: String,
    val pastor: String,
    val website: String
)

class ChurchResultAdapter : RecyclerView.Adapter<TextVH>() {
    private val data = mutableListOf<Church>()
    fun submit(list: List<Church>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): TextVH {
        val tv = android.widget.TextView(parent.context).apply {
            setPadding(16, 24, 16, 24)
            textSize = 16f
        }
        return TextVH(tv)
    }
    override fun onBindViewHolder(holder: TextVH, position: Int) {
        val c = data[position]
        holder.bind("${c.name}\n${c.address} · ${c.denomination}")
    }
    override fun getItemCount() = data.size
}

class TextVH(private val tv: android.widget.TextView) : RecyclerView.ViewHolder(tv) {
    fun bind(text: String) { tv.text = text }
}
