package com.peu.habittracker.viewModel

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peu.habittracker.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
// SETTINGS DATA MODEL
// ─────────────────────────────────────────────────────────────────────────────
data class AppSettings(
    // Notifications
    val notificationsEnabled : Boolean = true,
    val notificationHour     : Int     = 9,
    val notificationMinute   : Int     = 0,
    val notificationSound    : Boolean = true,
    val notificationVibrate  : Boolean = true,
    // Appearance
    val darkMode             : Boolean = true,
    val accentColorIndex     : Int     = 0,   // 0=Violet, 1=Cyan, 2=Pink, 3=Green, 4=Amber
    val compactMode          : Boolean = false,
    val showStreakAnimation  : Boolean = true,
    // Habit Defaults
    val defaultReminderEnabled: Boolean = false,
    val weekStartMonday      : Boolean  = true,
    val showCompletionConfetti: Boolean = true,
    // Privacy & Data
    val analyticsEnabled     : Boolean = false,
    val crashReportingEnabled: Boolean = true,
    val autoBackupEnabled    : Boolean = false,
    // App info
    val appVersion           : String  = "1.0.0",
    val buildNumber          : Int     = 1
)

enum class AccentColor(val label: String, val hex: Long) {
    VIOLET("Violet × Cyan",  0xFF7C6FFF),
    CYAN  ("Cyan × Blue",    0xFF00D4FF),
    PINK  ("Pink × Violet",  0xFFFF6FD8),
    GREEN ("Green × Teal",   0xFF00F5A0),
    AMBER ("Amber × Orange", 0xFFFFB800)
}

