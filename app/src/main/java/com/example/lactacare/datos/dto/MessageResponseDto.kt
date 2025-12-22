package com.example.lactacare.datos.dto
import com.google.gson.annotations.SerializedName

data class MessageResponseDto(
    @SerializedName("message")
    val message: String,

    // Agrego 'status' como opcional porque vi que tu backend también lo envía
    // (ej: "SUCCESS" o "ERROR"). Si no lo usas, no estorba.
    @SerializedName("status")
    val status: String? = null
)
