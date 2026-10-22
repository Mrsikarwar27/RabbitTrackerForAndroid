package com.example.rabbittracker.widget

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.rabbittracker.MainActivity
import com.example.rabbittracker.data.room.AppDatabase
import com.example.rabbittracker.data.toHabit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HabitWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val (habits, logs) = withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            val hList = db.habitDao().getAll().map { it.toHabit() }
            val lMap = db.habitLogDao().getLogsForDate(today).associate { it.habitId to it }
            Pair(hList, lMap)
        }

        val doneCount = habits.count { habit ->
            val log = logs[habit.id]
            log?.completed == true
        }
        val totalCount = habits.size
        val progressText = "$doneCount of $totalCount done"
        val displayHabits = habits.take(6)

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.background)
                        .padding(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Vertical.CenterVertically,
                        horizontalAlignment = Alignment.Horizontal.Start
                    ) {
                        Text(
                            text = "Today",
                            style = TextStyle(
                                color = GlanceTheme.colors.onBackground,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Text(
                            text = progressText,
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        )
                    }
                    Spacer(modifier = GlanceModifier.height(8.dp))

                    if (displayHabits.isEmpty()) {
                        Text(
                            text = "No habits today",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 13.sp
                            ),
                            modifier = GlanceModifier.padding(vertical = 8.dp)
                        )
                    } else {
                        displayHabits.forEach { habit ->
                            val log = logs[habit.id]
                            val completed = log?.completed == true

                            Row(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Vertical.CenterVertically
                            ) {
                                val nameModifier = GlanceModifier
                                    .defaultWeight()
                                    .clickable(actionStartActivity(ComponentName(context, MainActivity::class.java)))
                                Text(
                                    text = habit.name,
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onSurface,
                                        fontSize = 14.sp
                                    ),
                                    maxLines = 1,
                                    modifier = nameModifier
                                )
                                Spacer(modifier = GlanceModifier.width(8.dp))

                                Text(
                                    text = if (completed) "✓" else "☐",
                                    style = TextStyle(
                                        color = if (completed) GlanceTheme.colors.primary else GlanceTheme.colors.onSurfaceVariant,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = GlanceModifier.clickable(
                                        actionRunCallback<ToggleHabitAction>(
                                            parameters = androidx.glance.action.actionParametersOf(ToggleHabitAction.HabitIdKey to habit.id)
                                        )
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Footer row
                    Text(
                        text = "Open Rabbit Habit Tracker",
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier
                            .padding(top = 4.dp)
                            .clickable(actionStartActivity(ComponentName(context, MainActivity::class.java)))
                    )
                }
            }
        }
    }
}
