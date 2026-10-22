package com.example.rabbittracker.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.example.rabbittracker.data.room.AppDatabase
import com.example.rabbittracker.data.room.HabitLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ToggleHabitAction : ActionCallback {
    companion object {
        val HabitIdKey = ActionParameters.Key<Int>("habitId")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val habitId = parameters[HabitIdKey] ?: return
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            val habit = db.habitDao().getById(habitId) ?: return@withContext
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val current = db.habitLogDao().getLog(habitId, today)
            val completed = !(current?.completed ?: false)
            db.habitLogDao().insert(
                HabitLogEntity(
                    habitId = habitId,
                    date = today,
                    completed = completed,
                    note = current?.note ?: ""
                )
            )
        }
        HabitWidget().updateAll(context)
    }
}
