package com.example.rabbittracker.data

import com.example.rabbittracker.data.room.AppDatabase
import com.example.rabbittracker.data.room.DayNoteEntity
import com.example.rabbittracker.data.room.HabitEntity
import com.example.rabbittracker.data.room.HabitLogEntity

class HabitRepository(private val db: AppDatabase) {
    suspend fun getAllHabits(): List<HabitEntity> = db.habitDao().getAll()
    suspend fun getHabitById(id: Int): HabitEntity? = db.habitDao().getById(id)
    suspend fun insertHabit(habit: HabitEntity): Long = db.habitDao().insert(habit)
    suspend fun updateHabit(habit: HabitEntity) = db.habitDao().update(habit)
    suspend fun deleteHabit(habit: HabitEntity) = db.habitDao().delete(habit)
    suspend fun deleteLogsForHabit(habitId: Int) = db.habitLogDao().deleteLogsForHabit(habitId)

    suspend fun getLog(habitId: Int, date: String): HabitLogEntity? =
        db.habitLogDao().getLog(habitId, date)

    suspend fun getLogsForDate(date: String): List<HabitLogEntity> =
        db.habitLogDao().getLogsForDate(date)

    suspend fun saveLog(log: HabitLogEntity) = db.habitLogDao().insert(log)

    suspend fun getDayNote(date: String): DayNoteEntity? = db.dayNoteDao().getNote(date)

    suspend fun saveDayNote(note: DayNoteEntity) = db.dayNoteDao().insert(note)
}

fun HabitEntity.toHabit(): Habit = Habit(
    id = id,
    name = name,
    category = category,
    frequency = frequency,
    iconName = iconName,
    colorAccent = colorAccent,
    description = description,
    reminderTime = reminderTime,
    reminderEnabled = reminderEnabled
)

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    name = name,
    category = category,
    frequency = frequency,
    iconName = iconName,
    colorAccent = colorAccent,
    description = description,
    reminderTime = reminderTime,
    reminderEnabled = reminderEnabled
)

fun HabitLogEntity.toLog(): HabitLog = HabitLog(
    habitId = habitId,
    date = date,
    completed = completed,
    note = note
)

fun HabitLog.toEntity(): HabitLogEntity = HabitLogEntity(
    habitId = habitId,
    date = date,
    completed = completed,
    note = note
)
