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
data class ReservaDto(
    @SerializedName("idReserva") val id: Long,
    @SerializedName("estado") val estado: String?, // "ACTIVA", "CANCELADA"
    @SerializedName("fecha") val fecha: String?,   // "2025-12-22"
    @SerializedName("horaInicio") val horaInicio: String?,
    @SerializedName("salaLactancia") val salaLactancia: SalaLactanciaDto?,
    @SerializedName("personaPaciente") val personaPaciente: PacienteDto?
)