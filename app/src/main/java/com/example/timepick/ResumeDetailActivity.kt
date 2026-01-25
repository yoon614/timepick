package com.example.timepick

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class ResumeDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var btnBack: ImageButton
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button

    private var userId: Int = 0

    // ActivityResultLauncher 사용
    private val editResumeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // 수정 완료 후 바로 데이터 다시 로드
            android.util.Log.d("ResumeDetail", "Edit completed, reloading data...")
            loadResumeData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resume_detail)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]

        loadUserId()
        initViews()

        // 데이터 로드 전까지 ScrollView 숨기기
        hideScrollView()

        loadResumeData()
        setupClickListeners()
    }

    // ScrollView 숨기기
    private fun hideScrollView() {
        findScrollView()?.visibility = View.INVISIBLE
    }

    // ScrollView 보이기
    private fun showScrollView() {
        findScrollView()?.visibility = View.VISIBLE
    }

    // ScrollView 찾기
    private fun findScrollView(): android.widget.ScrollView? {
        return findScrollViewInView(window.decorView)
    }

    private fun findScrollViewInView(view: View): android.widget.ScrollView? {
        if (view is android.widget.ScrollView) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val found = findScrollViewInView(view.getChildAt(i))
                if (found != null) return found
            }
        }
        return null
    }

    private fun loadUserId() {
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        val userIdString = sharedPref.getString("USER_ID", "0") ?: "0"
        userId = userIdString.toIntOrNull() ?: 0
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_resume_detail_back)
        btnEdit = findViewById(R.id.btn_resume_edit)
        btnDelete = findViewById(R.id.btn_resume_delete)
    }

    private fun loadResumeData() {
        android.util.Log.d("ResumeDetail", "Loading resume data for userId: $userId")
        viewModel.loadResume(userId) { resume ->
            if (resume == null) {
                android.util.Log.d("ResumeDetail", "Resume is null")
                android.widget.Toast.makeText(this, "이력서가 없습니다.", android.widget.Toast.LENGTH_SHORT).show()
                finish()
                return@loadResume
            }

            android.util.Log.d("ResumeDetail", "Resume loaded: ${resume.name}, updated: ${resume.updatedDate}")

            // UI 업데이트를 메인 스레드에서 확실히 실행
            runOnUiThread {
                val container = findScrollViewContainer()
                if (container != null) {
                    // 기존 뷰들의 visibility를 모두 초기화
                    resetAllVisibility(container)
                    // 새 데이터로 업데이트
                    updateTextViews(container, resume)
                    // 강제로 레이아웃 다시 그리기
                    container.requestLayout()
                    container.invalidate()
                    // 업데이트 완료 후 ScrollView 보이기
                    showScrollView()
                    android.util.Log.d("ResumeDetail", "UI updated successfully")
                } else {
                    android.util.Log.e("ResumeDetail", "Container is null!")
                }
            }
        }
    }

    // 모든 선택사항 항목들의 visibility를 VISIBLE로 초기화
    private fun resetAllVisibility(container: LinearLayout) {
        val allLayouts = mutableListOf<View>()
        collectAllLayouts(container, allLayouts)
        allLayouts.forEach { it.visibility = View.VISIBLE }
    }

    private fun collectAllLayouts(view: View, list: MutableList<View>) {
        if (view is LinearLayout) {
            list.add(view)
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                collectAllLayouts(view.getChildAt(i), list)
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

    private fun updateTextViews(container: LinearLayout, resume: com.example.timepick.data.entity.ResumeEntity) {
        val allTextViews = mutableListOf<TextView>()
        collectAllTextViews(container, allTextViews)

        android.util.Log.d("ResumeDetail", "Total TextViews found: ${allTextViews.size}")

        // 인덱스 기반으로 TextView 찾기 (더 안정적)
        var dateTextView: TextView? = null
        var nameTextView: TextView? = null
        var introTextView: TextView? = null
        var phoneTextView: TextView? = null
        var addressTextView: TextView? = null
        var emailTextView: TextView? = null
        var locationTextView: TextView? = null
        var jobTextView: TextView? = null
        var educationTextView: TextView? = null
        var skillsTextView: TextView? = null
        var careerTextView: TextView? = null

        // "소개" 라벨 다음 TextView, "경력사항" 라벨 다음 TextView 등을 찾기
        for (i in allTextViews.indices) {
            val tv = allTextViews[i]
            val text = tv.text.toString()
            val parentLayout = tv.parent as? LinearLayout

            when {
                // 날짜는 상단에 위치하고 회색 작은 글씨
                dateTextView == null && text.matches(Regex("\\d{4}\\.\\d{2}\\.\\d{2}")) -> {
                    dateTextView = tv
                    android.util.Log.d("ResumeDetail", "Found date: $text")
                }
                // 이름은 날짜 바로 다음, 크고 굵은 글씨
                nameTextView == null && dateTextView != null && i > allTextViews.indexOf(dateTextView)
                        && tv.textSize > 18f -> {
                    nameTextView = tv
                    android.util.Log.d("ResumeDetail", "Found name: $text")
                }
                // "소개" 라벨 다음 TextView
                text == "소개" && i + 1 < allTextViews.size -> {
                    introTextView = allTextViews[i + 1]
                    android.util.Log.d("ResumeDetail", "Found intro at index ${i+1}")
                }
                // "휴대폰" 라벨과 같은 줄의 TextView
                text == "휴대폰" && parentLayout != null -> {
                    phoneTextView = findNextTextViewInLayout(parentLayout, tv)
                    android.util.Log.d("ResumeDetail", "Found phone")
                }
                // "주소" 라벨과 같은 줄의 TextView
                text == "주소" && parentLayout != null -> {
                    addressTextView = findNextTextViewInLayout(parentLayout, tv)
                    android.util.Log.d("ResumeDetail", "Found address")
                }
                // "이메일" 라벨과 같은 줄의 TextView
                text == "이메일" && parentLayout != null -> {
                    emailTextView = findNextTextViewInLayout(parentLayout, tv)
                    android.util.Log.d("ResumeDetail", "Found email")
                }
                // "희망 지역" 라벨과 같은 줄의 TextView
                text == "희망 지역" && parentLayout != null -> {
                    locationTextView = findNextTextViewInLayout(parentLayout, tv)
                    android.util.Log.d("ResumeDetail", "Found location")
                }
                // "희망 직종" 라벨과 같은 줄의 TextView
                text == "희망 직종" && parentLayout != null -> {
                    jobTextView = findNextTextViewInLayout(parentLayout, tv)
                    android.util.Log.d("ResumeDetail", "Found job")
                }
                // "최종 학력" 라벨과 같은 줄의 TextView
                text == "최종 학력" && parentLayout != null -> {
                    educationTextView = findNextTextViewInLayout(parentLayout, tv)
                    android.util.Log.d("ResumeDetail", "Found education")
                }
                // "자격 및 능력" 라벨과 같은 줄의 TextView
                text == "자격 및 능력" && parentLayout != null -> {
                    skillsTextView = findNextTextViewInLayout(parentLayout, tv)
                    android.util.Log.d("ResumeDetail", "Found skills")
                }
                // "경력사항" 라벨 다음 TextView
                text == "경력사항" && i + 1 < allTextViews.size -> {
                    careerTextView = allTextViews[i + 1]
                    android.util.Log.d("ResumeDetail", "Found career at index ${i+1}")
                }
            }
        }

        // 찾은 TextViews에 데이터 설정
        dateTextView?.text = resume.updatedDate ?: getCurrentDate()
        nameTextView?.text = resume.name
        introTextView?.text = resume.intro
        phoneTextView?.text = resume.phone
        locationTextView?.text = resume.desiredRegion
        jobTextView?.text = resume.desiredJob
        careerTextView?.text = resume.career

        // 선택사항 처리 (빈 값이면 부모 LinearLayout 숨기기)
        if (resume.address.isNullOrBlank()) {
            (addressTextView?.parent as? LinearLayout)?.visibility = View.GONE
        } else {
            (addressTextView?.parent as? LinearLayout)?.visibility = View.VISIBLE
            addressTextView?.text = resume.address
        }

        if (resume.email.isNullOrBlank()) {
            (emailTextView?.parent as? LinearLayout)?.visibility = View.GONE
        } else {
            (emailTextView?.parent as? LinearLayout)?.visibility = View.VISIBLE
            emailTextView?.text = resume.email
        }

        if (resume.education.isNullOrBlank()) {
            (educationTextView?.parent as? LinearLayout)?.visibility = View.GONE
        } else {
            (educationTextView?.parent as? LinearLayout)?.visibility = View.VISIBLE
            educationTextView?.text = resume.education
        }

        if (resume.skills.isNullOrBlank()) {
            (skillsTextView?.parent as? LinearLayout)?.visibility = View.GONE
        } else {
            (skillsTextView?.parent as? LinearLayout)?.visibility = View.VISIBLE
            skillsTextView?.text = resume.skills
        }

        android.util.Log.d("ResumeDetail", "All data updated - Name: ${resume.name}, Phone: ${resume.phone}")
    }

    // LinearLayout 내에서 특정 TextView 다음에 오는 TextView 찾기
    private fun findNextTextViewInLayout(layout: LinearLayout, currentTextView: TextView): TextView? {
        var found = false
        for (i in 0 until layout.childCount) {
            val child = layout.getChildAt(i)
            if (found && child is TextView) {
                return child
            }
            if (child == currentTextView) {
                found = true
            }
        }
        return null
    }

    private fun getCurrentDate(): String {
        val formatter = java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault())
        return formatter.format(java.util.Date())
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
            editResumeLauncher.launch(intent)  // ActivityResultLauncher 사용
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