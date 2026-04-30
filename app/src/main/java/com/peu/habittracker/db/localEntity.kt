package com.peu.habittracker.db
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Relation

import kotlinx.coroutines.flow.Flow

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String,
    val color: Int,
    val order: Int = 0
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val color: Int,
    val icon: String = "🎯",
    val categoryId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCompletions: Int = 0
)

@Entity(
    tableName = "habit_completions",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId"), Index("date")]
)
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String,
    val completed: Boolean = true,
    val completedAt: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val icon: String,
    val requirement: Int,
    val type: String,
    val unlockedAt: Long? = null,
    val isUnlocked: Boolean = false
)

// Relation data classes
data class HabitWithCategory(
    @Embedded val habit: Habit,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category?
)

data class DailyStat(
    val date: String,
    val count: Int
)

// Default data
val DEFAULT_CATEGORIES = listOf(
    Category(id = 1, name = "Health", icon = "💪", color = 0xFF4CAF50.toInt(), order = 0),
    Category(id = 2, name = "Learning", icon = "📚", color = 0xFF2196F3.toInt(), order = 1),
    Category(id = 3, name = "Productivity", icon = "⚡", color = 0xFFFFC107.toInt(), order = 2),
    Category(id = 4, name = "Mindfulness", icon = "🧘", color = 0xFF9C27B0.toInt(), order = 3),
    Category(id = 5, name = "Finance", icon = "💰", color = 0xFF4CAF50.toInt(), order = 4),
    Category(id = 6, name = "Social", icon = "👥", color = 0xFFFF5722.toInt(), order = 5)
)

val DEFAULT_ACHIEVEMENTS = listOf(
    Achievement(id = 1, title = "First Step", description = "Complete your first habit",
        icon = "🌱", requirement = 1, type = "total_completions"),
    Achievement(id = 2, title = "Hot Streak", description = "Maintain a 3-day streak",
        icon = "🔥", requirement = 3, type = "streak"),
    Achievement(id = 3, title = "Week Warrior", description = "Maintain a 7-day streak",
        icon = "💪", requirement = 7, type = "streak"),
    Achievement(id = 4, title = "Month Master", description = "Maintain a 30-day streak",
        icon = "⭐", requirement = 30, type = "streak"),
    Achievement(id = 5, title = "Century Club", description = "Complete 100 habits",
        icon = "💯", requirement = 100, type = "total_completions"),
    Achievement(id = 6, title = "Perfectionist", description = "Complete all habits for 7 days",
        icon = "💎", requirement = 7, type = "perfect_week"),
    Achievement(id = 7, title = "Habit Hero", description = "Maintain a 100-day streak",
        icon = "🏆", requirement = 100, type = "streak")
)