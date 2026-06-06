package com.example.ui

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CalendarEvent
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier
) {
    // Force RTL local layout direction for Arabic visual design
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val currentYear by viewModel.currentYear.collectAsStateWithLifecycle()
        val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
        val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()

        val monthlyEvents by viewModel.monthlyEvents.collectAsStateWithLifecycle()
        val dailyEvents by viewModel.dailyEvents.collectAsStateWithLifecycle()

        // Local state for interactive features
        var activeTab by remember { mutableStateOf("calendar") } // calendar, tasks, reminders
        var isSearchActive by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }

        var showAddEditDialog by remember { mutableStateOf(false) }
        var eventToEdit by remember { mutableStateOf<CalendarEvent?>(null) }

        val context = LocalContext.current

        // Arabic Month Names mapping
        val arabicMonths = listOf(
            "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
            "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
        )
        val monthName = arabicMonths.getOrElse(currentMonth - 1) { "" }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        eventToEdit = null
                        showAddEditDialog = true
                    },
                    containerColor = Color(0xFFD3E3FD), // Professional Polish light-blue container
                    contentColor = Color(0xFF001D35), // Professional Polish dark navy
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("add_event_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إضافة حدث جديد"
                    )
                }
            },
            bottomBar = {
                CustomBottomNavigation(
                    activeTab = activeTab,
                    onTabSelected = { tab ->
                        activeTab = tab
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color(0xFFF7F9FC)) // Theme: SlateBackground
            ) {
                // Top Custom Header: Material You style matching mockup
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Title representing current month & chevrons
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "$monthName $currentYear",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF1B1B1F) // SlateTypography
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Previous Month Chevron
                                IconButton(
                                    onClick = { viewModel.previousMonth() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Text("◀", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF1B1B1F).copy(alpha = 0.6f))
                                }

                                // Next Month Chevron
                                IconButton(
                                    onClick = { viewModel.nextMonth() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Text("▶", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF1B1B1F).copy(alpha = 0.6f))
                                }
                            }
                        }

                        // Search and Initials actions
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = { isSearchActive = !isSearchActive },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSearchActive) Color(0xFFF1F3F4) else Color.Transparent)
                            ) {
                                Text("🔍", style = MaterialTheme.typography.titleLarge)
                            }

                            // Initials AS container from Mockup layout
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD3E3FD))
                                    .border(1.dp, Color(0xFFA8C7FA), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "AS",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF001D35)
                                )
                            }
                        }
                    }

                    // Interactive Search Bar
                    AnimatedVisibility(
                        visible = isSearchActive,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("ابحث في عنوان الفعالية أو المهام...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                .testTag("event_search_bar"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0056D2),
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }

                    Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                }

                // Swapped Tab Viewports
                when (activeTab) {
                    "calendar" -> {
                        // Render standard Calendar Grid & Daily Events
                        CalendarViewTab(
                            currentYear = currentYear,
                            currentMonth = currentMonth,
                            selectedDay = selectedDay,
                            monthlyEvents = monthlyEvents,
                            searchQuery = searchQuery,
                            dailyEvents = dailyEvents,
                            monthName = monthName,
                            viewModel = viewModel,
                            onEventClick = { ev ->
                                eventToEdit = ev
                                showAddEditDialog = true
                            },
                            onEventDelete = { ev ->
                                viewModel.deleteEvent(ev)
                            }
                        )
                    }
                    "tasks" -> {
                        // Task Checklist View for all events in current month
                        TaskListViewTab(
                            events = filterSearch(monthlyEvents, searchQuery),
                            onEventClick = { ev ->
                                eventToEdit = ev
                                showAddEditDialog = true
                            },
                            onEventDelete = { ev ->
                                viewModel.deleteEvent(ev)
                            }
                        )
                    }
                    "reminders" -> {
                        // Reminders list for events that have alarms scheduled
                        AlarmListViewTab(
                            events = filterSearch(monthlyEvents, searchQuery).filter { it.reminderMinutesBefore >= 0 },
                            onEventClick = { ev ->
                                eventToEdit = ev
                                showAddEditDialog = true
                            },
                            onEventDelete = { ev ->
                                viewModel.deleteEvent(ev)
                            }
                        )
                    }
                }
            }
        }

        // Add / Edit Dialog Window
        if (showAddEditDialog) {
            AddEditEventDialog(
                event = eventToEdit,
                onDismiss = { showAddEditDialog = false },
                onSave = { title, desc, startH, startM, endH, endM, color, reminder ->
                    if (eventToEdit == null) {
                        viewModel.addEvent(
                            title = title,
                            description = desc,
                            year = currentYear,
                            month = currentMonth,
                            day = selectedDay,
                            startHour = startH,
                            startMinute = startM,
                            endHour = endH,
                            endMinute = endM,
                            colorArgb = color,
                            reminderMinutesBefore = reminder
                        )
                        Toast.makeText(context, "تم حفظ الفعالية وجدولة التنبيه بنجاح", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.updateEvent(
                            eventToEdit!!.copy(
                                title = title,
                                description = desc,
                                startHour = startH,
                                startMinute = startM,
                                endHour = endH,
                                endMinute = endM,
                                colorArgb = color.toInt(),
                                reminderMinutesBefore = reminder
                            )
                        )
                        Toast.makeText(context, "تم تحديث الفعالية بنجاح", Toast.LENGTH_SHORT).show()
                    }
                    showAddEditDialog = false
                }
            )
        }
    }
}

