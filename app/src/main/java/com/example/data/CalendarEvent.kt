package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val year: Int,
    val month: Int, // 1 - 12
    val day: Int,   // 1 - 31
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val colorArgb: Int,
    val reminderMinutesBefore: Int = 15 // 0 for at design time, -1 for no reminder
)
