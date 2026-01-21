package com.example.timepick

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

/**
 * ApplyCompleteActivity - 아르바이트 지원 완료 화면
 *
 * 플로우:
 * - 지원 완료 메시지 표시
 * - 확인 버튼 클릭 -> JobListActivity(공고 리스트)로 이동
 * - 뒤로가기 방지 (지원 완료는 되돌릴 수 없음)
 */
class ApplyCompleteActivity : AppCompatActivity() {

    // UI 컴포넌트
    private lateinit var btnConfirm: Button

    // 지원한 공고 제목
    private var jobTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apply_complete)

        // Intent에서 데이터 받기
        jobTitle = intent.getStringExtra("JOB_TITLE") ?: ""

        // View 초기화
        initViews()

        // 클릭 리스너 설정
        setupClickListeners()

        // 뒤로가기 버튼 비활성화
        setupBackPressHandler()
    }

    /**
     * View 초기화
     */
    private fun initViews() {
        btnConfirm = findViewById(R.id.btn_complete_confirm)
    }

    /**
     * 클릭 이벤트 리스너 설정
     */
    private fun setupClickListeners() {
        // 확인 버튼 클릭 -> 공고 리스트로 이동
        btnConfirm.setOnClickListener {
            // 공고 리스트 화면으로 이동 (스택 정리)
            val intent = Intent(this, JobListActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    /**
     * 뒤로가기 버튼 비활성화
     */
    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 지원 완료 화면에서는 뒤로가기 방지
                // 확인 버튼을 통해서만 이동 가능
            }
        })
    }
}