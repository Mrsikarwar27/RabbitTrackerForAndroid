package com.example.rabbittracker.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val frequency: String,
    val iconName: String,
    val colorAccent: String,
    val description: String,
    val reminderTime: String = "08:00 AM",
    val reminderEnabled: Boolean = true
)

@Entity(tableName = "habit_logs", primaryKeys = ["habitId", "date"])
data class HabitLogEntity(
    val habitId: Int,
    val date: String,
    val completed: Boolean = false,
    val note: String = ""
)

@Entity(tableName = "day_notes", primaryKeys = ["date"])
data class DayNoteEntity(
    val date: String,
    val text: String
)
