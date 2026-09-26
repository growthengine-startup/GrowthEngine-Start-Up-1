package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GrowthEngineGold,
    onPrimary = Color(0xFF141414),
    primaryContainer = GrowthEngineGoldContainer,
    onPrimaryContainer = GrowthEngineGoldDark,
    secondary = DarkInk,
    onSecondary = Color.White,
    secondaryContainer = SurfaceSubtle,
    onSecondaryContainer = TextPrimary,
    tertiary = SuccessGreen,
    background = BackgroundWhite,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderLight,
    error = ErrorRed,
    errorContainer = ErrorRedContainer
)

@Composable
fun GrowthEngineTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    // User explicitly requested white background with correct match to screenshots
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
