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
    onPrimary = Color(0xFF003732),
    secondary = AmberSun,
    onSecondary = Color(0xFF3E2200),
    tertiary = Coral,
    background = Color(0xFF0D1A21),
    onBackground = InkDark,
    surface = Color(0xFF14232C),
    onSurface = InkDark,
    surfaceVariant = Color(0xFF253B47),
    onSurfaceVariant = Color(0xFFBCD0D8)
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald40,
    onPrimary = Color.White,
    secondary = OceanBlue,
    onSecondary = Color.White,
    tertiary = AmberSun,
    onTertiary = Color.White,
    background = SlateLight,
    onBackground = InkLight,
    surface = Color(0xFFF8FCFD),
    onSurface = InkLight,
    surfaceVariant = SlateMid,
    onSurfaceVariant = Color(0xFF425962)
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
        content = content
    )
}
