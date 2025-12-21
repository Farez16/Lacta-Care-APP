package com.example.lactacare.vistas.auth

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.repository.AuthRepositoryImpl
import com.example.lactacare.datos.dto.*
import com.example.lactacare.datos.repository.RolValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.lactacare.dominio.model.RolUsuario

/**
 * ViewModel para autenticación
 * Maneja tanto login tradicional como Google Sign-In
 * ACTUALIZADO: Valida que el usuario solo pueda iniciar sesión desde el rol correcto
 */
class AuthViewModel(
    private val repository: AuthRepositoryImpl,
    private val googleAuthManager: GoogleAuthManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState = _authState.asStateFlow()

    private val _rolActual = MutableStateFlow(RolUsuario.PACIENTE)
    val rolActual = _rolActual.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError = _mensajeError.asStateFlow()

    private val _loginExitoso = MutableStateFlow(false)
    val loginExitoso = _loginExitoso.asStateFlow()

    // Estado para perfil incompleto de Google
    private val _profileIncompleteData = MutableStateFlow<ProfileIncompleteResponse?>(null)
    val profileIncompleteData = _profileIncompleteData.asStateFlow()

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        val session = repository.getCurrentSession()
        if (session != null && !session.isTokenExpired()) {
            _authState.value = AuthState.Authenticated(session)
            _loginExitoso.value = true
        }
    }

    fun setRol(rol: RolUsuario) {
        _rolActual.value = rol
        _mensajeError.value = null
    }

    fun onEmailChange(text: String) {
        _email.value = text
        _mensajeError.value = null
    }

    fun onPasswordChange(text: String) {
        _password.value = text
        _mensajeError.value = null
    }

    // ==================== LOGIN TRADICIONAL CON VALIDACIÓN DE ROL ====================

    fun login() {
        // VALIDACIÓN MEJORADA DE CAMPOS
        if (_email.value.isBlank() && _password.value.isBlank()) {
            _mensajeError.value = "Por favor, ingrese su email y contraseña"
            return
        }

        if (_email.value.isBlank()) {
            _mensajeError.value = "Por favor, ingrese su email"
            return
        }

        if (_password.value.isBlank()) {
            _mensajeError.value = "Por favor, ingrese su contraseña"
            return
        }

        if (!isValidEmail(_email.value)) {
            _mensajeError.value = "El formato del email no es válido"
            return
        }

        if (_password.value.length < 6) {
            _mensajeError.value = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading
            _mensajeError.value = null

            val result = repository.login(_email.value, _password.value)

            _isLoading.value = false

            result.onSuccess {
                // VALIDAR QUE EL ROL DEL USUARIO COINCIDA CON EL PORTAL
                val session = repository.getCurrentSession()
                if (session != null) {
                    if (RolValidator.validarRol(session.rol, _rolActual.value)) {
                        // Rol correcto
                        _authState.value = AuthState.Authenticated(session)
                        _loginExitoso.value = true
                        _mensajeError.value = null
                    } else {
                        // Rol incorrecto - cerrar sesión y mostrar mensaje
                        repository.logout()
                        _authState.value = AuthState.Error(RolValidator.obtenerMensajeError(session.rol))
                        _mensajeError.value = RolValidator.obtenerMensajeError(session.rol)
                        _loginExitoso.value = false
                    }
                }
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Error desconocido")
                _mensajeError.value = error.message ?: "Credenciales incorrectas"
                _loginExitoso.value = false
            }
        }
    }

    // ==================== GOOGLE SIGN-IN CON VALIDACIÓN DE ROL ====================

    /**
     * Obtiene el Intent para Google Sign-In
     * Úsalo con ActivityResultLauncher
     * ACTUALIZADO: Fuerza la selección de cuenta
     */
    fun getGoogleSignInIntent(): Intent {
        // Cerrar sesión local de Google para forzar selección de cuenta
        googleAuthManager.signOut { }
        return googleAuthManager.getSignInIntent()
    }

    /**
     * Maneja el resultado de Google Sign-In
     * Llama a este método desde el ActivityResultLauncher
     * ACTUALIZADO: Valida el rol del usuario
     */
    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading
            _mensajeError.value = null

            val account = googleAuthManager.handleSignInResult(data)

            if (account == null) {
                _isLoading.value = false
                _authState.value = AuthState.Error("Error al obtener cuenta de Google")
                _mensajeError.value = "No se pudo iniciar sesión con Google"
                return@launch
            }

            // Obtener el ID Token
            val idToken = googleAuthManager.getIdToken(account)

            if (idToken == null) {
                _isLoading.value = false
                _authState.value = AuthState.Error("Error al obtener token de Google")
                _mensajeError.value = "No se pudo obtener el token de Google"
                return@launch
            }

            // Enviar al backend
            val result = repository.loginWithGoogle(idToken)

            _isLoading.value = false

            result.onSuccess { response ->
                when (response) {
                    is AuthResponse -> {
                        // Usuario existente con perfil completo
                        val session = repository.getCurrentSession()
                        if (session != null) {
                            // VALIDAR ROL
                            if (RolValidator.validarRol(session.rol, _rolActual.value)) {
                                _authState.value = AuthState.Authenticated(session)
                                _loginExitoso.value = true
                                _mensajeError.value = null
                            } else {
                                // Rol incorrecto
                                repository.logout()
                                googleAuthManager.signOut { }
                                _authState.value = AuthState.Error(RolValidator.obtenerMensajeError(session.rol))
                                _mensajeError.value = RolValidator.obtenerMensajeError(session.rol)
                                _loginExitoso.value = false
                            }
                        }
                    }
                    is ProfileIncompleteResponse -> {
                        // Usuario nuevo, necesita completar perfil
                        _authState.value = AuthState.ProfileIncomplete(
                            response.googleUserData,
                            response.requiredFields
                        )
                        _profileIncompleteData.value = response
                        _mensajeError.value = null
                    }
                }
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Error desconocido")
                _mensajeError.value = error.message ?: "Error al iniciar sesión con Google"
                _loginExitoso.value = false
            }
        }
    }

    /**
     * Completa el perfil después de Google Sign-In
     * ACTUALIZADO: Valida que el rol seleccionado coincida
     */
    fun completeProfile(request: CompleteProfileRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading
            _mensajeError.value = null

            val result = repository.completeProfile(request)

            _isLoading.value = false

            result.onSuccess {
                val session = repository.getCurrentSession()
                if (session != null) {
                    // VALIDAR ROL (el usuario completó perfil con el rol seleccionado)
                    if (RolValidator.validarRol(session.rol, _rolActual.value)) {
                        _authState.value = AuthState.Authenticated(session)
                        _loginExitoso.value = true
                        _mensajeError.value = null
                        _profileIncompleteData.value = null
                    } else {
                        // Esto no debería pasar, pero por seguridad
                        repository.logout()
                        _authState.value = AuthState.Error("Error de configuración de rol")
                        _mensajeError.value = "Hubo un error al configurar su cuenta"
                    }
                }
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Error desconocido")
                _mensajeError.value = error.message ?: "Error al completar perfil"
            }
        }
    }

    // ==================== RECUPERAR CONTRASEÑA ====================

    /**
     * Envía un correo de recuperación de contraseña
     */
    fun enviarRecuperacionPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank()) {
            onError("Por favor, ingrese su email")
            return
        }

        if (!isValidEmail(email)) {
            onError("El formato del email no es válido")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            // TODO: Implementar llamada al backend para enviar correo de recuperación
            // Por ahora simulamos éxito
            kotlinx.coroutines.delay(1500)

            _isLoading.value = false
            onSuccess()
        }
    }

    // ==================== UTILIDADES ====================

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            googleAuthManager.signOut { }
            _authState.value = AuthState.Unauthenticated
            _loginExitoso.value = false
            _email.value = ""
            _password.value = ""
            _mensajeError.value = null
            _profileIncompleteData.value = null
        }
    }

    fun clearError() {
        _mensajeError.value = null
    }

    fun resetLoginSuccess() {
        _loginExitoso.value = false
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Factory para crear el ViewModel con dependencias
     */
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(
                    AuthRepositoryImpl(context),
                    GoogleAuthManager(context)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
