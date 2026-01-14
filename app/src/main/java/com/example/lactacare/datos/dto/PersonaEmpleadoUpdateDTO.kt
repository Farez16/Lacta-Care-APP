package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class PersonaEmpleadoUpdateDTO(
    @SerializedName("perfilEmpleadoImg") val perfilEmpleadoImg: String? = null,
    @SerializedName("cedula") val cedula: String? = null,
    @SerializedName("primerNombre") val primerNombre: String? = null,
    @SerializedName("segundoNombre") val segundoNombre: String? = null,
    @SerializedName("primerApellido") val primerApellido: String? = null,
    @SerializedName("segundoApellido") val segundoApellido: String? = null,
    @SerializedName("correo") val correo: String? = null,
    @SerializedName("telefono") val telefono: String? = null,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String? = null, // Backend usa LocalDate, Gson lo serializa como string "yyyy-MM-dd"
    @SerializedName("rolId") val rolId: Int? = null,
    @SerializedName("salaLactanciaId") val salaLactanciaId: Int? = null,
    @SerializedName("horarioEmpleadoId") val horarioEmpleadoId: Int? = null,
    @SerializedName("diasLaborablesEmpleadoId") val diasLaborablesEmpleadoId: Int? = null,
    @SerializedName("password") val password: String? = null
)
