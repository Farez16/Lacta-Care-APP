package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

data class ContenedorLecheDto(
    @SerializedName("id") val id: Long,
    @SerializedName("fechaHoraExtraccion") val fechaHoraExtraccion: String?, // Probable formato ISO
    @SerializedName("fechaHoraCaducidad") val fechaHoraCaducidad: String?,
    @SerializedName("estado") val estado: String?, // Ej: "Disponible", "Caducado"
    @SerializedName("cantidadMililitros") val cantidadMililitros: Double?
)
