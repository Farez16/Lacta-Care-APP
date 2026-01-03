package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

data class SistemaAlertaDto(
    @SerializedName("id") val id: Long,
    @SerializedName("tipoAlerta") val tipoAlerta: String?,
    @SerializedName("temperaturaAlerta") val temperaturaAlerta: Float,
    @SerializedName("fechaHoraAlerta") val fechaHoraAlerta: String? // Recibimos como String para facilitar parsing
)
