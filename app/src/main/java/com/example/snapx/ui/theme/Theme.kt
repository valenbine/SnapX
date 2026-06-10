package com.example.snapx.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color as ComposeColor

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = ComposeColor(0xFFFFFBFE),
    surface = ComposeColor(0xFFFFFBFE),
    onPrimary = ComposeColor.White,
    onSecondary = ComposeColor.White,
    onTertiary = ComposeColor.White,
    onBackground = ComposeColor(0xFF1C1B1F),
    onSurface = ComposeColor(0xFF1C1B1F)
)

@Composable
fun SnapXTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}