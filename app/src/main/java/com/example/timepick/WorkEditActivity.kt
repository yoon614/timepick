package com.example.timepick

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListPopupWindow
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.timepick.data.entity.WorkScheduleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * WorkEditActivity - 근무 일정 추가/수정 화면
 *
 * 플로우:
 * - 근무지, 근무시간, 시급 입력
 * - 드롭다운 메뉴로 시간 선택
 * - 매주 고정 근무 체크박스
 * - 완료 버튼 클릭 → DB 저장 후 종료
 * - 매주 고정 해제 시 → groupId 전체 삭제 후 해당 날짜만 재생성
 */
class WorkEditActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private var userId: Int = 0
    private var selectedDate: String = LocalDate.now().toString()
    private var existingSchedule: WorkScheduleEntity? = null

    // Error TextViews
    private lateinit var tvPlaceError: TextView
    private lateinit var tvTimeError: TextView
    private lateinit var tvWageError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_edit)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]

        loadUserId()
        loadIntentData()

        // --- 뷰 가져오기 (Spinner 대신 TextView로 변경됨) ---
        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        val btnComplete = findViewById<Button>(R.id.btn_complete)
        val etPlace = findViewById<EditText>(R.id.et_place)
        val etWage = findViewById<EditText>(R.id.et_wage)
        val cbRepeat = findViewById<CheckBox>(R.id.cb_repeat_weekly)

        val tvStartHour = findViewById<TextView>(R.id.tv_start_hour)
        val tvStartMinute = findViewById<TextView>(R.id.tv_start_minute)
        val tvEndHour = findViewById<TextView>(R.id.tv_end_hour)
        val tvEndMinute = findViewById<TextView>(R.id.tv_end_minute)

        // Error TextViews 초기화
        tvPlaceError = findViewById(R.id.tv_place_error)
        tvTimeError = findViewById(R.id.tv_time_error)
        tvWageError = findViewById(R.id.tv_wage_error)

        // --- 데이터 준비 ---
        val hourList = (0..23).map { it.toString().padStart(2, '0') }
        val minuteList = (0..5).map { (it * 10).toString().padStart(2, '0') }

        // --- 드롭다운 기능 연결 (함수 호출) ---
        setupDropdown(tvStartHour, hourList)
        setupDropdown(tvEndHour, hourList)
        setupDropdown(tvStartMinute, minuteList)
        setupDropdown(tvEndMinute, minuteList)

        // 기존 데이터 로드 (수정 모드)
        loadExistingData(etPlace, tvStartHour, tvStartMinute, tvEndHour, tvEndMinute, etWage, cbRepeat)

        // --- 클릭 이벤트 ---
        btnBack.setOnClickListener { finish() }

        btnComplete.setOnClickListener {
            attemptSaveSchedule(
                etPlace,
                tvStartHour,
                tvStartMinute,
                tvEndHour,
                tvEndMinute,
                etWage,
                cbRepeat
            )
        }
    }

    private fun loadUserId() {
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        val userIdString = sharedPref.getString("USER_ID", "0") ?: "0"
        userId = userIdString.toIntOrNull() ?: 0
    }

    private fun loadIntentData() {
        selectedDate = intent.getStringExtra("SELECTED_DATE") ?: LocalDate.now().toString()
        existingSchedule = intent.getSerializableExtra("SCHEDULE") as? WorkScheduleEntity
    }

    private fun loadExistingData(
        etPlace: EditText,
        tvStartHour: TextView,
        tvStartMinute: TextView,
        tvEndHour: TextView,
        tvEndMinute: TextView,
        etWage: EditText,
        cbRepeat: CheckBox
    ) {
        existingSchedule?.let { schedule ->
            etPlace.setText(schedule.workplaceName)

            val startParts = schedule.startTime.split(":")
            tvStartHour.text = startParts[0]
            tvStartMinute.text = startParts[1]

            val endParts = schedule.endTime.split(":")
            tvEndHour.text = endParts[0]
            tvEndMinute.text = endParts[1]

            etWage.setText(schedule.hourlyRate.toString())
            cbRepeat.isChecked = schedule.isWeeklyFixed

            selectedDate = schedule.workDate
        }
    }

    private fun attemptSaveSchedule(
        etPlace: EditText,
        tvStartHour: TextView,
        tvStartMinute: TextView,
        tvEndHour: TextView,
        tvEndMinute: TextView,
        etWage: EditText,
        cbRepeat: CheckBox
    ) {
        // 에러 메시지 초기화
        tvPlaceError.visibility = View.GONE
        tvTimeError.visibility = View.GONE
        tvWageError.visibility = View.GONE

        val place = etPlace.text.toString().trim()
        val wage = etWage.text.toString().trim()

        // TextView에 적힌 글자를 가져옴
        val sHour = tvStartHour.text.toString()
        val sMin = tvStartMinute.text.toString()
        val eHour = tvEndHour.text.toString()
        val eMin = tvEndMinute.text.toString()

        // 유효성 검사
        if (place.isEmpty()) {
            tvPlaceError.visibility = View.VISIBLE
            Toast.makeText(this, "근무지를 입력해주세요.", Toast.LENGTH_SHORT).show()
            etPlace.requestFocus()
            return
        }

        if (sHour.isEmpty() || sMin.isEmpty() || eHour.isEmpty() || eMin.isEmpty()) {
            tvTimeError.visibility = View.VISIBLE
            Toast.makeText(this, "근무시간을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (wage.isEmpty()) {
            tvWageError.visibility = View.VISIBLE
            Toast.makeText(this, "시급을 입력해주세요.", Toast.LENGTH_SHORT).show()
            etWage.requestFocus()
            return
        }

        val wageInt = wage.toIntOrNull() ?: 0
        if (wageInt <= 0) {
            tvWageError.visibility = View.VISIBLE
            Toast.makeText(this, "올바른 시급을 입력해주세요.", Toast.LENGTH_SHORT).show()
            etWage.requestFocus()
            return
        }

        val startTime = "$sHour:$sMin"
        val endTime = "$eHour:$eMin"
        val isWeeklyFixed = cbRepeat.isChecked

        // 수정 모드이고, 매주 고정 → 해제로 변경한 경우
        if (existingSchedule != null && existingSchedule!!.isWeeklyFixed && !isWeeklyFixed) {
            val groupId = existingSchedule!!.groupId

            if (groupId != null) {
                // groupId로 묶인 모든 일정 삭제 후 단일 일정으로 재생성
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val database = com.example.timepick.data.AppDatabase.getInstance(applicationContext)
                        val dao = database.workScheduleDao()

                        // 1. groupId로 묶인 모든 일정 삭제
                        dao.deleteFutureSchedules(groupId, "1900-01-01")  // 모든 날짜 삭제

                        // 2. 해당 날짜만 단일 일정으로 재생성
                        val newSchedule = WorkScheduleEntity(
                            id = 0,
                            userId = userId,
                            workplaceName = place,
                            workDate = selectedDate,
                            startTime = startTime,
                            endTime = endTime,
                            hourlyRate = wageInt,
                            applyTax = false,
                            isWeeklyFixed = false,
                            groupId = null
                        )
                        dao.insertSchedule(newSchedule)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@WorkEditActivity,
                                "매주 고정 일정이 해제되었습니다ㅔ.",
                                Toast.LENGTH_LONG).show()
                            finish()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@WorkEditActivity,
                                "일정 수정 중 오류가 발생했습니다.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                return  // 여기서 함수 종료
            }
        }

        // 수정 모드이고, 단일 → 매주 고정으로 변경한 경우
        if (existingSchedule != null && !existingSchedule!!.isWeeklyFixed && isWeeklyFixed) {
            // 기존 단일 일정 삭제 후 매주 고정 생성
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val database = com.example.timepick.data.AppDatabase.getInstance(applicationContext)
                    val dao = database.workScheduleDao()

                    // 1. 기존 단일 일정 삭제
                    dao.deleteSchedule(existingSchedule!!)

                    // 2. 매주 고정 일정 생성 (52주)
                    withContext(Dispatchers.Main) {
                        val schedule = WorkScheduleEntity(
                            id = 0,
                            userId = userId,
                            workplaceName = place,
                            workDate = selectedDate,
                            startTime = startTime,
                            endTime = endTime,
                            hourlyRate = wageInt,
                            applyTax = false,
                            isWeeklyFixed = true,
                            groupId = null
                        )
                        viewModel.saveWorkSchedule(schedule)

                        Toast.makeText(this@WorkEditActivity,
                            "매주 고정 일정으로 변경되었습니다.",
                            Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@WorkEditActivity,
                            "일정 수정 중 오류가 발생했습니다.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
            return  // 여기서 함수 종료
        }

        // 일반적인 저장 (신규 추가 or 일반 수정)
        val schedule = WorkScheduleEntity(
            id = existingSchedule?.id ?: 0,
            userId = userId,
            workplaceName = place,
            workDate = selectedDate,
            startTime = startTime,
            endTime = endTime,
            hourlyRate = wageInt,
            applyTax = false,
            isWeeklyFixed = isWeeklyFixed,
            groupId = existingSchedule?.groupId
        )

        viewModel.saveWorkSchedule(schedule)
        Toast.makeText(this, "일정이 저장되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    // 커스텀 드롭다운을 만들어주는 함수
    private fun setupDropdown(view: TextView, items: List<String>) {
        view.setOnClickListener {
            val listPopupWindow = ListPopupWindow(this)
            listPopupWindow.anchorView = view // 이 뷰 바로 아래에 뜸

            // 둥근 배경 적용
            listPopupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_dropdown_panel))

            // 너비 설정
            listPopupWindow.width = view.width
            listPopupWindow.height = 500 // 목록 높이 제한 (스크롤)

            // 어댑터 설정
            val adapter = ArrayAdapter(this, R.layout.item_spinner_text, items)
            listPopupWindow.setAdapter(adapter)

            // 클릭했을 때 동작
            listPopupWindow.setOnItemClickListener { _, _, position, _ ->
                view.text = items[position] // 선택한 값으로 글자 변경
                listPopupWindow.dismiss() // 팝업 닫기
            }

            listPopupWindow.show() // 보여주기
        }
    }
}