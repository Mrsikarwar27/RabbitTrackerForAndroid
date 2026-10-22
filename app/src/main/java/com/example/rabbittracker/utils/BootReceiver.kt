package com.example.rabbittracker.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.rabbittracker.data.room.AppDatabase
import com.example.rabbittracker.data.toHabit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val habits = db.habitDao().getAll().map { it.toHabit() }
                    for (habit in habits) {
                        if (habit.reminderEnabled) {
                            val (hour, minute) = NotificationHelper.parseReminderTime(habit.reminderTime)
                            NotificationHelper.scheduleHabitAlarm(
                                context,
                                habit.id,
                                habit.name,
                                hour,
                                minute
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.w("BootReceiver", "Failed to reschedule reminders", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
