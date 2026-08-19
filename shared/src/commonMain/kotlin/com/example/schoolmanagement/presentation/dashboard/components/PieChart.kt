package com.example.schoolmanagement.presentation.dashboard.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie

@Composable
fun DonutChart(
    male: Int,
    female: Int,
    modifier: Modifier = Modifier
) {
    val total = (male + female).coerceAtLeast(1)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        PieChart(
            modifier = Modifier.size(220.dp),
            data = listOf(
                Pie(
                    label = "Male",
                    data = male.toDouble(),
                    color = Color(0xFF3B82F6),
                    selectedColor = Color(0xFF2563EB)
                ),
                Pie(
                    label = "Female",
                    data = female.toDouble(),
                    color = Color(0xFFF43F5E),
                    selectedColor = Color(0xFFE11D48)
                )
            ),
            style = Pie.Style.Stroke(width = 28.dp),
            selectedScale = 1.05f
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = total.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E293B)
            )
            Text(
                text = "Total Students",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
