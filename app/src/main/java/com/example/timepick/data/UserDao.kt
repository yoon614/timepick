package com.example.timepick.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.timepick.data.UserEntity

@Dao
interface UserDao {

    // 회원 정보 insert
    @Insert
    suspend fun insertUser(user: UserEntity)

    // 이메일(=아이디) 중복 시 유저 객체 반환, 없으면 null 반환
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    // 로그인 (이메일, 비밀번호 동시에 일치)
    @Query("""
        SELECT * FROM users
        WHERE email = :email AND password = :password
        LIMIT 1
        """)
    suspend fun login(email: String, password: String): UserEntity?

}