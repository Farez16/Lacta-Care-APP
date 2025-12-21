package com.example.lactacare.datos.repository

import android.content.Context
import com.example.lactacare.datos.datos.local.SessionManager
import com.example.lactacare.datos.dto.*
import com.example.lactacare.datos.network.ApiResponseHandler
import com.example.lactacare.datos.network.AuthApiService
import com.example.lactacare.datos.network.RetrofitClient
import com.example.lactacare.dominio.model.Administrador
import com.example.lactacare.dominio.model.Medico
import com.example.lactacare.dominio.model.Paciente
import com.example.lactacare.dominio.repository.AdminRepository
import com.example.lactacare.dominio.repository.MedicoRepository
import com.example.lactacare.dominio.repository.PacienteRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementación real del repositorio de autenticación
 * Conecta con el backend Spring Boot
 * ACTUALIZADO con soporte para Google Sign-In
 */
class AuthRepositoryImpl(private val context: Context) :
    PacienteRepository,
    MedicoRepository,
    AdminRepository {

    private val apiService: AuthApiService by lazy {
        RetrofitClient.getAuthService(context)
    }

    private val sessionManager: SessionManager by lazy {
        SessionManager(context)
    }

    private val gson = Gson()

    // ==================== GOOGLE SIGN-IN ====================

    /**
     * Autentica con Google usando el ID Token
     * @param idToken Token de Google obtenido después del sign-in
     * @return Result con AuthResponse o ProfileIncompleteResponse
     */
    suspend fun loginWithGoogle(idToken: String): Result<Any> = withContext(Dispatchers.IO) {
        try {
            val request = GoogleAuthRequest(
                idToken = idToken,
                platform = "ANDROID"
            )

            val response = apiService.loginWithGoogle(request)

            if (response.isSuccessful) {
                val body = response.body()

                // El backend puede retornar AuthResponse o ProfileIncompleteResponse
                when {
                    response.code() == 200 -> {
                        // Usuario existente con perfil completo
                        val authResponse = gson.fromJson(
                            gson.toJson(body),
                            AuthResponse::class.java
                        )
                        sessionManager.saveUserSession(authResponse)
                        Result.success(authResponse)
                    }
                    response.code() == 202 -> {
                        // Usuario nuevo, perfil incompleto
                        val profileIncomplete = gson.fromJson(
                            gson.toJson(body),
                            ProfileIncompleteResponse::class.java
                        )
                        Result.success(profileIncomplete)
                    }
                    else -> {
                        Result.failure(Exception("Respuesta inesperada del servidor"))
                    }
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = try {
                    gson.fromJson(errorBody, ErrorResponse::class.java)
                } catch (e: Exception) {
                    ErrorResponse("ERROR", "Error del servidor", null, response.code(), null)
                }
                Result.failure(Exception(errorResponse.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Completa el perfil después de Google Sign-In
     * Se usa cuando el usuario es nuevo y necesita completar datos
     */
    suspend fun completeProfile(request: CompleteProfileRequest): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            when (val result = ApiResponseHandler.safeApiCall { apiService.completeProfile(request) }) {
                is Resource.Success -> {
                    sessionManager.saveUserSession(result.data)
                    Result.success(true)
                }
                is Resource.Error -> {
                    Result.failure(Exception(result.message))
                }
                is Resource.Loading -> {
                    Result.failure(Exception("Loading state unexpected"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== PACIENTE ====================

    override suspend fun registrar(paciente: Paciente): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterPacienteRequest(
                cedula = paciente.cedula,
                primerNombre = paciente.primerNombre,
                segundoNombre = paciente.segundoNombre,
                primerApellido = paciente.primerApellido,
                segundoApellido = paciente.segundoApellido,
                correo = paciente.correo,
                password = paciente.password,
                telefono = paciente.telefono,
                fechaNacimiento = paciente.fechaNacimiento,
                discapacidad = paciente.discapacidad
            )

            when (val result = ApiResponseHandler.safeApiCall { apiService.registerPaciente(request) }) {
                is Resource.Success -> {
                    sessionManager.saveUserSession(result.data)
                    Result.success(true)
                }
                is Resource.Error -> {
                    Result.failure(Exception(result.message))
                }
                is Resource.Loading -> {
                    Result.failure(Exception("Loading state unexpected"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(correo: String, clave: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = LoginRequest(correo = correo, password = clave)

            when (val result = ApiResponseHandler.safeApiCall { apiService.login(request) }) {
                is Resource.Success -> {
                    sessionManager.saveUserSession(result.data)
                    Result.success(true)
                }
                is Resource.Error -> {
                    Result.failure(Exception(result.message))
                }
                is Resource.Loading -> {
                    Result.failure(Exception("Loading state unexpected"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerPacientePorId(id: Int): Paciente? = withContext(Dispatchers.IO) {
        try {
            val session = sessionManager.getUserSession() ?: return@withContext null
            val token = "Bearer ${session.accessToken}"

            when (val result = ApiResponseHandler.safeApiCall { apiService.getCurrentUser(token) }) {
                is Resource.Success -> {
                    val userInfo = result.data
                    Paciente(
                        idPaciente = userInfo.id.toInt(),
                        correo = userInfo.correo,
                        primerNombre = extractFirstName(userInfo.nombreCompleto),
                        primerApellido = extractLastName(userInfo.nombreCompleto),
                        fotoPerfil = userInfo.imagenPerfil ?: "",
                        discapacidad = null
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    // ==================== MEDICO ====================

    override suspend fun registrar(medico: Medico): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterEmpleadoRequest(
                cedula = medico.cedula,
                primerNombre = medico.primerNombre,
                segundoNombre = medico.segundoNombre,
                primerApellido = medico.primerApellido,
                segundoApellido = medico.segundoApellido,
                correo = medico.correo,
                password = medico.password,
                telefono = medico.telefono,
                fechaNacimiento = medico.fechaNacimiento,
                codigoCredencial = "DOCTOR2025"
            )

            when (val result = ApiResponseHandler.safeApiCall { apiService.registerEmpleado(request) }) {
                is Resource.Success -> {
                    sessionManager.saveUserSession(result.data)
                    Result.success(true)
                }
                is Resource.Error -> {
                    Result.failure(Exception(result.message))
                }
                is Resource.Loading -> {
                    Result.failure(Exception("Loading state unexpected"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerMedicoPorId(id: Int): Medico? = withContext(Dispatchers.IO) {
        try {
            val session = sessionManager.getUserSession() ?: return@withContext null
            val token = "Bearer ${session.accessToken}"

            when (val result = ApiResponseHandler.safeApiCall { apiService.getCurrentUser(token) }) {
                is Resource.Success -> {
                    val userInfo = result.data
                    Medico(
                        idEmpleado = userInfo.id.toInt(),
                        correo = userInfo.correo,
                        primerNombre = extractFirstName(userInfo.nombreCompleto),
                        primerApellido = extractLastName(userInfo.nombreCompleto),
                        fotoPerfil = userInfo.imagenPerfil ?: "",
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    // ==================== ADMINISTRADOR ====================

    override suspend fun registrar(admin: Administrador): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterEmpleadoRequest(
                cedula = admin.cedula,
                primerNombre = admin.primerNombre,
                segundoNombre = admin.segundoNombre,
                primerApellido = admin.primerApellido,
                segundoApellido = admin.segundoApellido,
                correo = admin.correo,
                password = admin.password,
                telefono = admin.telefono,
                fechaNacimiento = admin.fechaNacimiento,
                codigoCredencial = "ADMIN2025"
            )

            when (val result = ApiResponseHandler.safeApiCall { apiService.registerEmpleado(request) }) {
                is Resource.Success -> {
                    sessionManager.saveUserSession(result.data)
                    Result.success(true)
                }
                is Resource.Error -> {
                    Result.failure(Exception(result.message))
                }
                is Resource.Loading -> {
                    Result.failure(Exception("Loading state unexpected"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerAdminPorId(id: Int): Administrador? = withContext(Dispatchers.IO) {
        try {
            val session = sessionManager.getUserSession() ?: return@withContext null
            val token = "Bearer ${session.accessToken}"

            when (val result = ApiResponseHandler.safeApiCall { apiService.getCurrentUser(token) }) {
                is Resource.Success -> {
                    val userInfo = result.data
                    Administrador(
                        idEmpleado = userInfo.id.toInt(),
                        correo = userInfo.correo,
                        primerNombre = extractFirstName(userInfo.nombreCompleto),
                        primerApellido = extractLastName(userInfo.nombreCompleto),
                        fotoPerfil = userInfo.imagenPerfil ?: "",
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    // ==================== UTILIDADES ====================

    private fun extractFirstName(nombreCompleto: String): String {
        return nombreCompleto.split(" ").firstOrNull() ?: ""
    }

    private fun extractLastName(nombreCompleto: String): String {
        val parts = nombreCompleto.split(" ")
        return if (parts.size > 1) parts[1] else ""
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        sessionManager.clearSession()
    }

    fun getCurrentSession(): UserSession? {
        return sessionManager.getUserSession()
    }

    fun isLoggedIn(): Boolean {
        return sessionManager.getUserSession() != null
    }
}