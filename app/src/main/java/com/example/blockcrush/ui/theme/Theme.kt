package com.example.blockcrush.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DarkWalnut,
    onPrimary = Color.White,
    background = Sand,
    surface = Color.White,
    onSurface = Color(0xFF2B2B2B),
    secondary = Walnut,
    onSecondary = Color.White
)

@Composable
fun BlockCrushTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
