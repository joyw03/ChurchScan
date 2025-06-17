package com.churchscan.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // 🔁 Google 로그인 Activity Result Launcher
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                auth.signInWithCredential(credential)
                    .addOnSuccessListener {
                        val user = auth.currentUser
                        val db = FirebaseFirestore.getInstance()

                        val userData = mapOf(
                            "email" to (user?.email ?: ""),
                            "name" to (user?.displayName ?: ""),
                            "uid" to (user?.uid ?: "")
                        )

                        db.collection("users").document(user!!.uid).set(userData)
                            .addOnSuccessListener {
                                Log.d("Firestore", "Google 로그인 사용자 정보 저장 완료")
                            }
                            .addOnFailureListener {
                                Log.e("Firestore", "저장 실패: ${it.message}")
                            }

                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Google 로그인 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                    }

            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Google sign in failed", e)
                Toast.makeText(this, "Google 로그인 오류 발생", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoogle = findViewById<ImageButton>(R.id.btnGoogleCustom)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)

        // 📧 이메일 로그인
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "로그인 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // 🔵 Google 로그인
        btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        // ➕ 회원가입 화면으로 이동
        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // 🔁 자동 로그인 처리
    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
