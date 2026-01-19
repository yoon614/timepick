package com.example.timepick

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

/**
 * RoleSelectActivity - 사용자 역할 선택 화면
 *
 * 플로우:
 * - 알바생 카드 선택 -> SignUpWorkerActivity로 이동
 * - 사장님 카드 선택 -> SignUpBossActivity로 이동 (현재 개발 중)
 */
class RoleSelectActivity : AppCompatActivity() {

    // UI 컴포넌트
    private lateinit var btnBack: ImageButton
    private lateinit var cvWorker: CardView
    private lateinit var cvBoss: CardView
    private lateinit var btnNext: Button

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

    /**
     * View 초기화
     */
    private fun initViews() {
        btnBack = findViewById(R.id.btn_role_back)
        cvWorker = findViewById(R.id.cv_role_worker)
        cvBoss = findViewById(R.id.cv_role_boss)
        btnNext = findViewById(R.id.btn_role_next)
    }

    /**
     * 클릭 이벤트 리스너 설정
     */
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
     * 역할 선택 처리
     */
    private fun selectRole(role: String) {
        selectedRole = role

        // 선택 상태에 따른 UI 업데이트
        when (role) {
            "worker" -> {
                cvWorker.setCardBackgroundColor(getColor(R.color.selected_card_color))
                cvBoss.setCardBackgroundColor(getColor(R.color.default_card_color))
            }
            "boss" -> {
                cvBoss.setCardBackgroundColor(getColor(R.color.selected_card_color))
                cvWorker.setCardBackgroundColor(getColor(R.color.default_card_color))
            }
        }
    }
}