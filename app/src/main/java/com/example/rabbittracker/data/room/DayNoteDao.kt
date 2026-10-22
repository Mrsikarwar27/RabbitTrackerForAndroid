package com.example.rabbittracker.data.room

import androidx.room.*

@Dao
interface DayNoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: DayNoteEntity)

    @Update
    suspend fun update(note: DayNoteEntity)

    @Query("SELECT * FROM day_notes WHERE date = :date")
    suspend fun getNote(date: String): DayNoteEntity?

    @Query("SELECT * FROM day_notes ORDER BY date DESC")
    suspend fun getAll(): List<DayNoteEntity>
}
