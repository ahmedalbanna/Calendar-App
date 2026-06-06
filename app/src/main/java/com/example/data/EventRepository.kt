package com.example.data

import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {
    fun getEventsForMonth(year: Int, month: Int): Flow<List<CalendarEvent>> =
        eventDao.getEventsForMonth(year, month)

    fun getEventsForDay(year: Int, month: Int, day: Int): Flow<List<CalendarEvent>> =
        eventDao.getEventsForDay(year, month, day)

    suspend fun insert(event: CalendarEvent): Long = eventDao.insertEvent(event)

    suspend fun delete(event: CalendarEvent) = eventDao.deleteEvent(event)

    suspend fun update(event: CalendarEvent) = eventDao.updateEvent(event)
}
