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
import com.google.gson.Gson
import org.json.JSONObject
import com.example.lactacare.datos.dto.GoogleAccountException
import com.example.lactacare.datos.dto.RolMismatchRecoveryException
import com.example.lactacare.datos.dto.UnauthorizedEmailException
import com.example.lactacare.datos.dto.UserNotFoundException
import com.example.lactacare.datos.dto.PasswordChangeRequiredException
import com.example.lactacare.datos.dto.ChangeTemporaryPasswordRequest

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApiService,
    private val sessionManager: SessionManager,
    private val responseHandler: ApiResponseHandler,
    private val googleSignInClient: GoogleSignInClient
) : AuthRepository {
    // ========================================================================
    // LOGIN TRADICIONAL (MEJORADO)
    // ========================================================================
    override suspend fun login(correo: String, pass: String, rol: RolUsuario): Result<Unit> {
        return try {
            val request = LoginRequest(correo, pass, rol.name)
            val response = api.login(request)

            when (response.code()) {
                200 -> {
                    // Login exitoso normal
                    val body = response.body()!!
                    val userData = body.data ?: return Result.failure(Exception("No se recibieron datos del usuario"))
                    val token = userData["access_token"] as? String ?: ""
                    val id = (userData["id"] as? Double)?.toLong() ?: 0L
                    val name = userData["nombre_completo"] as? String ?: ""
                    val roleStr = userData["rol"] as? String ?: ""
                    sessionManager.saveAuthData(
                        token = token,
                        id = id,
                        name = name,
                        role = roleStr,
                        completed = true
                    )
                    Result.success(Unit)
                }
                202 -> {
                    // ⭐ NUEVO: Requiere cambio de contraseña temporal
                    val body = response.body()
                    val data = body?.data
                    if (data?.get("status") == "PASSWORD_CHANGE_REQUIRED") {
                        val tempToken = data["tempToken"] as? String ?: ""
                        val correoEmpleado = data["correo"] as? String ?: ""
                        val rolEmpleado = data["rol"] as? String ?: ""
                        val mensaje = data["message"] as? String ?: ""

                        throw PasswordChangeRequiredException(
                            tempToken, correoEmpleado, rolEmpleado, mensaje
                        )
                    }
                    Result.failure(Exception("Respuesta inesperada"))
                }
                else -> {
                    // MEJORA: Clasificar el error según el código del backend
                    val errorState = parseLoginError(response)
                    Result.failure(AuthStateException(errorState))
                }
            }
        } catch (e: PasswordChangeRequiredException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================================================
    // GOOGLE OAUTH2 (MEJORADO)
    // ========================================================================
    /**
     * MEJORA 1: Forzar selector de cuentas
     * Hace signOut() antes de retornar el intent para que siempre muestre el selector
     */
    override fun getGoogleSignInIntent(): Intent {
        // Limpiar la sesión anterior para forzar selección de cuenta
        googleSignInClient.signOut()
        return googleSignInClient.signInIntent
    }

    override suspend fun loginWithGoogle(intent: Intent?, rol: RolUsuario): Result<AuthState> {
        return try {
            // 1. Obtener token de Google
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val googleAccount = task.getResult(ApiException::class.java)
            val idToken = googleAccount?.idToken
                ?: return Result.failure(Exception("No se pudo obtener el ID Token de Google"))
            val request = GoogleAuthRequest(
                idToken = idToken,
                platform = "ANDROID",
                tipoUsuario = rol.name  // ⭐ NUEVO
            )
            // 2. Decidir endpoint según rol
            val response = when (rol) {
                RolUsuario.PACIENTE -> api.googleLogin(request)
                RolUsuario.MEDICO, RolUsuario.ADMINISTRADOR -> api.googleLoginEmpleado(request)
            }
            // 3. Manejo de respuestas
            when (response.code()) {
                200 -> {
                    val body = response.body()
                    if (body != null && body.userInfo != null) {
                        guardarSesionLocalmente(body)
                        return Result.success(AuthState.Authenticated)
                    }
                    Result.failure(Exception("Respuesta del servidor incompleta"))
                }
                202 -> {
                    val body = response.body()
                    if (body != null && body.status == "USER_PROFILE_INCOMPLETE") {
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
                    Result.failure(Exception("Perfil incompleto pero sin datos"))
                }
                403 -> {
                    // MEJORA: Clasificar el error 403
                    val errorState = parseGoogleError403(response)
                    return Result.success(errorState)
                }
                401 -> {
                    Result.success(AuthState.GenericError("Token de Google inválido o expirado"))
                }
                else -> {
                    val errorMsg = response.errorBody()?.string()
                        ?: "Error en backend Google: ${response.code()}"
                    Result.success(AuthState.GenericError(errorMsg))
                }
            }
        } catch (e: ApiException) {
            Result.failure(Exception("Error de Google Sign-In: ${e.message}"))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ========================================================================
    // COMPLETAR PERFIL
    // ========================================================================
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

    // ========================================================================
    // REGISTRO PACIENTE
    // ========================================================================
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
                if (response.body() != null) guardarSesionLocalmente(response.body()!!)
                Result.success(true)
            } else {
                Result.failure(Exception(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================================================
    // PERFIL
    // ========================================================================
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

    // ========================================================================
    // RECUPERAR PASSWORD
    // ========================================================================
    override suspend fun solicitarCodigoRecuperacion(
        correo: String,
        rol: RolUsuario
    ): Result<String> {
        return try {
            val response = when (rol) {
                RolUsuario.PACIENTE -> api.forgotPasswordPaciente(correo)
                RolUsuario.MEDICO, RolUsuario.ADMINISTRADOR ->
                    api.forgotPasswordEmpleado(correo)
            }
            when (response.code()) {
                200 -> {
                    // Éxito
                    Result.success(response.body()!!.message)
                }
                400 -> {
                    // Error 400: Formato inválido o cuenta Google
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = parseErrorMessage(errorBody) ?: "Error al enviar código"
                    if (errorMsg.contains("GOOGLE_ACCOUNT")) {
                        Result.failure(GoogleAccountException(
                            errorMsg.replace("GOOGLE_ACCOUNT: ", "")
                        ))
                    } else {
                        Result.failure(Exception(errorMsg))
                    }
                }
                403 -> {
                    // Error 403: Rol incorrecto o correo no autorizado
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = parseErrorMessage(errorBody) ?: "Acceso denegado"
                    when {
                        errorMsg.contains("ROL_MISMATCH") -> {
                            Result.failure(RolMismatchRecoveryException(
                                errorMsg.replace("ROL_MISMATCH: ", "")
                            ))
                        }
                        errorMsg.contains("CORREO_NO_AUTORIZADO") -> {
                            Result.failure(UnauthorizedEmailException(
                                errorMsg.replace("CORREO_NO_AUTORIZADO: ", "")
                            ))
                        }
                        else -> {
                            Result.failure(Exception(errorMsg))
                        }
                    }
                }
                404 -> {
                    // Error 404: Usuario no encontrado (solo pacientes)
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = parseErrorMessage(errorBody) ?: "Usuario no encontrado"
                    Result.failure(UserNotFoundException(
                        errorMsg.replace("USUARIO_NO_ENCONTRADO: ", "")
                    ))
                }
                else -> {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = parseErrorMessage(errorBody) ?: "Error desconocido"
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper para parsear mensajes de error
    private fun parseErrorMessage(errorBody: String?): String? {
        return try {
            if (errorBody != null) {
                val json = JSONObject(errorBody)
                json.optString("message", null)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun verificarCodigo(
        correo: String,
        codigo: String,
        rol: RolUsuario
    ): Result<String> {
        return try {
            val request = VerifyCodeRequest(correo, codigo)
            val response = when (rol) {
                RolUsuario.PACIENTE -> api.verifyResetCodePaciente(request)
                RolUsuario.MEDICO, RolUsuario.ADMINISTRADOR ->
                    api.verifyResetCodeEmpleado(request)
            }
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.resetToken != null) {
                    Result.success(body.resetToken)
                } else {
                    Result.failure(Exception(body.message ?: "Código inválido"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = parseErrorMessage(errorBody) ?: "Error al verificar código"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cambiarPassword(
        resetToken: String,
        nuevaPassword: String,
        rol: RolUsuario
    ): Result<Unit> {
        return try {
            val request = ResetPasswordWithCodeRequest(resetToken, nuevaPassword)
            val response = when (rol) {
                RolUsuario.PACIENTE -> api.resetPasswordPaciente(request)
                RolUsuario.MEDICO, RolUsuario.ADMINISTRADOR ->
                    api.resetPasswordEmpleado(request)
            }
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(body.message))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = parseErrorMessage(errorBody) ?: "Error al cambiar contraseña"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================================================
    // CAMBIAR CONTRASEÑA TEMPORAL
    // ========================================================================

    override suspend fun changeTemporaryPassword(
        token: String,
        correo: String,
        currentPassword: String,
        newPassword: String
    ): Result<Unit> {
        return try {
            val request = ChangeTemporaryPasswordRequest(
                correo = correo,
                temporaryPassword = currentPassword,  // Mapeo: currentPassword -> temporaryPassword
                newPassword = newPassword
            )

            val response = api.changeTemporaryPassword("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(body.message))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = try {
                    val json = JSONObject(errorBody ?: "")
                    json.optString("message", "Error al cambiar contraseña")
                } catch (e: Exception) {
                    "Error al cambiar contraseña"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================================================
    // LOGOUT
    // ========================================================================
    override suspend fun logout() {
        sessionManager.clearSession()
        googleSignInClient.signOut()
    }

    // ========================================================================
    // HELPERS PRIVADOS (MEJORADOS)
    // ========================================================================
    private suspend fun guardarSesionLocalmente(authResponse: AuthResponseDto) {
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

    /**
     * MEJORA: Parsear errores de login tradicional
     * Clasifica el error según el código del backend
     */
    private fun parseLoginError(response: retrofit2.Response<AuthResponseDto>): AuthState {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val json = JSONObject(errorBody)
                val code = json.optString("code", "")
                val message = json.optString("message", "Error desconocido")
                when (code) {
                    "USUARIO_NO_REGISTRADO" -> AuthState.UserNotFound(message)
                    "CONTRASEÑA_INCORRECTA", "CREDENCIALES_INVALIDAS" -> AuthState.InvalidCredentials(message)
                    "ROL_MISMATCH" -> {
                        val rolCorrecto = json.optString("rolCorrecto", "")
                        AuthState.RolMismatch(message, rolCorrecto)
                    }
                    else -> AuthState.GenericError(message)
                }
            } else {
                AuthState.GenericError("Error al iniciar sesión")
            }
        } catch (e: Exception) {
            AuthState.GenericError("Error al procesar respuesta del servidor")
        }
    }

    /**
     * MEJORA: Parsear errores 403 de Google
     */
    private fun parseGoogleError403(response: retrofit2.Response<AuthResponseDto>): AuthState {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val error = Gson().fromJson(errorBody, RolMismatchError::class.java)
                when (error.error) {
                    "ROL_MISMATCH" -> AuthState.RolMismatch(error.message, error.rolCorrecto)
                    "UNAUTHORIZED_EMAIL" -> AuthState.UnauthorizedEmail(error.message)
                    else -> AuthState.GenericError(error.message)
                }
            } else {
                AuthState.GenericError("Acceso denegado")
            }
        } catch (e: Exception) {
            AuthState.GenericError("Error al procesar respuesta: ${e.message}")
        }
    }
}
/**
 * Excepción personalizada que contiene un AuthState
 * Permite propagar estados de error específicos
 */
class AuthStateException(val authState: AuthState) : Exception(
    when (authState) {
        is AuthState.RolMismatch -> authState.mensaje
        is AuthState.UnauthorizedEmail -> authState.mensaje
        is AuthState.UserNotFound -> authState.mensaje
        is AuthState.InvalidCredentials -> authState.mensaje
        is AuthState.GenericError -> authState.mensaje
        else -> "Error de autenticación"
    }
)