package com.example.timepick

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * TimePickActivity - 근무 가능 시간 선택 화면
 *
 * 플로우:
 * - 7시~24시, 월~일 시간표에서 드래그로 시간 선택
 * - 확인 버튼 클릭 -> JobListActivity(공고 리스트)로 이동
 */
class TimePickActivity : AppCompatActivity() {

    // UI 컴포넌트
    private lateinit var rvTimeTable: RecyclerView
    private lateinit var btnConfirm: Button

    // 시간표 어댑터
    private lateinit var timeSlotAdapter: TimeSlotAdapter

    // 선택된 시간 슬롯 (252개: 18시간 x 2(30분 단위) x 7일)
    private val selectedSlots = BooleanArray(252) { false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_pick)

        // View 초기화
        initViews()

        // RecyclerView 설정
        setupRecyclerView()

        // 클릭 리스너 설정
        setupClickListeners()
    }

    /**
     * View 초기화
     */
    private fun initViews() {
        rvTimeTable = findViewById(R.id.rv_time_table)
        btnConfirm = findViewById(R.id.btn_timepick_confirm)
    }

    /**
     * RecyclerView 설정
     */
    private fun setupRecyclerView() {
        // GridLayout: 7개 열(월~일)
        rvTimeTable.layoutManager = GridLayoutManager(this, 7)

        // 어댑터 생성 및 설정
        timeSlotAdapter = TimeSlotAdapter(selectedSlots) { position ->
            // 슬롯 클릭 시 선택 상태 토글
            selectedSlots[position] = !selectedSlots[position]
        }

        rvTimeTable.adapter = timeSlotAdapter
    }

    /**
     * 클릭 이벤트 리스너 설정
     */
    private fun setupClickListeners() {
        // 확인 버튼 클릭 -> 공고 리스트로 이동
        btnConfirm.setOnClickListener {
            // 선택된 시간이 있는지 확인
            val hasSelection = selectedSlots.any { it }

            if (!hasSelection) {
                Toast.makeText(this, "근무 가능한 시간을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 선택된 시간 데이터를 저장 (SharedPreferences)
            saveSelectedTime()

            // 공고 리스트 화면으로 이동
            val intent = Intent(this, JobListActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * 선택된 시간 데이터 저장
     * TODO: 추후 DB 연동 시 서버에 저장
     */
    private fun saveSelectedTime() {
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        with(sharedPref.edit()) {
            // Boolean 배열을 String으로 변환하여 저장
            val selectedString = selectedSlots.joinToString(",") { if (it) "1" else "0" }
            putString("SELECTED_TIME_SLOTS", selectedString)
            apply()
        }
    }
}

/**
 * TimeSlotAdapter - 시간표 셀 어댑터
 */
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

        // 선택 상태에 따라 배경 색상 변경
        if (isSelected) {
            holder.frameLayout.setBackgroundColor(
                android.graphics.Color.parseColor("#0068FF")
            )
        } else {
            holder.frameLayout.setBackgroundResource(R.drawable.bg_time_slot)
        }

        // 클릭 이벤트
        holder.frameLayout.setOnClickListener {
            onSlotClick(position)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = selectedSlots.size

    // dp를 px로 변환하는 확장 함수
    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}