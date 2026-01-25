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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * SignUpWorkerActivity - 알바생 회원가입 화면 (ViewModel 사용)
 *
 * 플로우:
 * - 이름, 이메일, 비밀번호 입력
 * - 이메일 중복 확인
 * - 비밀번호 확인 일치 검사
 * - 가입하기 버튼 클릭 -> DB 저장 후 SharedPreferences에 로그인 정보 저장
 * - SignUpCompleteActivity로 이동
 */
class SignUpWorkerActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnCheckEmail: Button
    private lateinit var tvEmailMsg: TextView
    private lateinit var etPw: EditText
    private lateinit var etPwConfirm: EditText
    private lateinit var tvPwConfirmError: TextView
    private lateinit var btnSubmit: Button

    private lateinit var viewModel: MainViewModel

    private var isEmailChecked = false
    private var isEmailAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_worker)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        initViews()
        setupClickListeners()
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
        btnBack.setOnClickListener {
            finish()
        }

        btnCheckEmail.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "올바른 이메일 형식을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.isEmailAvailable(email) { available ->
                isEmailChecked = true
                isEmailAvailable = available

                if (available) {
                    tvEmailMsg.text = "사용 가능한 이메일입니다."
                    tvEmailMsg.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                    tvEmailMsg.visibility = View.VISIBLE
                } else {
                    tvEmailMsg.text = "이미 사용 중인 이메일입니다."
                    tvEmailMsg.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                    tvEmailMsg.visibility = View.VISIBLE
                }
            }
        }

        btnSubmit.setOnClickListener {
            attemptSignUp()
        }
    }

    private fun setupTextWatchers() {
        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isEmailChecked = false
                isEmailAvailable = false
                tvEmailMsg.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etPwConfirm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val pw = etPw.text.toString()
                val pwConfirm = s.toString()

                if (pwConfirm.isEmpty()) {
                    tvPwConfirmError.text = "비밀번호 확인을 입력해주세요."
                    tvPwConfirmError.visibility = View.VISIBLE
                } else if (!viewModel.doPasswordMatch(pw, pwConfirm)) {
                    tvPwConfirmError.text = "비밀번호가 일치하지 않습니다."
                    tvPwConfirmError.visibility = View.VISIBLE
                } else {
                    tvPwConfirmError.visibility = View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun attemptSignUp() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val pw = etPw.text.toString()
        val pwConfirm = etPwConfirm.text.toString()

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

        // 회원가입 시도
        viewModel.signUp(email, pw, name) { success ->
            if (success) {
                // 회원가입 성공 후 DB에서 userId 조회
                saveLoginInfo(email, name)
            } else {
                Toast.makeText(this, "회원가입 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 회원가입 성공 후 DB에서 userId 조회하여 SharedPreferences 저장
    private fun saveLoginInfo(email: String, name: String) {
        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) {
                val db = com.example.timepick.data.AppDatabase.getInstance(applicationContext)
                db.userDao().getUserByEmail(email)
            }

            if (user != null) {
                // SharedPreferences에 로그인 정보 저장
                val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
                sharedPref.edit().apply {
                    putBoolean("IS_LOGGED_IN", true)
                    putString("USER_ID", user.userId.toString())
                    putString("USER_NAME", user.name)
                    apply()
                }

                // 회원가입 완료 화면으로 이동
                val intent = Intent(this@SignUpWorkerActivity, SignUpCompleteActivity::class.java)
                intent.putExtra("USER_NAME", name)
                startActivity(intent)
                finishAffinity()
            } else {
                Toast.makeText(this@SignUpWorkerActivity, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}