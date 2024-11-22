package com.unibague.pocketpal.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Colores para Light Mode
private val LightColors = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightTertiary,
    background = LightBackground,
    surface = LightBackground,
    onPrimary = Color.White,  // Texto sobre colores primarios
    onSecondary = Color.White,
    onBackground = LightTextPrimary,  // Texto sobre fondo
    onSurface = LightTextPrimary,  // Texto sobre superficie
    surfaceVariant = LightAccent,
    onSurfaceVariant = LightTextSecondary
)

// Colores para Dark Mode
private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    background = DarkBackground,
    surface = DarkBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkAccent,
    onSurfaceVariant = DarkTextSecondary
)

@Composable
fun PocketPalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (!darkTheme) LightColors else DarkColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content,
    )

}