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

class ResumeDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button

    private lateinit var tvName: TextView
    private lateinit var container: LinearLayout

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

        tvName = findViewById(R.id.tv_resume_detail_name)

        // ScrollView 안의 LinearLayout 찾기
        val scrollView = findViewById<View>(R.id.btn_resume_detail_back).parent.parent as ViewGroup
        for (i in 0 until scrollView.childCount) {
            val child = scrollView.getChildAt(i)
            if (child is android.widget.ScrollView) {
                container = child.getChildAt(0) as LinearLayout
                break
            }
        }
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

        tvName.text = name

        // 소개 찾아서 설정
        findAndSetText("소개", intro)

        // 필수 항목들
        findAndSetText("휴대폰", phone)
        findAndSetText("희망 지역", location)
        findAndSetText("희망 직종", job)

        // 경력은 "소개" 다음에 표시되므로 별도 처리 필요
        // 일단 간단하게 처리

        // 선택 항목: 학력 (없으면 숨김)
        if (education.isNullOrBlank()) {
            hideSection("최종 학력")
        } else {
            findAndSetText("최종 학력", education)
        }
    }

    private fun findAndSetText(label: String, value: String) {
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is LinearLayout) {
                for (j in 0 until child.childCount) {
                    val textView = child.getChildAt(j)
                    if (textView is TextView && textView.text.toString() == label) {
                        // 다음 TextView가 값
                        if (j + 1 < child.childCount) {
                            val valueView = child.getChildAt(j + 1)
                            if (valueView is TextView) {
                                valueView.text = value
                            }
                        }
                        return
                    }
                }
            } else if (child is TextView) {
                if (child.text.toString() == label) {
                    // 다음 child가 값
                    if (i + 1 < container.childCount) {
                        val valueView = container.getChildAt(i + 1)
                        if (valueView is TextView) {
                            valueView.text = value
                        }
                    }
                    return
                }
            }
        }
    }

    private fun hideSection(label: String) {
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is LinearLayout) {
                for (j in 0 until child.childCount) {
                    val textView = child.getChildAt(j)
                    if (textView is TextView && textView.text.toString() == label) {
                        child.visibility = View.GONE
                        return
                    }
                }
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