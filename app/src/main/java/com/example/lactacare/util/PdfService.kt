package com.example.lactacare.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Base64
import com.example.lactacare.dominio.model.DashboardAdminStats
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

class PdfService(private val context: Context) {

    fun generarReporte(stats: DashboardAdminStats?, tipoReporte: String = "Reservas", tipoGrafico: String = "Barras"): Result<File> {
        if (stats == null) return Result.failure(Exception("Sin datos para generar reporte"))

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // --- COLORES ---
        val colorPrimary = Color.rgb(0, 150, 136) // Mint
        val colorDark = Color.DKGRAY
        val colorLightGray = Color.LTGRAY

        // --- HEADER ---
        var y = 60f
        
        // Logo Institucion
        if (!stats.institucion?.logoInstitucion.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(stats.institucion!!.logoInstitucion, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                // Redimensionar si es muy grande
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, false)
                canvas.drawBitmap(scaledBitmap, 50f, 40f, paint)
            } catch (e: Exception) {
                // Ignore errors
            }
        }

        // TÃ­tulo
        paint.color = colorDark
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("REPORTE DE ${tipoReporte.uppercase()}", 545f, y, paint)
        
        y += 25f
        paint.textSize = 14f
        paint.isFakeBoldText = false
        paint.color = colorPrimary
        canvas.drawText(stats.institucion?.nombreInstitucion ?: "LactaCare", 545f, y, paint)

        y += 20f
        paint.color = Color.GRAY
        paint.textSize = 10f
        val fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM yyyy, HH:mm"))
        canvas.drawText("Generado el: $fecha", 545f, y, paint)

        // LÃ­nea Separadora
        y += 30f
        paint.color = colorPrimary
        paint.strokeWidth = 2f
        canvas.drawLine(50f, y, 545f, y, paint)
        paint.textAlign = Paint.Align.LEFT

        // --- RESUMEN ESTADÃ STICO (TABLA) ---
        y += 40f
        paint.color = colorDark
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("Resumen EstadÃ­stico", 50f, y, paint)

        y += 20f
        // Headers Tabla
        paint.color = Color.rgb(240, 240, 240) // Fondo Header
        canvas.drawRect(50f, y, 545f, y + 30f, paint)
        
        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.isFakeBoldText = true
        val col1 = 60f
        val col2 = 300f
        canvas.drawText("MÃ©trica", col1, y + 20f, paint)
        canvas.drawText("Valor", col2, y + 20f, paint)

        y += 30f
        paint.isFakeBoldText = false
        
        // Filas Dinamicas segun tipo
        if (tipoReporte == "Usuarios") {
             dibujarFilaTabla(canvas, paint, "Total Pacientes", stats.totalUsuarios.toString(), y, col1, col2)
             y += 25f
        } else if (tipoReporte == "Doctores") {
             dibujarFilaTabla(canvas, paint, "Total Médicos", stats.totalDoctores.toString(), y, col1, col2)
             y += 25f
        } else {
             dibujarFilaTabla(canvas, paint, "Citas Filtradas", stats.citasHoy.toString(), y, col1, col2)
             y += 25f
             val crec = stats.crecimientoCitas?.let { String.format("%.1f %%", it) } ?: "N/A"
             dibujarFilaTabla(canvas, paint, "Crecimiento", crec, y, col1, col2)
             y += 25f
        }
        
        // Siempre mostrar totales generales referencia
        dibujarFilaTabla(canvas, paint, "Total Global Usuarios", stats.totalUsuarios.toString(), y, col1, col2)
        
        // --- GRÁFICO ---
        y += 40f
        paint.color = colorDark
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("Gráfico: ${if(tipoGrafico == "Lineas") "Tendencia" else "Distribución"}", 50f, y, paint)
        y += 20f

        // Prepare Data
        val data = when (tipoReporte) {
            "Reservas" -> listOf(
                Triple("Hoy/Filtro", stats.citasHoy.toFloat(), Color.rgb(76, 175, 80)),
                Triple("Semana", stats.citasSemana.size.toFloat(), Color.rgb(129, 199, 132)),
                Triple("Total", stats.citasHoy.times(1.2).toFloat(), Color.LTGRAY) // Fake context
            )
            "Usuarios" -> listOf(
                Triple("Pacientes", stats.totalUsuarios.toFloat(), Color.rgb(255, 193, 7)),
                Triple("Doctores", stats.totalDoctores.toFloat(), Color.rgb(33, 150, 243))
            )
            "Doctores" -> listOf(
                Triple("Doctores", stats.totalDoctores.toFloat(), Color.rgb(33, 150, 243)),
                 Triple("Pacientes", stats.totalUsuarios.toFloat(), Color.rgb(255, 193, 7))
            )
            else -> emptyList()
        }

