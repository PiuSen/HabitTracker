package com.peu.habittracker.util

data class HabitAnalytics(
    val habitId: Long,
    val habitName: String,
    val habitIcon: String,
    val completionRate: Float, // 0.0 - 1.0
    val completionRatePercentage: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCompletions: Int,
    val bestDayOfWeek: String,
    val worstDayOfWeek: String,
    val last7DaysProgress: List<Boolean>,
    val last30DaysCompletions: Int,
    val trend: Trend
)

enum class Trend {
    IMPROVING, STABLE, DECLINING
}

data class OverallStatistics(
    val totalHabits: Int,
    val activeHabits: Int,
    val totalCompletions: Int,
    val overallCompletionRate: Float,
    val averageStreak: Float,
    val longestStreak: Int,
    val currentActiveStreaks: Int,
    val thisWeekCompletions: Int,
    val lastWeekCompletions: Int,
    val thisMonthCompletions: Int
)
