package com.example.financeapp.domain

import java.util.Calendar

data class MonthRange(
    val start: Long,
    val end: Long
)

fun currentMonthRange(nowMillis: Long = System.currentTimeMillis()): MonthRange {
    val startCalendar = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val endCalendar = (startCalendar.clone() as Calendar).apply {
        add(Calendar.MONTH, 1)
        add(Calendar.MILLISECOND, -1)
    }

    return MonthRange(
        start = startCalendar.timeInMillis,
        end = endCalendar.timeInMillis
    )
}
