package com.example.lactacare.datos.dto

import java.math.BigDecimal

data class CrearAtencionCompletaRequest(
    val idReserva: Long,
    val idEmpleado: Long,
    val contenedores: List<ContenedorDto>
) {
    data class ContenedorDto(
        val cantidadMl: BigDecimal,
        val idRefrigerador: Long,
        val piso: Int,
        val fila: Int,
        val columna: Int
    )
}

data class AtencionCompletaResponse(
    val idAtencion: Long = 0,
    val idReserva: Long = 0,
    val cantidadContenedores: Int = 0,
    val totalMililitros: BigDecimal = BigDecimal.ZERO,
    val mensaje: String = ""
)
