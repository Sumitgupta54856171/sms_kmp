package com.example.schoolmanagement.presentation.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.schoolmanagement.api.models.AttendanceTrendItem

@Composable
fun AreaChart(
    data: List<AttendanceTrendItem>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxVal = data.maxOf { it.total }.coerceAtLeast(1)

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1)

        val path = Path()
        val fillPath = Path()

        data.forEachIndexed { i, item ->
            val x = i * stepX
            val y = height - (item.present.toFloat() / maxVal) * height

            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            
            if (i == data.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        // Draw Area Fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF10B981).copy(alpha = 0.3f), Color.Transparent)
            )
        )

        // Draw Line
        drawPath(
            path = path,
            color = Color(0xFF10B981),
            style = Stroke(width = 3.dp.toPx())
        )
    }
}
