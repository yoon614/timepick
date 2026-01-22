package com.example.timepick.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "user_times",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["userId"], // UserEntity의 기본키
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class UserTimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,         // 어떤 알바생의 시간인지 (외래키)
    val timeIndex: Int       // 선택한 시간 인덱스 (0~251)
)