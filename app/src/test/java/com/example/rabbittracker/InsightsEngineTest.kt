package com.example.rabbittracker

import com.example.rabbittracker.data.Habit
import com.example.rabbittracker.data.HabitLog
import com.example.rabbittracker.utils.InsightsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightsEngineTest {

    private val sampleHabit = Habit(
        id = 1,
        name = "Morning Walk",
        category = "Health",
        frequency = "Daily",
        iconName = "DirectionsRun",
        colorAccent = "Orange"
    )

    @Test
    fun testUnder7DaysProducesPlaceholder() {
        val logs = mapOf(
            "2026-10-01" to mapOf(1 to HabitLog(1, "2026-10-01", true))
        )
        val insights = InsightsEngine.computeInsights(listOf(sampleHabit), logs, "2026-10-01")
        assertEquals(1, insights.size)
        assertTrue(insights[0].contains("Your coach is watching"))
    }

    @Test
    fun testPerfectDaysInsight() {
        val logs = mutableMapOf<String, Map<Int, HabitLog>>()
        // 14 days, all completed (14 total entries >= 10) -> 14 perfect days
        val dates = (1..14).map { String.format("2026-10-%02d", it) }
        dates.forEach { d ->
            logs[d] = mapOf(1 to HabitLog(1, d, true))
        }

        val insights = InsightsEngine.computeInsights(listOf(sampleHabit), logs, "2026-10-14")
        assertTrue(insights.any { it.contains("perfect days") })
    }
}
