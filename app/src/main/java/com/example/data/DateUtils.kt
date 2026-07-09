package com.example.data

import java.util.Calendar

object DateUtils {
    fun isToday(timestamp: Long): Boolean {
        if (timestamp <= 0L) return false
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isYesterday(timestamp: Long): Boolean {
        if (timestamp <= 0L) return false
        val cal1 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun getRemainingTimeUntilMidnight(): String {
        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 24)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMs = midnight.timeInMillis - now.timeInMillis
        val diffHours = diffMs / (1000 * 60 * 60)
        val diffMinutes = (diffMs / (1000 * 60)) % 60
        return if (diffHours > 0) {
            "${diffHours}h ${diffMinutes}m"
        } else {
            "${diffMinutes}m"
        }
    }
}
