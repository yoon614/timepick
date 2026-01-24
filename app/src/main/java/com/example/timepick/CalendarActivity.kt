package com.example.timepick

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// 근무 기록 데이터 모델
data class WorkLog(
    val place: String,   // 근무지 이름
    val hours: String,   // 근무 시간
    val pay: String      // 급여
)

class CalendarActivity : AppCompatActivity() {

    // 날짜별 근무 기록을 임시로 저장한 더미 데이터
    private val scheduleData = mapOf(
        LocalDate.of(2026, 1, 8) to listOf(WorkLog("맥도날드", "8시간", "80,000원")),
        LocalDate.of(2026, 1, 12) to listOf(
            WorkLog("스타벅스", "5시간", "50,000원"),
            WorkLog("올리브영", "3시간", "30,000원")
        ),
        LocalDate.of(2026, 1, 15) to listOf(WorkLog("편의점", "3시간", "28,000원")),
        LocalDate.of(2026, 1, 17) to listOf(WorkLog("맥도날드", "5시간", "50,000원"))
    )

    // 선택된 날짜의 근무 목록을 표시할 RecyclerView 어댑터
    private lateinit var workAdapter: WorkAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // 화면에 사용되는 주요 뷰들 연결
        val calendarView = findViewById<CalendarView>(R.id.calendar_view_custom)
        val tvCurrentMonth = findViewById<TextView>(R.id.tv_current_month)
        val tvSelectedDate = findViewById<TextView>(R.id.tv_selected_date)
        val btnAddWork = findViewById<ImageButton>(R.id.btn_add_work)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val rvList = findViewById<RecyclerView>(R.id.rv_daily_work_list)

        // 근무 리스트 RecyclerView 기본 설정
        workAdapter = WorkAdapter(mutableListOf())
        rvList.adapter = workAdapter
        rvList.layoutManager = LinearLayoutManager(this)

        // 근무 카드 클릭 시 동작
        workAdapter.onItemClick = { workLog ->
            Toast.makeText(this, "${workLog.place} 수정 화면으로 이동", Toast.LENGTH_SHORT).show()
        }

