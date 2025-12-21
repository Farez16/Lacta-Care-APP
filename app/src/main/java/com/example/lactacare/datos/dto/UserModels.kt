package com.example.lactacare.datos.dto
import com.example.lactacare.dominio.model.RolUsuario

/**
 * Modelo de sesión del usuario (para guardar localmente)
 */
data class UserSession(
    val userId: Long,
    val correo: String,
    val nombreCompleto: String,
    val rol: RolUsuario, // Usa el del paquete dominio
    val accessToken: String,
    val refreshToken: String,
    val tokenExpiration: Long, // Timestamp cuando expira
    val imagenPerfil: String? = null
) {
    /**
     * Verifica si el token ha expirado
     */
    fun isTokenExpired(): Boolean {
        return System.currentTimeMillis() > tokenExpiration
    }
}

/**
 * Estado de autenticación
 */
sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val userSession: UserSession) : AuthState()
    data class ProfileIncomplete(val googleData: GoogleUserData, val requiredFields: List<String>) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * Resultado de operación
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val code: Int? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
