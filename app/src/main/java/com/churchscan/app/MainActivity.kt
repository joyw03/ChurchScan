package com.churchscan.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var etMainSearch: EditText
    private lateinit var btnMainSearch: Button
    private lateinit var btnUploadImage: Button

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
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavView)

        btnMainSearch.setOnClickListener {
            val query = etMainSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra("search_query", query)
                startActivity(intent)
            } else {
                Toast.makeText(this, "검색어를 입력해주세요", Toast.LENGTH_SHORT).show()
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
    }
}
