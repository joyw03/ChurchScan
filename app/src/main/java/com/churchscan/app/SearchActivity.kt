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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SearchActivity : AppCompatActivity() {

    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var historyAdapter: SearchHistoryAdapter

    private lateinit var editText: EditText
    private lateinit var searchButton: Button
    private lateinit var rvHistory: RecyclerView

    // 결과 리스트
    private lateinit var rvResults: RecyclerView
    private lateinit var resultsAdapter: ChurchResultAdapter
    private var progressBar: ProgressBar? = null
    private var tvResultsTitle: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Firebase
        FirebaseApp.initializeApp(this)

        // Prefs
        prefsHelper = SharedPreferencesHelper(this)

        // 뷰 바인딩
        editText = findViewById(R.id.etSearchText)
        searchButton = findViewById(R.id.btnSearchText)
        val btnClearAll = findViewById<Button>(R.id.btnClearAll)
        rvHistory = findViewById(R.id.recyclerViewSearchHistory)

        rvResults = findViewById(R.id.rvResults)
        progressBar = findViewById(R.id.progressSearch)
        tvResultsTitle = findViewById(R.id.tvResultsTitle)

        // 최근 검색 어댑터
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

        // 결과 리스트 어댑터
        resultsAdapter = ChurchResultAdapter()
        rvResults.layoutManager = LinearLayoutManager(this)
        rvResults.adapter = resultsAdapter

        // 인텐트로 넘어온 검색어 처리
        val passedQuery = intent.getStringExtra("search_query")
        if (!passedQuery.isNullOrEmpty()) {
            editText.setText(passedQuery)
            prefsHelper.saveRecentSearch(passedQuery)
            if (historyList.contains(passedQuery)) historyAdapter.removeItem(passedQuery)
            historyAdapter.notifyDataSetChanged()
            doSearch(passedQuery)
        }

        // 검색 버튼
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

        // 엔터 -> 검색
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchButton.performClick()
                true
            } else false
        }

        // 최근검색 전체 삭제
        btnClearAll.setOnClickListener {
            prefsHelper.clearAllSearches()
            historyAdapter.clearAll()
        }

        // 하단 네비
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavView)
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish(); true
                }
                R.id.menu_search -> true
                R.id.menu_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish(); true
                }
                else -> false
            }
        }
        bottomNavView.selectedItemId = R.id.menu_search
    }

    /** 띄어쓰기/대소문자 무시 정규화 */
    private fun normalizeForSearch(input: String): String =
        input.trim().lowercase().replace("\\s+".toRegex(), "")

    /** 교회 검색(name_norm prefix) + 이단 여부 체크 */
    private fun doSearch(rawKeyword: String) {
        val q = normalizeForSearch(rawKeyword)
        if (q.isEmpty()) {
            resultsAdapter.submit(emptyList())
            return
        }

        progressBar?.visibility = android.view.View.VISIBLE
        tvResultsTitle?.text = "검색 결과"

        val db = FirebaseFirestore.getInstance()

        // 교회 리스트 검색
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
                showAlert("오류", "검색 중 오류가 발생했습니다: ${e.message}")
            }

        // 이단 여부 체크 (title_norm + reason 안전 처리)
        searchHeresyByChurchName(rawKeyword)

        // 검색 후 입력창 초기화
        editText.setText("")
    }

    /** jesus114_decisions 이단 여부 조회 (띄어쓰기·대소문자 무시 + reason 타입 안전) */
    private fun searchHeresyByChurchName(keyword: String) {
        val db = FirebaseFirestore.getInstance()
        val normalized = normalizeForSearch(keyword)

        db.collection("jesus114_decisions")
            .get()
            .addOnSuccessListener { documents ->
                var found = false
                for (doc in documents) {
                    val titleNorm = doc.getString("title_norm") ?: ""

                    // reason: String / List / Map 어떤 타입이든 문자열로 병합
                    val reasonText = when (val r = doc.get("reason")) {
                        is String -> r
                        is List<*> -> r.joinToString(" ") { it?.toString().orEmpty() }
                        is Map<*, *> -> r.values.joinToString(" ") { it?.toString().orEmpty() }
                        else -> ""
                    }

                    if (titleNorm.contains(normalized) ||
                        reasonText.lowercase().contains(keyword.lowercase())) {
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
