package com.peu.habittracker.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.peu.habittracker.db.Achievement
import com.peu.habittracker.db.AchievementDao
import com.peu.habittracker.db.Habit
import com.peu.habittracker.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    application: Application,
    private val achievementDao: AchievementDao,
    private val repository: HabitRepository
) : AndroidViewModel(application) {

    val achievements: StateFlow<List<Achievement>> = achievementDao.getAllAchievements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unlockedCount: StateFlow<Int> = achievements.map { list ->
        list.count { it.isUnlocked }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun checkAchievements(habit: Habit) {
        viewModelScope.launch {
            val allAchievements = achievementDao.getAllAchievements().first()

            allAchievements.forEach { achievement ->
                if (!achievement.isUnlocked) {
                    val shouldUnlock = when (achievement.type) {
                        "total_completions" -> habit.totalCompletions >= achievement.requirement
                        "streak" -> habit.currentStreak >= achievement.requirement
                        else -> false
                    }

                    if (shouldUnlock) {
                        unlockAchievement(achievement)
                    }
                }
            }
        }
    }

    private suspend fun unlockAchievement(achievement: Achievement) {
        val updated = achievement.copy(
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis()
        )
        achievementDao.updateAchievement(updated)

        // Show achievement unlock notification/animation
        showAchievementUnlockNotification(achievement)
    }

    private fun showAchievementUnlockNotification(achievement: Achievement) {
        // TODO: Implement notification or in-app animation
    }
}
