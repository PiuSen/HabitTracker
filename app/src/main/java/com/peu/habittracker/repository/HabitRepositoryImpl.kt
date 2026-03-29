package com.peu.habittracker.repository
import android.os.Build
import androidx.annotation.RequiresApi
import com.peu.habittracker.db.DailyStat
import com.peu.habittracker.db.Habit
import com.peu.habittracker.db.HabitCompletion
import com.peu.habittracker.db.HabitDao

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao
) : HabitRepository {

    override fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    override fun getHabitById(habitId: Long): Flow<Habit?> =
        habitDao.getHabitById(habitId)

    override suspend fun insertHabit(habit: Habit): Long =
        habitDao.insertHabit(habit)

    override suspend fun updateHabit(habit: Habit) =
        habitDao.updateHabit(habit)

    override suspend fun deleteHabit(habit: Habit) =
        habitDao.deleteHabit(habit)

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun toggleHabitCompletion(habitId: Long, date: String) {

        val isCompleted = habitDao.isCompleted(habitId, date)

        if (isCompleted) {
            val existing = habitDao.getCompletion(habitId, date) ?: return
            habitDao.deleteCompletion(existing)
            updateAfterUncomplete(habitId, date)
        } else {
            habitDao.insertCompletion(
                HabitCompletion(habitId = habitId, date = date)
            )
            updateAfterComplete(habitId, date)
        }
    }

    override suspend fun isHabitCompletedOnDate(
        habitId: Long,
        date: String
    ): Boolean = habitDao.isCompleted(habitId, date)

    override fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>> =
        habitDao.getCompletionsForHabit(habitId)

    override suspend fun getCompletionsInRange(
        habitId: Long,
        startDate: String,
        endDate: String
    ): List<HabitCompletion> =
        habitDao.getCompletionsInRange(habitId, startDate, endDate)

    // -------------------------------
    // 🔥 STREAK LOGIC (OPTIMIZED)
    // -------------------------------

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun updateAfterComplete(habitId: Long, date: String) {
        val habit = habitDao.getHabitByIdOnce(habitId) ?: return

        val streak = calculateStreak(habitId, date)

        habitDao.updateHabit(
            habit.copy(
                currentStreak = streak,
                longestStreak = maxOf(habit.longestStreak, streak),
                totalCompletions = habit.totalCompletions + 1
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun updateAfterUncomplete(habitId: Long, date: String) {
        val habit = habitDao.getHabitByIdOnce(habitId) ?: return

        val streak = calculateStreak(habitId, date)

        habitDao.updateHabit(
            habit.copy(
                currentStreak = streak,
                totalCompletions = maxOf(0, habit.totalCompletions - 1)
            )
        )
    }

    // -------------------------------
    // ⚡ OPTIMIZED STREAK CALCULATION
    // -------------------------------

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun calculateStreak(
        habitId: Long,
        fromDate: String
    ): Int {

        val completions = habitDao
            .getCompletionsBeforeDate(habitId, fromDate)

        if (completions.isEmpty()) return 0

        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        var streak = 0
        var expectedDate = LocalDate.parse(fromDate)

        for (completion in completions) {
            val completionDate = LocalDate.parse(completion.date)

            if (completionDate == expectedDate) {
                streak++
                expectedDate = expectedDate.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }

    override suspend fun getDailyStats(): List<DailyStat> {
        return habitDao.getDailyCompletionStats()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getWeeklyStats(): List<Int> {

        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        // Total habits (avoid divide by zero)
        val totalHabits = habitDao.getAllHabitsOnce().size.coerceAtLeast(1)

        val result = mutableListOf<Int>()

        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong()).format(formatter)

            val completedCount = habitDao.getCompletionCountByDate(date)

            // Convert to percentage (0–100)
            val percentage = (completedCount * 100) / totalHabits

            result.add(percentage)
        }

        return result
    }

}