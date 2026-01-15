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
        
        val paint = android.graphics.Paint().apply {
            this.color = android.graphics.Color.DKGRAY
            this.textSize = 32f
            this.textAlign = android.graphics.Paint.Align.CENTER
        }
        
        // Reserve space for labels at the bottom
        val labelHeight = 50f
        val chartHeight = size.height - labelHeight

        val barWidth = size.width / (data.size * 2f)
        val space = size.width / data.size
        val maxVal = data.maxOfOrNull { it.second } ?: 1f
        val safeMax = if (maxVal == 0f) 1f else maxVal

        data.forEachIndexed { index, (label, value) ->
            val barHeight = (value / safeMax) * chartHeight
            val x = index * space + (space - barWidth) / 2
            val y = chartHeight - barHeight

            // Draw Bar
            drawRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )
            
            // Draw Value (Optional, above bar)
            if (value > 0) {
                 drawContext.canvas.nativeCanvas.drawText(
                    value.toInt().toString(),
                    x + barWidth / 2,
                    y - 10f,
                    paint
                )
            }

            // Draw Label (Axis)
            drawContext.canvas.nativeCanvas.drawText(
                label,
                x + barWidth / 2,
                size.height - 10f, 
                paint
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

@Composable
fun SimpleLineChart(
    data: List<Pair<String, Float>>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val paint = android.graphics.Paint().apply {
             this.color = android.graphics.Color.DKGRAY
             this.textSize = 32f
             this.textAlign = android.graphics.Paint.Align.CENTER
        }
        
        // Espacio horizontal
        val spaceX = size.width / (data.size + 1)
        val labelHeight = 50f
        val chartHeight = size.height - labelHeight
        
        val maxVal = data.maxOfOrNull { it.second }?.coerceAtLeast(1f) ?: 1f

        val points = mutableListOf<Offset>()

        data.forEachIndexed { index, (label, value) ->
            val x = (index + 1) * spaceX
            val y = chartHeight - ((value / maxVal) * chartHeight)
            points.add(Offset(x, y))

            // Draw Labels (Axis X)
            drawContext.canvas.nativeCanvas.drawText(
                label,
                x,
                size.height - 10f,
                paint
            )
            // Draw Value (Points)
            if (value > 0) {
                drawContext.canvas.nativeCanvas.drawText(
                     value.toInt().toString(),
                     x,
                     y - 20f,
                     paint
                )
            }
        }

        // Draw Lines
        if (points.size > 1) {
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = color,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 5f
                )
            }
        }
        // Draw Dots
        points.forEach { point ->
            drawCircle(
                color = color,
                center = point,
                radius = 8f
            )
        }
    }
}
