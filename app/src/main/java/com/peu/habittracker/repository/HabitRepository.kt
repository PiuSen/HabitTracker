package com.peu.habittracker.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.peu.habittracker.db.DailyStat
import com.peu.habittracker.db.Habit
import com.peu.habittracker.db.HabitCompletion
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    fun getHabitById(habitId: Long): Flow<Habit?>
    suspend fun insertHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    suspend fun toggleHabitCompletion(habitId: Long, date: String)
    suspend fun isHabitCompletedOnDate(habitId: Long, date: String): Boolean
    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>>
    suspend fun getDailyStats(): List<DailyStat>
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getWeeklyStats(): List<Int>

    suspend fun getCompletionsInRange(habitId: Long, startDate: String, endDate: String): List<HabitCompletion>
}