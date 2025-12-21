package com.example.lactacare.datos.dto
import com.google.gson.annotations.SerializedName
// ==================== REQUEST MODELS ====================

/**
 * Request para login con Google
 */
data class GoogleAuthRequest(
    @SerializedName("idToken")
    val idToken: String,

    @SerializedName("platform")
    val platform: String = "ANDROID"
)

/**
 * Request para login tradicional
 */
data class LoginRequest(
    @SerializedName("correo")
    val correo: String,

    @SerializedName("password")
    val password: String
)

/**
 * Request para registrar paciente
 */
data class RegisterPacienteRequest(
    @SerializedName("cedula")
    val cedula: String,

    @SerializedName("primerNombre")
    val primerNombre: String,

    @SerializedName("segundoNombre")
    val segundoNombre: String? = null,

    @SerializedName("primerApellido")
    val primerApellido: String,

    @SerializedName("segundoApellido")
    val segundoApellido: String? = null,

    @SerializedName("correo")
    val correo: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("telefono")
    val telefono: String? = null,

    @SerializedName("fechaNacimiento")
    val fechaNacimiento: String, // Formato: YYYY-MM-DD

    @SerializedName("discapacidad")
    val discapacidad: String? = null,

    @SerializedName("googleId")
    val googleId: String? = null
)

/**
 * Request para registrar empleado (Doctor/Admin)
 */
data class RegisterEmpleadoRequest(
    @SerializedName("cedula")
    val cedula: String,

    @SerializedName("primerNombre")
    val primerNombre: String,

    @SerializedName("segundoNombre")
    val segundoNombre: String? = null,

    @SerializedName("primerApellido")
    val primerApellido: String,

    @SerializedName("segundoApellido")
    val segundoApellido: String? = null,

    @SerializedName("correo")
    val correo: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("telefono")
    val telefono: String? = null,

    @SerializedName("fechaNacimiento")
    val fechaNacimiento: String,

    @SerializedName("codigoCredencial")
    val codigoCredencial: String, // DOCTOR2025, ADMIN2025, etc.

    @SerializedName("googleId")
    val googleId: String? = null
)

/**
 * Request para completar perfil después de Google OAuth
 */
data class CompleteProfileRequest(
    @SerializedName("googleId")
    val googleId: String,

    @SerializedName("cedula")
    val cedula: String,

    @SerializedName("primerNombre")
    val primerNombre: String,

    @SerializedName("segundoNombre")
    val segundoNombre: String? = null,

    @SerializedName("primerApellido")
    val primerApellido: String,

    @SerializedName("segundoApellido")
    val segundoApellido: String? = null,

    @SerializedName("telefono")
    val telefono: String? = null,

    @SerializedName("fechaNacimiento")
    val fechaNacimiento: String,

    @SerializedName("discapacidad")
    val discapacidad: String? = null,

    @SerializedName("codigoCredencial")
    val codigoCredencial: String? = null // null = Paciente, código = Doctor/Admin
)

// ==================== RESPONSE MODELS ====================

/**
 * Response exitoso de autenticación
 */
data class AuthResponse(
    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("refreshToken")
    val refreshToken: String,

    @SerializedName("tokenType")
    val tokenType: String = "Bearer",

    @SerializedName("expiresIn")
    val expiresIn: Long, // Segundos

    @SerializedName("userInfo")
    val userInfo: UserInfo
)

/**
 * Información del usuario autenticado
 */
data class UserInfo(
    @SerializedName("id")
    val id: Long,

    @SerializedName("correo")
    val correo: String,

    @SerializedName("nombreCompleto")
    val nombreCompleto: String,

    @SerializedName("rol")
    val rol: String, // PACIENTE, DOCTOR, ADMINISTRADOR

    @SerializedName("authProvider")
    val authProvider: String, // LOCAL, GOOGLE

    @SerializedName("imagenPerfil")
    val imagenPerfil: String? = null,

    @SerializedName("profileCompleted")
    val profileCompleted: Boolean = true
)

/**
 * Response cuando el perfil está incompleto (primera vez con Google)
 */
data class ProfileIncompleteResponse(
    @SerializedName("status")
    val status: String = "USER_PROFILE_INCOMPLETE",

    @SerializedName("message")
    val message: String,

    @SerializedName("googleUserData")
    val googleUserData: GoogleUserData,

    @SerializedName("requiredFields")
    val requiredFields: List<String>
)

/**
 * Datos del usuario obtenidos de Google
 */
data class GoogleUserData(
    @SerializedName("googleId")
    val googleId: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("givenName")
    val givenName: String? = null,

    @SerializedName("familyName")
    val familyName: String? = null,

    @SerializedName("picture")
    val picture: String? = null
)

/**
 * Response genérico de mensaje
 */
data class MessageResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("timestamp")
    val timestamp: String? = null
)

/**
 * Response de error
 */
data class ErrorResponse(
    @SerializedName("error")
    val error: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("path")
    val path: String? = null,

    @SerializedName("status")
    val status: Int,

    @SerializedName("timestamp")
    val timestamp: String? = null
)