package com.example.lactacare.datos.dto

data class RefrigeradorDisponibleDto(
    val id: Long = 0,
    val nombre: String = "",
    val pisos: Int = 0,
    val filas: Int = 0,
    val columnas: Int = 0,
    val capacidadDisponible: Int = 0,
    val ubicacionesOcupadas: List<UbicacionOcupadaDto> = emptyList()
)

data class UbicacionOcupadaDto(
    val piso: Int = 0,
    val fila: Int = 0,
    val columna: Int = 0
)
