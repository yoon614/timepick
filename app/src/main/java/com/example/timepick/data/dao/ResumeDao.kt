package com.example.timepick.data.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.timepick.data.entity.ResumeEntity

interface ResumeDao {

    // userId로 이력서 조회 (Resume 객체 반환)
    @Query("SELECT * FROM resume WHERE userId = :userId")
    suspend fun getResumeByUserId(userId: Int): ResumeEntity?

    /**
     * 이력서 내용 저장
     * - insert + update 공통 함수
     * - 최초 작성 = insert
     * - 이후 userId가 이미 존재하면 기존 row 삭제 -> 새 insert를 수행함 = update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResume(resume: ResumeEntity)

    // userId로 이력서 삭제
    @Query("DELETE FROM resume WHERE userId = :userId")
    suspend fun deleteResumeByUserId(userId: Int)

    // 이력서 존재 여부 확인
    @Query("SELECT EXISTS(SELECT 1 FROM resume WHERE userId = :userId)")
    suspend fun isResumeExist(userId: Int): Boolean
}