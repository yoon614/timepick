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
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timepick.data.entity.WorkScheduleEntity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// 근무 기록 데이터 모델
data class WorkLog(
    val id: Int = 0,     // DB ID 추가
    val place: String,   // 근무지 이름
    val hours: String,   // 근무 시간
    val pay: String,     // 급여
    val schedule: WorkScheduleEntity? = null  // 원본 데이터 추가
)

class CalendarActivity : AppCompatActivity() {

    // ViewModel 추가
    private lateinit var viewModel: MainViewModel
    private var userId: Int = 0
    private var currentMonth: YearMonth = YearMonth.now()  // var로 선언 (재할당 가능)
    private var selectedDate: LocalDate = LocalDate.now()

    // 날짜별 근무 기록을 저장할 맵 (DB 데이터용)
    private val scheduleData = mutableMapOf<LocalDate, List<WorkLog>>()

    // 선택된 날짜의 근무 목록을 표시할 RecyclerView 어댑터
    private lateinit var workAdapter: WorkAdapter

    // 뷰들
    private lateinit var calendarView: CalendarView
    private lateinit var tvCurrentMonth: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var cbTaxCalc: CheckBox
    private lateinit var tvSelectedDate: TextView
    private lateinit var btnAddWork: ImageButton
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // ViewModel 초기화
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]

        loadUserId()
        initViews()
        setupRecyclerView()
        setupCalendar()
        setupClickListeners()
        setupBottomNavigation()
        observeSchedules()

        // 초기 데이터 로드
        loadMonthlyData(currentMonth)
    }

    override fun onResume() {
        super.onResume()
        // 네비게이션 바 선택 상태 재설정
        bottomNav.selectedItemId = R.id.nav_calendar
        // 화면 복귀 시 데이터 새로고침
        loadMonthlyData(currentMonth)
    }

    private fun loadUserId() {
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        val userIdString = sharedPref.getString("USER_ID", "0") ?: "0"
        userId = userIdString.toIntOrNull() ?: 0
    }

    private fun initViews() {
        calendarView = findViewById(R.id.calendar_view_custom)
        tvCurrentMonth = findViewById(R.id.tv_current_month)
        tvTotalIncome = findViewById(R.id.tv_total_income)
        cbTaxCalc = findViewById(R.id.cb_tax_calc)
        tvSelectedDate = findViewById(R.id.tv_selected_date)
        btnAddWork = findViewById(R.id.btn_add_work)
        bottomNav = findViewById(R.id.bottom_navigation)
    }

    private fun setupRecyclerView() {
        val rvList = findViewById<RecyclerView>(R.id.rv_daily_work_list)

        // 근무 리스트 RecyclerView 기본 설정
        workAdapter = WorkAdapter(mutableListOf())
        rvList.adapter = workAdapter
        rvList.layoutManager = LinearLayoutManager(this)

        // 근무 카드 클릭 시 동작 - 수정 화면으로 이동
        workAdapter.onItemClick = { workLog ->
            workLog.schedule?.let { schedule ->
                val intent = Intent(this, WorkEditActivity::class.java)
                intent.putExtra("SCHEDULE", schedule)
                startActivity(intent)
            }
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
                    val workLog = workAdapter.items[position]

                    // DB에서 삭제
                    workLog.schedule?.let { schedule ->
                        viewModel.deleteWorkSchedule(schedule)

                        // 즉시 UI 업데이트
                        val date = LocalDate.parse(schedule.workDate)
                        val updatedList = scheduleData[date]?.toMutableList() ?: mutableListOf()
                        updatedList.removeIf { it.id == schedule.id }

                        if (updatedList.isEmpty()) {
                            scheduleData.remove(date)
                        } else {
                            scheduleData[date] = updatedList
                        }

                        // 캘린더 배지 즉시 업데이트
                        calendarView.notifyCalendarChanged()

                        // 월급 재계산
                        calculateAndDisplayMonthlyIncome()
                    }

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
    }

    private fun setupCalendar() {
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
                        // 각 일정마다 배지 표시 (최대 3개)
                        works.take(3).forEach { work ->
                            val badgeView = LayoutInflater.from(this@CalendarActivity)
                                .inflate(
                                    R.layout.item_calendar_badge,
                                    container.badgeLayout,
                                    false
                                ) as TextView

                            // 근무지 이름 표시
                            badgeView.text = work.place

                            // marginTop을 0으로 설정 (겹침 방지)
                            val layoutParams = badgeView.layoutParams as android.view.ViewGroup.MarginLayoutParams
                            layoutParams.topMargin = 2  // 2dp로 작게
                            badgeView.layoutParams = layoutParams

                            container.badgeLayout.addView(badgeView)
                        }

                        // 3개 이상이면 "+" 표시
                        if (works.size > 3) {
                            val moreView = LayoutInflater.from(this@CalendarActivity)
                                .inflate(
                                    R.layout.item_calendar_badge,
                                    container.badgeLayout,
                                    false
                                ) as TextView
                            moreView.text = "+${works.size - 3}"

                            val layoutParams = moreView.layoutParams as android.view.ViewGroup.MarginLayoutParams
                            layoutParams.topMargin = 2
                            moreView.layoutParams = layoutParams

                            container.badgeLayout.addView(moreView)
                        }
                    }

                    // 날짜 클릭 시 하단 리스트 갱신
                    container.view.setOnClickListener {
                        selectedDate = data.date
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

        // 캘린더 범위 및 초기 월 설정 (로컬 변수 제거)
        calendarView.setup(
            currentMonth.minusMonths(100),
            currentMonth.plusMonths(100),
            DayOfWeek.SUNDAY
        )
        calendarView.scrollToMonth(currentMonth)

        // 월 변경 시 상단 월 텍스트 갱신 및 데이터 로드
        calendarView.monthScrollListener = { month ->
            currentMonth = month.yearMonth
            tvCurrentMonth.text =
                "${month.yearMonth.year}년 ${month.yearMonth.monthValue}월"
            loadMonthlyData(month.yearMonth)
        }
    }

    private fun setupClickListeners() {
        // 근무 추가 버튼 클릭 처리
        btnAddWork.setOnClickListener {
            val intent = Intent(this, WorkEditActivity::class.java)
            intent.putExtra("SELECTED_DATE", selectedDate.toString())
            startActivity(intent)
        }

        // 세금 체크박스
        cbTaxCalc.setOnCheckedChangeListener { _, _ ->
            calculateAndDisplayMonthlyIncome()
        }
    }

    private fun setupBottomNavigation() {
        // 하단 네비게이션 메뉴 이동 처리
        bottomNav.selectedItemId = R.id.nav_calendar

        bottomNav.setOnNavigationItemSelectedListener { item ->
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
    }

    private fun observeSchedules() {
        lifecycleScope.launch {
            viewModel.selectedDateSchedules.collect { schedules ->
                // WorkScheduleEntity를 WorkLog로 변환
                val workLogs = schedules.map { schedule ->
                    convertToWorkLog(schedule)
                }
                workAdapter.updateList(workLogs)
            }
        }
    }

    private fun loadMonthlyData(yearMonth: YearMonth) {
        val yearMonthString = yearMonth.toString() // "2026-01"
        viewModel.loadMonthlySchedules(userId, yearMonthString) { schedules ->
            // 날짜별로 그룹화
            scheduleData.clear()
            schedules.forEach { schedule ->
                val date = LocalDate.parse(schedule.workDate)
                val workLog = convertToWorkLog(schedule)
                val list = scheduleData[date]?.toMutableList() ?: mutableListOf()
                list.add(workLog)
                scheduleData[date] = list
            }

            // 캘린더 업데이트
            calendarView.notifyCalendarChanged()

            // 월급 계산
            calculateAndDisplayMonthlyIncome()
        }

        // 선택된 날짜의 일정도 로드
        viewModel.loadSchedulesByDate(userId, selectedDate.toString())
    }

    private fun calculateAndDisplayMonthlyIncome() {
        val allSchedules = scheduleData.values.flatten().mapNotNull { it.schedule }
        val applyTax = cbTaxCalc.isChecked  // 체크박스 상태에 따라 세금 적용
        val totalIncome = viewModel.calculateMonthlySalary(allSchedules, applyTax)

        tvTotalIncome.text = String.format("%,d원", totalIncome.toInt())
    }

    private fun convertToWorkLog(schedule: WorkScheduleEntity): WorkLog {
        // 근무 시간 계산
        val start = LocalTime.parse(schedule.startTime)
        val end = LocalTime.parse(schedule.endTime)
        var duration = Duration.between(start, end).toMinutes()
        if (duration < 0) duration += 24 * 60 // 자정 넘어가는 경우

        val hours = duration / 60.0
        val hoursText = "${schedule.startTime} ~ ${schedule.endTime} (${String.format("%.1f", hours)}시간)"

        // 급여 계산 (야간 수당 포함)
        var totalPay = hours * schedule.hourlyRate

        // 야간 수당 계산 (22:00 ~ 06:00)
        val nightMinutes = calculateNightMinutes(start, duration)
        totalPay += (nightMinutes / 60.0) * (schedule.hourlyRate * 0.5)

        // 세금 적용: UI의 체크박스 상태에 따라 적용
        // (개별 일정의 applyTax는 사용하지 않음, 전체 월급에만 적용)
        // 개별 카드에는 세금 미적용 급여 표시

        val payText = String.format("%,d원", totalPay.toInt())

        return WorkLog(
            id = schedule.id,
            place = schedule.workplaceName,
            hours = hoursText,
            pay = payText,
            schedule = schedule
        )
    }

    private fun calculateNightMinutes(start: LocalTime, totalDurationMinutes: Long): Long {
        var nightMinutes = 0L
        var currentTime = start

        for (i in 0 until totalDurationMinutes) {
            val hour = currentTime.hour
            if (hour >= 22 || hour < 6) {
                nightMinutes++
            }
            currentTime = currentTime.plusMinutes(1)
        }
        return nightMinutes
    }



    // 캘린더 날짜 셀 내부 뷰 홀더
    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val tvDay: TextView = view.findViewById(R.id.tv_calendar_day)
        val badgeLayout: LinearLayout = view.findViewById(R.id.layout_badge_container)
    }

    // 선택된 날짜의 근무 목록을 표시하는 RecyclerView 어댑터
    class WorkAdapter(var items: MutableList<WorkLog>) :
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