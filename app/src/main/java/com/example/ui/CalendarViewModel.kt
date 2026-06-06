package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CalendarEvent
import com.example.data.EventRepository
import com.example.receiver.EventScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EventRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = EventRepository(database.eventDao())
    }

    // Current Year and Month
    private val _currentYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()

    private val _currentMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1) // 1-12
    val currentMonth: StateFlow<Int> = _currentMonth.asStateFlow()

    // Current selected day
    private val _selectedDay = MutableStateFlow(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    // Active Monthly Events (to show dots in calendar)
    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyEvents: StateFlow<List<CalendarEvent>> = combine(_currentYear, _currentMonth) { year, month ->
        Pair(year, month)
    }.flatMapLatest { (year, month) ->
        repository.getEventsForMonth(year, month)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Daily Events for selected day (shown below the calendar list)
    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyEvents: StateFlow<List<CalendarEvent>> = combine(_currentYear, _currentMonth, _selectedDay) { year, month, day ->
        Triple(year, month, day)
    }.flatMapLatest { (year, month, day) ->
        repository.getEventsForDay(year, month, day)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectMonth(year: Int, month: Int) {
        _currentYear.value = year
        _currentMonth.value = month
        adjustSelectedDay()
    }

    fun selectDay(day: Int) {
        _selectedDay.value = day
    }

    fun nextMonth() {
        if (_currentMonth.value == 12) {
            _currentYear.value += 1
            _currentMonth.value = 1
        } else {
            _currentMonth.value += 1
        }
        adjustSelectedDay()
    }

    fun previousMonth() {
        if (_currentMonth.value == 1) {
            _currentYear.value -= 1
            _currentMonth.value = 12
        } else {
            _currentMonth.value -= 1
        }
        adjustSelectedDay()
    }

    private fun adjustSelectedDay() {
        val maxDays = getDaysInMonth(_currentYear.value, _currentMonth.value)
        if (_selectedDay.value > maxDays) {
            _selectedDay.value = maxDays
        }
    }

    fun addEvent(
        title: String,
        description: String,
        year: Int,
        month: Int,
        day: Int,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        colorArgb: Long,
        reminderMinutesBefore: Int
    ) {
        viewModelScope.launch {
            val newEvent = CalendarEvent(
                title = title,
                description = description,
                year = year,
                month = month,
                day = day,
                startHour = startHour,
                startMinute = startMinute,
                endHour = endHour,
                endMinute = endMinute,
                colorArgb = colorArgb.toInt(),
                reminderMinutesBefore = reminderMinutesBefore
            )
            val eventId = repository.insert(newEvent)
            val eventWithId = newEvent.copy(id = eventId)
            EventScheduler.scheduleAlarm(getApplication(), eventWithId)
        }
    }

    fun deleteEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.delete(event)
            EventScheduler.cancelAlarm(getApplication(), event.id)
        }
    }

    fun updateEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.update(event)
            EventScheduler.cancelAlarm(getApplication(), event.id)
            EventScheduler.scheduleAlarm(getApplication(), event)
        }
    }

    fun getDaysInMonth(year: Int, month: Int): Int {
        return when (month) {
            1 -> 31
            2 -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
            3 -> 31
            4 -> 30
            5 -> 31
            6 -> 30
            7 -> 31
            8 -> 31
            9 -> 30
            10 -> 31
            11 -> 30
            12 -> 31
            else -> 30
        }
    }

    // Return 0-indexed day of week for the 1st of the month (0 = Sunday, 6 = Saturday)
    fun getFirstDayOfWeek(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.get(Calendar.DAY_OF_WEEK) - 1
    }
}
