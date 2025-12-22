package com.example.lactacare.datos.network

import com.example.lactacare.datos.dto.AuthResponseDto
import com.example.lactacare.datos.dto.GoogleAuthRequest
import com.example.lactacare.datos.dto.LoginRequest
import com.example.lactacare.datos.dto.RegisterPacienteRequest
// --- AGREGAMOS ESTE IMPORT ---
import com.example.lactacare.datos.dto.CompleteProfileRequest
import com.example.lactacare.datos.dto.GoogleLoginRequest
import com.example.lactacare.datos.dto.MessageResponseDto
import com.example.lactacare.datos.dto.UpdateProfileRequest
import com.example.lactacare.datos.dto.UserProfileDto
// ----------------------------
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponseDto>

    @POST("api/auth/register/paciente")
    suspend fun registerPaciente(@Body request: RegisterPacienteRequest): Response<AuthResponseDto>

    // Tu backend tiene @PostMapping("/google"), así que usa esta:
    @POST("api/auth/google")
    suspend fun googleLogin(@Body request: GoogleAuthRequest): Response<AuthResponseDto>
    // NOTA: Borra la otra función 'loginGoogle' que tenías duplicada.
    // --- NUEVA LÍNEA PARA COMPLETAR PERFIL ---
    @POST("api/auth/complete-profile")
    suspend fun completeProfile(@Body request: CompleteProfileRequest): Response<AuthResponseDto>

    @POST("api/auth/forgot-password")
    suspend fun recuperarPassword(@Body emailMap: Map<String, String>): Response<AuthResponseDto>

    @GET("api/user/me")
    suspend fun getUserProfile(): Response<UserProfileDto>

    @PUT("api/user/profile")
    suspend fun updateUserProfile(@Body request: UpdateProfileRequest): Response<MessageResponseDto>
}