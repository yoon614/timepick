package com.example.timepick

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListPopupWindow
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat

class WorkEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_edit)

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

        // --- 데이터 준비 ---
        val hourList = (0..23).map { it.toString().padStart(2, '0') }
        val minuteList = (0..5).map { (it * 10).toString().padStart(2, '0') }

        // --- 드롭다운 기능 연결 (함수 호출) ---
        setupDropdown(tvStartHour, hourList)
        setupDropdown(tvEndHour, hourList)
        setupDropdown(tvStartMinute, minuteList)
        setupDropdown(tvEndMinute, minuteList)


        // --- 클릭 이벤트 ---
        btnBack.setOnClickListener { finish() }

        btnComplete.setOnClickListener {
            val place = etPlace.text.toString()
            val wage = etWage.text.toString()

            // TextView에 적힌 글자를 가져옴
            val sHour = tvStartHour.text.toString()
            val sMin = tvStartMinute.text.toString()
            val eHour = tvEndHour.text.toString()
            val eMin = tvEndMinute.text.toString()

            Toast.makeText(this,
                "저장됨: $place ($sHour:$sMin ~ $eHour:$eMin)",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
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