package com.example.competition.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Gold = Color(0xFFFFD700)
val DarkGold = Color(0xFFB8860B)
val BrightRed = Color(0xFFE53935)
val CorrectGreen = Color(0xFF43A047)
val WrongRed = Color(0xFFD32F2F)
val TimerOrange = Color(0xFFFF9800)
val DeepBlue = Color(0xFF1A237E)
val LightBlue = Color(0xFF42A5F5)
val Purple = Color(0xFF7B1FA2)
val SurfaceLight = Color(0xFFF5F5F5)
val SurfaceDark = Color(0xFF121212)
val CardLight = Color(0xFFFFFFFF)
val CardDark = Color(0xFF1E1E1E)
val OptionDefault = Color(0xFFE3F2FD)
val OptionDefaultDark = Color(0xFF1A2744)
val OptionHover = Color(0xFFBBDEFB)
val OptionHoverDark = Color(0xFF253D6B)

private val LightColorScheme = lightColorScheme(
    primary = DeepBlue,
    secondary = Purple,
    tertiary = LightBlue,
    background = SurfaceLight,
    surface = CardLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    error = BrightRed,
    errorContainer = Color(0xFFFFDAD6),
)

private val DarkColorScheme = darkColorScheme(
    primary = LightBlue,
    secondary = Purple,
    tertiary = Gold,
    background = SurfaceDark,
    surface = CardDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    error = WrongRed,
    errorContainer = Color(0xFF8C1D18),
)

@Composable
fun QuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
