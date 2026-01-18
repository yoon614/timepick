package com.example.timepick.data

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import com.example.timepick.data.UserDao
import com.example.timepick.data.UserEntity

@Database(
    entities = [UserEntity::class],
    version = 1
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timepick.db"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}