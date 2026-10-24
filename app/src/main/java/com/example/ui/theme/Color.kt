package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Gemini Palette
val GeminiBackground = Color(0xFF131314)
val GeminiSurface = Color(0xFF1E1F20)
val GeminiSurfaceVariant = Color(0xFF282A2C)
val GeminiBorder = Color(0xFF3C4043)
val GeminiBorderLight = Color(0xFF4A4E52)

val GeminiTextPrimary = Color(0xFFE3E3E3)
val GeminiTextSecondary = Color(0xFF9AA0A6)
val GeminiTextTertiary = Color(0xFF757B82)

val GeminiBlue = Color(0xFF4E88F5)
val GeminiPurple = Color(0xFF9B72CB)
val GeminiCoral = Color(0xFFD96570)
val GeminiGreen = Color(0xFF34A853)
val GeminiYellow = Color(0xFFFBBC04)
val GeminiRed = Color(0xFFEA4335)

// Light theme counterparts
val GeminiLightBackground = Color(0xFFF0F4F9)
val GeminiLightSurface = Color(0xFFFFFFFF)
val GeminiLightSurfaceVariant = Color(0xFFE1E5EA)
val GeminiLightBorder = Color(0xFFD0D5DD)
val GeminiLightTextPrimary = Color(0xFF1F1F1F)
val GeminiLightTextSecondary = Color(0xFF444746)

val GeminiSparkleBrush = Brush.linearGradient(
    colors = listOf(GeminiBlue, GeminiPurple, GeminiCoral)
)

val GeminiThinkingBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF8AB4F8), Color(0xFFC58AF9), Color(0xFFF28B82))
)
