package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para producción diaria de leche
 * Usado en reportes del doctor
 */
data class ProduccionDiariaDto(
    @SerializedName("fecha")
    val fecha: String, // yyyy-MM-dd
    
    @SerializedName("cantidadLitros")
    val cantidadLitros: Double,
    
    @SerializedName("numeroContenedores")
    val numeroContenedores: Long
)

/**
 * DTO para estadísticas del doctor
 * Contiene métricas clínicas y operativas
 */
data class EstadisticasDoctorDto(
    @SerializedName("totalAtenciones")
    val totalAtenciones: Long,
    
    @SerializedName("totalLecheLitros")
    val totalLecheLitros: Double,
    
    @SerializedName("totalPacientes")
    val totalPacientes: Long,
    
    @SerializedName("totalContenedores")
    val totalContenedores: Long,
    
    @SerializedName("solicitudesPendientes")
    val solicitudesPendientes: Long,
    
    @SerializedName("contenedoresPorEstado")
    val contenedoresPorEstado: Map<String, Long>,
    
    @SerializedName("produccionSemanal")
    val produccionSemanal: List<ProduccionDiariaDto>,
    
    @SerializedName("tasaCumplimiento")
    val tasaCumplimiento: Double
)
