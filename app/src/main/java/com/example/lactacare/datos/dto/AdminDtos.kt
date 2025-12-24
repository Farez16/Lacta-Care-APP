package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

data class CrearEmpleadoRequest(
    @SerializedName("cedula") val cedula: String,
    @SerializedName("primerNombre") val primerNombre: String,
    @SerializedName("segundoNombre") val segundoNombre: String,
    @SerializedName("primerApellido") val primerApellido: String,
    @SerializedName("segundoApellido") val segundoApellido: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String, // "YYYY-MM-DD"
    @SerializedName("rol") val rol: String // "DOCTOR" o "ADMINISTRADOR"
)
