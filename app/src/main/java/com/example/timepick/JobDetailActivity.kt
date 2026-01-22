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
 * JobDetailActivity - 공고 상세 화면
 *
 * 플로우:
 * - Intent로 받은 jobId로 DB에서 공고 상세 정보 조회
 * - 공고의 모든 상세 정보 표시
 * - 이력서 확인 버튼 -> ResumeDetailActivity 또는 ResumeEditActivity로 이동
 * - 지원하기 버튼 -> ApplyCompleteActivity로 이동
 * - 지도 API 미사용 시 지도 숨김 처리
 */
class JobDetailActivity : AppCompatActivity() {

    // UI 컴포넌트
    private lateinit var btnBack: ImageButton
    private lateinit var tvCompanyName: TextView
    private lateinit var mapFragment: View
    private lateinit var btnResume: Button
    private lateinit var btnApply: Button

    // ScrollView 내 LinearLayout
    private lateinit var layoutDetailContent: LinearLayout

    // 공고 데이터
    private var jobId: Int = 0
    private var currentJob: JobEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_detail)

        // Intent에서 공고 ID 받기
        jobId = intent.getIntExtra("JOB_ID", 0)

        // View 초기화
        initViews()

        // 지도 숨김 처리 (API 미사용)
        hideMapFragment()

        // 공고 정보 로드 및 표시
        loadJobDetail()

        // 클릭 리스너 설정
        setupClickListeners()
    }

    /**
     * View 초기화
     */
    private fun initViews() {
        btnBack = findViewById(R.id.btn_detail_back)
        tvCompanyName = findViewById(R.id.tv_detail_company_name)
        mapFragment = findViewById(R.id.map_fragment)
        btnResume = findViewById(R.id.btn_detail_resume)
        btnApply = findViewById(R.id.btn_detail_apply)

        // ScrollView를 찾아서 그 안의 LinearLayout 가져오기
        val rootView = findViewById<View>(android.R.id.content)
        layoutDetailContent = findScrollViewContent(rootView) ?: LinearLayout(this)
    }

    /**
     * View 계층에서 ScrollView 내부의 LinearLayout 찾기
     */
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

    /**
     * 지도 Fragment 숨김 처리
     */
    private fun hideMapFragment() {
        mapFragment.visibility = View.GONE
    }

    /**
     * 공고 상세 정보 로드
     */
    private fun loadJobDetail() {
        lifecycleScope.launch {
            try {
                // DB에서 직접 공고 조회
                val job = withContext(Dispatchers.IO) {
                    val database = AppDatabase.getInstance(applicationContext)
                    database.jobDao().getJobById(jobId)
                }
                currentJob = job
                displayJobInfo(job)
            } catch (e: Exception) {
                Toast.makeText(this@JobDetailActivity, "공고 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * 공고 정보 표시 (텍스트 직접 업데이트)
     */
    private fun displayJobInfo(job: JobEntity?) {
        if (job == null) return

        tvCompanyName.text = job.title

        // LinearLayout 내의 모든 TextView를 순회하며 업데이트
        updateTextViews(layoutDetailContent, job)
    }

    /**
     * ViewGroup 내 모든 TextView를 순회하며 업데이트
     */
    private fun updateTextViews(parent: ViewGroup, job: JobEntity) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)

            when (child) {
                is ViewGroup -> {
                    // 중첩된 ViewGroup이면 재귀 호출
                    updateTextViews(child, job)
                }
                is TextView -> {
                    // TextView의 현재 텍스트 확인
                    val text = child.text.toString()

                    // 텍스트 내용에 따라 업데이트
                    when {
                        text.contains("시급") -> {
                            child.text = "시급 ${String.format("%,d", job.hourlyRate)}원"
                        }
                        text == "화면 구성을 위한 예시 텍스트" -> {
                            // 부모 LinearLayout에서 형제 TextView 확인
                            val parentLayout = child.parent as? LinearLayout
                            if (parentLayout != null && parentLayout.childCount >= 2) {
                                val labelView = parentLayout.getChildAt(0) as? TextView
                                val label = labelView?.text?.toString() ?: ""

                                when {
                                    label.contains("업종") -> child.text = job.category
                                    label.contains("고용 형태") -> child.text = job.employmentType
                                    label.contains("모집 마감") -> child.text = job.deadline
                                    label.contains("모집 인원") -> child.text = "${job.recruitCount}명"
                                    label.contains("모집 분야") -> child.text = job.recruitField
                                    label.contains("학력") -> child.text = job.education
                                    label.contains("우대") -> child.text = job.preferences
                                    label.contains("근무지역") -> child.text = job.address
                                }
                            }
                        }
                        text.contains("상세 요강") && child.parent is LinearLayout -> {
                            // "상세 요강" 다음에 오는 TextView 찾기
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

    /**
     * 클릭 이벤트 리스너 설정
     */
    private fun setupClickListeners() {
        // 뒤로가기 버튼
        btnBack.setOnClickListener {
            finish()
        }

        // 이력서 확인 버튼
        btnResume.setOnClickListener {
            // 로그인 상태 확인
            val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)

            if (!isLoggedIn) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: DB에서 이력서 존재 여부 확인
            // 현재는 이력서가 없다고 가정
            val hasResume = false

            if (hasResume) {
                // 이력서가 있으면 상세 화면으로
                val intent = Intent(this, ResumeDetailActivity::class.java)
                startActivity(intent)
            } else {
                // 이력서가 없으면 작성 화면으로
                Toast.makeText(this, "이력서를 먼저 작성해주세요.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ResumeEditActivity::class.java)
                startActivity(intent)
            }
        }

        // 지원하기 버튼
        btnApply.setOnClickListener {
            applyForJob()
        }
    }

    /**
     * 지원하기 처리
     */
    private fun applyForJob() {
        // 로그인 상태 확인
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)

        if (!isLoggedIn) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: 실제 지원 데이터 저장
        // - 사용자 ID
        // - 공고 ID
        // - 지원 일시
        // DB 또는 서버에 저장

        // 지원 완료 화면으로 이동
        val intent = Intent(this, ApplyCompleteActivity::class.java)
        intent.putExtra("JOB_TITLE", currentJob?.title ?: "")
        startActivity(intent)
    }
}