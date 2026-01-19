package com.example.timepick

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.timepick.data.AppDatabase
import com.example.timepick.data.UserEntity
import kotlinx.coroutines.launch
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getInstance(application).userDao()


    /* ---------- 로그인 ---------- */

    // 로그인 성공 시 유저 정보 반환, 아이디/비번 틀리면 null 반환
    fun logIn(email: String, password: String, onResult: (UserEntity?) -> Unit) {
        viewModelScope.launch {
            val hashed = hashPassword(password)
            val user = withContext(Dispatchers.IO) {
                userDao.login(email, hashed)
            }
            onResult(user)
        }
    }

    /* ---------- 회원가입 / 탈퇴 ---------- */

    // 회원 가입 함수 (가입 완료시 유저 객체 반환 + 자동 로그인)
    fun signUp(email: String, password: String, name: String, onResult: (UserEntity) -> Unit) {
        viewModelScope.launch {
            val hashed = hashPassword(password)
            val user = UserEntity(email = email, password = hashed, name = name)

            // DB에 회원 저장
            withContext(Dispatchers.IO) {
                userDao.insertUser(user)
            }
            onResult(user)
        }
    }

    // 회원 탈퇴 함수 (탈퇴 완료 시 true 반환)
    fun deleteUser(userId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            // 회원 삭제
            withContext(Dispatchers.IO) {
                userDao.deleteUserById(userId)
            }
            onResult(true)
        }
    }

    /* ---------- 유효성 검사, 해시화 ---------- */

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

    // 비밀번호 해시화 함수 (평문으로 저장 x)
    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /* ---------- 마이페이지 사용자 정보 수정 ---------- */

    fun updateUserInfo(
        userId: Int,
        newName: String,
        currentPassword: String,
        newPassword: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            // 유저 정보 조회
            val user = withContext(Dispatchers.IO) {
                userDao.getUserById(userId)
            }

            if (user == null) {
                onResult(false)
                return@launch
            }

            // 현재 비밀번호 검증 (필수)
            val currentHashed = hashPassword(currentPassword)
            if (user.password != currentHashed) {
                onResult(false)
                return@launch
            }

            // 새 비밀번호가 있음 -> 해시, 없으면 기존 비밀번호 사용
            val passwordToSave = if (newPassword.isNullOrBlank()) {
                user.password
            } else {
                hashPassword(newPassword)
            }

            // 사용자 정보 업데이트
            withContext(Dispatchers.IO) {
                userDao.updateUser(
                    userId = userId,
                    name = newName,
                    password = passwordToSave
                )
            }
            onResult(true)
        }
    }
}