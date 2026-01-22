package com.example.timepick.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.timepick.data.entity.JobTimeEntity


@Dao
interface JobTimeDao {
    // 1. 공고별 시간 데이터 미리 넣기: 더미 공고들에 대한 0~251번 인덱스 정보를 저장
    @Insert
    suspend fun insertJobTimes(jobTimes: List<JobTimeEntity>)

    // 2. 특정 공고의 시간 인덱스들 가져오기: 알바생이 입력한 시간과 대조하여 매칭 퍼센트(%)를 계산할 때 사용
    @Query("SELECT timeIndex FROM job_times WHERE jobId = :jobId")
    suspend fun getTimeIndicesForJob(jobId: Int): List<Int>
}