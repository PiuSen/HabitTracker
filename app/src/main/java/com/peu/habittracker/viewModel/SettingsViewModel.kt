package com.peu.habittracker.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.peu.habittracker.repository.HabitRepository
import com.peu.habittracker.util.NotificationScheduler
import com.peu.habittracker.util.SettingsDataStore

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val repository: HabitRepository,
    private val settingsDataStore: SettingsDataStore // Clean constructor injection
) : AndroidViewModel(application) {

    val notificationsEnabled: StateFlow<Boolean> = settingsDataStore.notificationsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val notificationTime: StateFlow<Pair<Int, Int>> = settingsDataStore.notificationTime
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Pair(9, 0)
        )

    val darkMode: StateFlow<Boolean> = settingsDataStore.darkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setNotificationsEnabled(enabled)
            if (enabled) {
                rescheduleAllHabits()
            } else {
                NotificationScheduler.cancelAllNotifications(getApplication())
            }
        }
    }

    fun updateNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsDataStore.setNotificationTime(hour, minute)
            if (notificationsEnabled.value) {
                rescheduleAllHabits()
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDarkMode(enabled)
        }
    }

    private suspend fun rescheduleAllHabits() {
        val (hour, minute) = notificationTime.value
        repository.getAllHabits().first().forEach { habit ->
            NotificationScheduler.scheduleNotification(
                getApplication(),
                habit,
                hour,
                minute
            )
        }
    }
}