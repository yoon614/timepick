package com.example.timepick

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import android.graphics.Color

/**
 RoleSelectActivity - 사용자 역할 선택 화면

 플로우:
  - 알바생 카드 선택 -> SignUpWorkerActivity로 이동
  - 사장님 카드 선택 -> SignUpBossActivity로 이동
 */
class RoleSelectActivity : AppCompatActivity() {

    // UI 컴포넌트
    private lateinit var btnBack: ImageButton
    private lateinit var cvWorker: CardView
    private lateinit var cvBoss: CardView
    private lateinit var btnNext: Button

    private lateinit var tvWorkerTitle: TextView
    private lateinit var tvWorkerDesc: TextView
    private lateinit var tvBossTitle: TextView
    private lateinit var tvBossDesc: TextView

    // 선택된 역할 (null: 선택 안 함, "worker": 알바생, "boss": 사장님)
    private var selectedRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_select)

        // View 초기화
        initViews()

        // 클릭 리스너 설정
        setupClickListeners()
    }


    private fun initViews() {
        btnBack = findViewById(R.id.btn_role_back)
        cvWorker = findViewById(R.id.cv_role_worker)
        cvBoss = findViewById(R.id.cv_role_boss)
        btnNext = findViewById(R.id.btn_role_next)

        tvWorkerTitle = findViewById(R.id.tv_worker_title)
        tvWorkerDesc = findViewById(R.id.tv_worker_desc)
        tvBossTitle = findViewById(R.id.tv_boss_title)
        tvBossDesc = findViewById(R.id.tv_boss_desc)
    }


    private fun setupClickListeners() {
        // 뒤로가기 버튼
        btnBack.setOnClickListener {
            finish()
        }

        // 알바생 카드 선택
        cvWorker.setOnClickListener {
            selectRole("worker")
        }

        // 사장님 카드 선택
        cvBoss.setOnClickListener {
            selectRole("boss")
        }

        // 다음 버튼 클릭
        btnNext.setOnClickListener {
            when (selectedRole) {
                "worker" -> {
                    // 알바생 회원가입 화면으로 이동
                    val intent = Intent(this, SignUpWorkerActivity::class.java)
                    startActivity(intent)
                }
                "boss" -> {
                    // 사장님 회원가입 화면으로 이동 (현재 개발 중)
                    val intent = Intent(this, SignUpBossActivity::class.java)
                    startActivity(intent)
                }
                else -> {
                    // 역할을 선택하지 않은 경우
                    Toast.makeText(this, "역할을 선택해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     역할 선택 처리
     */
    private fun selectRole(role: String) {
        selectedRole = role

        // 색상 정의
        val selectedBgColor = Color.parseColor("#0068FF") // 선택된 배경 (파랑)
        val defaultBgColor = Color.parseColor("#F7F8F9")  // 기본 배경 (회색)

        val selectedTextColor = Color.parseColor("#FFFFFF") // 선택된 글자 (흰색)
        val defaultTitleColor = Color.parseColor("#18191C") // 기본 제목 (검정)
        val defaultDescColor = Color.parseColor("#777D88")  // 기본 설명 (회색)

        when (role) {
            "worker" -> {
                // [아르바이트생 선택]
                // 1. 카드 배경 변경
                cvWorker.setCardBackgroundColor(selectedBgColor)
                cvBoss.setCardBackgroundColor(defaultBgColor)

                // 2. 텍스트 색상 변경 (아르바이트생 -> 흰색)
                tvWorkerTitle.setTextColor(selectedTextColor)
                tvWorkerDesc.setTextColor(selectedTextColor)

                // 3. 텍스트 색상 원복 (사장님 -> 기본색)
                tvBossTitle.setTextColor(defaultTitleColor)
                tvBossDesc.setTextColor(defaultDescColor)
            }
            "boss" -> {
                // [사장님 선택]
                // 1. 카드 배경 변경
                cvBoss.setCardBackgroundColor(selectedBgColor)
                cvWorker.setCardBackgroundColor(defaultBgColor)

                // 2. 텍스트 색상 변경 (사장님 -> 흰색)
                tvBossTitle.setTextColor(selectedTextColor)
                tvBossDesc.setTextColor(selectedTextColor)

                // 3. 텍스트 색상 원복 (아르바이트생 -> 기본색)
                tvWorkerTitle.setTextColor(defaultTitleColor)
                tvWorkerDesc.setTextColor(defaultDescColor)
            }
        }
    }
}