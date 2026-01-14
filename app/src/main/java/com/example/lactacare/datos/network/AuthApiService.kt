package com.example.lactacare.datos.network

import com.example.lactacare.datos.dto.ApiResponse
import com.example.lactacare.datos.dto.AuthResponseDto
import com.example.lactacare.datos.dto.ChangeTemporaryPasswordRequest
import com.example.lactacare.datos.dto.GoogleAuthRequest
import com.example.lactacare.datos.dto.LoginRequest
import com.example.lactacare.datos.dto.RegisterPacienteRequest
// --- AGREGAMOS ESTE IMPORT ---
import com.example.lactacare.datos.dto.CompleteProfileRequest
import com.example.lactacare.datos.dto.MessageResponseDto
import com.example.lactacare.datos.dto.ResetPasswordWithCodeRequest
import com.example.lactacare.datos.dto.UpdateProfileRequest
import com.example.lactacare.datos.dto.UserProfileDto
import com.example.lactacare.datos.dto.VerifyCodeRequest
import com.example.lactacare.datos.dto.VerifyCodeResponse
// ----------------------------
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.Path

interface AuthApiService {

    // ========================================================================
    // LOGIN TRADICIONAL (Email + Password)
    // ========================================================================

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponseDto>
    // ========================================================================
    // REGISTRO
    // ========================================================================

    @POST("api/auth/register/paciente")
    suspend fun registerPaciente(@Body request: RegisterPacienteRequest): Response<AuthResponseDto>
    // ========================================================================
    // GOOGLE OAUTH2 - PACIENTES
    // ========================================================================

    /**
     * Login con Google OAuth2 para PACIENTES
     * Endpoint: POST /api/auth/google
     *
     * Respuestas:
     * - 200 OK: Login exitoso, retorna tokens
     * - 202 Accepted: Perfil incompleto, necesita completar datos
     * - 403 Forbidden: ROL_MISMATCH (el usuario es empleado, no paciente)
     */
    @POST("api/auth/google")
    suspend fun googleLogin(@Body request: GoogleAuthRequest): Response<AuthResponseDto>
    // ========================================================================
    // GOOGLE OAUTH2 - EMPLEADOS (NUEVO)
    // ========================================================================

    /**
     * Login con Google OAuth2 para EMPLEADOS (Médicos y Administradores)
     * Endpoint: POST /api/auth/google/empleado
     *
     * IMPORTANTE: Solo correos autorizados (lista blanca en backend)
     *
     * Respuestas:
     * - 200 OK: Login exitoso, retorna tokens
     * - 403 Forbidden con ROL_MISMATCH: El usuario es paciente, no empleado
     * - 403 Forbidden con UNAUTHORIZED_EMAIL: Correo no autorizado
     * - 401 Unauthorized: Token de Google inválido
     */
    @POST("api/auth/google/empleado")
    suspend fun googleLoginEmpleado(@Body request: GoogleAuthRequest): Response<AuthResponseDto>
    // ========================================================================
    // COMPLETAR PERFIL (Google)
    // ========================================================================

    @POST("api/auth/complete-profile")
    suspend fun completeProfile(@Body request: CompleteProfileRequest): Response<AuthResponseDto>
    // ========================================================================
    // RECUPERACIÓN DE CONTRASEÑA
    // ========================================================================
    // Para PACIENTES
    @POST("api/auth/forgot-password")
    suspend fun forgotPasswordPaciente(@Query("correo") correo: String): Response<ApiResponse>
    @POST("api/auth/verify-reset-code")
    suspend fun verifyResetCodePaciente(@Body request: VerifyCodeRequest): Response<VerifyCodeResponse>
    @POST("api/auth/reset-password-with-code")
    suspend fun resetPasswordPaciente(@Body request: ResetPasswordWithCodeRequest): Response<ApiResponse>
    // Para EMPLEADOS
    @POST("api/auth/empleado/forgot-password")
    suspend fun forgotPasswordEmpleado(@Query("correo") correo: String): Response<ApiResponse>
    @POST("api/auth/empleado/verify-reset-code")
    suspend fun verifyResetCodeEmpleado(@Body request: VerifyCodeRequest): Response<VerifyCodeResponse>
    @POST("api/auth/empleado/reset-password-with-code")
    suspend fun resetPasswordEmpleado(@Body request: ResetPasswordWithCodeRequest): Response<ApiResponse>
    // ========================================================================
    // PERFIL DE USUARIO
    // ========================================================================

    @GET("api/user/me")
    suspend fun getUserProfile(): Response<UserProfileDto>
    @PUT("api/user/profile")
    suspend fun updateUserProfile(@Body request: UpdateProfileRequest): Response<MessageResponseDto>
    // ========================================================================
    // CAMBIO DE CONTRASEÑA TEMPORAL
    // ========================================================================
    @POST("api/auth/empleado/change-temporary-password")
    suspend fun changeTemporaryPassword(
        @Header("Authorization") token: String, @Body request: ChangeTemporaryPasswordRequest): Response<ApiResponse>
    @GET("api/empleados/correo/{correo}")
    suspend fun getEmpleadoPorCorreo(@Path("correo") correo: String): com.example.lactacare.datos.dto.PersonaEmpleadoResponseDto
}