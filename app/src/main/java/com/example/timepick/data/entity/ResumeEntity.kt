package com.example.timepick.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "resume",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE // userId 삭제 시 이력서 자동 삭제
        )
    ],
    indices = [
        Index(value = ["userId"], unique = true) // userId 하나 당 이력서 1개
    ])
data class ResumeEntity(

    @PrimaryKey(autoGenerate = true)
    val resumeId: Int = 0,
    val userId: Int,

    // 필수 입력 값
    val name: String,
    val intro: String,
    val phone: String,
    val desiredRegion: String,
    val desiredJob: String,
    val career: String,

    // 선택 입력 값
    val address: String?,
    val email: String?,
    val education: String?,
    val skills: String?

)
