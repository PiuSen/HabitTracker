package com.peu.habittracker.db

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [Habit::class, HabitCompletion::class],
    version = 1,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}