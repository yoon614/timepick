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
import androidx.lifecycle.ViewModelProvider

/**
LoginActivity - 로그인 화면 (ViewModel 사용)

 플로우:
  - 이메일, 비밀번호 입력
  - 로그인 버튼 클릭 -> 로그인 성공 시 타임테이블 화면으로 이동
  - 회원가입하기 텍스트 클릭 -> RoleSelectActivity로 이동
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

    // ViewModel
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // ViewModel 초기화
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

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
     * 로그인 시도 (ViewModel 사용)
     */
    private fun attemptLogin() {
        // 에러 메시지 초기화
        tvErrorEmail.visibility = View.GONE
        tvErrorPw.visibility = View.GONE

        val email = etEmail.text.toString().trim()
        val pw = etPw.text.toString()

        // 유효성 검사
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

        // ViewModel을 통한 로그인 처리
        viewModel.logIn(email, pw) { user ->
            if (user != null) {
                // 로그인 성공
                saveLoginInfo(user.userId.toString(), user.name)
                loginSuccess()
            } else {
                // 로그인 실패 (이메일 또는 비밀번호 불일치)
                tvErrorEmail.visibility = View.VISIBLE
                tvErrorPw.visibility = View.VISIBLE
                Toast.makeText(this, "이메일 또는 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 로그인 성공 시 메인 화면으로 이동
     */
    private fun loginSuccess() {
        Toast.makeText(this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show()

        // 타임픽 화면으로 이동
        val intent = Intent(this, TimePickActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * 로그인 정보 저장 (SharedPreferences 사용)
     */
    private fun saveLoginInfo(userId: String, userName: String) {
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_ID", userId)
            putString("USER_NAME", userName)
            putBoolean("IS_LOGGED_IN", true)
            apply()
        }
    }
}