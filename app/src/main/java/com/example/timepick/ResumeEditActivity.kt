package com.example.timepick

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.example.timepick.data.entity.ResumeEntity

class ResumeEditActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
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
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resume_edit)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]

        loadUserId()
        initViews()
        setupEducationDropdown()
        loadResumeData()
        setupClickListeners()
    }

    private fun loadUserId() {
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        val userIdString = sharedPref.getString("USER_ID", "0") ?: "0"
        userId = userIdString.toIntOrNull() ?: 0
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
        viewModel.loadResume(userId) { resume ->
            resume?.let {
                etName.setText(it.name)
                etIntro.setText(it.intro)
                etPhone.setText(it.phone)
                etLocation.setText(it.desiredRegion)
                etJob.setText(it.desiredJob)
                etCareer.setText(it.career)

                it.education?.let { edu ->
                    selectedEducation = edu
                    tvEducationSelected.text = edu
                    tvEducationSelected.setTextColor(Color.parseColor("#18191C"))
                }
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

        val resume = ResumeEntity(
            userId = userId,
            name = name,
            intro = intro,
            phone = phone,
            desiredRegion = location,
            desiredJob = job,
            career = career,
            address = null,
            email = null,
            education = selectedEducation,
            skills = null
        )

        viewModel.saveResume(resume) { success ->
            if (success) {
                Toast.makeText(this, "이력서가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "이력서 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}