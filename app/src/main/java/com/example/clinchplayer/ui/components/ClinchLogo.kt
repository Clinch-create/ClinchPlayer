package com.example.clinchplayer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.clinchplayer.R

@Composable
fun ClinchLogo(
    modifier: Modifier = Modifier,
    height: Dp = 65.dp
) {
    Image(
        painter = painterResource(
            id = R.drawable.clinch_player_logo
        ),
        contentDescription = "Clinch Player",
        contentScale = ContentScale.Fit,
        modifier = modifier.height(height)
    )
}