package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

data class DoctorReservaDto(
    @SerializedName("id") val id: Long,
    @SerializedName("fecha") val fecha: String, // "yyyy-MM-dd"
    @SerializedName("horaInicio") val horaInicio: String, // "HH:mm:ss"
    @SerializedName("horaFin") val horaFin: String?,
    @SerializedName("estado") val estado: String,
    @SerializedName("personaPaciente") val paciente: PacienteDtoNested?,
    @SerializedName("salaLactancia") val sala: SalaLactanciaDtoNested?
)

data class SalaLactanciaDtoNested(
    @SerializedName("idLactario") val id: Int,
    @SerializedName("nombre") val nombre: String
)

data class PacienteDtoNested(
    @SerializedName("id") val id: Long,
    @SerializedName("primerNombre") val primerNombre: String?,
    @SerializedName("primerApellido") val primerApellido: String?,
    @SerializedName("cedula") val cedula: String?
) {
    fun nombreCompleto(): String {
        return "${primerNombre ?: ""} ${primerApellido ?: ""}".trim()
    }
}

data class CrearAtencionRequest(
    @SerializedName("fecha") val fecha: String, // yyyy-MM-dd
    @SerializedName("hora") val hora: String,   // HH:mm:ss
    @SerializedName("reserva") val reserva: ReservaIdDto,
    @SerializedName("empleado") val empleado: EmpleadoIdDto? = null // Opcional si el back lo toma del token, pero aquí parece explícito
)

data class ReservaIdDto(@SerializedName("id") val id: Long)
data class EmpleadoIdDto(@SerializedName("id") val id: Long)
