package com.example.lactacare.datos.repository

import com.example.lactacare.dominio.repository.AuthRepository
import com.example.lactacare.dominio.model.Paciente
import com.example.lactacare.datos.network.AuthApiService
import com.example.lactacare.datos.network.ApiResponseHandler
import com.example.lactacare.datos.local.SessionManager
import com.example.lactacare.datos.dto.LoginRequest
import com.example.lactacare.datos.dto.RegisterPacienteRequest
import com.example.lactacare.datos.dto.AuthResponseDto
import com.example.lactacare.datos.dto.CompleteProfileRequest
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApiService,
    private val sessionManager: SessionManager,
    private val responseHandler: ApiResponseHandler
) : AuthRepository {

    // --- LOGIN ---
    override suspend fun login(correo: String, pass: String): Result<Boolean> {
        return try {
            val request = LoginRequest(correo, pass)
            val response = api.login(request)

            responseHandler.handleSuccess(response) { authResponse ->
                guardarSesionLocalmente(authResponse)
                true
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- REGISTRO PACIENTE ---
    override suspend fun registrarPaciente(paciente: Paciente): Result<Boolean> {
        return try {
            val request = RegisterPacienteRequest(
                cedula = paciente.cedula,
                primerNombre = paciente.primerNombre,
                segundoNombre = paciente.segundoNombre,
                primerApellido = paciente.primerApellido,
                segundoApellido = paciente.segundoApellido,
                correo = paciente.correo,
                telefono = paciente.telefono,
                fechaNacimiento = paciente.fechaNacimiento,
                password = paciente.password,
                discapacidad = if (paciente.discapacidad == "si" || paciente.discapacidad == "true") "true" else "false"
            )

            val response = api.registerPaciente(request)

            responseHandler.handleSuccess(response) { authResponse ->
                guardarSesionLocalmente(authResponse)
                true
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- COMPLETAR PERFIL ---
    override suspend fun completarPerfil(request: CompleteProfileRequest): Result<Boolean> {
        return try {
            val response = api.completeProfile(request)

            responseHandler.handleSuccess(response) { authResponse ->
                guardarSesionLocalmente(authResponse)
                true
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- RECUPERAR PASSWORD (AGREGADO) ---
    override suspend fun recuperarPassword(email: String): Result<Boolean> {
        return try {
            // Creamos un mapa simple {"email": "correo@ejemplo.com"}
            val body = mapOf("email" to email)

            // Llamamos a la API (Asegúrate de haber agregado esta función en AuthApiService)
            val response = api.recuperarPassword(body)

            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- LOGOUT ---
    override suspend fun logout() {
        sessionManager.clearSession()
    }

    // --- FUNCIÓN PRIVADA PARA GUARDAR DATOS ---
    private suspend fun guardarSesionLocalmente(authResponse: AuthResponseDto) {
        val info = authResponse.userInfo
        sessionManager.saveAuthData(
            token = authResponse.accessToken,
            id = info.id,
            name = info.fullName,
            role = info.role,
            completed = info.profileCompleted
        )
    }
}