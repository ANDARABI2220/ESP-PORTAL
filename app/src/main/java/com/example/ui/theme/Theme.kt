package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GeminiBlue,
    onPrimary = Color.White,
    primaryContainer = GeminiSurfaceVariant,
    onPrimaryContainer = GeminiTextPrimary,
    secondary = GeminiPurple,
    onSecondary = Color.White,
    secondaryContainer = GeminiSurfaceVariant,
    onSecondaryContainer = GeminiTextPrimary,
    tertiary = GeminiCoral,
    onTertiary = Color.White,
    background = GeminiBackground,
    onBackground = GeminiTextPrimary,
    surface = GeminiSurface,
    onSurface = GeminiTextPrimary,
    surfaceVariant = GeminiSurfaceVariant,
    onSurfaceVariant = GeminiTextSecondary,
    outline = GeminiBorder
)

private val LightColorScheme = lightColorScheme(
    primary = GeminiBlue,
    onPrimary = Color.White,
    primaryContainer = GeminiLightSurfaceVariant,
    onPrimaryContainer = GeminiLightTextPrimary,
    secondary = GeminiPurple,
    onSecondary = Color.White,
    background = GeminiLightBackground,
    onBackground = GeminiLightTextPrimary,
    surface = GeminiLightSurface,
    onSurface = GeminiLightTextPrimary,
    surfaceVariant = GeminiLightSurfaceVariant,
    onSurfaceVariant = GeminiLightTextSecondary,
    outline = GeminiLightBorder
)

@Composable
fun EspManagerTheme(
    darkTheme: Boolean = true, // Default to Gemini obsidian dark mode
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
