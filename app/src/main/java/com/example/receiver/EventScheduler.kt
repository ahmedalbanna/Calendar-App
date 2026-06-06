package com.example.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.CalendarEvent
import java.util.Calendar

object EventScheduler {
    private const val TAG = "EventScheduler"

    fun scheduleAlarm(context: Context, event: CalendarEvent) {
        if (event.reminderMinutesBefore < 0) return // No reminder scheduled

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val eventCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, event.year)
            set(Calendar.MONTH, event.month - 1) // 0-indexed month
            set(Calendar.DAY_OF_MONTH, event.day)
            set(Calendar.HOUR_OF_DAY, event.startHour)
            set(Calendar.MINUTE, event.startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Subtract the reminder duration
        eventCalendar.add(Calendar.MINUTE, -event.reminderMinutesBefore)
        val triggerTimeInMillis = eventCalendar.timeInMillis

        // If the calculated trigger time is in the past, do not schedule
        if (triggerTimeInMillis <= System.currentTimeMillis()) {
            Log.d(TAG, "Not scheduling: Alarm trigger time $triggerTimeInMillis in past.")
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EVENT_ID", event.id)
            putExtra("EVENT_TITLE", event.title)
            putExtra("EVENT_DESC", event.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        try {
            if (canScheduleExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeInMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Successfully scheduled alarm for event ID ${event.id} at ${eventCalendar.time}")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: exact alarm scheduling failed, falling back to non-exact.", e)
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTimeInMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm for event ID ${event.id}", e)
        }
    }

    fun cancelAlarm(context: Context, eventId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm for event ID $eventId")
        }
    }
}
