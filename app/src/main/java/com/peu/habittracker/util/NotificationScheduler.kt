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
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.peu.habittracker.db.Habit
import com.peu.habittracker.worker.ReminderWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val UNIQUE_WORK_PREFIX = "habit_reminder_"

    fun scheduleNotification(
        context: Context,
        habit: Habit,
        hour: Int,
        minute: Int
    ) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        val delay = target.timeInMillis - now.timeInMillis

        val work = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "habitId" to habit.id,
                    "habitName" to habit.name,
                    "habitIcon" to habit.icon,
                    "hour" to hour,
                    "minute" to minute
                )
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "habit_${habit.id}",
                ExistingWorkPolicy.REPLACE,
                work
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




