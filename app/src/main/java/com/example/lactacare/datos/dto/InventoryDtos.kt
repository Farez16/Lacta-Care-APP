package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

data class ContenedorLecheDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("estado")
    val estado: String,

    @SerializedName("cantidadMl")
    val cantidadMl: Double,

    @SerializedName("fechaExtraccion")
    val fechaExtraccion: String?,

    @SerializedName("fechaCaducidad")
    val fechaCaducidad: String?,
    // Hypothetical fields based on UbicacionContenedor logic
    @SerializedName("refrigeradorId") val refrigeradorId: Long? = null,
    @SerializedName("piso") val piso: Int? = null,
    @SerializedName("fila") val fila: Int? = null,
    @SerializedName("columna") val columna: Int? = null
)

data class CrearContenedorRequest(
    val fechaHoraExtraccion: String,
    val cantidadMililitros: Double,
    val refrigeradorId: Long,
    val piso: Int,
    val fila: Int,
    val columna: Int,
    val atencionId: Long // Vinculo con la atencion/paciente
)