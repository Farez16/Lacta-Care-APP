package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName
import com.example.lactacare.dominio.model.Institucion

data class SalaLactanciaDto(
    @SerializedName("idLactario") val id: Long? = null,
    @SerializedName("nombreCMedico") val nombre: String?,
    @SerializedName("direccionCMedico") val direccion: String?,
    @SerializedName("telefonoCMedico") val telefono: String?,
    @SerializedName("correoCMedico") val correo: String?,
    @SerializedName("latitudCMedico") val latitud: String? = null,
    @SerializedName("longitudCMedico") val longitud: String? = null,
    @SerializedName("estado") val estado: String? = "Activo",
    
    // Relaciones
    @SerializedName("institucion") val institucion: Institucion? = null,
    @SerializedName("horarioSala") val horario: HorariosSalaDto? = null,
    @SerializedName("diasLaborablesSala") val dias: DiasLaborablesSalaDto? = null,
    @SerializedName("numeroCubiculos") val numeroCubiculos: Int? = null // Solo lectura, para listados si aplica
)

data class SalaLactanciaConCubiculosDTO(
    @SerializedName("salaLactancia") val salaLactancia: SalaLactanciaDto,
    @SerializedName("numeroCubiculos") val numeroCubiculos: Int
)

data class HorariosSalaDto(
    @SerializedName("id_horario_sala") val id: Long? = null,
    @SerializedName("hora_inicio") val horaInicio: String, // HH:mm:ss
    @SerializedName("hora_fin") val horaFin: String,
    @SerializedName("hora_inicio_descanso") val horaInicioDescanso: String? = null,
    @SerializedName("hora_fin_descanso") val horaFinDescanso: String? = null
)

data class DiasLaborablesSalaDto(
    @SerializedName("id_dia_laborable_sala") val id: Long? = null,
    @SerializedName("lunes") val lunes: Boolean = false,
    @SerializedName("martes") val martes: Boolean = false,
    @SerializedName("miercoles") val miercoles: Boolean = false,
    @SerializedName("jueves") val jueves: Boolean = false,
    @SerializedName("viernes") val viernes: Boolean = false,
    @SerializedName("sabado") val sabado: Boolean = false,
    @SerializedName("domingo") val domingo: Boolean = false
)

// DTO para disponibilidad de horarios (usado en reservas)
data class BloqueHorarioDto(
    @SerializedName("horaInicio") val horaInicio: String,
    @SerializedName("horaFin") val horaFin: String,
    @SerializedName("disponible") val disponible: Boolean
)
