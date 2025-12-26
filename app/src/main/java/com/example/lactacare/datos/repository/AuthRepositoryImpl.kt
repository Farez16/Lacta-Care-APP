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
    private val googleSignInClient: GoogleSignInClient
) : AuthRepository {

    // --- LOGIN NORMAL ---
    override suspend fun login(correo: String, pass: String, rol: RolUsuario): Result<Unit> {
        return try {
            // 1. Enviamos el nombre del rol (PACIENTE, MEDICO, ADMINISTRADOR)
            val request = LoginRequest(correo, pass, rol.name)
            val response = api.login(request)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val userData = body.data ?: return Result.failure(Exception("No se recibieron datos del usuario"))

                // 2. Extraemos los datos usando las llaves EXACTAS que puso el backend en Java
                val token = userData["access_token"] as? String ?: ""
                val id = (userData["id"] as? Double)?.toLong() ?: 0L // Retrofit/Gson lee números como Double
                val name = userData["nombre_completo"] as? String ?: ""
                val roleStr = userData["rol"] as? String ?: ""

                // 3. Guardamos en SessionManager
                sessionManager.saveAuthData(
                    token = token,
                    id = id,
                    name = name,
                    role = roleStr,
                    completed = true
                )
                Result.success(Unit)
            } else {
                val errorMsg = responseHandler.parseErrorBody(response) // Usamos tu handler
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

    // --- GOOGLE: PROCESAR LOGIN (CORREGIDO Y ADAPTADO AL DTO) ---
    override suspend fun loginWithGoogle(intent: Intent?): Result<AuthState> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val googleAccount = task.getResult(ApiException::class.java)
            val idToken = googleAccount?.idToken
                ?: return Result.failure(Exception("No se pudo obtener el ID Token de Google"))

            val request = GoogleAuthRequest(idToken = idToken)
            val response = api.googleLogin(request)

            // Aceptamos 200 (OK) y 202 (Accepted/Incompleto)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!

                // CASO 1: PERFIL INCOMPLETO (Status 202 o flag explícito)
                // Verificamos código 202 O el status string O mensaje específico
                if (response.code() == 202 || body.status == "USER_PROFILE_INCOMPLETE") {

                    // Verificamos que googleUserData no sea null. Si es null, lo creamos con datos del SDK
                    val googleData = body.googleUserData ?: GoogleUserData(
                        googleId = googleAccount.id ?: "",
                        email = googleAccount.email ?: "",
                        name = googleAccount.displayName ?: "",
                        givenName = googleAccount.givenName,
                        familyName = googleAccount.familyName,
                        picture = googleAccount.photoUrl?.toString()
                    )

                    return Result.success(AuthState.ProfileIncomplete(
                        ProfileIncompleteData(googleData, idToken)
                    ))
                }

                // CASO 2: LOGIN EXITOSO (Status 200 y userInfo existe)
                if (body.userInfo != null) {
                    guardarSesionLocalmente(body)
                    return Result.success(AuthState.Authenticated)
                }

                // Si llegamos aquí, la respuesta no se entendió
                return Result.failure(Exception("Respuesta del servidor desconocida."))

            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error en backend Google: ${response.code()}"
                Result.failure(Exception(errorMsg))
            }

        } catch (e: Exception) {
            e.printStackTrace()
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
        googleSignInClient.signOut()
    }

    // --- HELPER: GUARDAR (CORREGIDO) ---
    private suspend fun guardarSesionLocalmente(authResponse: AuthResponseDto) {
        // CORRECCIÓN: Como accessToken y userInfo son nullable (?), debemos
        // usar el operador Elvis (?:) para evitar guardar nulos o salir de la función.
        val token = authResponse.accessToken ?: return
        val info = authResponse.userInfo ?: return

        sessionManager.saveAuthData(
            token = token,
            id = info.id,
            name = info.fullName,
            role = info.role,
            completed = info.profileCompleted
        )
    }

    // --- HELPER: VALIDAR ROL ---
    private fun roleMatches(backendRole: String, requestedRole: RolUsuario): Boolean {
        return backendRole.equals(requestedRole.name, ignoreCase = true)
    }
}