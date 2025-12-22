package com.example.lactacare.dominio.repository

import android.content.Intent
import com.example.lactacare.datos.dto.AuthState
import com.example.lactacare.dominio.model.Paciente
import com.example.lactacare.datos.dto.CompleteProfileRequest
import com.example.lactacare.datos.dto.UserProfileDto
import com.example.lactacare.dominio.model.RolUsuario

interface AuthRepository {
    // Login normal ahora recibe el ROL para validar
    suspend fun login(correo: String, pass: String, rol: RolUsuario): Result<Unit>

    suspend fun registrarPaciente(paciente: Paciente): Result<Boolean>

    suspend fun completarPerfil(request: CompleteProfileRequest): Result<Unit>

    suspend fun recuperarPassword(email: String): Result<Unit>

    // Métodos de Google
    fun getGoogleSignInIntent(): Intent
    suspend fun loginWithGoogle(intent: Intent?): Result<AuthState>

    suspend fun logout()

    suspend fun getUserProfile(): Result<UserProfileDto>

    suspend fun updateUserProfile(nombre: String?, imagenBase64: String?): Result<Boolean>
}