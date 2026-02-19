package com.example.financeapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Emerald80,
    onPrimary = Color(0xFF00373A),
    primaryContainer = Color(0xFF005A5D),
    onPrimaryContainer = Color(0xFFA5F5F4),
    secondary = Color(0xFF9BCBFF),
    onSecondary = Color(0xFF00315E),
    secondaryContainer = Color(0xFF004883),
    onSecondaryContainer = Color(0xFFD4E7FF),
    tertiary = Color(0xFFFFB870),
    onTertiary = Color(0xFF4C2800),
    tertiaryContainer = Color(0xFF703A00),
    onTertiaryContainer = Color(0xFFFFDDB4),
    error = Coral,
    background = Color(0xFF09171F),
    onBackground = Color(0xFFD6E6ED),
    surface = Color(0xFF10222B),
    onSurface = InkDark,
    surfaceVariant = Color(0xFF203945),
    onSurfaceVariant = Color(0xFFB6CDD8),
    outline = Color(0xFF5F7A86)
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7F1F0),
    onPrimaryContainer = Color(0xFF003739),
    secondary = OceanBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4E7FF),
    onSecondaryContainer = Color(0xFF062B54),
    tertiary = AmberSun,
    onTertiary = Color(0xFF422300),
    tertiaryContainer = Color(0xFFFFE1B5),
    onTertiaryContainer = Color(0xFF2B1800),
    error = Coral,
    background = SlateLight,
    onBackground = InkLight,
    surface = Color(0xFFF8FCFD),
    onSurface = InkLight,
    surfaceVariant = SlateMid,
    onSurfaceVariant = Color(0xFF415963),
    outline = Color(0xFF6C828D)
)

@Composable
fun FinanceAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = FinanceShapes,
        content = content
    )
}
