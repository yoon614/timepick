package com.example.timepick

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

/**
 SignUpCompleteActivity - 회원가입 완료 화면

 플로우:
  - 회원가입 완료 메시지 표시
  - "픽하러 가기" 버튼 클릭 -> TimePickActivity(타임테이블 화면)로 이동(구현 중)
 */
class SignUpCompleteActivity : AppCompatActivity() {

    // UI 컴포넌트
    private lateinit var tvUserName: TextView
    private lateinit var btnStart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_complete)

        // View 초기화
        initViews()

        // 사용자 이름 설정
        setupUserName()

        // 클릭 리스너 설정
        setupClickListeners()

        // 뒤로가기 버튼 비활성화
        setupBackPressHandler()
    }

    private fun initViews() {
        tvUserName = findViewById(R.id.tv_complete_user_name)
        btnStart = findViewById(R.id.btn_complete_start)
    }

    /**
     사용자 이름 설정
     */
    private fun setupUserName() {
        val userName = intent.getStringExtra("USER_NAME") ?: "사용자"
        tvUserName.text = "${userName}님,"
    }

    private fun setupClickListeners() {
        // 픽하러 가기 버튼 클릭 -> 타임테이블 화면으로 이동
        btnStart.setOnClickListener {
            // TODO: TimePickActivity로 이동 (타임테이블 화면)
            // val intent = Intent(this, TimePickActivity::class.java)
            // startActivity(intent)
            // finish()

            // 임시: 현재는 MainActivity로 이동
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    /**
     뒤로가기 버튼 비활성화
     */
    private fun setupBackPressHandler() {
        // OnBackPressedDispatcher를 사용하여 뒤로가기 동작 제어
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 회원가입 완료 화면에서는 뒤로가기 방지
                // 아무 동작도 하지 않음
            }
        })
    }
}