// Helper to filter events based on search query
private fun filterSearch(events: List<CalendarEvent>, query: String): List<CalendarEvent> {
    return if (query.isBlank()) {
        events
    } else {
        events.filter {
            it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
        }
    }
}

@Composable
fun CalendarViewTab(
    currentYear: Int,
    currentMonth: Int,
    selectedDay: Int,
    monthlyEvents: List<CalendarEvent>,
    searchQuery: String,
    dailyEvents: List<CalendarEvent>,
    monthName: String,
    viewModel: CalendarViewModel,
    onEventClick: (CalendarEvent) -> Unit,
    onEventDelete: (CalendarEvent) -> Unit
) {
    val filteredDailyEvents = filterSearch(dailyEvents, searchQuery)

    Column(modifier = Modifier.fillMaxSize()) {
        // Calendar Grid Section: pristine white card layout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // S M T W T F S / Week headers
                val weekDays = listOf("أحد", "إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekDays.forEach { dayName ->
                        Text(
                            text = dayName,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF94A3B8) // slate-400 matching mockup
                        )
                    }
                }

                // Grid Cells
                val daysInMonth = viewModel.getDaysInMonth(currentYear, currentMonth)
                val firstDayOfWeek = viewModel.getFirstDayOfWeek(currentYear, currentMonth)

                val todayCal = Calendar.getInstance()
                val todayYear = todayCal.get(Calendar.YEAR)
                val todayMonth = todayCal.get(Calendar.MONTH) + 1
                val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

                val totalSlots = daysInMonth + firstDayOfWeek
                val rowsCount = (totalSlots + 6) / 7

                for (r in 0 until rowsCount) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (c in 0 until 7) {
                            val slotIndex = r * 7 + c
                            val day = slotIndex - firstDayOfWeek + 1

                            if (slotIndex < firstDayOfWeek || day > daysInMonth) {
                                Box(modifier = Modifier.weight(1f))
                            } else {
                                val isSelected = selectedDay == day
                                val isToday = currentYear == todayYear && currentMonth == todayMonth && todayDay == day
                                val dayEvents = monthlyEvents.filter { it.day == day }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> Color(0xFF0056D2) // Professional Polish main accent blue
                                                isToday -> Color(0xFFD3E3FD).copy(alpha = 0.5f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isToday) 2.dp else 0.dp,
                                            color = if (isToday) Color(0xFF0056D2) else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.selectDay(day) }
                                        .testTag("day_cell_$day"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = day.toString(),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSelected -> Color.White
                                                isToday -> Color(0xFF0056D2)
                                                else -> Color(0xFF1B1B1F) // SlateTypography
                                            }
                                        )

                                        // Draw up to 3 colorful indicator dots underneath
                                        if (dayEvents.isNotEmpty()) {
                                            Row(
                                                modifier = Modifier.padding(top = 2.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                dayEvents.take(3).forEach { ev ->
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(horizontal = 1.dp)
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(ev.colorArgb))
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Rounded Dynamic Section: Day Details
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
        ) {
            // Rounded Container mimicking the mockup's shadow inner look
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color(0xFFF1F3F4)) // section background matching HTML mock
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Title info row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val fullDateText = getArabicFullDate(currentYear, currentMonth, selectedDay)
                        Text(
                            text = fullDateText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF64748B) // slate-500
                        )

                        val todayCal = Calendar.getInstance()
                        val isSelectedToday = todayCal.get(Calendar.YEAR) == currentYear &&
                                todayCal.get(Calendar.MONTH) + 1 == currentMonth &&
                                todayCal.get(Calendar.DAY_OF_MONTH) == selectedDay

                        if (isSelectedToday) {
                            Text(
                                text = "اليوم",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF0056D2) // blue-600 indicator
                            )
                        }
                    }

                    // Dynamic Events Feed
                    if (filteredDailyEvents.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "🔍 لا توجد فعاليات مجدولة للغد أو اليوم",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "تفرّغ لقضاء يوم هادئ أو أضف أحداثاً عاجلة.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredDailyEvents, key = { it.id }) { event ->
                                EventItemCompact(
                                    event = event,
                                    onClick = { onEventClick(event) },
                                    onDelete = { onEventDelete(event) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2. Tab: Tasks list (All events checklist)
@Composable
fun TaskListViewTab(
    events: List<CalendarEvent>,
    onEventClick: (CalendarEvent) -> Unit,
    onEventDelete: (CalendarEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "سجل المهام الشامل",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1B1B1F)
            )

            Text(
                text = "${events.size} مهمة مجدولة",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF0056D2)
            )
        }

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد مهام مسجلة حتى الآن. أضف بعض التفاعلات لتظهر هنا.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(events, key = { it.id }) { event ->
                    EventItemCompact(
                        event = event,
                        labelInfo = "${event.day}/${event.month}",
                        onClick = { onEventClick(event) },
                        onDelete = { onEventDelete(event) }
                    )
                }
            }
        }
    }
}

