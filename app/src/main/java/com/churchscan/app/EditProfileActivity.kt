package com.churchscan.app

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tvEditEmail = findViewById<TextView>(R.id.tvEditEmail)
        val etEditName = findViewById<EditText>(R.id.etEditName)
        val etEditPhone = findViewById<EditText>(R.id.etEditPhone)
        val etEditBirth = findViewById<EditText>(R.id.etEditBirth)
        val btnSaveChanges = findViewById<Button>(R.id.btnSaveChanges)

        val etNewPassword = findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)

        val user = auth.currentUser

        if (user != null) {
            val uid = user.uid
            tvEditEmail.text = "이메일 : ${user.email}"

            // 사용자 정보 불러오기
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        etEditName.setText(doc.getString("name") ?: "")
                        etEditPhone.setText(doc.getString("phone") ?: "")
                        etEditBirth.setText(doc.getString("birthdate") ?: "")
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }

            // 저장 버튼 클릭 시 Firestore에 업데이트
            btnSaveChanges.setOnClickListener {
                val updatedName = etEditName.text.toString().trim()
                val updatedPhone = etEditPhone.text.toString().trim()
                val updatedBirth = etEditBirth.text.toString().trim()

                if (updatedName.isEmpty() || updatedPhone.isEmpty() || updatedBirth.isEmpty()) {
                    Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val updates = mapOf(
                    "name" to updatedName,
                    "phone" to updatedPhone,
                    "birthdate" to updatedBirth
                )

                db.collection("users").document(uid).update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "정보가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, ProfileActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "저장 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            // 비밀번호 변경 버튼 클릭 시
            btnChangePassword.setOnClickListener {
                val newPassword = etNewPassword.text.toString().trim()
                val confirmPassword = etConfirmPassword.text.toString().trim()

                if (newPassword.length < 6) {
                    Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        Toast.makeText(this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                        etNewPassword.text.clear()
                        etConfirmPassword.text.clear()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "비밀번호 변경 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
