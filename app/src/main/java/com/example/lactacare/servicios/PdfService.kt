package com.example.lactacare.servicios

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.lactacare.datos.dto.EstadisticasDoctorDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio para generar y gestionar PDFs de reportes
 */
@Singleton
class PdfService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Genera PDF con estadísticas del doctor
     * @return Result con Uri del archivo generado o error
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun generarPdfEstadisticas(
        estadisticas: EstadisticasDoctorDto,
        nombreDoctor: String,
        filtroFecha: String
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            // 1. Crear archivo en Downloads
            val timestamp = System.currentTimeMillis()
            val fileName = "Reporte_${nombreDoctor.replace(" ", "_")}_$timestamp.pdf"
            
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: return@withContext Result.failure(Exception("No se pudo crear el archivo"))

            // 2. Generar contenido del PDF
            resolver.openOutputStream(uri)?.use { outputStream ->
                generarContenidoPdf(outputStream, estadisticas, nombreDoctor, filtroFecha)
            } ?: return@withContext Result.failure(Exception("No se pudo abrir el stream"))

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Genera el contenido visual del PDF
     */
    private fun generarContenidoPdf(
        outputStream: OutputStream,
        estadisticas: EstadisticasDoctorDto,
        nombreDoctor: String,
        filtroFecha: String
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        var y = 50f

        // Título principal
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = android.graphics.Color.parseColor("#1976D2")
        canvas.drawText("Reporte de Estadísticas", 50f, y, paint)
        y += 40f

        // Información del doctor
        paint.textSize = 14f
        paint.typeface = Typeface.DEFAULT
        paint.color = android.graphics.Color.BLACK
        canvas.drawText("Doctor: $nombreDoctor", 50f, y, paint)
        y += 25f
        canvas.drawText("Período: $filtroFecha", 50f, y, paint)
        y += 25f
        val fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        canvas.drawText("Fecha de generación: $fechaActual", 50f, y, paint)
        y += 40f

        // Línea separadora
        paint.strokeWidth = 2f
        canvas.drawLine(50f, y, 545f, y, paint)
        y += 30f

        // Sección: Métricas Principales
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = android.graphics.Color.parseColor("#1976D2")
        canvas.drawText("Métricas Principales", 50f, y, paint)
        y += 30f

        // Métricas en dos columnas
        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT
        paint.color = android.graphics.Color.BLACK

        val metricas = listOf(
            "Total Atenciones:" to estadisticas.totalAtenciones.toString(),
            "Leche Recolectada:" to "${String.format("%.2f", estadisticas.totalLecheLitros)} L",
            "Pacientes Atendidos:" to estadisticas.totalPacientes.toString(),
            "Total Contenedores:" to estadisticas.totalContenedores.toString(),
            "Solicitudes Pendientes:" to estadisticas.solicitudesPendientes.toString(),
            "Tasa de Cumplimiento:" to "${estadisticas.tasaCumplimiento.toInt()}%"
        )

        metricas.forEachIndexed { index, (label, value) ->
            val xLabel = if (index % 2 == 0) 50f else 320f
            val yPos = y + (index / 2) * 25f
            
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(label, xLabel, yPos, paint)
            paint.typeface = Typeface.DEFAULT
            canvas.drawText(value, xLabel + 150f, yPos, paint)
        }
        y += (metricas.size / 2 + 1) * 25f + 20f

        // Línea separadora
        paint.strokeWidth = 1f
        canvas.drawLine(50f, y, 545f, y, paint)
        y += 30f

        // Sección: Producción Diaria
        if (estadisticas.produccionSemanal.isNotEmpty()) {
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = android.graphics.Color.parseColor("#1976D2")
            canvas.drawText("Producción Diaria", 50f, y, paint)
            y += 30f

            paint.textSize = 11f
            paint.typeface = Typeface.DEFAULT
            paint.color = android.graphics.Color.BLACK

            estadisticas.produccionSemanal.forEach { dia ->
                val fecha = try {
                    LocalDate.parse(dia.fecha).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                } catch (e: Exception) {
                    dia.fecha
                }
                val texto = "$fecha: ${String.format("%.2f", dia.cantidadLitros)} L (${dia.numeroContenedores} contenedores)"
                canvas.drawText(texto, 70f, y, paint)
                y += 22f
            }
            y += 20f
        }

        // Sección: Estados de Contenedores
        if (estadisticas.contenedoresPorEstado.isNotEmpty()) {
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = android.graphics.Color.parseColor("#1976D2")
            canvas.drawText("Estados de Contenedores", 50f, y, paint)
            y += 30f

            paint.textSize = 11f
            paint.typeface = Typeface.DEFAULT
            paint.color = android.graphics.Color.BLACK

            estadisticas.contenedoresPorEstado.forEach { (estado, cantidad) ->
                canvas.drawText("$estado: $cantidad", 70f, y, paint)
                y += 22f
            }
        }

        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }

    /**
     * Abre el PDF con una aplicación externa
     */
    fun abrirPdf(uri: Uri, activity: Activity) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            if (intent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(intent)
            } else {
                Toast.makeText(
                    activity,
                    "No hay aplicación para abrir PDFs. Por favor, instala un visor de PDF.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                activity,
                "Error al abrir PDF: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Obtiene la ruta legible del archivo para mostrar al usuario
     */
    fun obtenerRutaLegible(uri: Uri): String {
        val nombreArchivo = uri.lastPathSegment?.substringAfterLast('/') ?: "reporte.pdf"
        return "Descargas/$nombreArchivo"
    }
}
