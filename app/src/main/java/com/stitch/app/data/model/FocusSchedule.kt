package com.stitch.app.data.model

import java.util.Calendar
import java.util.Locale

data class FocusSchedule(
    val id: String = "",
    val uid: String = "",
    val title: String = "Focus Block",
    val category: String = "Study", // "Study" | "Deep Work" | "Sleep" | "Custom"
    val blockedApps: List<String> = emptyList(),
    val blockedAppNames: List<String> = emptyList(),
    val startHour: Int = 14,
    val startMinute: Int = 0,
    val endHour: Int = 16,
    val endMinute: Int = 0,
    val daysOfWeek: List<Int> = listOf(2, 3, 4, 5, 6), // Mon-Fri
    val isAutoScheduled: Boolean = false,
    val autoScheduleReason: String = "",
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isCurrentlyActive(): Boolean {
        if (!isEnabled) return false
        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_WEEK)
        if (!daysOfWeek.contains(currentDay)) return false

        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val startTotalMinutes = startHour * 60 + startMinute
        val endTotalMinutes = endHour * 60 + endMinute

        return if (endTotalMinutes >= startTotalMinutes) {
            currentMinutes in startTotalMinutes..endTotalMinutes
        } else {
            // Crosses midnight (e.g., 23:00 to 07:00)
            currentMinutes >= startTotalMinutes || currentMinutes <= endTotalMinutes
        }
    }

    fun formattedTimeRange(): String {
        return String.format(Locale.getDefault(), "%02d:%02d – %02d:%02d", startHour, startMinute, endHour, endMinute)
    }

    fun formattedDays(): String {
        if (daysOfWeek.size == 7) return "Every day"
        if (daysOfWeek.containsAll(listOf(2, 3, 4, 5, 6)) && daysOfWeek.size == 5) return "Weekdays"
        if (daysOfWeek.containsAll(listOf(1, 7)) && daysOfWeek.size == 2) return "Weekends"

        val dayNames = mapOf(
            1 to "Sun", 2 to "Mon", 3 to "Tue", 4 to "Wed",
            5 to "Thu", 6 to "Fri", 7 to "Sat"
        )
        return daysOfWeek.sorted().mapNotNull { dayNames[it] }.joinToString(", ")
    }
}
