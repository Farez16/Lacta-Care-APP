package com.example.lactacare.dominio.model

data class Medico(
    // Ponemos 0 por defecto.
    // Al registrar, valdrá 0. Al consultar perfil, valdrá lo que diga la DB.
    val idEmpleado: Int = 0,

    val cedula: String = "",
    val primerNombre: String = "",
    val segundoNombre: String = "",
    val primerApellido: String = "",
    val segundoApellido: String = "",
    val correo: String = "",
    val telefono: String = "",
    val fechaNacimiento: String = "",
    val fotoPerfil: String = "",
    val password: String = "",
)