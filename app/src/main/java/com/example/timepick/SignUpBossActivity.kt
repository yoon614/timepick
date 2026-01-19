package com.example.timepick

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

/**
 * SignUpBossActivity - 사장님 회원가입 화면 (현재 개발 중)
 *
 * 플로우:
 * - 현재는 개발 중 안내 화면만 표시
 * - 추후 사장님 회원가입 기능 구현 예정
 */
class SignUpBossActivity : AppCompatActivity() {

    // UI 컴포넌트
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_boss)

        // View 초기화
        initViews()

        // 클릭 리스너 설정
        setupClickListeners()
    }

    /**
     * View 초기화
     */
    private fun initViews() {
        btnBack = findViewById(R.id.btn_boss_back)
    }

    /**
     * 클릭 이벤트 리스너 설정
     */
    private fun setupClickListeners() {
        // 뒤로가기 버튼
        btnBack.setOnClickListener {
            finish()
        }
    }
}