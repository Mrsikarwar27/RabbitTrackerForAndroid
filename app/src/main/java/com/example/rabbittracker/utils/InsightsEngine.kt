package com.example.rabbittracker.utils

import com.example.rabbittracker.data.Habit
import com.example.rabbittracker.data.HabitLog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CoachInsight(
    val text: String,
    val priority: Int
)

object InsightsEngine {

    fun computeInsights(
        habits: List<Habit>,
        recentLogs: Map<String, Map<Int, HabitLog>>,
        todayDateStr: String
    ): List<String> {
        val allDates = recentLogs.keys.sorted()
        val totalEntries = recentLogs.values.sumOf { dayMap -> dayMap.values.count { it.completed } }

        if (allDates.size < 7 || totalEntries < 10) {
            return listOf("Your coach is watching. Insights will appear here after about a week of tracking.")
        }

        val insights = mutableListOf<CoachInsight>()

        val isScheduled: (Habit, String) -> Boolean = { _, _ -> true }

        val weekdayMap = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()
        val cal = Calendar.getInstance()
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        allDates.forEach { dateStr ->
            val date = dateFmt.parse(dateStr)
            if (date != null) {
                cal.time = date
                val dow = cal.get(Calendar.DAY_OF_WEEK)
                val dayLogs = recentLogs[dateStr] ?: emptyMap()
                val scheduledHabits = habits.filter { isScheduled(it, dateStr) }
                if (scheduledHabits.isNotEmpty()) {
                    val completedCount = scheduledHabits.count { dayLogs[it.id]?.completed == true }
                    weekdayMap.getOrPut(dow) { mutableListOf() }.add(completedCount to scheduledHabits.size)
                }
            }
        }

        val weekdayStats = weekdayMap.mapNotNull { (dow, list) ->
            if (list.size >= 3) {
                val totalComp = list.sumOf { it.first }
                val totalSched = list.sumOf { it.second }
                val pct = if (totalSched > 0) (totalComp * 100) / totalSched else 0
                val name = when (dow) {
                    Calendar.MONDAY -> "Monday"
                    Calendar.TUESDAY -> "Tuesday"
                    Calendar.WEDNESDAY -> "Wednesday"
                    Calendar.THURSDAY -> "Thursday"
                    Calendar.FRIDAY -> "Friday"
                    Calendar.SATURDAY -> "Saturday"
                    else -> "Sunday"
                }
                Triple(name, pct, list.size)
            } else null
        }.sortedBy { it.second }

        if (weekdayStats.isNotEmpty()) {
            val weakest = weekdayStats.first()
            insights.add(CoachInsight("${weakest.first}s are your toughest day — only ${weakest.second}% completed in the last 4 weeks.", 2))

            if (weekdayStats.size > 1) {
                val strongest = weekdayStats.last()
                if (strongest.second >= weakest.second + 20) {
                    insights.add(CoachInsight("${strongest.first}s are your strongest day at ${strongest.second}%.", 3))
                }
            }
        }

        val habitStats = habits.mapNotNull { habit ->
            val scheduledDates = allDates.filter { isScheduled(habit, it) }
            if (scheduledDates.size >= 7) {
                val completedDays = scheduledDates.count { recentLogs[it]?.get(habit.id)?.completed == true }
                val pct = (completedDays * 100) / scheduledDates.size
                Pair(habit.name, pct)
            } else null
        }.sortedByDescending { it.second }

        if (habitStats.isNotEmpty()) {
            val best = habitStats.first()
            insights.add(CoachInsight("'${best.first}' is your most consistent habit — ${best.second}% this month.", 2))
        }

        val sortedDatesDesc = allDates.reversed()
        val thisWeekDates = sortedDatesDesc.take(7)
        val lastWeekDates = sortedDatesDesc.drop(7).take(7)

        if (thisWeekDates.size >= 5 && lastWeekDates.size >= 5) {
            val calcPct = { dates: List<String> ->
                var comp = 0
                var sched = 0
                dates.forEach { d ->
                    val dayLogs = recentLogs[d] ?: emptyMap()
                    habits.forEach { h ->
                        if (isScheduled(h, d)) {
                            sched++
                            if (dayLogs[h.id]?.completed == true) comp++
                        }
                    }
                }
                if (sched > 0) (comp * 100) / sched else 0
            }

            val thisWeekPct = calcPct(thisWeekDates)
            val lastWeekPct = calcPct(lastWeekDates)
            val diff = thisWeekPct - lastWeekPct

            if (diff >= 10) {
                insights.add(CoachInsight("You are improving — ${thisWeekPct}% completion this week, up from ${lastWeekPct}% last week.", 2))
            } else if (diff <= -10) {
                insights.add(CoachInsight("A slower week — ${thisWeekPct}% vs ${lastWeekPct}% last week. Small steps still count.", 2))
            }
        }

        val last14Dates = allDates.takeLast(14)
        var perfectDaysCount = 0
        last14Dates.forEach { d ->
            val dayLogs = recentLogs[d] ?: emptyMap()
            val scheduledHabits = habits.filter { isScheduled(it, d) }
            if (scheduledHabits.isNotEmpty()) {
                val allDone = scheduledHabits.all { dayLogs[it.id]?.completed == true }
                if (allDone) perfectDaysCount++
            }
        }

        if (perfectDaysCount >= 2) {
            insights.add(CoachInsight("You had $perfectDaysCount perfect days in the last 2 weeks. Well done.", 1))
        }

        return insights.sortedBy { it.priority }.take(3).map { it.text }
    }
}
