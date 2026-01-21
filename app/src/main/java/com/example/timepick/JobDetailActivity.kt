package com.example.timepick

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * JobDetailActivity - 공고 상세 화면
 *
 * 플로우:
 * - 공고의 상세 정보 표시
 * - 이력서 확인 버튼 -> 마이페이지(이력서)로 이동 (추후 구현)
 * - 지원하기 버튼 -> ApplyCompleteActivity로 이동
 */
class JobDetailActivity : AppCompatActivity() {

    // UI 컴포넌트
    private lateinit var btnBack: ImageButton
    private lateinit var tvCompanyName: TextView
    private lateinit var btnResume: Button
    private lateinit var btnApply: Button

    // 공고 데이터
    private var jobId: Int = 0
    private var jobTitle: String = ""
    private var jobLocation: String = ""
    private var jobCategory: String = ""
    private var jobMatchRate: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_detail)

        // Intent에서 공고 데이터 받기
        getJobDataFromIntent()

        // View 초기화
        initViews()

        // 공고 정보 표시
        displayJobInfo()

        // 클릭 리스너 설정
        setupClickListeners()
    }

    /**
     * Intent에서 공고 데이터 받기
     */
    private fun getJobDataFromIntent() {
        jobId = intent.getIntExtra("JOB_ID", 0)
        jobTitle = intent.getStringExtra("JOB_TITLE") ?: ""
        jobLocation = intent.getStringExtra("JOB_LOCATION") ?: ""
        jobCategory = intent.getStringExtra("JOB_CATEGORY") ?: ""
        jobMatchRate = intent.getIntExtra("JOB_MATCH_RATE", 0)
    }

    /**
     * View 초기화
     */
    private fun initViews() {
        btnBack = findViewById(R.id.btn_detail_back)
        tvCompanyName = findViewById(R.id.tv_detail_company_name)
        btnResume = findViewById(R.id.btn_detail_resume)
        btnApply = findViewById(R.id.btn_detail_apply)
    }

    /**
     * 공고 정보 표시
     * TODO: 추후 실제 공고 데이터로 모든 필드 채우기
     */
    private fun displayJobInfo() {
        tvCompanyName.text = jobTitle

        // TODO: 나머지 상세 정보들도 표시
        // - 급여, 업종, 고용형태
        // - 모집 마감일, 모집 인원, 모집 분야
        // - 학력, 우대사항
        // - 근무지역, 지도
        // - 상세 요강
    }

    /**
     * 클릭 이벤트 리스너 설정
     */
    private fun setupClickListeners() {
        // 뒤로가기 버튼
        btnBack.setOnClickListener {
            finish()
        }

        // 이력서 확인 버튼
        btnResume.setOnClickListener {
            // TODO: 마이페이지(이력서) 화면으로 이동
            Toast.makeText(this, "이력서 기능은 추후 구현 예정입니다.", Toast.LENGTH_SHORT).show()
        }

        // 지원하기 버튼
        btnApply.setOnClickListener {
            applyForJob()
        }
    }

    /**
     * 지원하기 처리
     * TODO: 추후 서버 API 연동 또는 DB 저장
     */
    private fun applyForJob() {
        // 로그인 상태 확인
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)

        if (!isLoggedIn) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            // TODO: 로그인 화면으로 이동
            return
        }

        // TODO: 실제 지원 데이터 저장
        // - 사용자 ID
        // - 공고 ID
        // - 지원 일시
        // DB 또는 서버에 저장

        // 지원 완료 화면으로 이동
        val intent = Intent(this, ApplyCompleteActivity::class.java)
        intent.putExtra("JOB_TITLE", jobTitle)
        startActivity(intent)
    }
}