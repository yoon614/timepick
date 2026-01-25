package com.example.timepick.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class JobWithTimes(
    @Embedded val job: JobEntity,
    @Relation(
        parentColumn = "jobId",    // JobEntity의 기본키
        entityColumn = "jobId"     // JobTimeEntity의 외래키
    )
    val times: List<JobTimeEntity>
)