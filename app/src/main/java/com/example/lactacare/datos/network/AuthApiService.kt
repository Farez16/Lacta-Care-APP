package com.example.lactacare.datos.network

import com.example.lactacare.datos.dto.AuthResponseDto
import com.example.lactacare.datos.dto.GoogleAuthRequest
import com.example.lactacare.datos.dto.LoginRequest
import com.example.lactacare.datos.dto.RegisterPacienteRequest
// --- AGREGAMOS ESTE IMPORT ---
import com.example.lactacare.datos.dto.CompleteProfileRequest
// ----------------------------
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponseDto>

    @POST("auth/register/paciente")
    suspend fun registerPaciente(@Body request: RegisterPacienteRequest): Response<AuthResponseDto>

    @POST("auth/google")
    suspend fun googleLogin(@Body request: GoogleAuthRequest): Response<AuthResponseDto>

    // --- NUEVA LÍNEA PARA COMPLETAR PERFIL ---
    @POST("auth/complete-profile")
    suspend fun completeProfile(@Body request: CompleteProfileRequest): Response<AuthResponseDto>

    // ... imports ...
// Agrega este endpoint dentro de la interfaz
    @POST("auth/forgot-password")
    suspend fun recuperarPassword(@Body emailMap: Map<String, String>): Response<AuthResponseDto>
}