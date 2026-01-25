package com.example.timepick

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

/**
 MainActivity - 앱 시작 화면 (회원가입/로그인 선택)

 플로우:
  - 회원가입 버튼 클릭 -> RoleSelectActivity로 이동
  - 로그인 버튼 클릭 -> LoginActivity로 이동
 */
class MainActivity : AppCompatActivity() {

    // UI 컴포넌트
    private lateinit var btnSignup: Button
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 앱 재실행 시 로그인 상태 유지
        // SharedPreferences으로 로그인 여부 조회
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)

        // 이미 로그인 된 상태 -> TimePick 화면으로 이동
        if(isLoggedIn) {
            startActivity(Intent(this, TimePickActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        // View 초기화
        initViews()

        // 클릭 리스너 설정
        setupClickListeners()
    }

    /**
     View 초기화
     */
    private fun initViews() {
        btnSignup = findViewById(R.id.btn_intro_signup)
        btnLogin = findViewById(R.id.btn_intro_login)
    }

    /**
     클릭 이벤트 리스너 설정
     */
    private fun setupClickListeners() {
        // 회원가입 버튼 클릭 -> 역할 선택 화면으로 이동
        btnSignup.setOnClickListener {
            val intent = Intent(this, RoleSelectActivity::class.java)
            startActivity(intent)
        }

        // 로그인 버튼 클릭 -> 로그인 화면으로 이동
        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}