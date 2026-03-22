package com.peu.habittracker.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        private val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFICATIONS_ENABLED] ?: true
        }

    val notificationTime: Flow<Pair<Int, Int>> = context.dataStore.data
        .map { preferences ->
            Pair(
                preferences[NOTIFICATION_HOUR] ?: 9,
                preferences[NOTIFICATION_MINUTE] ?: 0
            )
        }

    val darkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE] ?: false
        }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setNotificationTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_HOUR] = hour
            preferences[NOTIFICATION_MINUTE] = minute
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE] = enabled
        }
    }
}