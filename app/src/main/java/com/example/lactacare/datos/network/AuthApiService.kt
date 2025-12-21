package com.example.lactacare.datos.network

import com.example.lactacare.datos.dto.*
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

/**
 * Interface para los endpoints de autenticación del backend
 */
interface AuthApiService {

    /**
     * Health check del servicio
     * GET /api/auth/health
     */
    @GET("api/auth/health")
    suspend fun healthCheck(): Response<MessageResponse>

    /**
     * Login con Google OAuth2
     * POST /api/auth/google
     *
     * Puede retornar:
     * - 200: AuthResponse (usuario existente)
     * - 202: ProfileIncompleteResponse (usuario nuevo, perfil incompleto)
     * - 401: ErrorResponse (token inválido)
     */
    @POST("api/auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleAuthRequest
    ): Response<Any> // Usamos Any porque puede retornar diferentes tipos

    /**
     * Login tradicional con email y password
     * POST /api/auth/login
     *
     * Retorna:
     * - 200: AuthResponse
     * - 401: ErrorResponse
     */
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    /**
     * Registrar nuevo paciente
     * POST /api/auth/register/paciente
     *
     * Retorna:
     * - 201: AuthResponse
     * - 400: ErrorResponse
     */
    @POST("api/auth/register/paciente")
    suspend fun registerPaciente(
        @Body request: RegisterPacienteRequest
    ): Response<AuthResponse>

    /**
     * Registrar nuevo empleado (Doctor/Admin)
     * POST /api/auth/register/empleado
     *
     * Retorna:
     * - 201: AuthResponse
     * - 400/403: ErrorResponse
     */
    @POST("api/auth/register/empleado")
    suspend fun registerEmpleado(
        @Body request: RegisterEmpleadoRequest
    ): Response<AuthResponse>

    /**
     * Completar perfil después de Google OAuth
     * POST /api/auth/complete-profile
     *
     * Retorna:
     * - 200: AuthResponse
     * - 400: ErrorResponse
     */
    @POST("api/auth/complete-profile")
    suspend fun completeProfile(
        @Body request: CompleteProfileRequest
    ): Response<AuthResponse>

    /**
     * Obtener información del usuario actual (con token)
     * GET /api/user/me
     */
    @GET("api/user/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String // "Bearer {accessToken}"
    ): Response<UserInfo>

    @POST("api/auth/forgot-password")
    suspend fun solicitarRecuperacionPassword(
        @Body request: RecuperarPasswordRequest
    ): Response<MessageResponse>

    data class RecuperarPasswordRequest(
        @SerializedName("email")
        val email: String
    )
    @PUT("api/user/profile")
    suspend fun actualizarPerfil(
        @Header("Authorization") token: String,
        @Body request: ActualizarPerfilRequest
    ): Response<UserInfo>

    data class ActualizarPerfilRequest(
        @SerializedName("telefono")
        val telefono: String?,
        @SerializedName("imagenPerfil")
        val imagenPerfil: String?  // Base64 o URL
    )
}