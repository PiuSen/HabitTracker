package com.peu.habittracker.util
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.peu.habittracker.db.Habit
import com.peu.habittracker.worker.ReminderWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val UNIQUE_WORK_PREFIX = "habit_reminder_"

    fun scheduleNotification(
        context: Context,
        habit: Habit,
        hour: Int,
        minute: Int
    ) {
        val currentTime = java.util.Calendar.getInstance()
        val scheduledTime = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
        }

        // If scheduled time is in the past, schedule for tomorrow
        if (scheduledTime.before(currentTime)) {
            scheduledTime.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = scheduledTime.timeInMillis - currentTime.timeInMillis

        val inputData = workDataOf(
            "habitName" to habit.name,
            "habitIcon" to habit.icon,
            "habitId" to habit.id
        )

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "$UNIQUE_WORK_PREFIX${habit.id}",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Cancel notification for a specific habit
     */
    fun cancelNotification(context: Context, habitId: Long) {
        WorkManager.getInstance(context)
            .cancelUniqueWork("$UNIQUE_WORK_PREFIX$habitId")
    }

    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
    }
}




