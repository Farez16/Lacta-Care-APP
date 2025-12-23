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

// --- RESPONSES (MODIFICADO PARA SOPORTAR CASO 202) ---

data class AuthResponseDto(
    // Campos de Login Exitoso (Ahora son Nullable ? porque en el caso 202 no vienen)
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("expiresIn") val expiresIn: Long?,
    @SerializedName("userInfo") val userInfo: UserInfoDto?,

    // Campos de Perfil Incompleto (Nuevos campos para capturar la respuesta 202)
    @SerializedName("status") val status: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("googleUserData") val googleUserData: GoogleUserData?
)

data class UserInfoDto(
    @SerializedName("id") val id: Long,
    @SerializedName("correo") val email: String,
    @SerializedName("nombreCompleto") val fullName: String,
    @SerializedName("rol") val role: String,
    @SerializedName("authProvider") val authProvider: String,
    @SerializedName("imagenPerfil") val profileImage: String?,
    @SerializedName("profileCompleted") val profileCompleted: Boolean
)