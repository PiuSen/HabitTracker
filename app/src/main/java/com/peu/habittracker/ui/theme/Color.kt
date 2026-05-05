package com.peu.habittracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ═══ Brand Colors with Gradients ═══════════════════════════════════════════

// Primary Gradient (Purple/Violet)
val GradientPrimaryStart = Color(0xFF8B5CF6)  // Vibrant Purple
val GradientPrimaryEnd = Color(0xFF6366F1)    // Indigo
val GradientPrimary = Brush.horizontalGradient(
    listOf(GradientPrimaryStart, GradientPrimaryEnd)
)

// Success Gradient (Green)
val GradientSuccessStart = Color(0xFF10B981)
val GradientSuccessEnd = Color(0xFF059669)
val GradientSuccess = Brush.horizontalGradient(
    listOf(GradientSuccessStart, GradientSuccessEnd)
)

// Warning Gradient (Orange/Amber)
val GradientWarningStart = Color(0xFFF59E0B)
val GradientWarningEnd = Color(0xFFEF4444)
val GradientWarning = Brush.horizontalGradient(
    listOf(GradientWarningStart, GradientWarningEnd)
)

// Accent Gradient (Pink/Rose)
val GradientAccentStart = Color(0xFFEC4899)
val GradientAccentEnd = Color(0xFFF43F5E)

// Light Theme Colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF8B5CF6)  // Updated for vibrancy
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFFEC4899)

// Dark Theme Colors
val PurpleDark = Color(0xFFA78BFA)
val PinkDark = Color(0xFFF9A8D4)
val GreenDark = Color(0xFF6EE7B7)

// Background Gradients
val BackgroundGradientLight = Brush.verticalGradient(
    listOf(
        Color(0xFFFAFAFC),
        Color(0xFFF3F4F6)
    )
)

val BackgroundGradientDark = Brush.verticalGradient(
    listOf(
        Color(0xFF0F0F14),
        Color(0xFF1A1A24)
    )
)

// Surface Colors
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E1E2D)

val SurfaceVariantLight = Color(0xFFF3F4F6)
val SurfaceVariantDark = Color(0xFF2D2D3F)

// Text Colors
val TextPrimaryLight = Color(0xFF111827)
val TextPrimaryDark = Color(0xFFF9FAFB)
val TextSecondaryLight = Color(0xFF6B7280)
val TextSecondaryDark = Color(0xFF9CA3AF)

// ═══ Color Schemes ═══════════════════════════════════════════════════════

private val LightColorScheme = lightColorScheme(
    primary = GradientPrimaryStart,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF4C1D95),

    secondary = Pink40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCE7F3),
    onSecondaryContainer = Color(0xFF9F1239),

    tertiary = GradientSuccessStart,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),

    error = Color(0xFFEF4444),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),

    background = Color(0xFFFAFAFC),
    onBackground = TextPrimaryLight,

    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,

    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB)
)

private val DarkColorScheme = darkColorScheme(
    primary = PurpleDark,
    onPrimary = Color(0xFF1F2937),
    primaryContainer = Color(0xFF4C1D95),
    onPrimaryContainer = Color(0xFFEDE9FE),

    secondary = PinkDark,
    onSecondary = Color(0xFF1F2937),
    secondaryContainer = Color(0xFF9F1239),
    onSecondaryContainer = Color(0xFFFCE7F3),

    tertiary = GreenDark,
    onTertiary = Color(0xFF1F2937),
    tertiaryContainer = Color(0xFF065F46),
    onTertiaryContainer = Color(0xFFD1FAE5),

    error = Color(0xFFFCA5A5),
    onError = Color(0xFF1F2937),
    errorContainer = Color(0xFF991B1B),
    onErrorContainer = Color(0xFFFEE2E2),

    background = Color(0xFF0F0F14),
    onBackground = TextPrimaryDark,

    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,

    outline = Color(0xFF4B5563),
    outlineVariant = Color(0xFF374151)
)

@Composable
fun HabitTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}