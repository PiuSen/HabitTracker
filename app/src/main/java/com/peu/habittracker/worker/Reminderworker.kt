// ============================================================================
// PART 1: ENHANCED REMINDER WORKER
// ============================================================================

package com.peu.habittracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.peu.habittracker.MainActivity
import com.peu.habittracker.R
import com.peu.habittracker.db.Habit
import com.peu.habittracker.util.NotificationScheduler

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params)
{

    override suspend fun doWork(): Result {

        // 🔔 Show notification here

        val habitId = inputData.getLong("habitId", -1)
        val habitName = inputData.getString("habitName") ?: ""
        val habitIcon = inputData.getString("habitIcon") ?: ""
        val hour = inputData.getInt("hour", 8)
        val minute = inputData.getInt("minute", 0)

        // ✅ RESCHEDULE NEXT DAY
        NotificationScheduler.scheduleNotification(
            applicationContext,
            Habit(id = habitId, name = habitName, color = 0, icon = habitIcon),
            hour,
            minute
        )

        return Result.success()
    }

    private fun showNotification(habitName: String, habitIcon: String, habitId: Long) {
        val channelId = "habit_reminder"
        val channelName = "Habit Reminders"

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders for your habits"
                enableLights(true)
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        // Intent to open app when notification is tapped
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("habitId", habitId)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("$habitIcon Time for $habitName!")
            .setContentText("Don't break your streak! Complete your habit now.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        manager.notify(habitId.toInt(), notification)
    }
}

