package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

data class HorariosEmpleadoDto(
    @SerializedName("idHorarioEmpleado") val id: Int = 0,
    @SerializedName("horaInicio") val horaInicio: String, // "HH:mm"
    @SerializedName("horaFin") val horaFin: String
)
