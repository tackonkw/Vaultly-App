package com.project.vaultly.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object VaultlyColors {
    val MidnightBlue = Color(0xFF050C1A)
    val ElectricCyan = Color(0xFF00CFE8)
    val AccentSoft = Color(0xFFDDFBFF)
    val AppBackground = Color(0xFFF6F8FB)
    val Surface = Color.White
    val SurfaceMuted = Color(0xFFEEF3F8)
    val TextPrimary = Color(0xFF111827)
    val TextMuted = Color(0xFF667085)
    val Border = Color(0xFFD8DEE8)
    val Income = Color(0xFF0891B2)
    val Expense = Color(0xFFDC2626)
    val Warning = Color(0xFFD97706)
    val Success = Color(0xFF16A34A)
}

private val VaultlyColorScheme = lightColorScheme(
    primary = VaultlyColors.MidnightBlue,
    onPrimary = VaultlyColors.Surface,
    primaryContainer = VaultlyColors.AccentSoft,
    onPrimaryContainer = VaultlyColors.MidnightBlue,
    secondary = VaultlyColors.Income,
    onSecondary = VaultlyColors.Surface,
    secondaryContainer = VaultlyColors.AccentSoft,
    onSecondaryContainer = VaultlyColors.MidnightBlue,
    tertiary = VaultlyColors.Warning,
    onTertiary = VaultlyColors.Surface,
    background = VaultlyColors.AppBackground,
    onBackground = VaultlyColors.TextPrimary,
    surface = VaultlyColors.Surface,
    onSurface = VaultlyColors.TextPrimary,
    surfaceVariant = VaultlyColors.SurfaceMuted,
    onSurfaceVariant = VaultlyColors.TextMuted,
    outline = VaultlyColors.Border,
    error = VaultlyColors.Expense,
    onError = VaultlyColors.Surface,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D)
)

private val VaultlyShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(12.dp)
)

@Composable
fun VaultlyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VaultlyColorScheme,
        shapes = VaultlyShapes,
        content = content
    )
}
