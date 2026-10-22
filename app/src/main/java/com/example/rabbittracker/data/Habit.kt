package com.example.rabbittracker.data

data class Habit(
    val id: Int = 0,
    val name: String,
    val category: String,
    val frequency: String,
    val iconName: String,
    val colorAccent: String,
    val description: String = "",
    val reminderTime: String = "08:00 AM",
    val reminderEnabled: Boolean = true
)

data class HabitLog(
    val habitId: Int,
    val date: String, // "2026-10-21"
    val completed: Boolean = false,
    val note: String = ""
)

data class DayNote(
    val date: String,
    val text: String
)
