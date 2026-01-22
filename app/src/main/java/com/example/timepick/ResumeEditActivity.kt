package com.example.timepick

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class ResumeEditActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnSave: Button

    private lateinit var etName: EditText
    private lateinit var etIntro: EditText
    private lateinit var etPhone: EditText
    private lateinit var etLocation: EditText
    private lateinit var etJob: EditText
    private lateinit var etCareer: EditText

    private lateinit var layoutEducationTrigger: LinearLayout
    private lateinit var tvEducationSelected: TextView
    private lateinit var listCard: CardView

    private var selectedEducation: String? = null
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resume_edit)

        loadUserId()
        initViews()
        setupEducationDropdown()
        loadResumeData()
        setupClickListeners()
    }

    private fun loadUserId() {
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        userId = sharedPref.getString("USER_ID", "") ?: ""
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_resume_edit_back)
        btnSave = findViewById(R.id.btn_resume_complete)

        etName = findViewById(R.id.et_resume_name)
        etIntro = findViewById(R.id.et_resume_intro)
        etPhone = findViewById(R.id.et_resume_phone)
        etLocation = findViewById(R.id.et_resume_location)
        etJob = findViewById(R.id.et_resume_job)
        etCareer = findViewById(R.id.et_resume_career)

        layoutEducationTrigger = findViewById(R.id.layout_education_trigger)
        tvEducationSelected = findViewById(R.id.tv_education_selected)
        listCard = findViewById(R.id.cv_education_list)
    }

    private fun setupEducationDropdown() {
        layoutEducationTrigger.setOnClickListener {
            listCard.visibility = if (listCard.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        fun setSelectionBehavior(viewId: Int, text: String) {
            findViewById<View>(viewId).setOnClickListener {
                selectedEducation = text
                tvEducationSelected.text = text
                tvEducationSelected.setTextColor(Color.parseColor("#18191C"))
                listCard.visibility = View.GONE
            }
        }

        setSelectionBehavior(R.id.tv_option_high, "고등학교 졸업")
        setSelectionBehavior(R.id.tv_option_college_2, "대학(2년제) 졸업")
        setSelectionBehavior(R.id.tv_option_college_4, "대학(4년제) 졸업")
    }

    private fun loadResumeData() {
        val pref = getSharedPreferences("TimePick_Resume_$userId", MODE_PRIVATE)

        if (pref.contains("name")) {
            etName.setText(pref.getString("name", ""))
            etIntro.setText(pref.getString("intro", ""))
            etPhone.setText(pref.getString("phone", ""))
            etLocation.setText(pref.getString("location", ""))
            etJob.setText(pref.getString("job", ""))
            etCareer.setText(pref.getString("career", ""))

            pref.getString("education", null)?.let {
                selectedEducation = it
                tvEducationSelected.text = it
                tvEducationSelected.setTextColor(Color.parseColor("#18191C"))
            }
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { attemptSaveResume() }
    }

    private fun attemptSaveResume() {
        val name = etName.text.toString().trim()
        val intro = etIntro.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val job = etJob.text.toString().trim()
        val career = etCareer.text.toString().trim()

        when {
            name.isEmpty() -> {
                Toast.makeText(this, "필수 항목: 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                etName.requestFocus()
                return
            }
            intro.isEmpty() -> {
                Toast.makeText(this, "필수 항목: 소개를 입력해주세요.", Toast.LENGTH_SHORT).show()
                etIntro.requestFocus()
                return
            }
            phone.isEmpty() -> {
                Toast.makeText(this, "필수 항목: 휴대폰 번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                etPhone.requestFocus()
                return
            }
            location.isEmpty() -> {
                Toast.makeText(this, "필수 항목: 희망 지역을 입력해주세요.", Toast.LENGTH_SHORT).show()
                etLocation.requestFocus()
                return
            }
            job.isEmpty() -> {
                Toast.makeText(this, "필수 항목: 희망 직종을 입력해주세요.", Toast.LENGTH_SHORT).show()
                etJob.requestFocus()
                return
            }
            career.isEmpty() -> {
                Toast.makeText(this, "필수 항목: 경력 사항을 입력해주세요.", Toast.LENGTH_SHORT).show()
                etCareer.requestFocus()
                return
            }
        }

        val pref = getSharedPreferences("TimePick_Resume_$userId", MODE_PRIVATE)
        pref.edit().apply {
            putString("name", name)
            putString("intro", intro)
            putString("phone", phone)
            putString("location", location)
            putString("job", job)
            putString("career", career)

            if (selectedEducation.isNullOrBlank()) {
                remove("education")
            } else {
                putString("education", selectedEducation)
            }

            putBoolean("has_resume", true)
            apply()
        }

        Toast.makeText(this, "이력서가 저장되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }
}