// 3. Tab: Scheduled notifications (Alarms List)
@Composable
fun AlarmListViewTab(
    events: List<CalendarEvent>,
    onEventClick: (CalendarEvent) -> Unit,
    onEventDelete: (CalendarEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "التذكيرات والإشعارات المنبثقة",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1B1B1F)
            )

            Text(
                text = "${events.size} تنبيهاً نشطاً",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF0056D2)
            )
        }

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد فعاليات مع تذكيرات نشطة.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(events, key = { it.id }) { event ->
                    EventItemCompact(
                        event = event,
                        labelInfo = "تنبيه قبل بـ ${event.reminderMinutesBefore} دق",
                        onClick = { onEventClick(event) },
                        onDelete = { onEventDelete(event) }
                    )
                }
            }
        }
    }
}

// Compact Professional polished Card matching mockup designs
@Composable
fun EventItemCompact(
    event: CalendarEvent,
    labelInfo: String? = null,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("event_item_${event.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color Left strip ribbon matching mockup's rounded vertical pill
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .clip(CircleShape)
                    .background(Color(event.colorArgb))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF1B1B1F)
                    )

                    val startTimeFormatted = String.format("%02d:%02d", event.startHour, event.startMinute)
                    val endTimeFormatted = String.format("%02d:%02d", event.endHour, event.endMinute)

                    Text(
                        text = labelInfo ?: "$startTimeFormatted - $endTimeFormatted",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF0056D2)
                    )
                }

                if (event.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Quick Delete option
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .testTag("delete_event_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف الفعالية",
                    tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Programmatic Calendar Icon vector
@Composable
fun CalendarIcon(color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(22.dp), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // draw outer box
            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.1f, h * 0.2f),
                size = androidx.compose.ui.geometry.Size(w * 0.8f, h * 0.7f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.1f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.08f)
            )
            // draw separator line
            drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(w * 0.1f, h * 0.42f),
                end = androidx.compose.ui.geometry.Offset(w * 0.9f, h * 0.42f),
                strokeWidth = w * 0.08f
            )
            // binders
            drawRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.05f),
                size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.2f)
            )
            drawRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.67f, h * 0.05f),
                size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.2f)
            )
        }
    }
}

