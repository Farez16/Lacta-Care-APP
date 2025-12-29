package com.example.lactacare.datos.dto
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileIncompleteData(
    val googleUserData: GoogleUserData,
    val googleToken: String? = null // Opcional, dependiendo de tu backend
) : Parcelable
/**
 * Estados posibles de autenticación en la aplicación
 *
 * - Idle: Estado inicial, sin acción
 * - Loading: Procesando autenticación
 * - Authenticated: Usuario autenticado correctamente
 * - Error: Error genérico de autenticación
 * - ProfileIncomplete: Usuario de Google necesita completar perfil
 * - RolMismatch: Usuario intentó acceder con rol incorrecto (NUEVO)
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()

    data class Error(val message: String) : AuthState()

    data class ProfileIncomplete(val data: ProfileIncompleteData) : AuthState()

    // ========================================================================
    // ERRORES CON DIÁLOGO PERSONALIZADO
    // ========================================================================

    /**
     * Usuario intentó acceder con rol incorrecto
     * Ejemplo: Un doctor intenta entrar como paciente
     */
    data class RolMismatch(
        val mensaje: String,
        val rolCorrecto: String
    ) : AuthState()

    /**
     * Correo no autorizado (solo para empleados con Google)
     * El correo no está en la lista blanca del backend
     */
    data class UnauthorizedEmail(
        val mensaje: String
    ) : AuthState()

    /**
     * Usuario no encontrado en el sistema
     * El correo no existe en la base de datos
     */
    data class UserNotFound(
        val mensaje: String
    ) : AuthState()

    /**
     * Credenciales inválidas (contraseña incorrecta)
     */
    data class InvalidCredentials(
        val mensaje: String
    ) : AuthState()

    /**
     * Error genérico (red, servidor, etc.)
     */
    data class GenericError(
        val mensaje: String
    ) : AuthState()

    data class PasswordChangeRequired(
        val tempToken: String,
        val correo: String,
        val rol: String,
        val mensaje: String
    ) : AuthState()
}