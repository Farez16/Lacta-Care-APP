package com.example.lactacare.dominio.repository

import android.content.Intent
import com.example.lactacare.datos.dto.AuthState
import com.example.lactacare.dominio.model.Paciente
import com.example.lactacare.datos.dto.CompleteProfileRequest
import com.example.lactacare.datos.dto.UserProfileDto
import com.example.lactacare.dominio.model.RolUsuario

interface AuthRepository {
    // ========================================================================
    // LOGIN TRADICIONAL
    // ========================================================================

    suspend fun login(correo: String, pass: String, rol: RolUsuario): Result<Unit>

    // ========================================================================
    // GOOGLE OAUTH2
    // ========================================================================

    /**
     * Obtiene el Intent para iniciar el flujo de Google Sign-In
     */
    fun getGoogleSignInIntent(): Intent

    /**
     * Procesa el resultado del login con Google
     *
     * MODIFICADO: Ahora acepta el rol del usuario para decidir qué endpoint usar
     * - Si rol = PACIENTE -> usa /api/auth/google
     * - Si rol = MEDICO o ADMINISTRADOR -> usa /api/auth/google/empleado
     *
     * @param intent Intent con el resultado de Google Sign-In
     * @param rol Rol seleccionado por el usuario en la UI
     * @return Result con el estado de autenticación
     */
    suspend fun loginWithGoogle(intent: Intent?, rol: RolUsuario): Result<AuthState>

    /**
     * Completa el perfil de un usuario de Google (solo pacientes)
     */
    suspend fun completarPerfil(request: CompleteProfileRequest): Result<Unit>

    // ========================================================================
    // REGISTRO
    // ========================================================================

    suspend fun registrarPaciente(paciente: Paciente): Result<Boolean>

    // ========================================================================
    // PERFIL
    // ========================================================================

    suspend fun getUserProfile(): Result<UserProfileDto>
    suspend fun updateUserProfile(nombre: String?, imagenBase64: String?): Result<Boolean>

    // ========================================================================
    // RECUPERACIÓN DE CONTRASEÑA
    // ========================================================================
    suspend fun solicitarCodigoRecuperacion(
        correo: String,
        rol: RolUsuario
    ): Result<String>

    suspend fun verificarCodigo(
        correo: String,
        codigo: String,
        rol: RolUsuario
    ): Result<String> // Retorna resetToken

    suspend fun cambiarPassword(
        resetToken: String,
        nuevaPassword: String,
        rol: RolUsuario
    ): Result<Unit>

    // ========================================================================
    // LOGOUT
    // ========================================================================

    suspend fun logout()
    // ========================================================================
    // CAMBIO DE CONTRASEÑA TEMPORAL
    // ========================================================================

    suspend fun changeTemporaryPassword(
        token: String,
        correo: String,
        currentPassword: String,
        newPassword: String
    ): Result<Unit>

    // ========================================================================
    // DOCTOR / EMPLEADO HELPERS
    // ========================================================================
    suspend fun getEmpleadoData(correo: String): Result<com.example.lactacare.datos.dto.PersonaEmpleadoResponseDto>
}