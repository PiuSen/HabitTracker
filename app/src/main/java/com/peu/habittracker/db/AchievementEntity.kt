package com.peu.habittracker.db

import androidx.room.Entity
import androidx.room.PrimaryKey
//
//@Entity(tableName = "achievements")
//data class Achievement(
//    @PrimaryKey(autoGenerate = true)
//    val id: Long = 0,
//    val title: String,
//    val description: String,
//    val icon: String,
//    val requirement: Int,
//    val type: String, // "streak", "total_completions", "perfect_week"
//    val unlockedAt: Long? = null,
//    val isUnlocked: Boolean = false
//)
//
//val DEFAULT_ACHIEVEMENTS = listOf(
//    Achievement(
//        id = 1,
//        title = "First Step",
//        description = "Complete your first habit",
//        icon = "🌱",
//        requirement = 1,
//        type = "total_completions"
//    ),
//    Achievement(
//        id = 2,
//        title = "Hot Streak",
//        description = "Maintain a 3-day streak",
//        icon = "🔥",
//        requirement = 3,
//        type = "streak"
//    ),
//    Achievement(
//        id = 3,
//        title = "Week Warrior",
//        description = "Maintain a 7-day streak",
//        icon = "💪",
//        requirement = 7,
//        type = "streak"
//    ),
//    Achievement(
//        id = 4,
//        title = "Month Master",
//        description = "Maintain a 30-day streak",
//        icon = "⭐",
//        requirement = 30,
//        type = "streak"
//    ),
//    Achievement(
//        id = 5,
//        title = "Century Club",
//        description = "Complete 100 habits",
//        icon = "💯",
//        requirement = 100,
//        type = "total_completions"
//    ),
//    Achievement(
//        id = 6,
//        title = "Perfectionist",
//        description = "Complete all habits for 7 days",
//        icon = "💎",
//        requirement = 7,
//        type = "perfect_week"
//    ),
//    Achievement(
//        id = 7,
//        title = "Habit Hero",
//        description = "Maintain a 100-day streak",
//        icon = "🏆",
//        requirement = 100,
//        type = "streak"
//    )
//)