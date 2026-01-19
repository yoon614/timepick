package com.example.timepick

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

/**
 SignUpBossActivity - 사장님 회원가입 화면

 플로우:
  - 현재는 개발 중 안내 화면만 표시
 */
class SignUpBossActivity : AppCompatActivity() {

    // UI 컴포넌트
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_boss)

        initViews()

        setupClickListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_boss_back)
    }

    private fun setupClickListeners() {
        // 뒤로가기 버튼
        btnBack.setOnClickListener {
            finish()
        }
    }
}