package com.example.timepick

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

/**
 EditProfileActivity - 회원정보 수정 화면

 플로우:
  - 현재 사용자 정보 표시
  - 이름, 비밀번호 수정 가능
  - MainViewModel의 updateUserInfo() 사용
 */
class EditProfileActivity : AppCompatActivity() {

    // UI 컴포넌트
    private lateinit var btnBack: ImageButton
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etCurrentPw: EditText
    private lateinit var tvCurrentPwError: TextView
    private lateinit var etNewPw: EditText
    private lateinit var etNewPwConfirm: EditText
    private lateinit var tvNewPwConfirmError: TextView
    private lateinit var btnConfirm: Button

    // ViewModel
    private lateinit var viewModel: MainViewModel

    // 사용자 정보
    private var userId: Int = 0
    private var userName: String = ""
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // ViewModel 초기화
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // 로그인 정보 불러오기
        loadUserInfo()

        // View 초기화
        initViews()

        // 현재 정보 표시
        displayCurrentInfo()

        // TextWatcher 설정
        setupTextWatchers()

        // 클릭 리스너 설정
        setupClickListeners()
    }

    /**
     SharedPreferences에서 로그인 정보 불러오기
     */
    private fun loadUserInfo() {
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        userId = sharedPref.getString("USER_ID", "0")?.toIntOrNull() ?: 0
        userName = sharedPref.getString("USER_NAME", "") ?: ""

        // 이메일은 DB에서 가져와야 하지만, 임시로 SharedPreferences 사용
        userEmail = sharedPref.getString("USER_EMAIL", "") ?: ""
    }


    private fun initViews() {
        btnBack = findViewById(R.id.btn_profile_edit_back)
        etName = findViewById(R.id.et_edit_name)
        etEmail = findViewById(R.id.et_edit_email)
        etCurrentPw = findViewById(R.id.et_current_pw)
        tvCurrentPwError = findViewById(R.id.tv_current_pw_error)
        etNewPw = findViewById(R.id.et_new_pw)
        etNewPwConfirm = findViewById(R.id.et_new_pw_confirm)
        tvNewPwConfirmError = findViewById(R.id.tv_new_pw_confirm_error)
        btnConfirm = findViewById(R.id.btn_profile_edit_confirm)
    }

    /**
     현재 사용자 정보 표시
     */
    private fun displayCurrentInfo() {
        etName.setText(userName)
        etEmail.setText(userEmail)
    }

    /**
     TextWatcher 설정
     */
    private fun setupTextWatchers() {
        // 새 비밀번호 확인 실시간 검증
        etNewPwConfirm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkNewPasswordMatch()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    /**
     새 비밀번호 일치 여부 확인
     */
    private fun checkNewPasswordMatch() {
        val newPw = etNewPw.text.toString()
        val newPwConfirm = etNewPwConfirm.text.toString()

        if (newPwConfirm.isNotEmpty()) {
            if (viewModel.doPasswordMatch(newPw, newPwConfirm)) {
                tvNewPwConfirmError.visibility = View.GONE
            } else {
                tvNewPwConfirmError.visibility = View.VISIBLE
            }
        } else {
            tvNewPwConfirmError.visibility = View.GONE
        }
    }


    private fun setupClickListeners() {
        // 뒤로가기 버튼
        btnBack.setOnClickListener {
            finish()
        }

        // 확인 버튼 - 회원정보 수정
        btnConfirm.setOnClickListener {
            attemptUpdateProfile()
        }
    }

    /**
     회원정보 수정 시도
     */
    private fun attemptUpdateProfile() {
        // 에러 메시지 초기화
        tvCurrentPwError.visibility = View.GONE
        tvNewPwConfirmError.visibility = View.GONE

        val newName = etName.text.toString().trim()
        val currentPw = etCurrentPw.text.toString()
        val newPw = etNewPw.text.toString()
        val newPwConfirm = etNewPwConfirm.text.toString()

        // 유효성 검사
        if (newName.isEmpty()) {
            Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            etName.requestFocus()
            return
        }

        if (currentPw.isEmpty()) {
            tvCurrentPwError.visibility = View.VISIBLE
            Toast.makeText(this, "현재 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            etCurrentPw.requestFocus()
            return
        }

        // 새 비밀번호 입력 시 유효성 검사
        if (newPw.isNotEmpty()) {
            if (!viewModel.isPasswordValid(newPw)) {
                Toast.makeText(this, "비밀번호는 8자리 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                etNewPw.requestFocus()
                return
            }

            if (!viewModel.doPasswordMatch(newPw, newPwConfirm)) {
                tvNewPwConfirmError.visibility = View.VISIBLE
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                etNewPwConfirm.requestFocus()
                return
            }
        }

        // ViewModel을 통한 회원정보 수정
        viewModel.updateUserInfo(
            userId = userId,
            newName = newName,
            currentPassword = currentPw,
            newPassword = newPw
        ) { success ->
            if (success) {
                // 수정 성공
                updateSharedPreferences(newName)
                Toast.makeText(this, "회원정보가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                // 수정 실패 (현재 비밀번호 불일치)
                tvCurrentPwError.visibility = View.VISIBLE
                tvCurrentPwError.text = "현재 비밀번호가 일치하지 않습니다."
                Toast.makeText(this, "현재 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     SharedPreferences 업데이트
     */
    private fun updateSharedPreferences(newName: String) {
        val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_NAME", newName)
            apply()
        }
    }
}