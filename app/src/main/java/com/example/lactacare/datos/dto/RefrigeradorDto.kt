package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

data class RefrigeradorDto(
    @SerializedName("idRefrigerador") val id: Long = 0,
    @SerializedName("capacidadMaxRefri") val capacidad: Int,
    @SerializedName("pisoRefrigerador") val piso: Int,
    @SerializedName("filaRefrigerador") val fila: Int,
    @SerializedName("columnaRefrigerador") val columna: Int,
    // El backend espera un objeto completo o null
    @SerializedName("sala_lactancia") val sala: SalaLactanciaDto? = null
)
