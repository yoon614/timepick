package com.example.timepick

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.timepick.data.AppDatabase
import com.example.timepick.data.UserEntity
import kotlinx.coroutines.launch
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getInstance(application).userDao()

    // 이메일(=아이디) 중복 검사 함수
    // 검사 결과 중복 아이디가 없으면 (=null이면) true 반환 (사용 가능)
    fun isEmailAvailable(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) { userDao.getUserByEmail(email) }
            onResult(user == null)
        }
    }

    // 비밀번호 8자 이상 검사 함수 (8자 이상이면 true 반환)
    fun isPasswordValid(password: String): Boolean {
        return password.length >= 8
    }

    // 비밀번호 재확인 검사 함수
    fun doPasswordMatch(password: String, confirm: String): Boolean {
        return password == confirm
    }


    // 회원 가입 함수 (가입 완료시 true 반환)
    fun signUp(email: String, password: String, name: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val hashed = hashPassword(password)
            userDao.insertUser(UserEntity(email = email, password = hashed, name = name))
            onResult(true)
        }
    }

    // 로그인 함수
    // 로그인 성공 시 유저 정보 반환, 아이디/비번 틀리면 null 반환
    fun logIn(email: String, password: String, onResult: (UserEntity?) -> Unit) {
        viewModelScope.launch {
            val hashed = hashPassword(password)
            val user = userDao.login(email, hashed)
            onResult(user)
        }
    }

    // 비밀번호 해시화 함수 (평문으로 저장 x)
    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

}