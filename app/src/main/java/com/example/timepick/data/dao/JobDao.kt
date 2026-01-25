package com.example.timepick.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.timepick.data.entity.JobEntity
import com.example.timepick.data.entity.JobWithTimes


@Dao
interface JobDao {
    // 1. 초기 더미 데이터 삽입
    @Insert
    suspend fun insertAllJobs(jobs: List<JobEntity>): List<Long>

    // 2. 전체 공고 가져오기 (단순 JobEntity 리스트)
    @Query("SELECT * FROM jobs")
    suspend fun getAllJobs(): List<JobEntity>

    // 3. 공고 상세 내용 가져오기
    @Query("SELECT * FROM jobs WHERE jobId = :jobId")
    suspend fun getJobById(jobId: Int): JobEntity

    // 4. 전체 공고와 시간 정보를 한 번에 가져오기 (비동기 일회성 호출)
    @Transaction
    @Query("SELECT * FROM jobs")
    suspend fun getAllJobsWithTimes(): List<JobWithTimes>

    // 5. 전체 공고와 시간 정보를 LiveData로 가져오기 (DB 변경 시 자동 업데이트)
    // 메서드 이름을 위와 다르게 설정하여 충돌을 피합니다.
    @Transaction
    @Query("SELECT * FROM jobs")
    fun getAllJobsWithTimesLiveData(): LiveData<List<JobWithTimes>>

    // 6. 특정 공고 상세와 시간 정보 가져오기
    @Transaction
    @Query("SELECT * FROM jobs WHERE jobId = :jobId")
    suspend fun getJobWithTimesById(jobId: Int): JobWithTimes?
}