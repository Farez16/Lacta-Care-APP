package com.example.lactacare.dominio.repository

import com.example.lactacare.dominio.model.Paciente
import com.example.lactacare.datos.dto.CompleteProfileRequest

interface AuthRepository {
    suspend fun login(correo: String, pass: String): Result<Boolean>
    suspend fun registrarPaciente(paciente: Paciente): Result<Boolean>
    suspend fun logout()
    suspend fun completarPerfil(request: CompleteProfileRequest): Result<Boolean>
    suspend fun recuperarPassword(email: String): Result<Boolean>
}