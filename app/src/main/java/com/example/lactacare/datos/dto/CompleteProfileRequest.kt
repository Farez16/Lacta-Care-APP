package com.example.lactacare.datos.dto

data class CompleteProfileRequest(
    val googleId: String,
    val cedula: String,
    val primerNombre: String,
    val segundoNombre: String?,
    val primerApellido: String,
    val segundoApellido: String?,
    val telefono: String?,
    val fechaNacimiento: String,
    val discapacidad: String?,    // Puede ser nulo si no es paciente
    val codigoCredencial: String? // Puede ser nulo si es paciente
)