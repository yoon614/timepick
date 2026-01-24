package com.example.timepick.data

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import com.example.timepick.data.dao.JobDao
import com.example.timepick.data.dao.JobTimeDao
import com.example.timepick.data.dao.UserDao
import com.example.timepick.data.dao.UserTimeDao
import com.example.timepick.data.entity.UserEntity
import com.example.timepick.data.entity.JobEntity
import com.example.timepick.data.entity.JobTimeEntity
import com.example.timepick.data.entity.UserTimeEntity
import com.example.timepick.data.dao.AppliedJobDao
import com.example.timepick.data.dao.ResumeDao
import com.example.timepick.data.dao.WorkScheduleDao
import com.example.timepick.data.entity.AppliedJobEntity
import com.example.timepick.data.entity.ResumeEntity


@Database(
    entities = [UserEntity::class, UserTimeEntity::class, JobEntity::class, JobTimeEntity::class,
        AppliedJobEntity::class, ResumeEntity::class, WorkScheduleDao::class],
    version = 5
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userTimeDao(): UserTimeDao
    abstract fun jobDao(): JobDao
    abstract fun jobTimeDao(): JobTimeDao
    abstract fun appliedJobDao(): AppliedJobDao
    abstract fun resumeDao(): ResumeDao
    abstract fun workScheduleDao(): WorkScheduleDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timepick.db"
                )//기존 테이블 구조와 다를 경우 데이터를 비우고 새로 생성하여 충돌을 방지
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

