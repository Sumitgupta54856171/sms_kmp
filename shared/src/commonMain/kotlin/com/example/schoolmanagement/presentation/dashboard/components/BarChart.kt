package com.example.schoolmanagement.presentation.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolmanagement.api.models.EnrollmentChartItem

@Composable
fun BarChart(
    data: List<EnrollmentChartItem>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF0D9488)
) {
    if (data.isEmpty()) return

    val maxCount = data.maxOf { it.count }.coerceAtLeast(1)

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barWidth = (canvasWidth / data.size) * 0.6f
            val spacing = (canvasWidth / data.size) * 0.4f

            data.forEachIndexed { index, item ->
                val barHeight = (item.count.toFloat() / maxCount) * canvasHeight
                val x = (index * (barWidth + spacing)) + (spacing / 2)
                val y = canvasHeight - barHeight

                drawRect(
                    color = barColor.copy(alpha = 0.8f),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight)
                )
                
                // Optional: Draw count on top
                // (Need native text drawing for Canvas which is complex in KMP, 
                // so we'll just use Row labels below)
            }
        }
        
        // X-Axis Labels
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { item ->
                Text(
                    text = item.name.replace("Class ", ""),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.width(20.dp),
                    maxLines = 1
                )
            }
        }
    }
}
