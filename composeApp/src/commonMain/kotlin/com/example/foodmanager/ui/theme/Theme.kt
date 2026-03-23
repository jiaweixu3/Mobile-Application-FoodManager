package com.example.foodmanager.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme

// This defines the colors that will be used in the app.
private val  LightColorScheme = lightColorScheme(
    primary = Blue,
    onPrimary = White,
    secondary = LightBlue,
    onSecondary = DarkBlue,
    tertiary = DarkBlue,
    background = CardGray,
    surface = White,
    surfaceVariant = CardGray,
    error = ExpRed
)

@Composable
fun FoodManagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content, // This is done so the content of the app receives the desired color format
        typography = Typography
    )
}