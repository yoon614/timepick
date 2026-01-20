package com.example.timepick.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Query
import com.example.timepick.data.UserEntity

@Dao
interface UserDao {

    // 회원 정보 insert
    @Insert
    suspend fun insertUser(user: UserEntity)

    // 회원 정보 delete
    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUserById(userId: Int)

    // 회원 정보 update (email은 수정 x)
    @Query("""
    UPDATE users
    SET name = :name, password = :password
    WHERE userId = :userId
""")
    suspend fun updateUser(
        userId: Int,
        name: String,
        password: String
    )

    // userId로 유저 조회
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): UserEntity?

    // 이메일(=아이디) 로 유저 조회
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    // 로그인 (이메일, 비밀번호 일치 조회)
    @Query("""
        SELECT * FROM users
        WHERE email = :email AND password = :password
        LIMIT 1
        """)
    suspend fun login(email: String, password: String): UserEntity?
}