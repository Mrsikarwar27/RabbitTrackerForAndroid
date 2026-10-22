package com.example.rabbittracker.data.room

import androidx.room.*

@Dao
interface HabitLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: HabitLogEntity)

    @Update
    suspend fun update(log: HabitLogEntity)

    @Delete
    suspend fun delete(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Int)

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun getLog(habitId: Int, date: String): HabitLogEntity?

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    suspend fun getLogsForDate(date: String): List<HabitLogEntity>
}
