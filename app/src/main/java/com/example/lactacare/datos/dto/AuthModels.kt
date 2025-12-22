package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

// --- REQUESTS (Se quedan igual) ---

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
    @SerializedName("fechaNacimiento") val fechaNacimiento: String,
    @SerializedName("password") val password: String,
    @SerializedName("discapacidad") val discapacidad: String
)

data class GoogleAuthRequest(
    @SerializedName("idToken") val idToken: String
)

// --- RESPONSES (AQUÍ ESTÁ LA CORRECCIÓN) ---

data class AuthResponseDto(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("expiresIn") val expiresIn: Long,
    @SerializedName("userInfo") val userInfo: UserInfoDto
)

data class UserInfoDto(
    @SerializedName("id") val id: Long,

    // CORRECCIÓN: Backend envía "correo", lo guardamos en 'email'
    @SerializedName("correo") val email: String,

    // CORRECCIÓN: Backend envía "nombreCompleto", lo guardamos en 'fullName'
    @SerializedName("nombreCompleto") val fullName: String,

    // CORRECCIÓN: Backend envía "rol", lo guardamos en 'role'
    @SerializedName("rol") val role: String,

    @SerializedName("authProvider") val authProvider: String,

    // CORRECCIÓN: Backend envía "imagenPerfil"
    @SerializedName("imagenPerfil") val profileImage: String?,

    @SerializedName("profileCompleted") val profileCompleted: Boolean
)