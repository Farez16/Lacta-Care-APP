package com.example.lactacare.datos.repository

import android.content.Intent
import com.example.lactacare.datos.dto.*
import com.example.lactacare.dominio.repository.AuthRepository
import com.example.lactacare.dominio.model.Paciente
import com.example.lactacare.datos.network.AuthApiService
import com.example.lactacare.datos.network.ApiResponseHandler
import com.example.lactacare.datos.local.SessionManager
import com.example.lactacare.datos.dto.LoginRequest
import com.example.lactacare.datos.dto.RegisterPacienteRequest
import com.example.lactacare.datos.dto.AuthResponseDto
import com.example.lactacare.datos.dto.CompleteProfileRequest
import com.example.lactacare.dominio.model.RolUsuario
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import javax.inject.Inject
import com.example.lactacare.datos.dto.GoogleAuthRequest

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApiService,
    private val sessionManager: SessionManager,
    private val responseHandler: ApiResponseHandler,
    private val googleSignInClient: GoogleSignInClient // <--- NUEVA INYECCIÓN
) : AuthRepository {

    // --- LOGIN NORMAL ---
    override suspend fun login(correo: String, pass: String, rol: RolUsuario): Result<Unit> {
        return try {
            val request = LoginRequest(correo, pass)
            val response = api.login(request)

            // Usamos handleSuccess pero mapeamos a Unit al final
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                // VALIDACIÓN DE SEGURIDAD DE ROL
                // Asumimos que el backend devuelve el rol en authResponse.userInfo.role
                // "PACIENTE", "DOCTOR", "ADMINISTRADOR"
                if (!roleMatches(authResponse.userInfo.role, rol)) {
                    return Result.failure(Exception("No tienes permisos para acceder como ${rol.name}"))
                }

                guardarSesionLocalmente(authResponse)
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error en login"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- GOOGLE: OBTENER INTENT ---
    override fun getGoogleSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    // --- GOOGLE: PROCESAR LOGIN ---
    override suspend fun loginWithGoogle(intent: Intent?): Result<AuthState> {
        return try {
            // 1. Extraer cuenta de Google del Intent
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val googleAccount = task.getResult(ApiException::class.java)

            val idToken = googleAccount?.idToken
                ?: return Result.failure(Exception("No se pudo obtener el ID Token de Google"))

            // --- CORRECCIÓN AQUÍ ---

            // 1. Usamos GoogleAuthRequest (que es lo que pide tu ApiService)
            val request = GoogleAuthRequest(idToken = idToken)

            // 2. Llamamos a googleLogin (que es como se llama en tu ApiService)
            val response = api.googleLogin(request)

            // -----------------------

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                // Aquí el error de 'userInfo' desaparecerá porque tu AuthResponseDto ya lo tiene
                if (authResponse.userInfo.profileCompleted) {
                    guardarSesionLocalmente(authResponse)
                    Result.success(AuthState.Authenticated)
                } else {
                    // USUARIO NUEVO O INCOMPLETO -> FALTAN DATOS
                    // Construimos el objeto con datos de Google para pre-llenar el formulario
                    val googleData = GoogleUserData(
                        googleId = googleAccount.id ?: "",
                        email = googleAccount.email ?: "",
                        name = googleAccount.displayName ?: "",
                        givenName = googleAccount.givenName,
                        familyName = googleAccount.familyName,
                        picture = googleAccount.photoUrl?.toString()
                    )

                    val incompleteData = ProfileIncompleteData(
                        googleUserData = googleData,
                        googleToken = idToken // Guardamos el token para enviarlo luego
                    )

                    Result.success(AuthState.ProfileIncomplete(incompleteData))
                }
            } else {
                Result.failure(Exception("Error en backend Google: ${response.message()}"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- COMPLETAR PERFIL ---
    override suspend fun completarPerfil(request: CompleteProfileRequest): Result<Unit> {
        return try {
            val response = api.completeProfile(request)
            if (response.isSuccessful && response.body() != null) {
                guardarSesionLocalmente(response.body()!!)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string()))
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
                discapacidad = paciente.discapacidad ?: "Ninguna"
            )

            val response = api.registerPaciente(request)

            if (response.isSuccessful) {
                // Opcional: Auto-login tras registro
                if (response.body() != null) guardarSesionLocalmente(response.body()!!)
                Result.success(true)
            } else {
                Result.failure(Exception(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getUserProfile(): Result<UserProfileDto> {
        return try {
            val response = api.getUserProfile()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(nombre: String?, imagenBase64: String?): Result<Boolean> {
        return try {
            val request = UpdateProfileRequest(nombre, imagenBase64)
            val response = api.updateUserProfile(request)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // --- RECUPERAR PASSWORD ---
    override suspend fun recuperarPassword(email: String): Result<Unit> {
        return try {
            val response = api.recuperarPassword(mapOf("email" to email))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al enviar correo"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- LOGOUT ---
    override suspend fun logout() {
        sessionManager.clearSession()
        googleSignInClient.signOut() // También cerramos sesión en el cliente de Google
    }

    // --- HELPER: GUARDAR ---
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

    // --- HELPER: VALIDAR ROL ---
    private fun roleMatches(backendRole: String, requestedRole: RolUsuario): Boolean {
        // Normalizamos strings para comparar (ej: "PACIENTE" == "PACIENTE")
        return backendRole.equals(requestedRole.name, ignoreCase = true)
    }
}