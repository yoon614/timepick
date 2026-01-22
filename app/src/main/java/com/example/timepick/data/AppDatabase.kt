package com.example.timepick.data

import android.content.Context
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import com.example.timepick.data.UserDao
import com.example.timepick.data.UserEntity
import com.example.timepick.data.JobDao
import com.example.timepick.data.JobEntity
import com.example.timepick.data.JobTimeDao
import com.example.timepick.data.JobTimeEntity


@Database(
    entities = [UserEntity::class, UserTimeEntity::class, JobEntity::class, JobTimeEntity::class],
    version = 2
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userTimeDao(): UserTimeDao
    abstract fun jobDao(): JobDao
    abstract fun jobTimeDao(): JobTimeDao

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

