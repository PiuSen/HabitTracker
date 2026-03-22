package com.peu.habittracker.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.peu.habittracker.db.Habit
import com.peu.habittracker.db.HabitCompletion
import com.peu.habittracker.db.HabitDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao
) : HabitRepository
{

    override fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    override fun getHabitById(habitId: Long): Flow<Habit?> = habitDao.getHabitById(habitId)

    override suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)

    override suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    override suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun toggleHabitCompletion(habitId: Long, date: String) {
        val existing = habitDao.getCompletion(habitId, date)

        if (existing != null) {
            habitDao.deleteCompletion(existing)
            updateStreakOnUncomplete(habitId)
        } else {
            val completion = HabitCompletion(habitId = habitId, date = date)
            habitDao.insertCompletion(completion)
            updateStreakOnComplete(habitId, date)
        }
    }

    override suspend fun isHabitCompletedOnDate(habitId: Long, date: String): Boolean {
        return habitDao.getCompletion(habitId, date) != null
    }

    override fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>> {
        return habitDao.getCompletionsForHabit(habitId)
    }

    override suspend fun getCompletionsInRange(
        habitId: Long,
        startDate: String,
        endDate: String
    ): List<HabitCompletion> {
        return habitDao.getCompletionsInRange(habitId, startDate, endDate)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun updateStreakOnComplete(habitId: Long, dateStr: String) {
        val habit = habitDao.getHabitById(habitId).first() ?: return
        val completions = habitDao.getCompletionsForHabit(habitId).first()
            .sortedByDescending { it.date }

        val currentStreak = calculateCurrentStreakOptimized(habitId, dateStr)
        val longestStreak = maxOf(habit.longestStreak, currentStreak)

        habitDao.updateHabit(
            habit.copy(
                currentStreak = currentStreak,
                longestStreak = longestStreak,
                totalCompletions = habit.totalCompletions + 1
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun updateStreakOnUncomplete(habitId: Long) {
        val habit = habitDao.getHabitById(habitId).first() ?: return
        val completions = habitDao.getCompletionsForHabit(habitId).first()
            .sortedByDescending { it.date }

        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val currentStreak = calculateCurrentStreakOptimized(habitId, today)

        habitDao.updateHabit(
            habit.copy(
                currentStreak = currentStreak,
                totalCompletions = maxOf(0, habit.totalCompletions - 1)
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun calculateCurrentStreakOptimized(
        habitId: Long,
        fromDate: String
    ): Int {

        var streak = 0
        var currentDate = LocalDate.parse(fromDate)
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        while (true) {
            val entry = habitDao.getCompletion(
                habitId,
                currentDate.format(formatter)
            )

            if (entry != null) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }
}