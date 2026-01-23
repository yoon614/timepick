package com.example.timepick

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

/**
 TimePickActivity

 플로우:
  - 요일별(월~일), 시간대별(오전 8시~오후 10시) 타임 테이블 표시
  - 타임 슬롯 클릭 -> 선택/해제 토글
  - SharedPreferences에서 이전 선택 데이터 로드 (userId별 분리)
  - 저장 버튼 -> DB에 선택한 타임 저장
 */

class TimePickActivity : AppCompatActivity() {

    private lateinit var rvTimeTable: RecyclerView
    private lateinit var btnConfirm: Button
    private lateinit var bottomNav: BottomNavigationView

    private lateinit var viewModel: MainViewModel
    private lateinit var timeSlotAdapter: TimeSlotAdapter
    private val selectedSlots = BooleanArray(252) { false }
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_pick)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]

        loadUserInfo()
        initViews()

        if (userId != 0) {
            //loadPreviousSelection() //이 부분 주석처리
        }

        setupRecyclerView()
        setupClickListeners()
        setupBottomNavigation()
    }

    private fun loadUserInfo() {
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        val userIdString = sharedPref.getString("USER_ID", "0") ?: "0"
        userId = userIdString.toIntOrNull() ?: 0
        android.util.Log.d("TimePickActivity", "로드된 userId: $userId")
    }

    private fun initViews() {
        rvTimeTable = findViewById(R.id.rv_time_table)
        btnConfirm = findViewById(R.id.btn_timepick_confirm)
        bottomNav = findViewById(R.id.bottom_navigation)
    }

    private fun loadPreviousSelection() {
        // 먼저 배열 초기화
        for (i in selectedSlots.indices) {
            selectedSlots[i] = false
        }

        viewModel.loadUserTimes(userId) { savedIndices ->
            android.util.Log.d("TimePickActivity", "userId: $userId, 이전 선택 데이터: ${savedIndices.size}개")
            savedIndices.forEach { index ->
                if (index in 0 until 252) {
                    selectedSlots[index] = true
                }
            }
            timeSlotAdapter.notifyDataSetChanged()
        }
    }

    private fun setupRecyclerView() {
        rvTimeTable.layoutManager = GridLayoutManager(this, 7)
        timeSlotAdapter = TimeSlotAdapter(selectedSlots) { position ->
            selectedSlots[position] = !selectedSlots[position]
        }
        rvTimeTable.adapter = timeSlotAdapter
    }

    private fun setupClickListeners() {
        btnConfirm.setOnClickListener {
            val selectedIndices = selectedSlots.indices.filter { selectedSlots[it] }
            android.util.Log.d("TimePickActivity", "=== 확인 버튼 클릭 ===")
            android.util.Log.d("TimePickActivity", "선택된 시간 개수: ${selectedIndices.size}")

            if (userId == 0) {
                Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedIndices.isEmpty()) {
                android.util.Log.d("TimePickActivity", "=== 시간 미선택 - 전체 공고 표시 ===")
                lifecycleScope.launch {
                    val database = com.example.timepick.data.AppDatabase.getInstance(applicationContext)
                    val dbJobs = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        database.jobDao().getAllJobsWithTimes()
                    }

                    android.util.Log.d("TimePickActivity", "DB에서 조회한 공고 개수: ${dbJobs.size}")

                    if (dbJobs.isEmpty()) {
                        android.util.Log.e("TimePickActivity", "❌ DB에 공고가 없습니다!")
                        Toast.makeText(this@TimePickActivity,
                            "공고 데이터가 없습니다. 앱을 재설치해주세요.",
                            Toast.LENGTH_LONG).show()
                        return@launch
                    }

                    dbJobs.forEach { job ->
                        android.util.Log.d("TimePickActivity",
                            "공고: ${job.job.title}, 시간: ${job.times.size}개")
                    }

                    android.util.Log.d("TimePickActivity", "findMatchingJobs(빈 리스트) 호출")
                    viewModel.findMatchingJobs(emptyList())

                    kotlinx.coroutines.delay(500)

                    val matchedJobs = viewModel.matchedJobs.value
                    android.util.Log.d("TimePickActivity", "StateFlow에 저장된 공고 개수: ${matchedJobs.size}")

                    if (matchedJobs.isEmpty()) {
                        android.util.Log.e("TimePickActivity", "❌ StateFlow가 비어있습니다!")
                    }

                    android.util.Log.d("TimePickActivity", "JobListActivity로 이동")
                    val intent = Intent(this@TimePickActivity, JobListActivity::class.java)
                    intent.putExtra("TIME_SELECTED", false)
                    startActivity(intent)
                }
            } else {
                android.util.Log.d("TimePickActivity", "=== 시간 선택함 - DB 저장 및 매칭 ===")
                android.util.Log.d("TimePickActivity", "선택된 인덱스: $selectedIndices")

                viewModel.saveUserTimes(userId, selectedIndices) { success ->
                    android.util.Log.d("TimePickActivity", "시간 저장 결과: $success")

                    if (success) {
                        lifecycleScope.launch {
                            android.util.Log.d("TimePickActivity", "findMatchingJobs 호출")
                            viewModel.findMatchingJobs(selectedIndices)

                            kotlinx.coroutines.delay(500)

                            val matchedJobs = viewModel.matchedJobs.value
                            android.util.Log.d("TimePickActivity", "매칭된 공고 개수: ${matchedJobs.size}")

                            val intent = Intent(this@TimePickActivity, JobListActivity::class.java)
                            intent.putExtra("TIME_SELECTED", true)
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this@TimePickActivity, "시간 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_calendar -> {
                    // 캘린더는 아직 미구현
                    Toast.makeText(this@TimePickActivity, "캘린더 기능은 준비 중입니다.", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_home -> {
                    // 현재 화면 (타임테이블)
                    true
                }
                R.id.nav_mypage -> {
                    val intent = Intent(this@TimePickActivity, MyPageActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}

class TimeSlotAdapter(
    private val selectedSlots: BooleanArray,
    private val onSlotClick: (Int) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    class TimeSlotViewHolder(val frameLayout: android.widget.FrameLayout) :
        RecyclerView.ViewHolder(frameLayout)

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): TimeSlotViewHolder {
        val frameLayout = android.widget.FrameLayout(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                30.dpToPx(parent.context)
            )
            setBackgroundResource(R.drawable.bg_time_slot)
            isClickable = true
            isFocusable = true
        }
        return TimeSlotViewHolder(frameLayout)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val isSelected = selectedSlots[position]

        if (isSelected) {
            holder.frameLayout.setBackgroundColor(
                android.graphics.Color.parseColor("#0068FF")
            )
        } else {
            holder.frameLayout.setBackgroundResource(R.drawable.bg_time_slot)
        }

        holder.frameLayout.setOnClickListener {
            onSlotClick(position)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = selectedSlots.size

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}