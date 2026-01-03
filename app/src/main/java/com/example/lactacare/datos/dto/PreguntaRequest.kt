package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName
data class PreguntaRequest(
    @SerializedName("pregunta") val pregunta: String,
    @SerializedName("latitud") val latitud: Double? = null,
    @SerializedName("longitud") val longitud: Double? = null
)