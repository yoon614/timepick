package com.example.timepick.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.timepick.data.entity.AppliedJobEntity

@Dao
interface AppliedJobDao {
    // 1. 지원하기 (데이터 저장)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppliedJob(appliedJob: AppliedJobEntity)

    // 2. 이미 지원했는지 확인 (결과가 있으면 이미 지원한 것)
    @Query("SELECT * FROM applied_jobs WHERE userId = :userId AND jobId = :jobId")
    suspend fun getAppliedJob(userId: Int, jobId: Int): AppliedJobEntity?

    // 3. 특정 유저가 지원한 모든 공고 ID 가져오기 (전체 리스트에서 체크할 때 유용)
    @Query("SELECT jobId FROM applied_jobs WHERE userId = :userId")
    suspend fun getAppliedJobIds(userId: Int): List<Int>
}