package com.example.timepick

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
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
    private lateinit var etAddress: EditText  // 추가
    private lateinit var etEmail: EditText    // 추가
    private lateinit var etSkills: EditText   // 추가
    private lateinit var etLocation: EditText
    private lateinit var etJob: EditText
    private lateinit var etCareer: EditText

    private lateinit var tvDate: TextView  // 추가: 날짜 TextView

    private lateinit var layoutEducationTrigger: LinearLayout
    private lateinit var tvEducationSelected: TextView
    private lateinit var listCard: CardView

    private var selectedEducation: String? = null
    private var userId: Int = 0

    // 글자수 제한
    private val NAME_MAX_LENGTH = 15
    private val INTRO_MAX_LENGTH = 50
    private val CAREER_MAX_LENGTH = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resume_edit)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]

        loadUserId()
        initViews()
        setupTextWatchers()
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

        // 선택사항 필드들 - 전체 레이아웃에서 찾기
        val rootView = window.decorView.findViewById<View>(android.R.id.content)
        etAddress = findEditTextRecursive(rootView, "주소를 입력해주세요.") ?: EditText(this)
        etEmail = findEditTextRecursive(rootView, "timepick@email.com") ?: EditText(this)
        etSkills = findEditTextRecursive(rootView, "자격증이나 능력을 입력해주세요.") ?: EditText(this)

        // 날짜 TextView 찾기
        tvDate = findTextViewByText(rootView, "2026.01.16") ?: TextView(this)

        layoutEducationTrigger = findViewById(R.id.layout_education_trigger)
        tvEducationSelected = findViewById(R.id.tv_education_selected)
        listCard = findViewById(R.id.cv_education_list)

        // 글자수 제한
        etName.filters = arrayOf(InputFilter.LengthFilter(NAME_MAX_LENGTH))
        etIntro.filters = arrayOf(InputFilter.LengthFilter(INTRO_MAX_LENGTH))
        etCareer.filters = arrayOf(InputFilter.LengthFilter(CAREER_MAX_LENGTH))
    }

    // View를 재귀적으로 탐색하여 hint로 EditText 찾기
    private fun findEditTextRecursive(view: View, hint: String): EditText? {
        if (view is EditText && view.hint?.toString() == hint) {
            return view
        }
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                val found = findEditTextRecursive(view.getChildAt(i), hint)
                if (found != null) return found
            }
        }
        return null
    }

    // View를 재귀적으로 탐색하여 text로 TextView 찾기
    private fun findTextViewByText(view: View, text: String): TextView? {
        if (view is TextView && view.text?.toString() == text) {
            return view
        }
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                val found = findTextViewByText(view.getChildAt(i), text)
                if (found != null) return found
            }
        }
        return null
    }

    private fun setupTextWatchers() {
        setupCharCountWatcher(etName, NAME_MAX_LENGTH)
        setupCharCountWatcher(etIntro, INTRO_MAX_LENGTH)
        setupCharCountWatcher(etCareer, CAREER_MAX_LENGTH)
    }

    private fun setupCharCountWatcher(editText: EditText, maxLength: Int) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val parent = editText.parent as? FrameLayout
                parent?.let {
                    for (i in 0 until it.childCount) {
                        val child = it.getChildAt(i)
                        if (child is TextView && child.id != editText.id) {
                            child.text = "${s?.length ?: 0}/$maxLength"
                            break
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
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

                // 선택사항 불러오기
                etAddress.setText(it.address ?: "")
                etEmail.setText(it.email ?: "")
                etSkills.setText(it.skills ?: "")

                // 날짜 표시
                tvDate.text = it.updatedDate ?: getCurrentDate()

                it.education?.let { edu ->
                    selectedEducation = edu
                    tvEducationSelected.text = edu
                    tvEducationSelected.setTextColor(Color.parseColor("#18191C"))
                }
            } ?: run {
                // 새로 작성하는 경우 현재 날짜 표시
                tvDate.text = getCurrentDate()
            }
        }
    }

    private fun getCurrentDate(): String {
        val formatter = java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault())
        return formatter.format(java.util.Date())
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

        // 선택사항
        val address = etAddress.text.toString().trim().ifEmpty { null }
        val email = etEmail.text.toString().trim().ifEmpty { null }
        val skills = etSkills.text.toString().trim().ifEmpty { null }

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
            !isPhoneValid(phone) -> {
                Toast.makeText(this, "올바른 휴대폰 번호 형식이 아닙니다. (예: 010-1234-5678)", Toast.LENGTH_SHORT).show()
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
            address = address,
            email = email,
            education = selectedEducation,
            skills = skills,
            updatedDate = getCurrentDate()
        )

        viewModel.saveResume(resume) { success ->
            if (success) {
                Toast.makeText(this, "이력서가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "이력서 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isPhoneValid(phone: String): Boolean {
        val phonePattern = "^01[0-9]-\\d{3,4}-\\d{4}$".toRegex()
        return phonePattern.matches(phone)
    }
}