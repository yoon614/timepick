package com.example.timepick

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider

class ApplyMethodActivity : AppCompatActivity() {

    // ViewModel
    private lateinit var viewModel: MainViewModel

    // UI 컴포넌트
    private lateinit var btnBack: ImageButton
    private lateinit var btnNext: Button

    private lateinit var cvNoResume: CardView
    private lateinit var tvNoResumeTitle: TextView
    private lateinit var tvNoResumeDesc: TextView

    private lateinit var cvCreateResume: CardView
    private lateinit var tvCreateResumeTitle: TextView
    private lateinit var tvCreateResumeDesc: TextView

    // 선택된 옵션 (1: 이력서 없이 지원, 2: 이력서 작성)
    private var selectedOption: Int = 0

    // 데이터 변수
    private var jobId: Int = 0
    private var companyName: String = ""
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apply_method)

        // 1. ViewModel 초기화
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // 2. Intent 데이터 및 UserID 로드
        jobId = intent.getIntExtra("JOB_ID", 0)
        companyName = intent.getStringExtra("COMPANY_NAME") ?: ""
        loadUserId()

        initViews()
        setupClickListeners()
    }

    private fun loadUserId() {
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        val userIdString = sharedPref.getString("USER_ID", "0") ?: "0"
        userId = userIdString.toIntOrNull() ?: 0
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        btnNext = findViewById(R.id.btn_next)

        // 카드 1 (이력서 없이 지원)
        cvNoResume = findViewById(R.id.cv_no_resume)
        tvNoResumeTitle = findViewById(R.id.tv_no_resume_title)
        tvNoResumeDesc = findViewById(R.id.tv_no_resume_desc)

        // 카드 2 (이력서 만들러 가기)
        cvCreateResume = findViewById(R.id.cv_create_resume)
        tvCreateResumeTitle = findViewById(R.id.tv_create_resume_title)
        tvCreateResumeDesc = findViewById(R.id.tv_create_resume_desc)
    }

    private fun setupClickListeners() {
        // 뒤로가기
        btnBack.setOnClickListener { finish() }

        // 옵션 1 클릭
        cvNoResume.setOnClickListener {
            selectOption(1)
        }

        // 옵션 2 클릭
        cvCreateResume.setOnClickListener {
            selectOption(2)
        }

        // [다음] 버튼 클릭 로직 수정
        btnNext.setOnClickListener {
            when (selectedOption) {
                1 -> {
                    // [Case A] 이력서 없이 지원
                    // 1. DB에 지원 내역 저장
                    if (userId != 0 && jobId != 0) {
                        viewModel.applyToJob(userId, jobId)
                    }

                    // 2. 지원 완료 화면으로 이동
                    val intent = Intent(this, ApplyCompleteActivity::class.java)
                    intent.putExtra("JOB_ID", jobId)
                    intent.putExtra("COMPANY_NAME", companyName)
                    startActivity(intent)

                    finish() // 현재 화면 종료
                }
                2 -> {
                    // [Case B] 이력서 만들러 가기
                    // 1. DB 저장 안 함 (지원여부 X)

                    // 2. 이력서 작성 화면으로 이동
                    val intent = Intent(this, ResumeEditActivity::class.java)
                    startActivity(intent)

                    // 3. 현재 화면 종료
                    // (사용자가 이력서 작성 후 '저장'하면 다시 공고 상세 페이지로 돌아가서 '지원하기'를 눌러야 하므로,
                    // 중간 단계인 이 화면은 닫음)
                    finish()
                }
                else -> {
                    Toast.makeText(this, "지원 방식을 선택해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 선택 시 시각적 효과 처리
    private fun selectOption(option: Int) {
        selectedOption = option

        // 색상 정의
        val selectedBgColor = Color.parseColor("#0068FF") // 파랑
        val defaultBgColor = Color.parseColor("#F7F8F9")  // 회색

        val selectedTextColor = Color.parseColor("#FFFFFF") // 흰색
        val defaultTitleColor = Color.parseColor("#18191C") // 검정
        val defaultDescColor = Color.parseColor("#777D88")  // 회색 텍스트

        if (option == 1) {
            // [옵션 1: 이력서 없이 지원] 활성화
            cvNoResume.setCardBackgroundColor(selectedBgColor)
            cvCreateResume.setCardBackgroundColor(defaultBgColor)

            tvNoResumeTitle.setTextColor(selectedTextColor)
            tvNoResumeDesc.setTextColor(selectedTextColor)

            tvCreateResumeTitle.setTextColor(defaultTitleColor)
            tvCreateResumeDesc.setTextColor(defaultDescColor)

        } else if (option == 2) {
            // [옵션 2: 이력서 작성] 활성화
            cvCreateResume.setCardBackgroundColor(selectedBgColor)
            cvNoResume.setCardBackgroundColor(defaultBgColor)

            tvCreateResumeTitle.setTextColor(selectedTextColor)
            tvCreateResumeDesc.setTextColor(selectedTextColor)

            tvNoResumeTitle.setTextColor(defaultTitleColor)
            tvNoResumeDesc.setTextColor(defaultDescColor)
        }
    }
}