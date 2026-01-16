package com.example.lactacare.datos.dto

import java.math.BigDecimal

/**
 * DTO para solicitudes de retiro de contenedores
 * Corresponde a SolicitudRetiroDTO del backend
 */
data class SolicitudRetiroDto(
    val idContenedor: Long,
    val cantidadMl: BigDecimal,
    val fechaExtraccion: String,
    val fechaCaducidad: String,
    val idPaciente: Long,
    val nombrePaciente: String,
    val cedulaPaciente: String,
    val ubicacion: String
)
