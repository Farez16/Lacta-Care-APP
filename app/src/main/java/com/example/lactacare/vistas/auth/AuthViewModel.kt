package com.example.lactacare.vistas.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.dominio.repository.AuthRepository
import com.example.lactacare.dominio.model.RolUsuario
import com.example.lactacare.datos.dto.AuthState
import com.example.lactacare.datos.dto.ProfileIncompleteData
import com.example.lactacare.datos.dto.CompleteProfileRequest
import com.example.lactacare.datos.repository.AuthStateException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.lactacare.datos.dto.PasswordChangeRequiredException

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    // ========================================================================
    // ESTADOS DE UI
    // ========================================================================
    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _rolActual = MutableStateFlow(RolUsuario.PACIENTE)
    val rolActual = _rolActual.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    private val _loginExitoso = MutableStateFlow(false)
    val loginExitoso = _loginExitoso.asStateFlow()

    private val _profileIncompleteData = MutableStateFlow<ProfileIncompleteData?>(null)
    val profileIncompleteData = _profileIncompleteData.asStateFlow()

    // ========================================================================
    // SETTERS
    // ========================================================================
    fun onEmailChange(nuevoEmail: String) { _email.value = nuevoEmail }
    fun onPasswordChange(nuevoPass: String) { _password.value = nuevoPass }
    fun setRol(nuevoRol: RolUsuario) { _rolActual.value = nuevoRol }

    // ========================================================================
    // LOGIN TRADICIONAL (MEJORADO)
    // ========================================================================
    fun login() {
        val mail = _email.value
        val pass = _password.value
        val rol = _rolActual.value

        if (mail.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.GenericError("Por favor completa todos los campos")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Idle // Resetear estado

            val result = authRepository.login(mail, pass, rol)

            result.onSuccess {
                _authState.value = AuthState.Authenticated
                _loginExitoso.value = true
            }.onFailure { error ->
                _isLoading.value = false

                // ⭐ NUEVO: Manejar cambio de contraseña requerido
                if (error is PasswordChangeRequiredException) {
                    _authState.value = AuthState.PasswordChangeRequired(
                        tempToken = error.tempToken,
                        correo = error.correo,
                        rol = error.rol,
                        mensaje = error.message ?: "Debe cambiar su contraseña temporal"
                    )
                    return@onFailure
                }

                // Manejo de otros errores existentes
                if (error is AuthStateException) {
                    _authState.value = error.authState
                } else {
                    _authState.value = AuthState.GenericError(
                        error.message ?: "Error al iniciar sesión"
                    )
                }
            }

            _isLoading.value = false
        }
    }

    // ========================================================================
    // GOOGLE OAUTH2 (MEJORADO)
    // ========================================================================
    fun getGoogleSignInIntent(): Intent = authRepository.getGoogleSignInIntent()

    fun handleGoogleSignInResult(intent: Intent?) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Idle // Resetear estado

            val result = authRepository.loginWithGoogle(intent, _rolActual.value)

            result.onSuccess { estado ->
                _authState.value = estado
                when (estado) {
                    is AuthState.ProfileIncomplete -> {
                        _profileIncompleteData.value = estado.data
                    }
                    is AuthState.Authenticated -> {
                        _loginExitoso.value = true
                    }
                    // Los demás estados (errores) se manejan en la UI
                    else -> {}
                }
            }.onFailure { error ->
                _authState.value = AuthState.GenericError(
                    error.message ?: "Error con Google"
                )
            }
            _isLoading.value = false
        }
    }

    // ========================================================================
    // COMPLETAR PERFIL
    // ========================================================================
    fun completeProfile(request: CompleteProfileRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.completarPerfil(request)
            result.onSuccess {
                _authState.value = AuthState.Authenticated
                _loginExitoso.value = true
                _profileIncompleteData.value = null
            }.onFailure {
                _authState.value = AuthState.GenericError(it.message ?: "Error al completar perfil")
            }
            _isLoading.value = false
        }
    }

    // ========================================================================
    // LOGOUT
    // ========================================================================
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Idle
            _profileIncompleteData.value = null
            _loginExitoso.value = false
            _email.value = ""
            _password.value = ""
        }
    }

    // ========================================================================
    // RESET
    // ========================================================================
    fun resetLoginState() {
        _loginExitoso.value = false
        _profileIncompleteData.value = null
        _authState.value = AuthState.Idle
    }
}