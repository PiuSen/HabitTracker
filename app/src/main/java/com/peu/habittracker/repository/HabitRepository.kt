package com.peu.habittracker.repository

import com.peu.habittracker.db.Habit
import com.peu.habittracker.db.HabitCompletion
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    fun getHabitById(habitId: Long): Flow<Habit?>
    suspend fun insertHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    suspend fun toggleHabitCompletion(habitId: Long, date: String)
    suspend fun isHabitCompletedOnDate(habitId: Long, date: String): Boolean
    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>>
    suspend fun getCompletionsInRange(habitId: Long, startDate: String, endDate: String): List<HabitCompletion>
}