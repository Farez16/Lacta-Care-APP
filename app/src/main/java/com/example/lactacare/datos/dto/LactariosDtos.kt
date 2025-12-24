package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

data class SalaLactanciaDto(
    @SerializedName("idLactario") val id: Long,
    @SerializedName("nombreCMedico") val nombre: String?,
    @SerializedName("direccionCMedico") val direccion: String?,
    @SerializedName("telefonoCMedico") val telefono: String?,
    @SerializedName("correoCMedico") val correo: String?
)
