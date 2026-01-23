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

/**
 ResumeDetailActivity - 이력서 상세 화면

  플로우:
  - SharedPreferences에서 userId로 이력서 데이터 로드
  - 이력서 정보 표시 (이름, 소개, 휴대폰, 희망지역, 희망직종, 경력, 학력)
  - 선택 항목(학력)이 없으면 해당 섹션 숨김
  - 수정 버튼 -> ResumeEditActivity로 이동
  - 삭제 버튼 -> 확인 다이얼로그 후 SharedPreferences에서 이력서 삭제
 */

class ResumeDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button

    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resume_detail)

        loadUserId()
        initViews()
        loadResumeData()
        setupClickListeners()
    }

    private fun loadUserId() {
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        userId = sharedPref.getString("USER_ID", "") ?: ""
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_resume_detail_back)
        btnEdit = findViewById(R.id.btn_resume_edit)
        btnDelete = findViewById(R.id.btn_resume_delete)
    }

    private fun loadResumeData() {
        val pref = getSharedPreferences("TimePick_Resume_$userId", MODE_PRIVATE)

        if (!pref.contains("name")) {
            android.widget.Toast.makeText(this, "이력서가 없습니다.", android.widget.Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val name = pref.getString("name", "") ?: ""
        val intro = pref.getString("intro", "") ?: ""
        val phone = pref.getString("phone", "") ?: ""
        val location = pref.getString("location", "") ?: ""
        val job = pref.getString("job", "") ?: ""
        val career = pref.getString("career", "") ?: ""
        val education = pref.getString("education", null)

        // ScrollView > LinearLayout 찾기
        val container = findScrollViewContainer()
        if (container == null) {
            android.widget.Toast.makeText(this, "레이아웃을 찾을 수 없습니다.", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        // 모든 TextView 찾기
        val allTextViews = mutableListOf<TextView>()
        collectAllTextViews(container, allTextViews)

        // 데이터 순서대로 넣기
        var index = 0

        // 0: 날짜 (건너뛰기)
        // 1: 이름
        if (index + 1 < allTextViews.size) {
            allTextViews[index + 1].text = name
        }

        // "소개" 라벨 찾기 → 다음 TextView에 intro 넣기
        for (i in allTextViews.indices) {
            if (allTextViews[i].text.toString() == "소개") {
                if (i + 1 < allTextViews.size) {
                    allTextViews[i + 1].text = intro
                }
                break
            }
        }

        // "휴대폰", "주소", "이메일", "최종 학력", "희망 지역", "희망 직종", "경력사항" 찾아서 넣기
        for (i in allTextViews.indices) {
            val text = allTextViews[i].text.toString()

            when (text) {
                "휴대폰" -> {
                    if (i + 1 < allTextViews.size) {
                        allTextViews[i + 1].text = phone
                    }
                }
                "주소" -> {
                    // 주소는 선택 항목이지만 현재 입력받지 않으므로 숨김
                    (allTextViews[i].parent as? View)?.visibility = View.GONE
                }
                "이메일" -> {
                    // 이메일도 선택 항목이지만 현재 입력받지 않으므로 숨김
                    (allTextViews[i].parent as? View)?.visibility = View.GONE
                }
                "최종 학력" -> {
                    if (education.isNullOrBlank()) {
                        (allTextViews[i].parent as? View)?.visibility = View.GONE
                    } else {
                        if (i + 1 < allTextViews.size) {
                            allTextViews[i + 1].text = education
                        }
                    }
                }
                "자격 및 능력" -> {
                    // 자격증도 선택 항목이지만 현재 입력받지 않으므로 숨김
                    (allTextViews[i].parent as? View)?.visibility = View.GONE
                }
                "희망 지역" -> {
                    if (i + 1 < allTextViews.size) {
                        allTextViews[i + 1].text = location
                    }
                }
                "희망 직종" -> {
                    if (i + 1 < allTextViews.size) {
                        allTextViews[i + 1].text = job
                    }
                }
                "경력사항" -> {
                    if (i + 1 < allTextViews.size) {
                        allTextViews[i + 1].text = career
                    }
                }
            }
        }
    }

    private fun findScrollViewContainer(): LinearLayout? {
        val rootView = window.decorView.findViewById<View>(android.R.id.content)
        return findScrollViewRecursive(rootView)
    }

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

    private fun collectAllTextViews(view: View, list: MutableList<TextView>) {
        if (view is TextView) {
            list.add(view)
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                collectAllTextViews(view.getChildAt(i), list)
            }
        }
    }

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

    private fun deleteResume() {
        val pref = getSharedPreferences("TimePick_Resume_$userId", MODE_PRIVATE)
        pref.edit().clear().apply()

        android.widget.Toast.makeText(this, "이력서가 삭제되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
        finish()
    }
}