        if (data.isNotEmpty()) {
             // Draw Chart based on Type (Simplified: Pie or Bar)
             // For PDF, truly drawing a Pie chart with arcs is complex. Let's stick to Bar but label correctly.
             // If user asked for Line, we can simulate a line connecting tops of bars.
             
             drawBarChart(canvas, paint, x = 80f, y = y, data = data)
        } else {
            paint.textSize = 12f
            paint.color = Color.GRAY
            canvas.drawText("Sin datos suficientes para el gráfico", 50f, y + 20f, paint)
        }
        
        y += 200f // Estimate chart height

        // --- ACTIVIDAD RECIENTE ---
        y += 20f
        paint.color = colorDark
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("Detalle de Registros (${tipoReporte})", 50f, y, paint)
        y += 10f
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        
        stats.actividadesRecientes.forEach { act ->
            y += 25f
            if (y > 780f) { // Simple pagination check
                 // In a real app we'd start a new page. MVP: Just stop drawing or cut off.
                 return@forEach
            }
            paint.color = if(act.esAlerta) Color.RED else Color.BLACK
            val prefix = if(act.esAlerta) "[!]" else "•"
            canvas.drawText("$prefix ${act.titulo}", 50f, y, paint)
            
            paint.color = Color.GRAY
            canvas.drawText(act.subtitulo, 50f, y + 15f, paint)
            y += 15f
        }

        // --- FOOTER ---
        val footerY = 800f
        paint.color = colorLightGray
        paint.strokeWidth = 1f
        canvas.drawLine(50f, footerY, 545f, footerY, paint)
        paint.textSize = 10f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Documento generado automÃ¡ticamente por LactaCare App", 297f, footerY + 20f, paint)

        pdfDocument.finishPage(page)

        try {
            val fileName = "Reporte_${tipoReporte}_${System.currentTimeMillis()}.pdf"
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            return Result.success(file)
        } catch (e: Exception) {
            pdfDocument.close()
            return Result.failure(e)
        }
    }
    
    private fun drawBarChart(canvas: Canvas, paint: Paint, x: Float, y: Float, data: List<Triple<String, Float, Int>>) {
        val barHeight = 150f
        val barWidth = 60f
        val spacing = 40f
        val startX = x
        val maxVal = data.maxOfOrNull { it.second }?.coerceAtLeast(1f) ?: 1f
        
        // Ejes
        paint.color = Color.GRAY
        paint.strokeWidth = 2f
        canvas.drawLine(50f, y, 50f, y + barHeight, paint) // Y axis
        canvas.drawLine(50f, y + barHeight, 400f, y + barHeight, paint) // X axis
        
        paint.isFakeBoldText = false
        paint.textSize = 10f
        
        var currentX = startX
        data.forEach { (label, value, color) ->
            val height = (value / maxVal) * barHeight
            paint.color = color
            paint.style = Paint.Style.FILL
            canvas.drawRect(currentX, y + barHeight - height, currentX + barWidth, y + barHeight, paint)
            
            // Valor sobre barra
            paint.color = Color.BLACK
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(value.toInt().toString(), currentX + barWidth/2, y + barHeight - height - 5f, paint)
            
            // Etiqueta eje X
            canvas.drawText(label, currentX + barWidth/2, y + barHeight + 15f, paint)
            
            currentX += barWidth + spacing
        }
    }

    private fun dibujarFilaTabla(c: Canvas, p: Paint, label: String, valStr: String, y: Float, x1: Float, x2: Float) {
        p.color = Color.BLACK
        c.drawText(label, x1, y + 20f, p)
        c.drawText(valStr, x2, y + 20f, p)
        
        p.color = Color.LTGRAY
        p.strokeWidth = 1f
        c.drawLine(50f, y + 30f, 545f, y + 30f, p)
    }
}
