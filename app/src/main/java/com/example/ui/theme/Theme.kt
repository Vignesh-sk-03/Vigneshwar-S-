package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFD0E4FF), // Core elegant light blue
    onPrimary = androidx.compose.ui.graphics.Color(0xFF00315B), // Core text inside primary
    secondary = CricGold,
    onSecondary = androidx.compose.ui.graphics.Color(0xFF1A1C1E),
    tertiary = CricRed, // Coral Red / Live Pink-Red
    background = DarkBackground, // #1A1C1E
    onBackground = DarkTextPrimary, // #E2E2E6
    surface = DarkSurface, // #2F3033
    onSurface = DarkTextPrimary, // #E2E2E6
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF212428), // #212428 (Bottom Navigation & secondary containers)
    onSurfaceVariant = DarkTextSecondary // #C4C6CF
)

private val LightColorScheme = lightColorScheme(
    primary = CricGreen,
    onPrimary = LightBackground,
    secondary = CricGold,
    onSecondary = LightTextPrimary,
    tertiary = CricRed,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to preserve brand colors
    content: @Composable () -> Unit,
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
