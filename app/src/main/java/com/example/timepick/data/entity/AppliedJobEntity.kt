package com.example.timepick.data.entity

import androidx.room.Entity

@Entity(
    tableName = "applied_jobs",
    primaryKeys = ["userId", "jobId"] // 한 유저가 같은 공고에 중복 지원 방지
)
data class AppliedJobEntity(
    val userId: Int,
    val jobId: Int,
    val appliedDate: Long = System.currentTimeMillis() // 지원 시간 기록용 (선택)
)