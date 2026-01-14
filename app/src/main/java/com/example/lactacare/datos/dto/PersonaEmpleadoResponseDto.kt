package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

data class PersonaEmpleadoResponseDto(
    @SerializedName("idPerEmpleado") val id: Int,
    @SerializedName("cedula") val cedula: String,
    @SerializedName("primerNombre") val primerNombre: String,
    @SerializedName("primerApellido") val primerApellido: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("rol") val rol: RolDto?,
    @SerializedName("salaLactanciaId") val salaLactanciaId: Int?
)
