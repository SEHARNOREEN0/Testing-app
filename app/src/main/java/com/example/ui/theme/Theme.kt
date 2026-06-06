package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LuminaPrimary,
    secondary = LuminaSecondary,
    tertiary = LuminaAccent,
    background = LuminaBackground,
    surface = LuminaSurface,
    onBackground = LuminaText,
    onSurface = LuminaText,
    primaryContainer = LuminaCard,
    onPrimaryContainer = LuminaText,
    error = LuminaDanger
)

private val LightColorScheme = lightColorScheme(
    primary = LuminaSecondary,
    secondary = LuminaPrimary,
    tertiary = LuminaAccent,
    background = LuminaBackground,
    surface = LuminaSurface,
    onBackground = LuminaText,
    onSurface = LuminaText,
    primaryContainer = LuminaCard,
    onPrimaryContainer = LuminaText,
    error = LuminaDanger
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ (let's disable by default to preserve Lumina's beautiful space-dark design)
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // Force dark-premium color scheme for the cinematic experience
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
