package com.example.lactacare.datos.dto

/**
 * Excepción lanzada cuando un empleado necesita cambiar su contraseña temporal
 */
class PasswordChangeRequiredException(
    val tempToken: String,
    val correo: String,
    val rol: String,
    message: String
) : Exception(message)