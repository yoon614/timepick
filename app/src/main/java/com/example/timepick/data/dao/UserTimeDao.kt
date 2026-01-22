package com.example.timepick.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.timepick.data.entity.UserTimeEntity

@Dao
interface UserTimeDao {
    // 1. 알바생이 선택한 시간들 저장
    @Insert
    suspend fun insertUserTimes(userTimes: List<UserTimeEntity>)

    // 2. 알바생이 선택했던 시간들 불러오기 (매칭 로직용)
    @Query("SELECT timeIndex FROM user_times WHERE userId = :userId")
    suspend fun getUserTimes(userId: Int): List<Int>

    // 3. 기존에 저장된 시간 초기화 (시간을 새로 수정할 때 필요)
    @Query("DELETE FROM user_times WHERE userId = :userId")
    suspend fun deleteUserTimes(userId: Int)
}