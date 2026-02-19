package com.example.financeapp.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthRange(
    val start: Long,
    val end: Long
)

data class MonthSelection(
    val year: Int,
    val month: Int
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

fun currentMonthSelection(nowMillis: Long = System.currentTimeMillis()): MonthSelection {
    val calendar = Calendar.getInstance().apply { timeInMillis = nowMillis }
    return MonthSelection(
        year = calendar.get(Calendar.YEAR),
        month = calendar.get(Calendar.MONTH)
    )
}

fun monthRange(selection: MonthSelection): MonthRange {
    val startCalendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, selection.year)
        set(Calendar.MONTH, selection.month)
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

fun shiftMonth(selection: MonthSelection, offset: Int): MonthSelection {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, selection.year)
        set(Calendar.MONTH, selection.month)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    calendar.add(Calendar.MONTH, offset)
    return MonthSelection(
        year = calendar.get(Calendar.YEAR),
        month = calendar.get(Calendar.MONTH)
    )
}

fun formatMonthYear(selection: MonthSelection, locale: Locale): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, selection.year)
        set(Calendar.MONTH, selection.month)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val formatter = SimpleDateFormat("LLLL yyyy", locale)
    val raw = formatter.format(Date(calendar.timeInMillis))
    return raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}
