package com.example.timepick

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

/**
 ResumeDetailActivity - 이력서 상세 화면

 플로우:
  - MainViewModel에서 userId로 이력서 데이터 로드
  - 이력서 정보 표시 (이름, 소개, 휴대폰, 희망지역, 희망직종, 경력, 학력)
  - 선택 항목(학력)이 없으면 해당 섹션 숨김
  - 수정 버튼 -> ResumeEditActivity로 이동
  - 삭제 버튼 -> 확인 다이얼로그 후 DB에서 이력서 삭제
 */
class ResumeDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var btnBack: ImageButton
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button

    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resume_detail)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]

        loadUserId()
        initViews()
        loadResumeData()
        setupClickListeners()
    }

    // SharedPreferences에서 userId 로드
    private fun loadUserId() {
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        val userIdString = sharedPref.getString("USER_ID", "0") ?: "0"
        userId = userIdString.toIntOrNull() ?: 0
    }

    // View 초기화
    private fun initViews() {
        btnBack = findViewById(R.id.btn_resume_detail_back)
        btnEdit = findViewById(R.id.btn_resume_edit)
        btnDelete = findViewById(R.id.btn_resume_delete)
    }

    // MainViewModel에서 이력서 데이터 로드
    private fun loadResumeData() {
        viewModel.loadResume(userId) { resume ->
            if (resume == null) {
                android.widget.Toast.makeText(this, "이력서가 없습니다.", android.widget.Toast.LENGTH_SHORT).show()
                finish()
                return@loadResume
            }

            val container = findScrollViewContainer()
            if (container != null) {
                updateTextViews(container, resume)
            }
        }
    }

    // ScrollView 내부의 LinearLayout 찾기 (재귀)
    private fun findScrollViewContainer(): LinearLayout? {
        val rootView = window.decorView.findViewById<View>(android.R.id.content)
        return findScrollViewRecursive(rootView)
    }

    // ScrollView를 재귀적으로 찾는 헬퍼 함수
    private fun findScrollViewRecursive(view: View): LinearLayout? {
        if (view is android.widget.ScrollView) {
            val child = view.getChildAt(0)
            if (child is LinearLayout) {
                return child
            }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val result = findScrollViewRecursive(view.getChildAt(i))
                if (result != null) return result
            }
        }
        return null
    }

    // DB 데이터로 TextView 업데이트
    private fun updateTextViews(container: LinearLayout, resume: com.example.timepick.data.entity.ResumeEntity) {
        val allTextViews = mutableListOf<TextView>()
        collectAllTextViews(container, allTextViews)

        for (i in allTextViews.indices) {
            val tv = allTextViews[i]
            val text = tv.text.toString()

            when (text) {
                "김슈니" -> tv.text = resume.name
                "안녕하세요, 성실한 아르바이트생 김슈니입니다." -> tv.text = resume.intro
                "010-1234-5678" -> tv.text = resume.phone
                "서울 노원구" -> tv.text = resume.desiredRegion
                "베이커리, 카페, 음식점" -> tv.text = resume.desiredJob
                "대학(4년제)" -> {
                    if (resume.education.isNullOrBlank()) {
                        (tv.parent as? View)?.visibility = View.GONE
                    } else {
                        tv.text = resume.education
                    }
                }
            }

            if (text == "경력사항" && i + 1 < allTextViews.size) {
                allTextViews[i + 1].text = resume.career
            }
        }
    }

    // 모든 TextView 수집
    private fun collectAllTextViews(view: View, list: MutableList<TextView>) {
        if (view is TextView) {
            list.add(view)
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                collectAllTextViews(view.getChildAt(i), list)
            }
        }
    }

    // 클릭 리스너 설정
    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }

        btnEdit.setOnClickListener {
            val intent = Intent(this, ResumeEditActivity::class.java)
            startActivity(intent)
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmDialog()
        }
    }

    // 이력서 삭제 확인 다이얼로그 표시
    private fun showDeleteConfirmDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("이력서 삭제")
            .setMessage("정말 이력서를 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                deleteResume()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // DB에서 이력서 삭제
    private fun deleteResume() {
        viewModel.deleteResume(userId) { success ->
            if (success) {
                android.widget.Toast.makeText(this, "이력서가 삭제되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
                finish()
            } else {
                android.widget.Toast.makeText(this, "이력서 삭제에 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}