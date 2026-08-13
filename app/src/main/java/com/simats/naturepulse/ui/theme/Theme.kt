package com.simats.naturepulse.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = SurfaceVariantDark,
    onPrimaryContainer = ForestGreenDark,
    secondary = WarmGold,
    onSecondary = Color.White,
    secondaryContainer = LightGold,
    onSecondaryContainer = WarmGold,
    tertiary = ForestGreenLight,
    onTertiary = Color.White,
    background = BackgroundDark,
    onBackground = OnSurface,
    surface = SurfaceDark,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceMuted,
    outline = Color(0xFFD4DEC9),
    outlineVariant = Color(0xFFE2EBE2),
    error = ErrorRed,
    onError = Color.White,
    scrim = Color(0x66000000)
)

@Composable
fun NaturePulseTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = NaturePulseTypography,
        content = content
    )
}

@Composable
fun npOutlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ForestGreen,
    unfocusedBorderColor = Color(0xFFD4DEC9),
    focusedLabelColor = ForestGreen,
    unfocusedLabelColor = OnSurfaceMuted,
    cursorColor = ForestGreen,
    focusedTextColor = OnSurface,
    unfocusedTextColor = OnSurface,
    focusedContainerColor = SurfaceDark,
    unfocusedContainerColor = SurfaceDark
)