// Programmatic Tasks Icon vector
@Composable
fun TasksIcon(color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(22.dp), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val startX = w * 0.35f
            val endX = w * 0.95f
            val stroke = w * 0.08f

            // Line 1
            drawLine(color = color, start = androidx.compose.ui.geometry.Offset(startX, h * 0.25f), end = androidx.compose.ui.geometry.Offset(endX, h * 0.25f), strokeWidth = stroke)
            drawCircle(color = color, radius = w * 0.09f, center = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.25f))

            // Line 2
            drawLine(color = color, start = androidx.compose.ui.geometry.Offset(startX, h * 0.5f), end = androidx.compose.ui.geometry.Offset(endX, h * 0.5f), strokeWidth = stroke)
            drawCircle(color = color, radius = w * 0.09f, center = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.5f))

            // Line 3
            drawLine(color = color, start = androidx.compose.ui.geometry.Offset(startX, h * 0.75f), end = androidx.compose.ui.geometry.Offset(endX, h * 0.75f), strokeWidth = stroke)
            drawCircle(color = color, radius = w * 0.09f, center = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.75f))
        }
    }
}

// Material Design bottom navigation tab items
@Composable
fun RowScope.BottomNavTab(
    isSelected: Boolean,
    label: String,
    icon: @Composable (Color) -> Unit,
    onClick: () -> Unit
) {
    val activeColor = Color(0xFF001D35)
    val inactiveColor = Color(0xFF1B1B1F).copy(alpha = 0.5f)

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Active highlight pill container around icon
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (isSelected) Color(0xFFD3E3FD) else Color.Transparent)
                .padding(horizontal = 20.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            icon(if (isSelected) activeColor else inactiveColor)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) activeColor else inactiveColor
        )
    }
}

// Custom Bottom Navigation Bar mimicking HTML mockup
@Composable
fun CustomBottomNavigation(
    activeTab: String,
    onTabSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)) // subtle slate-100 top border
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Calendar
            BottomNavTab(
                isSelected = activeTab == "calendar",
                label = "التقويم",
                icon = { col -> CalendarIcon(col) },
                onClick = { onTabSelected("calendar") }
            )

            // Tab 2: Tasks
            BottomNavTab(
                isSelected = activeTab == "tasks",
                label = "المهام",
                icon = { col -> TasksIcon(col) },
                onClick = { onTabSelected("tasks") }
            )

            // Tab 3: Reminders
            BottomNavTab(
                isSelected = activeTab == "reminders",
                label = "التذكيرات",
                icon = { col -> Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = col, modifier = Modifier.size(20.dp)) },
                onClick = { onTabSelected("reminders") }
            )
        }
    }
}

