package com.example.doomscroller.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Authentic Bauhaus Palette
private val BauhausColorScheme = lightColorScheme(
    primary = BauhausBlue,
    onPrimary = Color.White,
    primaryContainer = BauhausMuted,
    onPrimaryContainer = BauhausBlack,
    secondary = BauhausRed,
    onSecondary = Color.White,
    secondaryContainer = BauhausMuted,
    onSecondaryContainer = BauhausBlack,
    tertiary = BauhausYellow,
    onTertiary = BauhausBlack,
    background = BauhausCanvas,
    onBackground = BauhausBlack,
    surface = BauhausCanvas,
    onSurface = BauhausBlack,
    surfaceVariant = BauhausMuted,
    onSurfaceVariant = BauhausBlack,
    error = BauhausRed,
    onError = Color.White,
    errorContainer = BauhausRed.copy(alpha = 0.15f),
    onErrorContainer = BauhausRed,
    outline = BauhausBlack
)

private val BauhausDarkColorScheme = darkColorScheme(
    primary = BauhausYellow,
    onPrimary = BauhausBlack,
    primaryContainer = Color(0xFF262626),
    onPrimaryContainer = Color.White,
    secondary = BauhausRed,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF262626),
    onSecondaryContainer = Color.White,
    tertiary = BauhausBlue,
    onTertiary = Color.White,
    background = Color(0xFF141414),
    onBackground = BauhausCanvas,
    surface = Color(0xFF1C1C1C),
    onSurface = BauhausCanvas,
    surfaceVariant = Color(0xFF282828),
    onSurfaceVariant = BauhausCanvas,
    error = BauhausRed,
    onError = Color.White,
    errorContainer = BauhausRed.copy(alpha = 0.25f),
    onErrorContainer = Color.White,
    outline = BauhausCanvas
)

@Composable
fun DoomscrollerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BauhausDarkColorScheme else BauhausColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}