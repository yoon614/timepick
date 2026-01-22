package com.example.timepick.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey


@Entity(
    tableName = "job_times",
    foreignKeys = [ForeignKey(
        entity = JobEntity::class,
        parentColumns = ["jobId"],
        childColumns = ["jobId"],
        onDelete = ForeignKey.CASCADE // 공고 삭제 시 시간 데이터도 자동 삭제
    )]
)
data class JobTimeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val jobId: Int,      // 어떤 공고의 시간인지 (외래키)
    val timeIndex: Int   // 시간 인덱스 (0~251)
)