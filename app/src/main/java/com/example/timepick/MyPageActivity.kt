package com.example.timepick

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 MyPageActivity

 플로우:
  - SharedPreferences에서 사용자 정보(이름) 표시
  - 이력서 존재 여부 확인 -> 있으면 카드 표시, 없으면 + 버튼 표시
  - 이력서 카드/빈 영역 클릭 -> ResumeDetailActivity 또는 ResumeEditActivity로 이동
  - 프로필 수정 버튼 -> EditProfileActivity로 이동
 */

class MyPageActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var btnEditProfile: TextView
    private lateinit var btnAddResume: ImageButton
    private lateinit var layoutResumeCardContainer: FrameLayout
    private lateinit var layoutResumeEmpty: LinearLayout
    private lateinit var bottomNav: BottomNavigationView

    private var userId: String = ""
    private var userName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        loadUserInfo()
        initViews()
        displayUserInfo()
        setupClickListeners()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()
        displayUserInfo()
        displayResumeStatus()
    }

    private fun loadUserInfo() {
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        userId = sharedPref.getString("USER_ID", "") ?: ""
        userName = sharedPref.getString("USER_NAME", "") ?: ""
    }

    private fun initViews() {
        tvUserName = findViewById(R.id.tv_user_name)
        btnEditProfile = findViewById(R.id.btn_edit_profile)
        btnAddResume = findViewById(R.id.btn_add_resume)
        layoutResumeCardContainer = findViewById(R.id.layout_resume_card_container)
        layoutResumeEmpty = findViewById(R.id.layout_resume_empty)
        bottomNav = findViewById(R.id.bottom_navigation)
    }

    private fun displayUserInfo() {
        tvUserName.text = "${userName}님, 환영합니다."
    }

    private fun displayResumeStatus() {
        val pref = getSharedPreferences("TimePick_Resume_$userId", MODE_PRIVATE)
        val hasResume = pref.getBoolean("has_resume", false)

        if (hasResume) {
            // 이력서 있음 → 카드 표시
            layoutResumeCardContainer.visibility = View.VISIBLE
            layoutResumeEmpty.visibility = View.GONE
            btnAddResume.visibility = View.GONE
            setupResumeCard()
        } else {
            // 이력서 없음 → + 버튼 표시
            layoutResumeCardContainer.visibility = View.GONE
            layoutResumeEmpty.visibility = View.VISIBLE
            btnAddResume.visibility = View.VISIBLE
        }
    }

    private fun setupResumeCard() {
        try {
            // item_resume_card.xml inflate
            layoutResumeCardContainer.removeAllViews()
            val cardView = layoutInflater.inflate(R.layout.item_resume_card, layoutResumeCardContainer, false)
            layoutResumeCardContainer.addView(cardView)

            // 이력서 데이터 표시
            val pref = getSharedPreferences("TimePick_Resume_$userId", MODE_PRIVATE)
            val name = pref.getString("name", "") ?: ""
            val location = pref.getString("location", "") ?: ""
            val job = pref.getString("job", "") ?: ""

            val tvName = cardView.findViewById<TextView>(R.id.tv_resume_name)
            val tvLocation = cardView.findViewById<TextView>(R.id.tv_resume_location)
            val tvCategory = cardView.findViewById<TextView>(R.id.tv_resume_category)
            val btnDetail = cardView.findViewById<ImageButton>(R.id.btn_resume_detail)

            tvName?.text = name
            tvLocation?.text = location
            tvCategory?.text = job

            // 클릭 이벤트
            val goToDetail = {
                try {
                    val intent = Intent(this, ResumeDetailActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            cardView.setOnClickListener { goToDetail() }
            btnDetail?.setOnClickListener { goToDetail() }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "카드 로드 오류: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupClickListeners() {
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        btnAddResume.setOnClickListener {
            val intent = Intent(this, ResumeEditActivity::class.java)
            startActivity(intent)
        }

        layoutResumeEmpty.setOnClickListener {
            val intent = Intent(this, ResumeEditActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        bottomNav.selectedItemId = R.id.nav_mypage

        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_calendar -> {
                    Toast.makeText(this@MyPageActivity, "캘린더 기능은 준비 중입니다.", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_home -> {
                    val intent = Intent(this@MyPageActivity, TimePickActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_mypage -> {
                    true
                }
                else -> false
            }
        }
    }
}