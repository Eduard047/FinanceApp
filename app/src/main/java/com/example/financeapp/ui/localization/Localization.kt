package com.example.financeapp.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage {
    UK,
    EN
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.UK }

@Composable
fun tr(uk: String, en: String): String {
    return when (LocalAppLanguage.current) {
        AppLanguage.UK -> uk
        AppLanguage.EN -> en
    }
}