        // 근무 카드 왼쪽 스와이프 삭제 처리
        val swipeHelper =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                override fun onMove(
                    p0: RecyclerView,
                    p1: RecyclerView.ViewHolder,
                    p2: RecyclerView.ViewHolder
                ) = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.bindingAdapterPosition
                    workAdapter.removeItem(position)
                    Toast.makeText(this@CalendarActivity, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        val view = viewHolder.itemView
                        val paint = Paint()

                        if (dX < 0) {
                            // 삭제 배경 및 텍스트 커스텀 드로잉
                            paint.color = Color.parseColor("#777D88")
                            val background = RectF(
                                view.right.toFloat() + dX - 60f,
                                view.top.toFloat(),
                                view.right.toFloat(),
                                view.bottom.toFloat()
                            )

                            val cornerRadius = 30f
                            val corners = floatArrayOf(
                                0f, 0f,
                                cornerRadius, cornerRadius,
                                cornerRadius, cornerRadius,
                                0f, 0f
                            )

                            val path = Path()
                            path.addRoundRect(background, corners, Path.Direction.CW)
                            c.drawPath(path, paint)

                            paint.color = Color.WHITE
                            paint.textSize = 40f
                            paint.textAlign = Paint.Align.CENTER

                            val visibleWidth = -dX
                            val textCenter = view.right.toFloat() - (visibleWidth / 2)
                            val textY = view.top.toFloat() + view.height / 2 + 15
                            c.drawText("삭제", textCenter, textY, paint)
                        }
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            })

        swipeHelper.attachToRecyclerView(rvList)

        // 하단 네비게이션 메뉴 이동 처리
        bottomNav.selectedItemId = R.id.nav_calendar
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, TimePickActivity::class.java))
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    true
                }

                R.id.nav_mypage -> {
                    startActivity(Intent(this, MyPageActivity::class.java))
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    true
                }

                R.id.nav_calendar -> true
                else -> false
            }
        }

        // 캘린더 날짜 셀 UI 및 배지 처리 로직
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {

            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.tvDay.text = data.date.dayOfMonth.toString()

                if (data.position == DayPosition.MonthDate) {

                    // 요일에 따른 날짜 색상 처리
                    when (data.date.dayOfWeek) {
                        DayOfWeek.SUNDAY -> container.tvDay.setTextColor(Color.RED)
                        DayOfWeek.SATURDAY -> container.tvDay.setTextColor(Color.BLUE)
                        else -> container.tvDay.setTextColor(Color.BLACK)
                    }

                    // 해당 날짜의 근무 배지 표시
                    val works = scheduleData[data.date]
                    container.badgeLayout.removeAllViews()

                    if (!works.isNullOrEmpty()) {
                        var totalHours = 0
                        for (work in works) {
                            val hourNum =
                                work.hours.replace("시간", "").trim().toIntOrNull() ?: 0
                            totalHours += hourNum
                        }

                        val badgeView = LayoutInflater.from(this@CalendarActivity)
                            .inflate(
                                R.layout.item_calendar_badge,
                                container.badgeLayout,
                                false
                            ) as TextView

                        badgeView.text = "${totalHours}시간"
                        container.badgeLayout.addView(badgeView)
                    }

                    // 날짜 클릭 시 하단 리스트 갱신
                    container.view.setOnClickListener {
                        val formatter = DateTimeFormatter.ofPattern("M월 d일 (E)")
                        tvSelectedDate.text = data.date.format(formatter)
                        val clickedDateWorks = scheduleData[data.date] ?: emptyList()
                        workAdapter.updateList(clickedDateWorks)
                    }

                } else {
                    // 현재 월이 아닌 날짜 처리
                    container.tvDay.setTextColor(Color.LTGRAY)
                    container.badgeLayout.removeAllViews()
                }
            }
        }

        // 캘린더 범위 및 초기 월 설정
        val currentMonth = YearMonth.now()
        calendarView.setup(
            currentMonth.minusMonths(100),
            currentMonth.plusMonths(100),
            DayOfWeek.SUNDAY
        )
        calendarView.scrollToMonth(currentMonth)

        // 월 변경 시 상단 월 텍스트 갱신
        calendarView.monthScrollListener = { month ->
            tvCurrentMonth.text =
                "${month.yearMonth.year}년 ${month.yearMonth.monthValue}월"
        }

        // 근무 추가 버튼 클릭 처리
        btnAddWork.setOnClickListener {
            // 추가 버튼 로직
        }
    }

    // 캘린더 날짜 셀 내부 뷰 홀더
    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val tvDay: TextView = view.findViewById(R.id.tv_calendar_day)
        val badgeLayout: LinearLayout = view.findViewById(R.id.layout_badge_container)
    }

    // 선택된 날짜의 근무 목록을 표시하는 RecyclerView 어댑터
    class WorkAdapter(private var items: MutableList<WorkLog>) :
        RecyclerView.Adapter<WorkAdapter.WorkViewHolder>() {

        var onItemClick: ((WorkLog) -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_work_log_card, parent, false)
            return WorkViewHolder(view)
        }

        override fun onBindViewHolder(holder: WorkViewHolder, position: Int) {
            val item = items[position]
            holder.tvPlace.text = item.place
            holder.tvHours.text = item.hours
            holder.tvPay.text = item.pay
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }

        override fun getItemCount() = items.size

        fun updateList(newItems: List<WorkLog>) {
            items = newItems.toMutableList()
            notifyDataSetChanged()
        }

        fun removeItem(position: Int) {
            if (position in items.indices) {
                items.removeAt(position)
                notifyItemRemoved(position)
            }
        }

        // 근무 카드 UI 뷰 홀더
        class WorkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvPlace: TextView = view.findViewById(R.id.tv_work_place)
            val tvHours: TextView = view.findViewById(R.id.tv_work_hours)
            val tvPay: TextView = view.findViewById(R.id.tv_work_pay)
        }
    }
}
