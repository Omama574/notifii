package com.notifii.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifii.app.data.Rung

// Dark-first, near-black canvas, electric cyan for "on". Each rung owns one colour and nothing else uses it.
private val Dark = darkColorScheme(
    primary = Color(0xFF4DE8FF), onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58), onPrimaryContainer = Color(0xFF9EEFFF),
    secondary = Color(0xFFB1CBD0), onSecondary = Color(0xFF1C3438),
    secondaryContainer = Color(0xFF263A3E), onSecondaryContainer = Color(0xFFCDE7EC),
    tertiary = Color(0xFFC3B5FF), onTertiary = Color(0xFF2B1A73),
    error = Color(0xFFFF6B6B), onError = Color(0xFF5C0000),
    errorContainer = Color(0xFF5C1414), onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0B0B0C), onBackground = Color(0xFFF2F2F3),
    surface = Color(0xFF0B0B0C), onSurface = Color(0xFFF2F2F3),
    surfaceVariant = Color(0xFF2A2B2E), onSurfaceVariant = Color(0xFFA9ABB0),
    surfaceContainerLowest = Color(0xFF060607),
    surfaceContainerLow = Color(0xFF141416),
    surfaceContainer = Color(0xFF19191C),
    surfaceContainerHigh = Color(0xFF222226),
    surfaceContainerHighest = Color(0xFF2C2C31),
    outline = Color(0xFF6F7177), outlineVariant = Color(0xFF34353A),
)

private val Light = lightColorScheme(
    primary = Color(0xFF006874), onPrimary = Color.White,
    primaryContainer = Color(0xFF97F0FF), onPrimaryContainer = Color(0xFF001F24),
    secondary = Color(0xFF4A6267), onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDE7EC), onSecondaryContainer = Color(0xFF051F23),
    tertiary = Color(0xFF5B4CB0), onTertiary = Color.White,
    error = Color(0xFFBA1A1A), onError = Color.White,
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFAFBFC), onBackground = Color(0xFF191C1D),
    surface = Color(0xFFFAFBFC), onSurface = Color(0xFF191C1D),
    surfaceVariant = Color(0xFFDBE4E6), onSurfaceVariant = Color(0xFF3F484A),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF2F4F5),
    surfaceContainer = Color(0xFFECEEF0),
    surfaceContainerHigh = Color(0xFFE6E8EA),
    surfaceContainerHighest = Color(0xFFE0E3E4),
    outline = Color(0xFF6F797A), outlineVariant = Color(0xFFBFC8CA),
)

private val Base = Typography()
private val Type = Typography(
    displaySmall = Base.displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineLarge = Base.headlineLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
    headlineMedium = Base.headlineMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.25).sp),
    headlineSmall = Base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = Base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = Base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = Base.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = Base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = Base.bodyLarge.copy(lineHeight = 24.sp),
)

private val Shape = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun NotifiiTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) Dark else Light, typography = Type, shapes = Shape, content = content)
}

/** Rung colours read on both dark and light surfaces (≥3:1 for the dot/chip, text uses onSurface). */
@Composable
fun Rung.color(): Color {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return when (this) {
        Rung.SIREN -> if (dark) Color(0xFFFF5A5F) else Color(0xFFD32F2F)
        Rung.RING -> MaterialTheme.colorScheme.primary
        Rung.LATER -> if (dark) Color(0xFFB9A7FF) else Color(0xFF5B4CB0)
        Rung.HIDE -> if (dark) Color(0xFF8D8F95) else Color(0xFF6B6E73)
    }
}

val SirenRed = Color(0xFFFF4D4F)