// Helper to calculate full Arabic Month Day Names
fun getArabicFullDate(year: Int, month: Int, day: Int): String {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month - 1)
    cal.set(Calendar.DAY_OF_MONTH, day)
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

    val arabicDayNames = listOf("الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
    val dayName = arabicDayNames.getOrElse(if (dayOfWeek == 1) 0 else dayOfWeek - 1) { "" }

    val arabicMonths = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )
    val monthName = arabicMonths.getOrElse(month - 1) { "" }

    return "$dayName، $day $monthName"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventDialog(
    event: CalendarEvent?,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, startH: Int, startM: Int, endH: Int, endM: Int, color: Long, reminder: Int) -> Unit
) {
    var title by remember { mutableStateOf(event?.title ?: "") }
    var description by remember { mutableStateOf(event?.description ?: "") }

    var startHour by remember { mutableStateOf(event?.startHour ?: 9) }
    var startMinute by remember { mutableStateOf(event?.startMinute ?: 0) }

    var endHour by remember { mutableStateOf(event?.endHour ?: 10) }
    var endMinute by remember { mutableStateOf(event?.endMinute ?: 0) }

    // Color options: Emerald, Teal, Crimson/Red, Gold, Purple
    val colors = listOf(
        0xFF10B981L to "أخضر شخصي",
        0xFF3B82F6L to "أزرق عمل",
        0xFFEF4444L to "أحمر تجاري",
        0xFFF59E0BL to "أصفر دراسة",
        0xFF8B5CF6L to "بنفسجي ترفيه"
    )
    var selectedColor by remember { mutableStateOf(event?.colorArgb?.toLong()?.let { argb -> colors.find { it.first == argb }?.first } ?: colors[0].first) }

    // Reminders option
    val reminderOptions = listOf(
        -1 to "بدون تذكير",
        0 to "في نفس وقت البدء",
        15 to "قبل بـ 15 دقيقة",
        30 to "قبل بـ 30 دقيقة",
        60 to "قبل بـ 1 ساعة"
    )
    var selectedReminder by remember { mutableStateOf(event?.reminderMinutesBefore ?: 15) }

    val context = LocalContext.current

    val startTimePickerDialog = TimePickerDialog(
        context,
        { _, h, m ->
            startHour = h
            startMinute = m
        },
        startHour,
        startMinute,
        true
    )

    val endTimePickerDialog = TimePickerDialog(
        context,
        { _, h, m ->
            endHour = h
            endMinute = m
        },
        endHour,
        endMinute,
        true
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(4.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (event == null) "إضافة حدث جديد" else "تعديل الحدث",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0056D2),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الفعالية أو المهمة") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_title_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0056D2),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("تفاصيل ووصف الحدث") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0056D2),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                // Scheduled times
                Text(
                    text = "التوقيت المجدول",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { startTimePickerDialog.show() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F3F4)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("وقت البدء", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                            Text(
                                text = String.format("%02d:%02d", startHour, startMinute),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0056D2)
                            )
                        }
                    }

                    Button(
                        onClick = { endTimePickerDialog.show() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F3F4)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("وقت الانتهاء", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                            Text(
                                text = String.format("%02d:%02d", endHour, endMinute),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0056D2)
                            )
                        }
                    }
                }

                // Choose Color ribbon
                Text(
                    text = "رمز اللون والنوع",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colors.forEach { (colorVal, _) ->
                        val isColorSelected = selectedColor == colorVal
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    border = if (isColorSelected) BorderStroke(3.dp, Color(0xFF1B1B1F)) else BorderStroke(0.dp, Color.Transparent),
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorVal }
                                .testTag("color_picker_$colorVal"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isColorSelected) {
                                Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Scheduled Notification Reminder
                Text(
                    text = "وقت الإشعار والتذكير",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    reminderOptions.take(3).forEach { (minutes, label) ->
                        val isReminderSelected = selectedReminder == minutes
                        FilterChip(
                            selected = isReminderSelected,
                            onClick = { selectedReminder = minutes },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Actions buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                Toast.makeText(context, "الرجاء إدخال عنوان للفعالية", Toast.LENGTH_SHORT).show()
                            } else {
                                onSave(title, description, startHour, startMinute, endHour, endMinute, selectedColor, selectedReminder)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_event_submit"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0056D2)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حفظ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
