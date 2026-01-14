package com.example.lactacare.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.lactacare.dominio.model.DashboardAdminStats
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PdfService(private val context: Context) {

    fun generarReporte(stats: DashboardAdminStats?): Result<File> {
        if (stats == null) return Result.failure(Exception("Sin datos para generar reporte"))

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size aprox
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // TÃ­tulo
        paint.color = Color.BLACK
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("Reporte General - LactaCare", 50f, 60f, paint)

        // Fecha
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        val fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        canvas.drawText("Generado el: $fecha", 50f, 85f, paint)

        // LÃ­nea separadora
        paint.color = Color.LTGRAY
        paint.strokeWidth = 2f
        canvas.drawLine(50f, 100f, 545f, 100f, paint)

        // EstadÃ­sticas Generales
        paint.color = Color.BLACK
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Resumen EstadÃ­stico", 50f, 140f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        var y = 170f
        
        dibujarFila(canvas, paint, "Total Pacientes:", stats.totalUsuarios.toString(), y)
        y += 25f
        dibujarFila(canvas, paint, "Total MÃ©dicos:", stats.totalDoctores.toString(), y)
        y += 25f
        dibujarFila(canvas, paint, "Citas Hoy:", stats.citasHoy.toString(), y)
        y += 25f
        val crecimientoStr = if(stats.crecimientoCitas != null) String.format("%.1f %%", stats.crecimientoCitas) else "N/A"
        dibujarFila(canvas, paint, "Crecimiento Citas (Mes):", crecimientoStr, y)

        // Actividad Reciente
        y += 50f
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Actividad Reciente", 50f, y, paint)
        y += 30f
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        stats.actividadesRecientes.forEach { act ->
            canvas.drawText("- ${act.titulo}: ${act.subtitulo}", 50f, y, paint)
            y += 20f
        }

        pdfDocument.finishPage(page)

        // Guardar archivo
        try {
            val fileName = "Reporte_LactaCare_${System.currentTimeMillis()}.pdf"
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            return Result.success(file)
        } catch (e: Exception) {
            pdfDocument.close()
            return Result.failure(e)
        }
    }

    private fun dibujarFila(canvas: android.graphics.Canvas, paint: Paint, label: String, value: String, y: Float) {
        val originalColor = paint.color
        paint.color = Color.BLACK
        canvas.drawText(label, 50f, y, paint)
        paint.isFakeBoldText = true
        canvas.drawText(value, 250f, y, paint)
        paint.isFakeBoldText = false
    }
}
