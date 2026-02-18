package com.example.financeapp.domain

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

private val hryvniaLocale = Locale.forLanguageTag("uk-UA")
private const val DISPLAY_DATE_PATTERN = "dd.MM.yyyy"

fun formatCurrency(value: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(hryvniaLocale).apply {
        currency = Currency.getInstance("UAH")
    }
    return formatter.format(value)
}

fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat(DISPLAY_DATE_PATTERN, hryvniaLocale)
    return formatter.format(Date(timestamp))
}

fun parseDateInput(text: String): Long? {
    if (text.isBlank()) {
        return null
    }

    val parser = SimpleDateFormat(DISPLAY_DATE_PATTERN, hryvniaLocale).apply {
        isLenient = false
    }

    return runCatching { parser.parse(text)?.time }.getOrNull()
}
