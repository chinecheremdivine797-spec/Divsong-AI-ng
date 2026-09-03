package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DivColorScheme = darkColorScheme(
    primary = DivPurpleLight,
    onPrimary = Color.White,
    primaryContainer = DivPurpleDark,
    onPrimaryContainer = Color.White,
    secondary = DivCyan,
    onSecondary = Color.Black,
    secondaryContainer = DivCyanDark,
    onSecondaryContainer = Color.White,
    tertiary = DivPink,
    onTertiary = Color.White,
    background = DivBackground,
    onBackground = DivTextPrimary,
    surface = DivSurfaceDark,
    onSurface = DivTextPrimary,
    surfaceVariant = DivCard,
    onSurfaceVariant = DivTextSecondary,
    outline = DivBorder,
    error = DivError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // DIV SONG AI is engineered as a signature premium dark studio
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DivColorScheme,
        typography = Typography,
        content = content
    )
}
