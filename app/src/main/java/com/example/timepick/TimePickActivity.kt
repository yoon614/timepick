package com.example.timepick

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.delay
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

    private val selectedStates = MutableList(252) { false }
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: TimePickAdapter
    private lateinit var rvTimeTable: RecyclerView
    private var userId: Int = 0

    // 드래그 상태 관리 변수
    private var isDragging = false
    private var initialSelectionState = false
    private var lastTouchedPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_pick)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        loadUserInfo()

        rvTimeTable = findViewById(R.id.rv_time_table)
        val btnConfirm = findViewById<Button>(R.id.btn_timepick_confirm)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // 1. 표 레이아웃 매니저 설정
        rvTimeTable.layoutManager = GridLayoutManager(this, 7)

        // 깜빡임 방지
        (rvTimeTable.itemAnimator as? androidx.recyclerview.widget.SimpleItemAnimator)?.supportsChangeAnimations = false

        adapter = TimePickAdapter()
        rvTimeTable.adapter = adapter

        if (userId != 0) loadPreviousSelection()

        // 2. 오토 스크롤 드래그 기능 적용
        setupDragSelection()

        // 3. 확인 버튼 클릭 (DB 저장 및 이동)
        btnConfirm.setOnClickListener {
            val selectedIndices = selectedStates.indices.filter { selectedStates[it] }

            if (userId == 0) {
                Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedIndices.isEmpty()) {
                // 선택 안 함 -> 전체 검색
                lifecycleScope.launch {
                    viewModel.findMatchingJobs(emptyList())
                    delay(500)
                    val intent = Intent(this@TimePickActivity, JobListActivity::class.java)
                    intent.putExtra("TIME_SELECTED", false)
                    startActivity(intent)
                }
            } else {
                // 선택 함 -> 저장 후 매칭
                viewModel.saveUserTimes(userId, selectedIndices) { success ->
                    if (success) {
                        lifecycleScope.launch {
                            viewModel.findMatchingJobs(selectedIndices)
                            delay(500)
                            val intent = Intent(this@TimePickActivity, JobListActivity::class.java)
                            intent.putExtra("TIME_SELECTED", true)
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this@TimePickActivity, "저장 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 4. 하단 네비게이션 설정
        setupBottomNavigation(bottomNav)
    }

    // 오토 스크롤이 포함된 드래그 로직 함수
    private fun setupDragSelection() {
        // XML에서 추가한 ScrollView 가져오기
        val scrollView = findViewById<ScrollView>(R.id.sv_time_table)

        rvTimeTable.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {

            private val SCROLL_SPEED = 20
            private val SCROLL_ZONE_HEIGHT = 150

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val action = e.action
                val x = e.x
                val y = e.y

                // 1. 드래그 중 스크롤 뷰 간섭 방지
                if (action == MotionEvent.ACTION_MOVE) {
                    rv.parent.requestDisallowInterceptTouchEvent(true)
                }

                // 2. 오토 스크롤 로직
                if (action == MotionEvent.ACTION_MOVE && isDragging) {
                    val location = IntArray(2)
                    rv.getLocationOnScreen(location)
                    val rvTopOnScreen = location[1]
                    val touchYOnScreen = rvTopOnScreen + y
                    val screenHeight = rv.resources.displayMetrics.heightPixels

                    // (1) 아래쪽 끝에 닿았을 때 -> 아래로 스크롤
                    if (touchYOnScreen > screenHeight - SCROLL_ZONE_HEIGHT) {
                        scrollView.smoothScrollBy(0, SCROLL_SPEED)
                    }
                    // (2) 위쪽 끝에 닿았을 때 -> 위로 스크롤
                    else if (touchYOnScreen < rvTopOnScreen + SCROLL_ZONE_HEIGHT) {
                        scrollView.smoothScrollBy(0, -SCROLL_SPEED)
                    }
                }

                // 3. 드래그 선택 로직
                when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        val view = rv.findChildViewUnder(x, y)
                        if (view != null) {
                            val position = rv.getChildAdapterPosition(view)
                            if (position != RecyclerView.NO_POSITION) {
                                isDragging = true
                                lastTouchedPosition = position
                                initialSelectionState = !selectedStates[position]
                                toggleSlot(position, initialSelectionState)
                            }
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (isDragging) {
                            val view = rv.findChildViewUnder(x, y)
                            if (view != null) {
                                val position = rv.getChildAdapterPosition(view)
                                if (position != RecyclerView.NO_POSITION && position != lastTouchedPosition) {
                                    lastTouchedPosition = position
                                    toggleSlot(position, initialSelectionState)
                                }
                            }
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isDragging = false
                        lastTouchedPosition = -1
                        rv.parent.requestDisallowInterceptTouchEvent(false) // 터치 끝나면 스크롤 잠금 해제
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    // 칸 상태 변경 헬퍼 함수
    private fun toggleSlot(position: Int, state: Boolean) {
        if (selectedStates[position] != state) {
            selectedStates[position] = state
            adapter.notifyItemChanged(position)
        }
    }

    private fun loadUserInfo() {
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        val userIdString = sharedPref.getString("USER_ID", "0") ?: "0"
        userId = userIdString.toIntOrNull() ?: 0
    }

    // 이전 데이터 불러오기
    private fun loadPreviousSelection() {
        viewModel.loadUserTimes(userId) { savedIndices ->
            for (i in selectedStates.indices) {
                selectedStates[i] = false
            }
            savedIndices.forEach { index ->
                if (index in selectedStates.indices) selectedStates[index] = true
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun setupBottomNavigation(bottomNav: BottomNavigationView) {
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_home -> true
                R.id.nav_mypage -> {
                    startActivity(Intent(this, MyPageActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    // 내부 어댑터 클래스
    inner class TimePickAdapter : RecyclerView.Adapter<TimePickAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_time_slot, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemView.isSelected = selectedStates[position]
        }

        override fun getItemCount(): Int = 252

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}
