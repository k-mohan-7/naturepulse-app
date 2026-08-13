package com.simats.naturepulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.simats.naturepulse.ui.theme.BackgroundDark
import com.simats.naturepulse.ui.theme.ForestGreen
import com.simats.naturepulse.ui.theme.ForestGreenDark

/**
 * Cohesive top header gradient used across all tabs:
 * Dark Green starting at status bar -> Rich Forest Green -> App Background at card bottom.
 */
@Composable
fun TopHeaderGradient(
    modifier: Modifier = Modifier,
    heightDp: Int = 180,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ForestGreenDark,
                        ForestGreen,
                        BackgroundDark
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
