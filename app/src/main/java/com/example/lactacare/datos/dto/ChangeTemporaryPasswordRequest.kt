package com.example.lactacare.datos.dto

data class ChangeTemporaryPasswordRequest(
    val correo: String,
    val temporaryPassword: String,  // ⭐ Cambio: temporaryPassword en lugar de currentPassword
    val newPassword: String
)