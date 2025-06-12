package com.churchscan.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvUserBirth: TextView
    private lateinit var btnGoToEdit: Button
    private lateinit var btnLogout: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvUserName = findViewById(R.id.tvUserName)
        tvUserPhone = findViewById(R.id.tvUserPhone)
        tvUserBirth = findViewById(R.id.tvUserBirth)
        btnGoToEdit = findViewById(R.id.btnGoToEdit)
        btnLogout = findViewById(R.id.btnLogout)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            tvUserEmail.text = "이메일 : ${user.email}"

            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: ""
                        val phone = document.getString("phone") ?: ""
                        val birth = document.getString("birthdate") ?: ""

                        tvUserName.text = "이름 : $name"
                        tvUserPhone.text = "전화번호 : $phone"
                        tvUserBirth.text = "생년월일 : $birth"
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "정보 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
        }

        btnGoToEdit.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("로그아웃")
                .setMessage("정말 로그아웃하시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("취소", null)
                .show()
        }

        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavView)
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.menu_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    finish()
                    true
                }
                R.id.menu_profile -> true
                else -> false
            }
        }

        bottomNavView.selectedItemId = R.id.menu_profile
    }
}
