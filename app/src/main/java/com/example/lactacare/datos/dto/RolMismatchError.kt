package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName
/**
 * DTO para manejar el error ROL_MISMATCH del backend
 * Se usa cuando un usuario intenta acceder con un rol incorrecto
 *
 * Ejemplo de respuesta del backend (403 Forbidden):
 * {
 *   "error": "ROL_MISMATCH",
 *   "message": "Tu cuenta está registrada como DOCTOR. Por favor, usa la opción correcta.",
 *   "rolCorrecto": "DOCTOR"
 * }
 */
data class RolMismatchError(
    @SerializedName("error")
    val error: String,           // "ROL_MISMATCH"

    @SerializedName("message")
    val message: String,          // Mensaje descriptivo del error

    @SerializedName("rolCorrecto")
    val rolCorrecto: String       // El rol real del usuario: "PACIENTE", "DOCTOR", "ADMINISTRADOR"
)