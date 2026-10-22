package com.example.rabbittracker.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.rabbittracker.MainActivity
import com.example.rabbittracker.R
import com.example.rabbittracker.data.room.AppDatabase
import com.example.rabbittracker.data.room.HabitLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val habitId = intent.getIntExtra("habit_id", 1001)
        val habitName = intent.getStringExtra("habit_name") ?: "Rabbit Habit Tracker"
        val message = intent.getStringExtra("message") ?: habitName
        val hour = intent.getIntExtra("hour", 8)
        val minute = intent.getIntExtra("minute", 0)
        val isSnoozed = intent.getBooleanExtra("snoozed", false)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (action == "com.example.rabbittracker.ACTION_DONE") {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val habit = db.habitDao().getById(habitId)
                    if (habit != null) {
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                        val currentLog = db.habitLogDao().getLog(habitId, today)
                        db.habitLogDao().insert(
                            HabitLogEntity(
                                habitId = habitId,
                                date = today,
                                completed = true,
                                note = currentLog?.note ?: ""
                            )
                        )
                    }
                    notificationManager.cancel(habitId)
                    val snoozeIntent = Intent(context, ReminderReceiver::class.java).setAction("com.example.rabbittracker.ACTION_SNOOZE")
                    val snoozePi = PendingIntent.getBroadcast(
                        context,
                        habitId + 500000,
                        snoozeIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    alarmManager.cancel(snoozePi)
                    snoozePi.cancel()
                } catch (e: Exception) {
                    Log.w("ReminderReceiver", "ACTION_DONE failed", e)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        if (action == "com.example.rabbittracker.ACTION_SNOOZE") {
            notificationManager.cancel(habitId)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerTime = System.currentTimeMillis() + 60 * 60 * 1000L

            val snoozeIntent = Intent(context, ReminderReceiver::class.java).setAction("com.example.rabbittracker.ACTION_SNOOZE").apply {
                putExtra("habit_id", habitId)
                putExtra("habit_name", habitName)
                putExtra("message", message)
                putExtra("hour", hour)
                putExtra("minute", minute)
                putExtra("snoozed", true)
            }
            val snoozePi = PendingIntent.getBroadcast(
                context,
                habitId + 500000,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        snoozePi
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        snoozePi
                    )
                }
            } catch (e: Exception) {
                Log.w("ReminderReceiver", "Snooze schedule failed", e)
            }
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "habit_reminders_v2",
                "Habit Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily habit tracker reminders"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "habit_reminders_v2")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Habit Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(pendingIntent)

        if (habitId != 9999 && habitId != 900001) {
            val doneIntent = Intent(context, ReminderReceiver::class.java).setAction("com.example.rabbittracker.ACTION_DONE").apply {
                putExtra("habit_id", habitId)
                putExtra("habit_name", habitName)
                putExtra("message", message)
                putExtra("hour", hour)
                putExtra("minute", minute)
            }
            val donePi = PendingIntent.getBroadcast(
                context,
                habitId,
                doneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val doneAction = NotificationCompat.Action.Builder(
                R.drawable.ic_notification,
                "Done",
                donePi
            ).build()

            val snoozeIntent = Intent(context, ReminderReceiver::class.java).setAction("com.example.rabbittracker.ACTION_SNOOZE").apply {
                putExtra("habit_id", habitId)
                putExtra("habit_name", habitName)
                putExtra("message", message)
                putExtra("hour", hour)
                putExtra("minute", minute)
            }
            val snoozePi = PendingIntent.getBroadcast(
                context,
                habitId,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val snoozeAction = NotificationCompat.Action.Builder(
                R.drawable.ic_notification,
                "Snooze 1h",
                snoozePi
            ).build()

            builder.addAction(doneAction)
            builder.addAction(snoozeAction)
        }

        notificationManager.notify(habitId, builder.build())

        // Reschedule for tomorrow if not test reminder and not snoozed
        if (habitId != 9999 && !isSnoozed) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val calendar = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val nextIntent = Intent(context, ReminderReceiver::class.java).apply {
                    putExtra("habit_id", habitId)
                    putExtra("habit_name", habitName)
                    putExtra("message", message)
                    putExtra("hour", hour)
                    putExtra("minute", minute)
                }
                val nextPendingIntent = PendingIntent.getBroadcast(
                    context,
                    habitId,
                    nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        nextPendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        nextPendingIntent
                    )
                }
            } catch (e: Exception) {
                Log.w("ReminderReceiver", "reschedule failed", e)
            }
        }
    }
}
