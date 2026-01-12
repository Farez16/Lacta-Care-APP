package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

data class SalaLactanciaDto(
    @SerializedName("idLactario") val id: Long,
    @SerializedName("nombreCMedico") val nombre: String?,
    @SerializedName("direccionCMedico") val direccion: String?,
    @SerializedName("telefonoCMedico") val telefono: String?,
    @SerializedName("correoCMedico") val correo: String?,
    @SerializedName("latitudCMedico") val latitud: String?,      // ✅ NUEVO
    @SerializedName("longitudCMedico") val longitud: String?,    // ✅ NUEVO
    @SerializedName("estado") val estado: String?,               // ✅ NUEVO
    @SerializedName("horaApertura") val horaApertura: String?,   // ✅ NUEVO
    @SerializedName("horaCierre") val horaCierre: String?,       // ✅ NUEVO
    @SerializedName("nombreInstitucion") val nombreInstitucion: String? // ✅ NUEVO
)
data class BloqueHorarioDto(
    @SerializedName("horaInicio") val horaInicio: String,
    @SerializedName("horaFin") val horaFin: String,
    @SerializedName("disponible") val disponible: Boolean
)
