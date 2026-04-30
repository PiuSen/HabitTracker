package com.peu.habittracker.util

import android.os.Build
import androidx.annotation.RequiresApi
import com.peu.habittracker.db.Habit
import com.peu.habittracker.db.HabitCompletion
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
class AnalyticsCalculator {

    fun calculateHabitAnalytics(
        habit: Habit,
        completions: List<HabitCompletion>
    ): HabitAnalytics {
        val last30Days = getLast30DaysCompletions(completions)
        val last7Days = getLast7DaysCompletions(completions)

        val completionRate = if (last30Days.isNotEmpty()) {
            last30Days.size / 30f
        } else 0f

        val (bestDay, worstDay) = calculateBestWorstDays(completions)
        val trend = calculateTrend(completions)

        return HabitAnalytics(
            habitId = habit.id,
            habitName = habit.name,
            habitIcon = habit.icon,
            completionRate = completionRate,
            completionRatePercentage = (completionRate * 100).toInt(),
            currentStreak = habit.currentStreak,
            longestStreak = habit.longestStreak,
            totalCompletions = habit.totalCompletions,
            bestDayOfWeek = bestDay,
            worstDayOfWeek = worstDay,
            last7DaysProgress = last7Days,
            last30DaysCompletions = last30Days.size,
            trend = trend
        )
    }

    private fun getLast30DaysCompletions(completions: List<HabitCompletion>): List<HabitCompletion> {
        val thirtyDaysAgo = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE)
        return completions.filter { it.date >= thirtyDaysAgo }
    }

    private fun getLast7DaysCompletions(completions: List<HabitCompletion>): List<Boolean> {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val completionDates = completions.map { it.date }.toSet()

        return (0..6).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong()).format(formatter)
            completionDates.contains(date)
        }.reversed()
    }

    private fun calculateBestWorstDays(completions: List<HabitCompletion>): Pair<String, String> {
        if (completions.isEmpty()) return "N/A" to "N/A"

        val byDayOfWeek = completions.groupBy { completion ->
            LocalDate.parse(completion.date).dayOfWeek
        }

        val bestDay = byDayOfWeek.maxByOrNull { it.value.size }?.key?.name ?: "N/A"
        val worstDay = byDayOfWeek.minByOrNull { it.value.size }?.key?.name ?: "N/A"

        return bestDay to worstDay
    }

    private fun calculateTrend(completions: List<HabitCompletion>): Trend {
        if (completions.size < 14) return Trend.STABLE

        val last7Days = completions.takeLast(7).size
        val previous7Days = completions.drop(completions.size - 14).take(7).size

        return when {
            last7Days > previous7Days + 1 -> Trend.IMPROVING
            last7Days < previous7Days - 1 -> Trend.DECLINING
            else -> Trend.STABLE
        }
    }

    fun calculateOverallStatistics(
        habits: List<Habit>,
        allCompletions: List<HabitCompletion>
    ): OverallStatistics {
        val activeHabits = habits.filter { it.currentStreak > 0 }.size
        val totalCompletions = habits.sumOf { it.totalCompletions }
        val averageStreak = if (habits.isNotEmpty()) {
            habits.map { it.currentStreak }.average().toFloat()
        } else 0f

        val longestStreak = habits.maxOfOrNull { it.longestStreak } ?: 0
        val activeStreaks = habits.count { it.currentStreak > 0 }

        val today = LocalDate.now()
        val weekAgo = today.minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val twoWeeksAgo = today.minusDays(14).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val monthAgo = today.minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE)

        val thisWeek = allCompletions.count { it.date >= weekAgo }
        val lastWeek = allCompletions.count { it.date >= twoWeeksAgo && it.date < weekAgo }
        val thisMonth = allCompletions.count { it.date >= monthAgo }

        val overallRate = if (habits.isNotEmpty() && thisMonth > 0) {
            thisMonth.toFloat() / (habits.size * 30)
        } else 0f

        return OverallStatistics(
            totalHabits = habits.size,
            activeHabits = activeHabits,
            totalCompletions = totalCompletions,
            overallCompletionRate = overallRate,
            averageStreak = averageStreak,
            longestStreak = longestStreak,
            currentActiveStreaks = activeStreaks,
            thisWeekCompletions = thisWeek,
            lastWeekCompletions = lastWeek,
            thisMonthCompletions = thisMonth
        )
    }
}
