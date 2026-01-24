package com.example.timepick.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "work_schedules")
data class WorkScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val workplaceName: String,  // 근무지 (예: 파리바게뜨)
    val workDate: String,       // 날짜 (ISO_LOCAL_DATE 형식: "2024-05-20")
    val startTime: String,      // 시작 시간 (예: "21:00")
    val endTime: String,        // 종료 시간 (예: "03:00")
    val hourlyRate: Int,        // 시급
    val applyTax: Boolean,      // 3.3% 세금 적용 여부
    val isWeeklyFixed: Boolean, // 매주 고정 여부
    val groupId: Long? = null   // 고정 일정들을 하나로 묶는 ID (반복 생성된 데이터들끼리 공유)
) : Serializable // 수정 화면으로 데이터를 넘기기 위해 Serializable 구현