package com.peu.habittracker.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun getHabitById(habitId: Long): Flow<Habit?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun getCompletion(habitId: Long, date: String): HabitCompletion?

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion)

    @Delete
    suspend fun deleteCompletion(completion: HabitCompletion)

    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitByIdOnce(habitId: Long): Habit?

    @Query("""
SELECT EXISTS(
SELECT 1 FROM habit_completions 
WHERE habitId = :habitId AND date = :date
)
""")
    suspend fun isCompleted(habitId: Long, date: String): Boolean


    @Query("""
SELECT * FROM habit_completions 
WHERE habitId = :habitId 
AND date <= :date 
ORDER BY date DESC
""")
    suspend fun getCompletionsBeforeDate(
        habitId: Long,
        date: String
    ): List<HabitCompletion>
    @Query("""
SELECT date, COUNT(*) as count 
FROM habit_completions
GROUP BY date
ORDER BY date ASC
""")
    suspend fun getDailyCompletionStats(): List<DailyStat>
    @Query("""
SELECT COUNT(*) FROM habit_completions 
WHERE date = :date
""")
    suspend fun getCompletionCountByDate(date: String): Int

    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsOnce(): List<Habit>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date >= :startDate AND date <= :endDate")
    suspend fun getCompletionsInRange(habitId: Long, startDate: String, endDate: String): List<HabitCompletion>
}


data class DailyStat(
    val date: String,
    val count: Int
)