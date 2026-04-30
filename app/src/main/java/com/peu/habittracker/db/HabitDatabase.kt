package com.peu.habittracker.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


//@Database(
//    entities = [Habit::class, HabitCompletion::class],
//    version = 1,
//    exportSchema = false
//)
//abstract class HabitDatabase : RoomDatabase() {
//    abstract fun habitDao(): HabitDao
//}


@Database(
entities = [
Habit::class,
HabitCompletion::class,
Category::class,
Achievement::class
],
version = 2,
exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun achievementDao(): AchievementDao
}

// Migration from version 1 to 2
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create categories table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                icon TEXT NOT NULL,
                color INTEGER NOT NULL,
                `order` INTEGER NOT NULL
            )
        """.trimIndent())

        // Add categoryId to habits table
        database.execSQL("""
            ALTER TABLE habits ADD COLUMN categoryId INTEGER
        """.trimIndent())

        // Create achievements table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS achievements (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                icon TEXT NOT NULL,
                requirement INTEGER NOT NULL,
                type TEXT NOT NULL,
                unlockedAt INTEGER,
                isUnlocked INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }
}