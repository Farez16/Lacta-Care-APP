package com.example.lactacare.vistas.admin.reportes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

@Composable
fun SimpleBarChart(
    data: List<Pair<String, Float>>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val barWidth = size.width / (data.size * 2f)
        val space = size.width / data.size
        val maxVal = data.maxOfOrNull { it.second } ?: 1f
        val safeMax = if (maxVal == 0f) 1f else maxVal

        data.forEachIndexed { index, (label, value) ->
            val barHeight = (value / safeMax) * size.height
            val x = index * space + (space - barWidth) / 2
            val y = size.height - barHeight

            drawRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
fun SimplePieChart(
    data: List<Pair<String, Float>>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val total = data.sumOf { it.second.toDouble() }.toFloat()
        var startAngle = -90f
        
        // Evitar division por cero
        val safeTotal = if (total == 0f) 1f else total

        data.forEachIndexed { index, (_, value) ->
            val sweepAngle = (value / safeTotal) * 360f
            val color = colors.getOrElse(index) { Color.Gray }
            
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(size.minDimension, size.minDimension),
                topLeft = Offset(
                    (size.width - size.minDimension) / 2,
                    (size.height - size.minDimension) / 2
                )
            )
            startAngle += sweepAngle
        }
    }
}
