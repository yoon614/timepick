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

/**
 * SignUpWorkerActivity - 알바생 회원가입 화면
 *
 * 플로우:
 * - 이름, 이메일, 비밀번호 입력
 * - 이메일 중복 확인
 * - 비밀번호 확인 일치 검사
 * - 가입하기 버튼 클릭 -> SignUpCompleteActivity로 이동
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

    // 이메일 중복 확인 여부
    private var isEmailChecked = false
    private var isEmailAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_worker)

        // View 초기화
        initViews()

        // 클릭 리스너 설정
        setupClickListeners()

        // TextWatcher 설정
        setupTextWatchers()
    }

    /**
     * View 초기화
     */
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

    /**
     * 클릭 이벤트 리스너 설정
     */
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
     * TextWatcher 설정 - 입력값 변경 감지
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
     * 이메일 중복 확인
     * TODO: 백엔드 API 연동 필요
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

        // TODO: 백엔드 API 호출로 실제 중복 확인
        // 현재는 임시로 모든 이메일을 사용 가능으로 처리
        isEmailChecked = true
        isEmailAvailable = true
        tvEmailMsg.text = "사용 가능한 이메일입니다."
        tvEmailMsg.setTextColor(getColor(android.R.color.holo_green_dark))
        tvEmailMsg.visibility = View.VISIBLE

        Toast.makeText(this, "이메일 중복 확인이 완료되었습니다.", Toast.LENGTH_SHORT).show()

        // 실제 구현 예시 (백엔드 연동 시):
        /*
        val apiService = RetrofitClient.getApiService()
        apiService.checkEmailDuplicate(email).enqueue(object : Callback<EmailCheckResponse> {
            override fun onResponse(call: Call<EmailCheckResponse>, response: Response<EmailCheckResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val isDuplicate = response.body()!!.isDuplicate
                    if (isDuplicate) {
                        isEmailChecked = true
                        isEmailAvailable = false
                        tvEmailMsg.text = "이미 사용 중인 이메일입니다."
                        tvEmailMsg.setTextColor(getColor(android.R.color.holo_red_dark))
                    } else {
                        isEmailChecked = true
                        isEmailAvailable = true
                        tvEmailMsg.text = "사용 가능한 이메일입니다."
                        tvEmailMsg.setTextColor(getColor(android.R.color.holo_green_dark))
                    }
                    tvEmailMsg.visibility = View.VISIBLE
                }
            }
            override fun onFailure(call: Call<EmailCheckResponse>, t: Throwable) {
                Toast.makeText(this@SignUpWorkerActivity, "서버 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
        */
    }

    /**
     * 비밀번호 일치 여부 확인
     */
    private fun checkPasswordMatch() {
        val pw = etPw.text.toString()
        val pwConfirm = etPwConfirm.text.toString()

        if (pwConfirm.isNotEmpty() && pw != pwConfirm) {
            tvPwConfirmError.visibility = View.VISIBLE
        } else {
            tvPwConfirmError.visibility = View.GONE
        }
    }

    /**
     * 회원가입 시도
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

        if (pw.length < 8) {
            Toast.makeText(this, "비밀번호는 8자리 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
            etPw.requestFocus()
            return
        }

        if (pw != pwConfirm) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            etPwConfirm.requestFocus()
            return
        }

        // TODO: 백엔드 API 호출로 실제 회원가입 처리
        // 현재는 임시로 바로 완료 화면으로 이동
        signUpSuccess(name)

        // 실제 구현 예시 (백엔드 연동 시):
        /*
        val signUpRequest = SignUpRequest(
            name = name,
            email = email,
            password = pw,
            role = "worker"
        )

        val apiService = RetrofitClient.getApiService()
        apiService.signUp(signUpRequest).enqueue(object : Callback<SignUpResponse> {
            override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    signUpSuccess(name)
                } else {
                    Toast.makeText(this@SignUpWorkerActivity, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                Toast.makeText(this@SignUpWorkerActivity, "서버 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
        */
    }

    /**
     * 회원가입 성공 시 완료 화면으로 이동
     */
    private fun signUpSuccess(userName: String) {
        val intent = Intent(this, SignUpCompleteActivity::class.java)
        intent.putExtra("USER_NAME", userName)
        startActivity(intent)
        // 회원가입 플로우의 모든 액티비티 종료
        finishAffinity()
    }
}