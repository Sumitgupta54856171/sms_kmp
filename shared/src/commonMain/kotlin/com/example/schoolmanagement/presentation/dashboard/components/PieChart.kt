package com.example.schoolmanagement.presentation.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun DonutChart(
    male: Int,
    female: Int,
    modifier: Modifier = Modifier
) {
    val total = (male + female).coerceAtLeast(1)
    val maleAngle = (male.toFloat() / total) * 360f
    val femaleAngle = (female.toFloat() / total) * 360f

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(150.dp)) {
            // Draw Female slice (Rose)
            drawArc(
                color = Color(0xFFF43F5E),
                startAngle = -90f,
                sweepAngle = femaleAngle,
                useCenter = false,
                style = Stroke(width = 30.dp.toPx())
            )
            // Draw Male slice (Blue)
            drawArc(
                color = Color(0xFF3B82F6),
                startAngle = -90f + femaleAngle,
                sweepAngle = maleAngle,
                useCenter = false,
                style = Stroke(width = 30.dp.toPx())
            )
        }
    }
}
