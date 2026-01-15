package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

// --- ESPEJO DE UsuarioResponse.java (Para Doctores y Admins) ---
data class UsuarioResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("cedula") val cedula: String,
    @SerializedName("nombreCompleto") val nombreCompleto: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("rol") val rol: String, // "DOCTOR", "ADMINISTRADOR"
    @SerializedName("imagen") val imagen: String?
)

// --- ESPEJO DE PacienteDTO.java ---
data class PacienteDto(
    @SerializedName("idPersona") val id: Long,
    @SerializedName("cedula") val cedula: String,
    @SerializedName("nombreCompleto") val nombreCompleto: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("telefono") val telefono: String?
)

// --- ESPEJO DE ReservaDTO.java ---
// --- ESPEJO DE ReservaDTO.java (Flattened to avoid 500 error recursion) ---
data class ReservaDto(
    @SerializedName("idReserva") val id: Long,
    @SerializedName("estado") val estado: String?,
    @SerializedName("fecha") val fecha: String?,
    @SerializedName("horaInicio") val horaInicio: String?,
    @SerializedName("horaFin") val horaFin: String?,

    // Flattened Fields
    @SerializedName("idPaciente") val idPaciente: Long?,
    @SerializedName("nombrePaciente") val nombrePaciente: String?,
    @SerializedName("apellidoPaciente") val apellidoPaciente: String?,

    @SerializedName("idSala") val idSala: Long?,
    @SerializedName("nombreSala") val nombreSala: String?,
    @SerializedName("nombreInstitucion") val nombreInstitucion: String?
)