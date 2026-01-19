package com.example.timepick

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

/**
SignUpWorkerActivity - 알바생 회원가입 화면 (ViewModel 사용)
 플로우:
  - 이름, 이메일, 비밀번호 입력
  - 이메일 중복 확인
  - 비밀번호 확인 일치 검사
  - 가입하기 버튼 클릭 -> SignUpCompleteActivity로 이동
 */
class SignUpWorkerActivity : AppCompatActivity() {

    // UI 컴포넌트
    private lateinit var btnBack: ImageButton
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnCheckEmail: Button
    private lateinit var tvEmailMsg: TextView
    private lateinit var etPw: EditText
    private lateinit var etPwConfirm: EditText
    private lateinit var tvPwConfirmError: TextView
    private lateinit var btnSubmit: Button

    // ViewModel
    private lateinit var viewModel: MainViewModel

    // 이메일 중복 확인 여부
    private var isEmailChecked = false
    private var isEmailAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_worker)

        // ViewModel 초기화
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // View 초기화
        initViews()

        // 클릭 리스너 설정
        setupClickListeners()

        // TextWatcher 설정
        setupTextWatchers()
    }


    private fun initViews() {
        btnBack = findViewById(R.id.btn_signup_back)
        etName = findViewById(R.id.et_signup_name)
        etEmail = findViewById(R.id.et_signup_email)
        btnCheckEmail = findViewById(R.id.btn_signup_check_email)
        tvEmailMsg = findViewById(R.id.tv_signup_email_msg)
        etPw = findViewById(R.id.et_signup_pw)
        etPwConfirm = findViewById(R.id.et_signup_pw_confirm)
        tvPwConfirmError = findViewById(R.id.tv_signup_pw_confirm_error)
        btnSubmit = findViewById(R.id.btn_signup_submit)
    }

    private fun setupClickListeners() {
        // 뒤로가기 버튼
        btnBack.setOnClickListener {
            finish()
        }

        // 이메일 중복 확인 버튼
        btnCheckEmail.setOnClickListener {
            checkEmailDuplicate()
        }

        // 가입하기 버튼
        btnSubmit.setOnClickListener {
            attemptSignUp()
        }
    }

    /**
     TextWatcher 설정 - 입력값 변경 감지
     */
    private fun setupTextWatchers() {
        // 이메일 입력 필드 변경 시 중복확인 초기화
        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isEmailChecked = false
                isEmailAvailable = false
                tvEmailMsg.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 비밀번호 확인 필드 변경 시 일치 여부 확인
        etPwConfirm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkPasswordMatch()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    /**
     이메일 중복 확인
     */
    private fun checkEmailDuplicate() {
        val email = etEmail.text.toString().trim()

        // 이메일 유효성 검사
        if (email.isEmpty()) {
            Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "올바른 이메일 형식을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // ViewModel을 통한 이메일 중복 확인
        viewModel.isEmailAvailable(email) { isAvailable ->
            isEmailChecked = true
            isEmailAvailable = isAvailable

            if (isAvailable) {
                // 사용 가능한 이메일
                tvEmailMsg.text = "사용 가능한 이메일입니다."
                tvEmailMsg.setTextColor(getColor(android.R.color.holo_green_dark))
                tvEmailMsg.visibility = View.VISIBLE
                Toast.makeText(this, "사용 가능한 이메일입니다.", Toast.LENGTH_SHORT).show()
            } else {
                // 이미 사용 중인 이메일
                tvEmailMsg.text = "이미 사용 중인 이메일입니다."
                tvEmailMsg.setTextColor(getColor(android.R.color.holo_red_dark))
                tvEmailMsg.visibility = View.VISIBLE
                Toast.makeText(this, "이미 사용 중인 이메일입니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     비밀번호 일치 여부 확인
     */
    private fun checkPasswordMatch() {
        val pw = etPw.text.toString()
        val pwConfirm = etPwConfirm.text.toString()

        if (pwConfirm.isNotEmpty()) {
            val isMatch = viewModel.doPasswordMatch(pw, pwConfirm)
            if (!isMatch) {
                tvPwConfirmError.visibility = View.VISIBLE
            } else {
                tvPwConfirmError.visibility = View.GONE
            }
        } else {
            tvPwConfirmError.visibility = View.GONE
        }
    }

    /**
     회원가입 시도
     */
    private fun attemptSignUp() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val pw = etPw.text.toString()
        val pwConfirm = etPwConfirm.text.toString()

        // 유효성 검사
        if (name.isEmpty()) {
            Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            etName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            etEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "올바른 이메일 형식을 입력해주세요.", Toast.LENGTH_SHORT).show()
            etEmail.requestFocus()
            return
        }

        if (!isEmailChecked || !isEmailAvailable) {
            Toast.makeText(this, "이메일 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (pw.isEmpty()) {
            Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            etPw.requestFocus()
            return
        }

        if (!viewModel.isPasswordValid(pw)) {
            Toast.makeText(this, "비밀번호는 8자리 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
            etPw.requestFocus()
            return
        }


        if (!viewModel.doPasswordMatch(pw, pwConfirm)) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            etPwConfirm.requestFocus()
            return
        }


        viewModel.signUp(email, pw, name) { success ->
            if (success) {
                signUpSuccess(name)
            } else {
                Toast.makeText(this, "회원가입 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     회원가입 성공 시 완료 화면으로 이동
     */
    private fun signUpSuccess(userName: String) {
        val intent = Intent(this, SignUpCompleteActivity::class.java)
        intent.putExtra("USER_NAME", userName)
        startActivity(intent)
        // 회원가입 플로우의 모든 액티비티 종료
        finishAffinity()
    }
}