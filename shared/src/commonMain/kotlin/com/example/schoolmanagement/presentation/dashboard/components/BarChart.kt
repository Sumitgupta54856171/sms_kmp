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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolmanagement.api.models.EnrollmentChartItem

import androidx.compose.ui.graphics.SolidColor
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.Bars

@Composable
fun BarChart(
    data: List<EnrollmentChartItem>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF0D9488)
) {
    if (data.isEmpty()) {
        Box(modifier = modifier.height(200.dp), contentAlignment = Alignment.Center) {
            Text("No data available", color = Color.Gray)
        }
        return
    }

    ColumnChart(
        modifier = modifier.fillMaxWidth().height(250.dp),
        data = data.map { item ->
            Bars(
                label = item.name.replace("Class ", ""),
                values = listOf(
                    Bars.Data(
                        value = item.count.toDouble(),
                        color = SolidColor(barColor)
                    )
                )
            )
        },
        animationSpec = tween(durationMillis = 1000)
    )
}
