package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

// DTO para las reservas vistas desde el Paciente
data class ReservaPacienteDto(
    @SerializedName("id") val id: Long,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("horaInicio") val horaInicio: String,
    @SerializedName("estado") val estado: String, // PENDIENTE, ATENDIDO
    @SerializedName("salaLactancia") val sala: SalaLactanciaDto, // Reutilizamos DTO de lactarios
    // El backend podría devolver el objeto sala completo o solo el ID, asumiremos objeto por ahora
    // Si falla, ajustaremos a SalaIdDtoNested
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
