package com.peu.habittracker.module

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.peu.habittracker.db.AchievementDao
import com.peu.habittracker.db.DEFAULT_ACHIEVEMENTS
import com.peu.habittracker.db.DEFAULT_CATEGORIES
import com.peu.habittracker.db.HabitDao
import com.peu.habittracker.db.HabitDatabase
import com.peu.habittracker.db.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.peu.habittracker.util.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHabitDatabase(@ApplicationContext context: Context): HabitDatabase {
        return Room.databaseBuilder(
            context,
            HabitDatabase::class.java,
            "habit_database"
        )
            .addMigrations(MIGRATION_1_2)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Insert default categories and achievements
                    CoroutineScope(Dispatchers.IO).launch {
                        val database = provideHabitDatabase(context)
                        database.habitDao().insertCategories(DEFAULT_CATEGORIES)
                        database.achievementDao().insertAchievements(DEFAULT_ACHIEVEMENTS)
                    }
                }
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideHabitDao(database: HabitDatabase): HabitDao {
        return database.habitDao()
    }

    @Provides
    @Singleton
    fun provideAchievementDao(database: HabitDatabase): AchievementDao {
        return database.achievementDao()
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(
        @ApplicationContext context: Context
    ): SettingsDataStore {
        return SettingsDataStore(context)
    }
}
//@Module
//@InstallIn(SingletonComponent::class)
//object DatabaseModule {
//
//    @Provides
//    @Singleton
//    fun provideHabitDatabase(@ApplicationContext context: Context): HabitDatabase {
//        return Room.databaseBuilder(
//            context,
//            HabitDatabase::class.java,
//            "habit_database"
//        )
//            .fallbackToDestructiveMigration()
//            .build()
//    }
//
//    @Provides
//    @Singleton
//    fun provideHabitDao(database: HabitDatabase): HabitDao {
//        return database.habitDao()
//    }
//
//    @Provides
//    @Singleton
//    fun provideSettingsDataStore(
//        @ApplicationContext context: Context
//    ): SettingsDataStore {
//        return SettingsDataStore(context)
//    }
//}