package com.example.timepick.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.timepick.data.entity.JobEntity
import com.example.timepick.data.entity.JobWithTimes


@Dao
interface JobDao {
    // 1. 초기 더미 데이터 삽입: 앱 실행 시 정보를 미리 넣어놓기 위함
    @Insert
    suspend fun insertAllJobs(jobs: List<JobEntity>): List<Long>

    // 2. 전체 공고 가져오기: 알바생이 시간 입력 후 '확인'을 눌렀을 때 리스트 생성
    @Query("SELECT * FROM jobs")
    suspend fun getAllJobs(): List<JobEntity>

    // 3. 공고 상세 내용 가져오기: 리스트에서 공고를 클릭했을 때 '상세 공고'을 보여줌
    @Query("SELECT * FROM jobs WHERE jobId = :jobId")
    suspend fun getJobById(jobId: Int): JobEntity

    @Transaction // 1:N 관계 조회를 위해 필수
    @Query("SELECT * FROM jobs")
    suspend fun getAllJobsWithTimes(): List<JobWithTimes>
}