// ─────────────────────────────────────────────────────────────────────────────
// VIEWMODEL
// ─────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: HabitRepository
) : ViewModel() {

    companion object {
        private const val PREFS_NAME          = "habit_tracker_settings"
        private const val KEY_NOTIF_ENABLED   = "notif_enabled"
        private const val KEY_NOTIF_HOUR      = "notif_hour"
        private const val KEY_NOTIF_MINUTE    = "notif_minute"
        private const val KEY_NOTIF_SOUND     = "notif_sound"
        private const val KEY_NOTIF_VIBRATE   = "notif_vibrate"
        private const val KEY_DARK_MODE       = "dark_mode"
        private const val KEY_ACCENT_COLOR    = "accent_color"
        private const val KEY_COMPACT_MODE    = "compact_mode"
        private const val KEY_STREAK_ANIM     = "streak_animation"
        private const val KEY_DEFAULT_REMINDER= "default_reminder"
        private const val KEY_WEEK_START_MON  = "week_start_monday"
        private const val KEY_CONFETTI        = "completion_confetti"
        private const val KEY_ANALYTICS       = "analytics_enabled"
        private const val KEY_CRASH_REPORTING = "crash_reporting"
        private const val KEY_AUTO_BACKUP     = "auto_backup"
        private const val NOTIF_CHANNEL_ID    = "habit_reminders"
        private const val NOTIF_REQUEST_CODE  = 1001
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Internal state ───────────────────────────────────────────────────────
    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    // ── Convenience flat flows (used by UI) ──────────────────────────────────
    val notificationsEnabled : StateFlow<Boolean>     = _settings.map { it.notificationsEnabled }.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val notificationTime: StateFlow<Pair<Int, Int>> =
        _settings
            .map { it.notificationHour to it.notificationMinute }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                Pair(9, 0)
            )
    val notificationSound    : StateFlow<Boolean>     = _settings.map { it.notificationSound }.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val notificationVibrate  : StateFlow<Boolean>     = _settings.map { it.notificationVibrate }.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val darkMode             : StateFlow<Boolean>     = _settings.map { it.darkMode }.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val accentColorIndex     : StateFlow<Int>         = _settings.map { it.accentColorIndex }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val compactMode          : StateFlow<Boolean>     = _settings.map { it.compactMode }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val showStreakAnimation  : StateFlow<Boolean>     = _settings.map { it.showStreakAnimation }.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val weekStartMonday      : StateFlow<Boolean>     = _settings.map { it.weekStartMonday }.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val showConfetti         : StateFlow<Boolean>     = _settings.map { it.showCompletionConfetti }.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val analyticsEnabled     : StateFlow<Boolean>     = _settings.map { it.analyticsEnabled }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val crashReporting       : StateFlow<Boolean>     = _settings.map { it.crashReportingEnabled }.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val autoBackup           : StateFlow<Boolean>     = _settings.map { it.autoBackupEnabled }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // ── Notification toggles ─────────────────────────────────────────────────
    fun toggleNotifications(enabled: Boolean) {
        update { it.copy(notificationsEnabled = enabled) }
        prefs.edit().putBoolean(KEY_NOTIF_ENABLED, enabled).apply()
        if (enabled) scheduleNotification() else cancelNotification()
    }

    fun updateNotificationTime(hour: Int, minute: Int) {
        update { it.copy(notificationHour = hour, notificationMinute = minute) }
        prefs.edit().putInt(KEY_NOTIF_HOUR, hour).putInt(KEY_NOTIF_MINUTE, minute).apply()
        if (_settings.value.notificationsEnabled) scheduleNotification()
    }

    fun toggleNotificationSound(enabled: Boolean) {
        update { it.copy(notificationSound = enabled) }
        prefs.edit().putBoolean(KEY_NOTIF_SOUND, enabled).apply()
    }

    fun toggleNotificationVibrate(enabled: Boolean) {
        update { it.copy(notificationVibrate = enabled) }
        prefs.edit().putBoolean(KEY_NOTIF_VIBRATE, enabled).apply()
    }

    // ── Appearance ───────────────────────────────────────────────────────────
    fun toggleDarkMode(enabled: Boolean) {
        update { it.copy(darkMode = enabled) }
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun setAccentColor(index: Int) {
        update { it.copy(accentColorIndex = index.coerceIn(0, AccentColor.entries.size - 1)) }
        prefs.edit().putInt(KEY_ACCENT_COLOR, index).apply()
    }

    fun toggleCompactMode(enabled: Boolean) {
        update { it.copy(compactMode = enabled) }
        prefs.edit().putBoolean(KEY_COMPACT_MODE, enabled).apply()
    }

    fun toggleStreakAnimation(enabled: Boolean) {
        update { it.copy(showStreakAnimation = enabled) }
        prefs.edit().putBoolean(KEY_STREAK_ANIM, enabled).apply()
    }

    // ── Habit defaults ───────────────────────────────────────────────────────
    fun toggleWeekStartMonday(enabled: Boolean) {
        update { it.copy(weekStartMonday = enabled) }
        prefs.edit().putBoolean(KEY_WEEK_START_MON, enabled).apply()
    }

    fun toggleConfetti(enabled: Boolean) {
        update { it.copy(showCompletionConfetti = enabled) }
        prefs.edit().putBoolean(KEY_CONFETTI, enabled).apply()
    }

    // ── Privacy & Data ───────────────────────────────────────────────────────
    fun toggleAnalytics(enabled: Boolean) {
        update { it.copy(analyticsEnabled = enabled) }
        prefs.edit().putBoolean(KEY_ANALYTICS, enabled).apply()
    }

    fun toggleCrashReporting(enabled: Boolean) {
        update { it.copy(crashReportingEnabled = enabled) }
        prefs.edit().putBoolean(KEY_CRASH_REPORTING, enabled).apply()
    }

    fun toggleAutoBackup(enabled: Boolean) {
        update { it.copy(autoBackupEnabled = enabled) }
        prefs.edit().putBoolean(KEY_AUTO_BACKUP, enabled).apply()
    }

    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                //repository.deleteAllData()
                onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun openPlayStoreListing() {
        try {
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun sendFeedbackEmail() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data    = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL,   arrayOf("feedback@habittracker.app"))
                putExtra(Intent.EXTRA_SUBJECT, "HabitTracker v${_settings.value.appVersion} Feedback")
            }
            context.startActivity(Intent.createChooser(intent, "Send Feedback")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun shareApp() {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT,
                    "I'm building better habits with HabitTracker! 🎯\nhttps://play.google.com/store/apps/details?id=${context.packageName}")
            }
            context.startActivity(Intent.createChooser(intent, "Share HabitTracker")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) { e.printStackTrace() }
    }

    // ── Notification scheduling ──────────────────────────────────────────────
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIF_CHANNEL_ID,
            "Habit Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description            = "Daily reminders to complete your habits"
            enableVibration(_settings.value.notificationVibrate)
            enableLights(true)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun scheduleNotification() {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent       = Intent(context, HabitReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, NOTIF_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, _settings.value.notificationHour)
                set(Calendar.MINUTE,      _settings.value.notificationMinute)
                set(Calendar.SECOND,      0)
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun cancelNotification() {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent       = Intent(context, HabitReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, NOTIF_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) { e.printStackTrace() }
    }

    // ── Internal helpers ─────────────────────────────────────────────────────
    private fun update(block: (AppSettings) -> AppSettings) {
        _settings.value = block(_settings.value)
    }

    private fun loadSettings() = AppSettings(
        notificationsEnabled  = prefs.getBoolean(KEY_NOTIF_ENABLED,    true),
        notificationHour      = prefs.getInt    (KEY_NOTIF_HOUR,        9),
        notificationMinute    = prefs.getInt    (KEY_NOTIF_MINUTE,      0),
        notificationSound     = prefs.getBoolean(KEY_NOTIF_SOUND,      true),
        notificationVibrate   = prefs.getBoolean(KEY_NOTIF_VIBRATE,    true),
        darkMode              = prefs.getBoolean(KEY_DARK_MODE,         true),
        accentColorIndex      = prefs.getInt    (KEY_ACCENT_COLOR,      0),
        compactMode           = prefs.getBoolean(KEY_COMPACT_MODE,      false),
        showStreakAnimation   = prefs.getBoolean(KEY_STREAK_ANIM,       true),
        weekStartMonday       = prefs.getBoolean(KEY_WEEK_START_MON,    true),
        showCompletionConfetti= prefs.getBoolean(KEY_CONFETTI,          true),
        analyticsEnabled      = prefs.getBoolean(KEY_ANALYTICS,         false),
        crashReportingEnabled = prefs.getBoolean(KEY_CRASH_REPORTING,   true),
        autoBackupEnabled     = prefs.getBoolean(KEY_AUTO_BACKUP,       false)
    )
}

// Placeholder — implement in your receivers package
class HabitReminderReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Build and show notification here
    }
}