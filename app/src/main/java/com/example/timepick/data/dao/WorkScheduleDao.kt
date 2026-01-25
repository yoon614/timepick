package com.example.timepick.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.timepick.data.entity.WorkScheduleEntity

@Dao
interface WorkScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: WorkScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<WorkScheduleEntity>)

    @Query("SELECT * FROM work_schedules WHERE userId = :userId AND workDate = :date")
    suspend fun getSchedulesByDate(userId: Int, date: String): List<WorkScheduleEntity>

    @Delete
    suspend fun deleteSchedule(schedule: WorkScheduleEntity)

    // 고정 일정 전체 삭제용 (수정/삭제 시 활용)
    @Query("DELETE FROM work_schedules WHERE groupId = :groupId AND workDate >= :startDate")
    suspend fun deleteFutureSchedules(groupId: Long, startDate: String)
    // data/dao/WorkScheduleDao.kt (또는 파일명 확인)

    @Query("SELECT * FROM work_schedules WHERE userId = :userId AND workDate LIKE :monthQuery")
    suspend fun getSchedulesByMonth(userId: Int, monthQuery: String): List<WorkScheduleEntity>
}