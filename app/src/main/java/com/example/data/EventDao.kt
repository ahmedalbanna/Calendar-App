package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE year = :year AND month = :month ORDER BY startHour ASC, startMinute ASC")
    fun getEventsForMonth(year: Int, month: Int): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM events WHERE year = :year AND month = :month AND day = :day ORDER BY startHour ASC, startMinute ASC")
    fun getEventsForDay(year: Int, month: Int, day: Int): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent): Long

    @Delete
    suspend fun deleteEvent(event: CalendarEvent)

    @Update
    suspend fun updateEvent(event: CalendarEvent)
}
