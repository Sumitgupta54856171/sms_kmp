package com.example.schoolmanagement.presentation.dashboard.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.schoolmanagement.api.models.AttendanceTrendItem

import androidx.compose.ui.graphics.SolidColor
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.Line

@Composable
fun AreaChart(
    data: List<AttendanceTrendItem>,
    modifier: Modifier = Modifier,
    presentColor: Color = Color(0xFF10B981),
    absentColor: Color = Color(0xFFF43F5E)
) {
    if (data.isEmpty()) {
        Box(modifier = modifier.height(200.dp), contentAlignment = Alignment.Center) {
            Text("No trend data", color = Color.Gray)
        }
        return
    }

    LineChart(
        modifier = modifier.fillMaxWidth().height(250.dp),
        data = listOf(
            Line(
                label = "Present",
                values = data.map { it.present.toDouble() },
                color = SolidColor(presentColor),
                firstGradientFillColor = presentColor.copy(alpha = 0.3f),
                secondGradientFillColor = Color.Transparent,
                curvedEdges = true
            ),
            Line(
                label = "Absent",
                values = data.map { it.absent.toDouble() },
                color = SolidColor(absentColor),
                firstGradientFillColor = absentColor.copy(alpha = 0.2f),
                secondGradientFillColor = Color.Transparent,
                curvedEdges = true
            )
        ),
        animationDelay = 300
    )
}
