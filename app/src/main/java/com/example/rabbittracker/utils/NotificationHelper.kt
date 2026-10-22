package com.example.rabbittracker.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.rabbittracker.MainActivity
import com.example.rabbittracker.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object NotificationHelper {
    private const val CHANNEL_ID = "habit_reminders_v2"
    private const val CHANNEL_NAME = "Habit Reminders"

    fun parseReminderTime(time: String): Pair<Int, Int> {
        return try {
            val sdf = SimpleDateFormat("h:mm a", Locale.US)
            val date = sdf.parse(time.trim())
            if (date != null) {
                val cal = Calendar.getInstance().apply { setTime(date) }
                Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
            } else {
                8 to 0
            }
        } catch (e: Exception) {
            8 to 0
        }
    }

    fun createChannel(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Daily habit tracker reminders"
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    fun showReminderNotification(context: Context, title: String, message: String, notificationId: Int = 1001) {
        createChannel(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(notificationId, builder.build())
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleHabitAlarm(context: Context, habitId: Int, habitName: String, hour: Int, minute: Int): Long {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("habit_id", habitId)
            putExtra("habit_name", habitName)
            putExtra("message", habitName)
            putExtra("hour", hour)
            putExtra("minute", minute)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Log.w("Reminder", "schedule failed", e)
        }
        return calendar.timeInMillis
    }

    fun cancelHabitAlarm(context: Context, habitId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        } catch (e: Exception) {
            Log.w("Reminder", "cancel failed", e)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleTestAlarmInOneMinute(context: Context): Long {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + 60 * 1000

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("habit_id", 9999)
            putExtra("habit_name", "Rabbit Habit Tracker Test")
            putExtra("message", "Test reminder is working!")
            putExtra("hour", 8)
            putExtra("minute", 0)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Log.w("Reminder", "schedule failed", e)
        }
        return triggerTime
    }
}
