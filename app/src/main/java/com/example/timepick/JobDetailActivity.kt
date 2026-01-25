package com.example.timepick

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.timepick.data.AppDatabase
import com.example.timepick.data.entity.JobEntity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

/**
JobDetailActivity - 공고 상세 화면

플로우:
- Intent로 받은 jobId로 DB에서 공고 상세 정보 조회
- 공고의 모든 상세 정보 표시 (급여, 업종, 고용형태, 모집조건, 근무지역, 상세요강)
- 이미 지원한 공고인지 확인 -> 지원 완료면 버튼 회색 + 비활성화
- 이력서 확인 버튼 -> 이력서 있으면 ResumeDetailActivity, 없으면 토스트 + ResumeEditActivity
- 지원하기 버튼
- 구글 지도 API 연동 (OnMapReadyCallback 구현)
 */
class JobDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var viewModel: MainViewModel
    private lateinit var btnBack: ImageButton
    private lateinit var tvCompanyName: TextView
    private lateinit var btnResume: MaterialButton
    private lateinit var btnApply: Button
    private lateinit var layoutDetailContent: LinearLayout
    private var timeContainer: LinearLayout? = null

    private var jobId: Int = 0
    private var userId: Int = 0
    private var currentJob: JobEntity? = null


    // 구글 맵 객체
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_detail)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]

        jobId = intent.getIntExtra("JOB_ID", 0)
        loadUserId()
        initViews()
        initMap()
        loadJobDetail()
        checkIfAlreadyApplied()
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
        btnBack = findViewById(R.id.btn_detail_back)
        tvCompanyName = findViewById(R.id.tv_detail_company_name)
        btnResume = findViewById(R.id.btn_detail_resume)
        btnApply = findViewById(R.id.btn_detail_apply)
        timeContainer = findViewById(R.id.layout_detail_time_list)

        val rootView = findViewById<View>(android.R.id.content)
        layoutDetailContent = findScrollViewContent(rootView) ?: LinearLayout(this)
    }

    // 지도 프래그먼트 초기화 및 로딩
    private fun initMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    // 지도가 준비되면 호출되는 콜백 (OnMapReadyCallback)
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        currentJob?.let { job ->
            updateMapLocation(job.address, job.title)
        }
    }

    // 주소를 좌표로 변환하여 지도 이동 및 마커 표시
    private fun updateMapLocation(address: String, title: String) {
        if (googleMap == null) return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(this@JobDetailActivity)
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(address, 1)

                if (!addresses.isNullOrEmpty()) {
                    val location = addresses[0]
                    val latLng = LatLng(location.latitude, location.longitude)

                    withContext(Dispatchers.Main) {
                        googleMap?.apply {
                            clear()
                            addMarker(MarkerOptions().position(latLng).title(title))
                            moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                        }
                    }
                } else {
                    Log.e("JobDetailActivity", "주소를 찾을 수 없습니다: $address")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // ScrollView 내부의 LinearLayout 찾기
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

    // 이미 지원했는지 확인 후 버튼 비활성화
    private fun checkIfAlreadyApplied() {
        viewModel.checkIfApplied(userId, jobId)

        lifecycleScope.launch {
            viewModel.isAlreadyApplied.collect { isApplied ->
                if (isApplied) {
                    btnApply.apply {
                        text = "지원 완료"
                        backgroundTintList = android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#A6AAB3")
                        )
                        isEnabled = false
                    }
                }
            }
        }
    }

    // DB에서 공고 상세 정보 로드
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

    // 공고 정보를 화면에 표시
    /*private fun displayJobInfo(job: JobEntity) {
        tvCompanyName.text = job.title
        updateTextViews(layoutDetailContent, job)
        updateMapLocation(job.address, job.title)
    }*/

    // 공고 정보를 화면에 표시
    private fun displayJobInfo(job: JobEntity) {
        tvCompanyName.text = job.title
        updateTextViews(layoutDetailContent, job)

        timeContainer?.let { container ->
            for (i in 0 until container.childCount) {
                val rowLayout = container.getChildAt(i) as? ViewGroup ?: continue

                if (rowLayout.childCount >= 2) {
                    val labelView = rowLayout.getChildAt(0) as? TextView
                    val valueView = rowLayout.getChildAt(1) as? TextView

                    val labelText = labelView?.text?.toString() ?: ""

                    when {
                        labelText.contains("요일") -> {
                            valueView?.text = job.workPeriod
                        }
                        labelText.contains("시간") -> {
                            valueView?.text = job.workTime
                        }
                    }
                }
            }
        }

        updateMapLocation(job.address, job.title)
    }

    // 재귀적으로 모든 TextView를 찾아서 DB 데이터로 업데이트
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

                                    /*label.contains("요일") || label.contains("기간") -> child.text = job.workPeriod
                                    label.contains("시간") -> child.text = job.workTime*/

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



    // 클릭 리스너 설정
    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        // 이력서 확인 버튼
        btnResume.setOnClickListener {
            val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)

            if (!isLoggedIn) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.checkResumeExists(userId) { hasResume ->
                if (hasResume) {
                    val intent = Intent(this, ResumeDetailActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "이력서를 작성해주세요.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, ResumeEditActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        // 지원하기 버튼
        btnApply.setOnClickListener {
            applyForJob()
        }
    }

    // 지원하기 버튼 로직
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

        // 1. 이력서 존재 여부 먼저 확인
        viewModel.checkResumeExists(userId) { hasResume ->
            if (hasResume) {
                // [Case A] 이력서가 있음 -> 바로 지원 처리 및 DB 저장
                viewModel.applyToJob(userId, jobId)

                Toast.makeText(this, "지원이 완료되었습니다.", Toast.LENGTH_SHORT).show()

                // 버튼 비활성화 (지원 완료 상태)
                btnApply.apply {
                    text = "지원 완료"
                    backgroundTintList = android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#A6AAB3")
                    )
                    isEnabled = false
                }

                // 지원 완료 화면으로 이동
                val intent = Intent(this, ApplyCompleteActivity::class.java)
                intent.putExtra("JOB_ID", jobId)
                intent.putExtra("COMPANY_NAME", currentJob?.title)
                startActivity(intent)

            } else {
                // [Case B] 이력서가 없음 -> 지원 방식 선택 화면으로 이동 (아직 DB 저장 안 함)
                // ApplyMethodActivity에서 '이력서 없이 지원'을 눌러야 DB에 저장됨
                val intent = Intent(this, ApplyMethodActivity::class.java)
                intent.putExtra("JOB_ID", jobId) // 나중에 지원 처리를 위해 전달
                intent.putExtra("COMPANY_NAME", currentJob?.title)
                startActivity(intent)
            }
        }
    }
}