package com.example.timepick

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * MyPageActivity - 마이페이지 화면
 *
 * 플로우:
 * - SharedPreferences에서 사용자 정보(이름) 표시
 * - MainViewModel로 이력서 존재 여부 확인 -> 있으면 카드 표시, 없으면 + 버튼 표시
 * - 이력서 카드/빈 영역 클릭 -> ResumeDetailActivity 또는 ResumeEditActivity로 이동
 * - 프로필 수정 버튼 -> EditProfileActivity로 이동
 * - 더보기 버튼 -> 회원탈퇴/로그아웃 메뉴 표시
 * - 하단 네비게이션 (캘린더=준비중, 홈=타임테이블, 마이페이지=현재화면)
 */
class MyPageActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var tvUserName: TextView
    private lateinit var btnEditProfile: TextView
    private lateinit var btnMore: ImageButton
    private lateinit var btnAddResume: ImageButton
    private lateinit var layoutResumeCardContainer: FrameLayout
    private lateinit var layoutResumeEmpty: LinearLayout
    private lateinit var cvMoreMenu: CardView
    private lateinit var btnMenuWithdraw: TextView
    private lateinit var btnMenuLogout: TextView
    private lateinit var bottomNav: BottomNavigationView

    private var userId: Int = 0
    private var userName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]

        loadUserInfo()
        initViews()
        displayUserInfo()
        setupClickListeners()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()  // SharedPreferences에서 최신 정보 로드
        displayUserInfo()  // 화면에 표시
        displayResumeStatus()
    }

    // SharedPreferences에서 사용자 정보 로드
    private fun loadUserInfo() {
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        val userIdString = sharedPref.getString("USER_ID", "0") ?: "0"
        userId = userIdString.toIntOrNull() ?: 0
        userName = sharedPref.getString("USER_NAME", "") ?: ""
    }

    // View 초기화
    private fun initViews() {
        tvUserName = findViewById(R.id.tv_user_name)
        btnEditProfile = findViewById(R.id.btn_edit_profile)
        btnMore = findViewById(R.id.btn_more)
        btnAddResume = findViewById(R.id.btn_add_resume)
        layoutResumeCardContainer = findViewById(R.id.layout_resume_card_container)
        layoutResumeEmpty = findViewById(R.id.layout_resume_empty)
        cvMoreMenu = findViewById(R.id.cv_more_menu)
        btnMenuWithdraw = findViewById(R.id.btn_menu_withdraw)
        btnMenuLogout = findViewById(R.id.btn_menu_logout)
        bottomNav = findViewById(R.id.bottom_navigation)
    }

    // 사용자 이름 표시
    private fun displayUserInfo() {
        tvUserName.text = "${userName}님, 환영합니다."
    }

    // 이력서 존재 여부에 따라 카드/빈 화면 표시
    private fun displayResumeStatus() {
        viewModel.checkResumeExists(userId) { exists ->
            if (exists) {
                layoutResumeCardContainer.visibility = View.VISIBLE
                layoutResumeEmpty.visibility = View.GONE
                btnAddResume.visibility = View.GONE
                setupResumeCard()
            } else {
                layoutResumeCardContainer.visibility = View.GONE
                layoutResumeEmpty.visibility = View.VISIBLE
                btnAddResume.visibility = View.VISIBLE
            }
        }
    }

    // 이력서 카드 설정 (클릭 이벤트)
    private fun setupResumeCard() {
        try {
            layoutResumeCardContainer.removeAllViews()
            val cardView = layoutInflater.inflate(R.layout.item_resume_card, layoutResumeCardContainer, false)
            layoutResumeCardContainer.addView(cardView)

            viewModel.loadResume(userId) { resume ->
                resume?.let {
                    cardView.findViewById<TextView>(R.id.tv_resume_name)?.text = it.name
                    cardView.findViewById<TextView>(R.id.tv_resume_location)?.text = it.desiredRegion
                    cardView.findViewById<TextView>(R.id.tv_resume_category)?.text = it.desiredJob
                }
            }

            val btnDetail = cardView.findViewById<ImageButton>(R.id.btn_resume_detail)
            val goToDetail = {
                val intent = Intent(this, ResumeDetailActivity::class.java)
                startActivity(intent)
            }

            cardView.setOnClickListener { goToDetail() }
            btnDetail?.setOnClickListener { goToDetail() }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "카드 로드 오류: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // 클릭 리스너 설정
    private fun setupClickListeners() {
        // 프로필 수정
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // 이력서 추가
        btnAddResume.setOnClickListener {
            val intent = Intent(this, ResumeEditActivity::class.java)
            startActivity(intent)
        }

        // 빈 화면 클릭
        layoutResumeEmpty.setOnClickListener {
            val intent = Intent(this, ResumeEditActivity::class.java)
            startActivity(intent)
        }

        // 더보기 버튼 (메뉴 토글)
        btnMore.setOnClickListener {
            cvMoreMenu.visibility = if (cvMoreMenu.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        // 회원탈퇴
        btnMenuWithdraw.setOnClickListener {
            cvMoreMenu.visibility = View.GONE
            showWithdrawDialog()
        }

        // 로그아웃
        btnMenuLogout.setOnClickListener {
            cvMoreMenu.visibility = View.GONE
            showLogoutDialog()
        }
    }

    // 회원탈퇴 확인 다이얼로그
    private fun showWithdrawDialog() {
        AlertDialog.Builder(this)
            .setTitle("회원탈퇴")
            .setMessage("정말 탈퇴하시겠습니까?\n모든 데이터가 삭제되며 복구할 수 없습니다.")
            .setPositiveButton("탈퇴") { _, _ ->
                withdrawUser()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 로그아웃 확인 다이얼로그
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("로그아웃")
            .setMessage("로그아웃 하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                logout()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 회원탈퇴 처리
    private fun withdrawUser() {
        viewModel.deleteUser(userId) { success ->
            if (success) {
                // SharedPreferences 초기화
                val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
                sharedPref.edit().clear().apply()

                Toast.makeText(this, "회원탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()

                // 로그인 화면으로 이동
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "회원탈퇴에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 로그아웃 처리
    private fun logout() {
        // 로그인 상태만 false로 변경 (userId, userName 유지)
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        sharedPref.edit().apply {
            putBoolean("IS_LOGGED_IN", false)
            apply()
        }

        Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()

        // 로그인 화면으로 이동
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // 하단 네비게이션 설정
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