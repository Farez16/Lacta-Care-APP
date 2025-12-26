package com.example.lactacare.vistas.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.dominio.repository.AuthRepository
import com.example.lactacare.dominio.model.RolUsuario
import com.example.lactacare.datos.dto.AuthState
import com.example.lactacare.datos.dto.ProfileIncompleteData
import com.example.lactacare.datos.dto.CompleteProfileRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // --- ESTADOS DE UI ---
    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _rolActual = MutableStateFlow(RolUsuario.PACIENTE)
    val rolActual = _rolActual.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError = _mensajeError.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    // --- ESTA ES LA VARIABLE QUE TE FALTABA ---
    private val _loginExitoso = MutableStateFlow(false)
    val loginExitoso = _loginExitoso.asStateFlow()
    // ------------------------------------------

    // Datos temporales si Google devuelve un usuario nuevo (perfil incompleto)
    private val _profileIncompleteData = MutableStateFlow<ProfileIncompleteData?>(null)
    val profileIncompleteData = _profileIncompleteData.asStateFlow()

    // --- SETTERS ---
    fun onEmailChange(nuevoEmail: String) { _email.value = nuevoEmail }
    fun onPasswordChange(nuevoPass: String) { _password.value = nuevoPass }
    fun setRol(nuevoRol: RolUsuario) { _rolActual.value = nuevoRol }

    // --- LOGIN ---
    fun login() {
        val mail = _email.value
        val pass = _password.value
        val rol = _rolActual.value

        if (mail.isBlank() || pass.isBlank()) {
            _mensajeError.value = "Por favor completa todos los campos"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _mensajeError.value = null

            val result = authRepository.login(mail, pass, rol)

            result.onSuccess {
                _authState.value = AuthState.Authenticated
                _loginExitoso.value = true // <--- Aquí actualizamos el estado
            }.onFailure { error ->
                _mensajeError.value = error.message ?: "Error al iniciar sesión"
                _authState.value = AuthState.Error(error.message ?: "Error")
            }

            _isLoading.value = false
        }
    }

    // --- GOOGLE ---
    fun getGoogleSignInIntent(): Intent = authRepository.getGoogleSignInIntent()

    fun handleGoogleSignInResult(intent: Intent?) {
        viewModelScope.launch {
            _isLoading.value = true
            _mensajeError.value = null

            val result = authRepository.loginWithGoogle(intent)

            result.onSuccess { estado ->
                _authState.value = estado
                if (estado is AuthState.ProfileIncomplete) {
                    _profileIncompleteData.value = estado.data
                } else if (estado is AuthState.Authenticated) {
                    _loginExitoso.value = true
                }
            }.onFailure { error ->
                _mensajeError.value = error.message ?: "Error Google"
                _authState.value = AuthState.Error(error.message ?: "Error")
            }
            _isLoading.value = false
        }
    }

    // --- COMPLETAR PERFIL (Google) ---
    fun completeProfile(request: CompleteProfileRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.completarPerfil(request)
            result.onSuccess {
                _authState.value = AuthState.Authenticated
                _loginExitoso.value = true // <--- Y aquí también
                _profileIncompleteData.value = null
            }.onFailure {
                _mensajeError.value = it.message
            }
            _isLoading.value = false
        }
    }

    // --- RECUPERAR PASSWORD ---
    fun enviarRecuperacionPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = authRepository.recuperarPassword(email)
            res.onSuccess { onSuccess() }
                .onFailure {
                    _mensajeError.value = it.message
                    onError(it.message ?: "Error")
                }
            _isLoading.value = false
        }
    }

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
    fun resetLoginState() {
        _loginExitoso.value = false
        _profileIncompleteData.value = null
    }
}