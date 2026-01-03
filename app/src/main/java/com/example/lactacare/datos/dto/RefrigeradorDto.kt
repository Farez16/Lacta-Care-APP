package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

data class RefrigeradorDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("filaRefrigerador") val filas: Int,
    @SerializedName("columnaRefrigerador") val columnas: Int,
    // Relación con Sala de Lactancia. Idealmente enviamos el objeto completo o solo el ID según requiera el backend.
    // Viendo el controller: current.setSala_lactancia(entity.getSala_lactancia());
    // Probablemente espera un objeto con ID.
    @SerializedName("sala_lactancia") val sala: SalaLactanciaDto? = null
)
