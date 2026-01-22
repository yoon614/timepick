package com.example.timepick

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class ResumeEditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resume_edit)

        // 1. XML에서 만든 뷰
        val triggerLayout = findViewById<LinearLayout>(R.id.layout_education_trigger) // 누르는 버튼 영역
        val selectedText = findViewById<TextView>(R.id.tv_education_selected)         // "학력을 선택해주세요" 글자
        val listCard = findViewById<CardView>(R.id.cv_education_list)                 // 숨겨진 목록 박스

        // 2. [목록 열기/닫기] 버튼을 눌렀을 때의 동작
        triggerLayout.setOnClickListener {
            if (listCard.visibility == View.VISIBLE) {
                listCard.visibility = View.GONE   // 이미 열려있으면 닫기
            } else {
                listCard.visibility = View.VISIBLE // 닫혀있으면 열기
            }
        }

        // 3. [항목 선택] 각 항목을 눌렀을 때의 동작을 함수로 만들어서 적용
        // (text: 선택한 항목의 이름)
        fun setSelectionBehavior(viewId: Int, text: String) {
            findViewById<View>(viewId).setOnClickListener {
                selectedText.text = text                       // 1. 위쪽 글자를 바꾼다
                selectedText.setTextColor(Color.parseColor("#18191C")) // 2. 글자색을 검정(활성)으로 변경
                listCard.visibility = View.GONE                // 3. 목록을 닫는다
            }
        }

        // 각 항목에 동작 연결하기
        setSelectionBehavior(R.id.tv_option_high, "고등학교 졸업")
        setSelectionBehavior(R.id.tv_option_college_2, "대학(2년제) 졸업")
        setSelectionBehavior(R.id.tv_option_college_4, "대학(4년제) 졸업")
    }
}