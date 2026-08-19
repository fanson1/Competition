package com.example.competition.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ========================================================================
// Legacy constants — kept for compatibility with existing usages.
// ========================================================================
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
val Success = Color(0xFF2BD47C)
val Danger = Color(0xFFFF5B7A)
val Warning = Color(0xFFFFB74D)
val Info = Color(0xFF5AA9FF)
val VioletAccent = Color(0xFF9E7BFF)

// ========================================================================
// Brand palette — refined gold-on-midnight "trivia arena" identity.
// ========================================================================
object QuizPalette {
    // Scene background (deep night blue)
    val NightDeep = Color(0xFF060A1C)
    val Night = Color(0xFF0A1230)
    val NightMid = Color(0xFF0F1D45)
    val NightSoft = Color(0xFF16294F)

    // Glass surfaces
    val Glass = Color(0x12FFFFFF)
    val GlassStrong = Color(0x1FFFFFFF)
    val GlassBorder = Color(0x26FFFFFF)

    // Primary CTA gradient gold
    val GoldPeak = Color(0xFFFFE08A)
    val Gold = Color(0xFFFFCF4D)
    val GoldDeep = Color(0xFFE9A13B)

    // Semantic accents
    val Success = Color(0xFF37D67A)
    val Danger = Color(0xFFFF5B6C)
    val Warning = Color(0xFFFFB365)
    val Info = Color(0xFF6BB4FF)
    val Violet = Color(0xFFA48BFF)

    val TextPrimary = Color(0xFFF5F7FF)
    val TextSecondary = Color(0xFFB8C2E0)
    val TextMuted = Color(0xFF7C87A8)
}

// ========================================================================
// Spacing / radii / typography tokens
// ========================================================================
object QuizSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
}

object QuizRadii {
    val sm = RoundedCornerShape(10.dp)
    val md = RoundedCornerShape(14.dp)
    val lg = RoundedCornerShape(18.dp)
    val xl = RoundedCornerShape(24.dp)
    val pill = RoundedCornerShape(50)
}

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 52.sp,
        lineHeight = 56.sp,
        letterSpacing = 1.sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.5.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.3.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 0.6.sp
    )
)

private val LightColorScheme = lightColorScheme(
    primary = Gold,
    secondary = Purple,
    tertiary = LightBlue,
    background = SurfaceLight,
    surface = CardLight,
    onPrimary = DeepBlue,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    error = BrightRed,
    errorContainer = Color(0xFFFFDAD6),
)

private val DarkColorScheme = darkColorScheme(
    primary = QuizPalette.Gold,
    onPrimary = QuizPalette.NightDeep,
    secondary = QuizPalette.Violet,
    onSecondary = Color.White,
    tertiary = Info,
    background = QuizPalette.NightDeep,
    surface = QuizPalette.NightMid,
    onBackground = QuizPalette.TextPrimary,
    onSurface = QuizPalette.TextPrimary,
    error = Danger,
    errorContainer = Color(0xFF5A1B2B),
)

@Composable
fun QuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}