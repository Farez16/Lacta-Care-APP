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
    @SerializedName("cubiculos") val cubiculo: CubiculoIdDto,
    @SerializedName("estado") val estado: String = "PENDIENTE"
)

data class PacienteIdDto(@SerializedName("id") val id: Long)
data class SalaIdDto(@SerializedName("idLactario") val id: Long)
data class CubiculoIdDto(@SerializedName("id") val id: Long)

// DTO para Cubículo
data class CubiculoDto(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("disponible") val disponible: Boolean,
    @SerializedName("idSalaLactancia") val idSalaLactancia: Long,
    @SerializedName("nombreSala") val nombreSala: String?
)

// DTO para cancelar reserva
data class CancelarReservaRequest(
    @SerializedName("estado") val estado: String = "CANCELADA"
)

// DTO para perfil completo del paciente
data class PacientePerfilDto(
    @SerializedName("id") val id: Long,
    @SerializedName("cedula") val cedula: String,
    @SerializedName("imagenPerfil") val imagenPerfil: String?,
    @SerializedName("primerNombre") val primerNombre: String,
    @SerializedName("segundoNombre") val segundoNombre: String?,
    @SerializedName("primerApellido") val primerApellido: String,
    @SerializedName("segundoApellido") val segundoApellido: String?,
    @SerializedName("correo") val correo: String,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String
)

data class ActualizarPerfilRequest(
    @SerializedName("primerNombre") val primerNombre: String?,
    @SerializedName("segundoNombre") val segundoNombre: String?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("imagenPerfil") val imagenPerfil: String?
)
