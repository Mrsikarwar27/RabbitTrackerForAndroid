package com.example.rabbittracker

import com.example.rabbittracker.data.Habit
import com.example.rabbittracker.utils.AdaptiveReminderEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AdaptiveReminderEngineTest {

    private val sampleHabit = Habit(
        id = 1,
        name = "Morning Walk",
        category = "Health",
        frequency = "Daily",
        iconName = "DirectionsRun",
        colorAccent = "Orange",
        reminderTime = "06:00 AM",
        reminderEnabled = true
    )

    @Test
    fun testEligibleSuggestionProduced() {
        // 5 completions around 7:20 AM with 6:00 AM reminder
        val times = listOf("07:20", "07:22", "07:18", "07:20", "07:25")
        val suggestion = AdaptiveReminderEngine.evaluateSuggestion(
            habit = sampleHabit,
            globalRemindersEnabled = true,
            completionTimes = times,
            lastDismissedMillis = 0L,
            currentTimeMillis = System.currentTimeMillis()
        )

        assertNotNull(suggestion)
        assertEquals("Morning Walk", suggestion?.habitName)
        assertEquals("7:20 AM", suggestion?.medianTimeFormatted)
        assertEquals("7:00 AM", suggestion?.suggestedReminderTime)
    }

    @Test
    fun testInsufficientCompletionsNoSuggestion() {
        // Only 3 completions -> should return null
        val times = listOf("07:20", "07:22", "07:18")
        val suggestion = AdaptiveReminderEngine.evaluateSuggestion(
            habit = sampleHabit,
            globalRemindersEnabled = true,
            completionTimes = times,
            lastDismissedMillis = 0L,
            currentTimeMillis = System.currentTimeMillis()
        )

        assertNull(suggestion)
    }

    @Test
    fun testMedianWithin30MinutesNoSuggestion() {
        // Completions around 6:15 AM with 6:00 AM reminder (diff < 30 mins) -> should return null
        val times = listOf("06:15", "06:12", "06:18", "06:15", "06:14")
        val suggestion = AdaptiveReminderEngine.evaluateSuggestion(
            habit = sampleHabit,
            globalRemindersEnabled = true,
            completionTimes = times,
            lastDismissedMillis = 0L,
            currentTimeMillis = System.currentTimeMillis()
        )

        assertNull(suggestion)
    }

    @Test
    fun testDismissedRecentlyNoSuggestion() {
        // 5 completions around 7:20 AM but dismissed 2 days ago -> should return null
        val times = listOf("07:20", "07:22", "07:18", "07:20", "07:25")
        val now = System.currentTimeMillis()
        val twoDaysAgo = now - (2 * 24 * 60 * 60 * 1000L)

        val suggestion = AdaptiveReminderEngine.evaluateSuggestion(
            habit = sampleHabit,
            globalRemindersEnabled = true,
            completionTimes = times,
            lastDismissedMillis = twoDaysAgo,
            currentTimeMillis = now
        )

        assertNull(suggestion)
    }
}
