package com.peu.habittracker.viewModel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peu.habittracker.db.HabitWithCompletions
import com.peu.habittracker.repository.HabitRepository
import com.peu.habittracker.screen.ExportFormat
import com.peu.habittracker.screen.ExportRange
import com.peu.habittracker.screen.ExportStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel()
{

    // ── Export stats (shown in summary card) ─────────────────────────────────
    private val _exportStats = MutableStateFlow(ExportStats())
    val exportStats: StateFlow<ExportStats> = _exportStats.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllHabitsWithCompletions().collect { habitsWithCompletions ->
                val totalHabits      = habitsWithCompletions.size
                val totalCompletions = habitsWithCompletions.sumOf { it.completions.count { c -> c.completed } }
                val totalStreakDays  = habitsWithCompletions.sumOf { it.habit.currentStreak }
                val earliest = habitsWithCompletions
                    .flatMap { it.completions }
                    .filter { it.completed }
                    .minOfOrNull { it.date }
                    ?.let {
                        try {
                            val d = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)
                            SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(d!!)
                        } catch (e: Exception) { "—" }
                    } ?: "—"

                _exportStats.value = ExportStats(
                    totalHabits      = totalHabits,
                    totalCompletions = totalCompletions,
                    totalStreakDays  = totalStreakDays,
                    earliestDate     = earliest
                )
            }
        }
    }

    // ── Main export function ─────────────────────────────────────────────────
    suspend fun exportData(
        context: Context,
        format: ExportFormat,
        range: ExportRange,
        includeNotes: Boolean,
        includeStreaks: Boolean
    ): Uri? {
        return try {
            val habitsWithCompletions = repository.getAllHabitsWithCompletionsOnce()
            val cutoffDate = range.days?.let { days ->
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -days)
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            }

            val content = when (format) {
                ExportFormat.CSV  -> buildCsv(habitsWithCompletions, cutoffDate, includeNotes, includeStreaks)
                ExportFormat.JSON -> buildJson(habitsWithCompletions, cutoffDate, includeNotes, includeStreaks)
                ExportFormat.TXT  -> buildTxt(habitsWithCompletions, cutoffDate, includeNotes, includeStreaks)
            }

            val dateStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName  = "habits_export_$dateStamp${format.extension}"
            val file      = File(context.cacheDir, fileName)
            file.writeText(content)

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ── CSV builder ──────────────────────────────────────────────────────────
    private fun buildCsv(
        data: List<HabitWithCompletions>,
        cutoff: String?,
        includeNotes: Boolean,
        includeStreaks: Boolean
    ): String = buildString {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        appendLine("# HabitTracker Export — $dateStr")

        // Header
        append("habit_name,habit_icon,date,completed,day_of_week")
        if (includeStreaks) append(",current_streak,best_streak,total_completions")
        if (includeNotes)   append(",note")
        appendLine()

        data.forEach { hwc ->
            val habit       = hwc.habit
            val completions = hwc.completions
                .filter { cutoff == null || it.date >= cutoff }
                .sortedByDescending { it.date }

            completions.forEach { c ->
                val dayOfWeek = try {
                    val d = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(c.date)
                    SimpleDateFormat("EEEE", Locale.getDefault()).format(d!!)
                } catch (e: Exception) { "" }

                append("\"${habit.name.replace("\"", "\"\"")}\",${habit.icon},${c.date},${c.completed},$dayOfWeek")
                if (includeStreaks) append(",${habit.currentStreak},${habit.longestStreak},${habit.totalCompletions}")
                if (includeNotes)   append(",\"${c.note.replace("\"", "\"\"")}\"")
                appendLine()
            }
        }
    }

    // ── JSON builder ─────────────────────────────────────────────────────────
    private fun buildJson(
        data: List<HabitWithCompletions>,
        cutoff: String?,
        includeNotes: Boolean,
        includeStreaks: Boolean
    ): String = buildString {
        val dateStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
        appendLine("{")
        appendLine("  \"export\": {")
        appendLine("    \"generated_at\": \"$dateStr\",")
        appendLine("    \"app\": \"HabitTracker\",")
        appendLine("    \"total_habits\": ${data.size},")
        appendLine("    \"habits\": [")

        data.forEachIndexed { hi, hwc ->
            val habit       = hwc.habit
            val completions = hwc.completions
                .filter { cutoff == null || it.date >= cutoff }
                .sortedByDescending { it.date }

            appendLine("      {")
            appendLine("        \"id\": ${habit.id},")
            appendLine("        \"name\": \"${habit.name.replace("\"", "\\\"")}\",")
            appendLine("        \"icon\": \"${habit.icon}\",")
            appendLine("        \"category_id\": ${habit.categoryId ?: "null"},")
            if (includeStreaks) {
                appendLine("        \"current_streak\": ${habit.currentStreak},")
                appendLine("        \"best_streak\": ${habit.longestStreak},")
                appendLine("        \"total_completions\": ${habit.totalCompletions},")
            }
            appendLine("        \"completions\": [")
            completions.forEachIndexed { ci, c ->
                append("          { \"date\": \"${c.date}\", \"completed\": ${c.completed}")
                if (includeNotes && c.note.isNotBlank()) append(", \"note\": \"${c.note.replace("\"", "\\\"")}\"")
                append(" }")
                if (ci < completions.lastIndex) appendLine(",") else appendLine()
            }
            append("        ]")
            appendLine()
            append("      }")
            if (hi < data.lastIndex) appendLine(",") else appendLine()
        }

        appendLine("    ]")
        appendLine("  }")
        append("}")
    }

    // ── TXT builder ──────────────────────────────────────────────────────────
    private fun buildTxt(
        data: List<HabitWithCompletions>,
        cutoff: String?,
        includeNotes: Boolean,
        includeStreaks: Boolean
    ): String = buildString {
        val dateStr = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault()).format(Date())
        appendLine("╔══════════════════════════════════╗")
        appendLine("║     HABITTRACKER EXPORT          ║")
        appendLine("╚══════════════════════════════════╝")
        appendLine("Generated : $dateStr")
        appendLine("Habits    : ${data.size}")
        val total = data.sumOf { it.completions.filter { c -> cutoff == null || c.date >= cutoff }.count { c -> c.completed } }
        appendLine("Completions: $total")
        appendLine()

        data.forEach { hwc ->
            val habit       = hwc.habit
            val completions = hwc.completions
                .filter { cutoff == null || it.date >= cutoff }
                .sortedByDescending { it.date }

            appendLine("┌─────────────────────────────────")
            appendLine("│ ${habit.icon}  ${habit.name.uppercase()}")
            if (includeStreaks) {
                appendLine("│ 🔥 Streak: ${habit.currentStreak} days  ⭐ Best: ${habit.longestStreak} days")
                appendLine("│ ✅ Total done: ${habit.totalCompletions}")
            }
            appendLine("├─────────────────────────────────")
            completions.take(10).forEach { c ->
                val status = if (c.completed) "✓" else "✗"
                append("│  $status  ${c.date}")
                if (includeNotes && c.note.isNotBlank()) append("  — ${c.note}")
                appendLine()
            }
            if (completions.size > 10) appendLine("│  ... and ${completions.size - 10} more entries")
            appendLine("└─────────────────────────────────")
            appendLine()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helper data model  (add to your db package or inline here)
// ─────────────────────────────────────────────────────────────────────────────
//data class HabitWithCompletions(
//    val habit: com.peu.habittracker.db.Habit,
//    val completions: List<com.peu.habittracker.db.HabitCompletion>
//)