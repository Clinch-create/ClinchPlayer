package com.example.clinchplayer.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ClinchPlayerTheme(
    content: @Composable () -> Unit,
) {
    val colorScheme = darkColorScheme(
        primary = SelectionYellow,
        onPrimary = Color.Black,
        background = BackgroundDark,
        onBackground = TextWhite,
        surface = BackgroundDark,
        onSurface = TextWhite,
        secondary = TextGray,
        onSecondary = Color.Black
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
