package com.example.timepick

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * LoginActivity - 로그인 화면
 *
 * 플로우:
 * - 이메일, 비밀번호 입력
 * - 로그인 버튼 클릭 -> 로그인 성공 시 타임테이블 화면으로 이동
 * - 회원가입하기 텍스트 클릭 -> RoleSelectActivity로 이동
 */
class LoginActivity : AppCompatActivity() {

    // UI 컴포넌트
    private lateinit var btnBack: ImageButton
    private lateinit var etEmail: EditText
    private lateinit var tvErrorEmail: TextView
    private lateinit var etPw: EditText
    private lateinit var tvErrorPw: TextView
    private lateinit var btnSubmit: Button
    private lateinit var tvGoSignup: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // View 초기화
        initViews()

        // 클릭 리스너 설정
        setupClickListeners()
    }

    /**
     * View 초기화
     */
    private fun initViews() {
        btnBack = findViewById(R.id.btn_login_back)
        etEmail = findViewById(R.id.et_login_email)
        tvErrorEmail = findViewById(R.id.tv_error_email)
        etPw = findViewById(R.id.et_login_pw)
        tvErrorPw = findViewById(R.id.tv_error_pw)
        btnSubmit = findViewById(R.id.btn_login_submit)
        tvGoSignup = findViewById(R.id.tv_login_go_signup)
    }

    /**
     * 클릭 이벤트 리스너 설정
     */
    private fun setupClickListeners() {
        // 뒤로가기 버튼
        btnBack.setOnClickListener {
            finish()
        }

        // 로그인 버튼
        btnSubmit.setOnClickListener {
            attemptLogin()
        }

        // 회원가입하기 텍스트 클릭
        tvGoSignup.setOnClickListener {
            val intent = Intent(this, RoleSelectActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * 로그인 시도
     */
    private fun attemptLogin() {
        // 에러 메시지 초기화
        tvErrorEmail.visibility = View.GONE
        tvErrorPw.visibility = View.GONE

        val email = etEmail.text.toString().trim()
        val pw = etPw.text.toString()

        // 유효성 검사
        var isValid = true

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

        if (pw.isEmpty()) {
            Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            etPw.requestFocus()
            return
        }

        // TODO: 백엔드 API 호출로 실제 로그인 처리
        // 현재는 임시로 바로 로그인 성공 처리
        loginSuccess()

        // 실제 구현 예시 (백엔드 연동 시):
        /*
        val loginRequest = LoginRequest(
            email = email,
            password = pw
        )

        val apiService = RetrofitClient.getApiService()
        apiService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!

                    // 로그인 정보 저장 (SharedPreferences 또는 DataStore)
                    saveLoginInfo(loginResponse.token, loginResponse.userId, loginResponse.userName)

                    loginSuccess()
                } else {
                    // 로그인 실패 처리
                    when (response.code()) {
                        401 -> {
                            // 이메일 또는 비밀번호 오류
                            tvErrorEmail.visibility = View.VISIBLE
                            tvErrorPw.visibility = View.VISIBLE
                        }
                        else -> {
                            Toast.makeText(this@LoginActivity, "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "서버 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
        */
    }

    /**
     * 로그인 성공 시 메인 화면으로 이동
     */
    private fun loginSuccess() {
        Toast.makeText(this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show()

        // TODO: TimePickActivity로 이동 (타임테이블 화면)
        // val intent = Intent(this, TimePickActivity::class.java)
        // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        // startActivity(intent)
        // finish()

        // 임시: 현재는 MainActivity로 이동
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * 로그인 정보 저장 (SharedPreferences 사용)
     * TODO: 백엔드 연동 시 사용
     */
    private fun saveLoginInfo(token: String, userId: String, userName: String) {
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("TOKEN", token)
            putString("USER_ID", userId)
            putString("USER_NAME", userName)
            putBoolean("IS_LOGGED_IN", true)
            apply()
        }
    }
}