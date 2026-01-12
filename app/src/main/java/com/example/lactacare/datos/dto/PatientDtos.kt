package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

// DTO para las reservas vistas desde el Paciente
data class ReservaPacienteDto(
    @SerializedName("idReserva") val id: Long,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("horaInicio") val horaInicio: String,
    @SerializedName("horaFin") val horaFin: String?,  // ✅ AGREGAR
    @SerializedName("estado") val estado: String,
    @SerializedName("idSala") val idSala: Long,  // ✅ CAMBIAR
    @SerializedName("nombreSala") val nombreSala: String,  // ✅ AGREGAR
    @SerializedName("nombreInstitucion") val nombreInstitucion: String?,  // ✅ AGREGAR
    @SerializedName("nombrePaciente") val nombrePaciente: String?,  // ✅ AGREGAR
    @SerializedName("apellidoPaciente") val apellidoPaciente: String?  // ✅ AGREGAR
)
// DTO para crear una reserva
data class CrearReservaRequest(
    @SerializedName("fecha") val fecha: String, // yyyy-MM-dd
    @SerializedName("horaInicio") val horaInicio: String, // HH:mm:ss
    @SerializedName("horaFin") val horaFin: String,      // HH:mm:ss
    @SerializedName("personaPaciente") val paciente: PacienteIdDto,
    @SerializedName("salaLactancia") val sala: SalaIdDto,
    @SerializedName("estado") val estado: String = "PENDIENTE"
)

data class PacienteIdDto(@SerializedName("id") val id: Long)
data class SalaIdDto(@SerializedName("idLactario") val id: Long)
