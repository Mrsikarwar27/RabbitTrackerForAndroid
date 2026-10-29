package com.example.rabbittracker.utils

import com.example.rabbittracker.data.Habit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

data class AdaptiveSuggestion(
    val habitId: Int,
    val habitName: String,
    val medianTimeFormatted: String,
    val suggestedReminderTime: String
)

object AdaptiveReminderEngine {

    fun parseTimeToMinutes(timeStr: String): Int? {
        val trimmed = timeStr.trim()
        return try {
            if (trimmed.contains("AM", ignoreCase = true) || trimmed.contains("PM", ignoreCase = true)) {
                val sdf = SimpleDateFormat("h:mm a", Locale.US)
                val date = sdf.parse(trimmed) ?: return null
                val cal = Calendar.getInstance().apply { time = date }
                cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            } else if (trimmed.contains(":")) {
                val parts = trimmed.split(":")
                val h = parts[0].toInt()
                val m = parts[1].toInt()
                h * 60 + m
            } else null
        } catch (_: Exception) {
            null
        }
    }

    fun formatMinutesTo12Hour(totalMinutes: Int): String {
        val norm = (totalMinutes % 1440 + 1440) % 1440
        val h24 = norm / 60
        val m = norm % 60
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, h24)
            set(Calendar.MINUTE, m)
        }
        return SimpleDateFormat("h:mm a", Locale.US).format(cal.time)
    }

    fun computeMedianMinutes(minutesList: List<Int>): Int {
        if (minutesList.isEmpty()) return 0
        val sorted = minutesList.sorted()
        val size = sorted.size
        return if (size % 2 == 1) {
            sorted[size / 2]
        } else {
            (sorted[size / 2 - 1] + sorted[size / 2]) / 2
        }
    }

    fun calculateSuggestedReminderTime(medianMinutes: Int): String {
        val raw = medianMinutes - 20
        val norm = (raw % 1440 + 1440) % 1440
        val rounded = ((norm + 2) / 5) * 5 % 1440
        return formatMinutesTo12Hour(rounded)
    }

    fun evaluateSuggestion(
        habit: Habit,
        globalRemindersEnabled: Boolean,
        completionTimes: List<String>,
        lastDismissedMillis: Long,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): AdaptiveSuggestion? {
        // Condition (a): At least 5 recorded completions
        if (completionTimes.size < 5) return null

        // Condition (b): Habit reminder enabled AND global reminders enabled
        if (!habit.reminderEnabled || !globalRemindersEnabled) return null

        // Condition (d): No dismissal in last 14 days
        val fourteenDaysMillis = 14 * 24 * 60 * 60 * 1000L
        if (lastDismissedMillis > 0 && (currentTimeMillis - lastDismissedMillis) < fourteenDaysMillis) {
            return null
        }

        // Parse valid completion times
        val validMins = completionTimes.mapNotNull { parseTimeToMinutes(it) }
        if (validMins.size < 5) return null

        val currentReminderMins = parseTimeToMinutes(habit.reminderTime) ?: return null
        val medianMins = computeMedianMinutes(validMins)

        // Condition (c): Median differs from current reminder by 30+ minutes
        val directDiff = abs(currentReminderMins - medianMins)
        val circularDiff = min(directDiff, 1440 - directDiff)
        if (circularDiff < 30) return null

        val medianFormatted = formatMinutesTo12Hour(medianMins)
        val suggestedFormatted = calculateSuggestedReminderTime(medianMins)

        return AdaptiveSuggestion(
            habitId = habit.id,
            habitName = habit.name,
            medianTimeFormatted = medianFormatted,
            suggestedReminderTime = suggestedFormatted
        )
    }
}
