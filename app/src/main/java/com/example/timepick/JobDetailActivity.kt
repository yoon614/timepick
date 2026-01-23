package com.example.timepick

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.timepick.data.AppDatabase
import com.example.timepick.data.entity.JobEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 JobDetailActivity - 공고 상세 화면

 플로우:
  - Intent로 받은 jobId로 DB에서 공고 상세 정보 조회
  - 공고의 모든 상세 정보 표시 (급여, 업종, 고용형태, 모집조건, 근무지역, 상세요강)
  - 이력서 확인 버튼 -> 이력서 있으면 ResumeDetailActivity, 없으면 토스트 + ResumeEditActivity
  - 지원하기 버튼 -> ApplyCompleteActivity로 이동
 */
class JobDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvCompanyName: TextView
    private lateinit var mapFragment: View
    private lateinit var btnResume: Button
    private lateinit var btnApply: Button
    private lateinit var layoutDetailContent: LinearLayout

    private var jobId: Int = 0
    private var currentJob: JobEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_detail)

        jobId = intent.getIntExtra("JOB_ID", 0)
        initViews()
        hideMapFragment()
        loadJobDetail()
        setupClickListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_detail_back)
        tvCompanyName = findViewById(R.id.tv_detail_company_name)
        mapFragment = findViewById(R.id.map_fragment)
        btnResume = findViewById(R.id.btn_detail_resume)
        btnApply = findViewById(R.id.btn_detail_apply)

        val rootView = findViewById<View>(android.R.id.content)
        layoutDetailContent = findScrollViewContent(rootView) ?: LinearLayout(this)
    }

    private fun findScrollViewContent(view: View): LinearLayout? {
        if (view is android.widget.ScrollView) {
            return view.getChildAt(0) as? LinearLayout
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val result = findScrollViewContent(view.getChildAt(i))
                if (result != null) return result
            }
        }
        return null
    }

    private fun hideMapFragment() {
        mapFragment.visibility = View.GONE
    }

    private fun loadJobDetail() {
        if (jobId == 0) {
            Toast.makeText(this, "잘못된 공고 정보입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            val job = withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(applicationContext)
                db.jobDao().getJobById(jobId)
            }

            if (job != null) {
                currentJob = job
                displayJobInfo(job)
            } else {
                Toast.makeText(this@JobDetailActivity, "공고를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayJobInfo(job: JobEntity) {
        tvCompanyName.text = job.title
        updateTextViews(layoutDetailContent, job)
    }

    private fun updateTextViews(parent: ViewGroup, job: JobEntity) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)

            when (child) {
                is ViewGroup -> {
                    updateTextViews(child, job)
                }
                is TextView -> {
                    val text = child.text.toString()

                    when {
                        text.contains("시급") -> {
                            child.text = "시급 ${String.format("%,d", job.hourlyRate)}원"
                        }
                        text == "화면 구성을 위한 예시 텍스트" -> {
                            val parentLayout = child.parent as? LinearLayout
                            if (parentLayout != null && parentLayout.childCount >= 2) {
                                val labelView = parentLayout.getChildAt(0) as? TextView
                                val label = labelView?.text?.toString() ?: ""

                                when {
                                    label.contains("업종") -> child.text = job.category
                                    label.contains("고용 형태") -> child.text = job.employmentType
                                    label.contains("모집 분야") -> child.text = job.recruitField
                                    label.contains("학력") -> child.text = job.education
                                    label.contains("우대") -> child.text = job.preferences
                                    else -> child.text = job.address
                                }
                            } else {
                                child.text = job.address
                            }
                        }
                        text.contains("2026") && text.contains("XX") -> {
                            child.text = job.deadline
                        }
                        text == "nn명" -> {
                            child.text = "${job.recruitCount}명"
                        }
                        text.contains("상세 요강") && child.parent is LinearLayout -> {
                            val parentLayout = child.parent as LinearLayout
                            val index = parentLayout.indexOfChild(child)
                            if (index + 1 < parentLayout.childCount) {
                                val descView = parentLayout.getChildAt(index + 1) as? TextView
                                descView?.text = job.description
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnResume.setOnClickListener {
            val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)

            if (!isLoggedIn) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = sharedPref.getString("USER_ID", "") ?: ""
            val resumePref = getSharedPreferences("TimePick_Resume_$userId", MODE_PRIVATE)
            val hasResume = resumePref.getBoolean("has_resume", false)

            if (hasResume) {
                val intent = Intent(this, ResumeDetailActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "이력서를 작성해주세요.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ResumeEditActivity::class.java)
                startActivity(intent)
            }
        }

        btnApply.setOnClickListener {
            applyForJob()
        }
    }

    private fun applyForJob() {
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)

        if (!isLoggedIn) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentJob == null) {
            Toast.makeText(this, "공고 정보를 불러오는 중입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ApplyCompleteActivity::class.java)
        intent.putExtra("JOB_ID", jobId)
        intent.putExtra("COMPANY_NAME", currentJob?.title)
        startActivity(intent)
    }
}