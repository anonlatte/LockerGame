package com.example.lockergame.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val LockerColors = Colors(
    primary = Color(0xFFC9CED6),
    primaryVariant = Color(0xFF9099A6),
    secondary = Color(0xFFD4A63B),
    secondaryVariant = Color(0xFF8E6A12),
    background = Color(0xFF060708),
    surface = Color(0xFF111317),
    error = Color(0xFFFF6B6B),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFFF3F4F6),
    onSurface = Color(0xFFF3F4F6),
    onError = Color.Black,
)

@Composable
fun LockerGameTheme(content: @Composable () -> Unit) {
    MaterialTheme(colors = LockerColors, content = content)
}
