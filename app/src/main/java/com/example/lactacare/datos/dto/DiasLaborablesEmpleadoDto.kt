package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

data class DiasLaborablesEmpleadoDto(
    @SerializedName("idDiaLaborableEmpleado") val id: Int = 0,
    @SerializedName("lunes") val lunes: Boolean = false,
    @SerializedName("martes") val martes: Boolean = false,
    @SerializedName("miercoles") val miercoles: Boolean = false,
    @SerializedName("jueves") val jueves: Boolean = false,
    @SerializedName("viernes") val viernes: Boolean = false,
    @SerializedName("sabado") val sabado: Boolean = false,
    @SerializedName("domingo") val domingo: Boolean = false
)
