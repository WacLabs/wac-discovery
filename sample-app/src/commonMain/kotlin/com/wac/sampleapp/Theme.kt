package com.wac.sampleapp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF82B1FF),
    onPrimary = Color(0xFF002F6C),
    primaryContainer = Color(0xFF004BA0),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFF80CBC4),
    onSecondary = Color(0xFF00332E),
    secondaryContainer = Color(0xFF004D44),
    onSecondaryContainer = Color(0xFFA7F3EC),
    tertiary = Color(0xFFFFAB91),
    onTertiary = Color(0xFF5D1800),
    tertiaryContainer = Color(0xFF832600),
    onTertiaryContainer = Color(0xFFFFDBCF),
    background = Color(0xFF0F1318),
    onBackground = Color(0xFFE1E2E8),
    surface = Color(0xFF161A21),
    onSurface = Color(0xFFE1E2E8),
    surfaceVariant = Color(0xFF1E2530),
    onSurfaceVariant = Color(0xFFC0C7D4),
    outline = Color(0xFF8A919E),
    error = Color(0xFFFF8A80),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3E),
    secondary = Color(0xFF00897B),
    onSecondary = Color.White,
    background = Color(0xFFF8F9FE),
    onBackground = Color(0xFF1A1C20),
    surface = Color.White,
    onSurface = Color(0xFF1A1C20),
    surfaceVariant = Color(0xFFE8ECF4),
    onSurfaceVariant = Color(0xFF44474E),
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
