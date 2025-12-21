package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

// --- REQUESTS (Lo que envías al servidor) ---

data class LoginRequest(
    @SerializedName("correo") val correo: String,
    @SerializedName("password") val password: String
)

data class RegisterPacienteRequest(
    @SerializedName("cedula") val cedula: String,
    @SerializedName("primerNombre") val primerNombre: String,
    @SerializedName("segundoNombre") val segundoNombre: String,
    @SerializedName("primerApellido") val primerApellido: String,
    @SerializedName("segundoApellido") val segundoApellido: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String, // YYYY-MM-DD
    @SerializedName("password") val password: String,
    @SerializedName("discapacidad") val discapacidad: String // "true" o "false"
)

data class GoogleAuthRequest(
    @SerializedName("idToken") val idToken: String
)

// --- RESPONSES (Lo que recibes del servidor) ---

data class AuthResponseDto(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("expiresIn") val expiresIn: Long,
    @SerializedName("userInfo") val userInfo: UserInfoDto
)

data class UserInfoDto(
    @SerializedName("id") val id: Long,
    @SerializedName("email") val email: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("role") val role: String,
    @SerializedName("authProvider") val authProvider: String,
    @SerializedName("profileImage") val profileImage: String?,
    @SerializedName("profileCompleted") val profileCompleted: Boolean
)