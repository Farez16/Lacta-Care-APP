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
    // NOTA: Si tienes configurado GoogleSignInClient en tu módulo DI, inyéctalo aquí.
) : ViewModel() {

    // --- ESTADOS DE LOS CAMPOS DE TEXTO ---
    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _rolActual = MutableStateFlow(RolUsuario.PACIENTE)
    val rolActual = _rolActual.asStateFlow()

    // --- ESTADOS DE LA APP ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError = _mensajeError.asStateFlow()

    private val _loginExitoso = MutableStateFlow(false)
    val loginExitoso = _loginExitoso.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    private val _profileIncompleteData = MutableStateFlow<ProfileIncompleteData?>(null)
    val profileIncompleteData = _profileIncompleteData.asStateFlow()

    // --- SETTERS ---
    fun onEmailChange(nuevoEmail: String) { _email.value = nuevoEmail }
    fun onPasswordChange(nuevoPass: String) { _password.value = nuevoPass }
    fun setRol(nuevoRol: RolUsuario) { _rolActual.value = nuevoRol }

    // --- LÓGICA DE LOGIN ---
    fun login() {
        val mail = _email.value
        val pass = _password.value

        if (mail.isBlank() || pass.isBlank()) {
            _mensajeError.value = "Por favor completa todos los campos"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _mensajeError.value = null

            val result = authRepository.login(mail, pass)

            result.onSuccess {
                _authState.value = AuthState.Authenticated
                _loginExitoso.value = true
            }.onFailure { error ->
                _mensajeError.value = error.message ?: "Error al iniciar sesión"
                _authState.value = AuthState.Error(error.message ?: "Error")
            }

            _isLoading.value = false
        }
    }

    // --- NUEVO: RECUPERAR PASSWORD ---
    fun enviarRecuperacionPassword(email: String, onSuccess: () -> Unit) { // <--- Agregamos onSuccess
        if (email.isBlank()) {
            _mensajeError.value = "Por favor ingresa tu correo electrónico"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _mensajeError.value = null

            val result = authRepository.recuperarPassword(email)

            result.onSuccess {
                _mensajeError.value = "Correo enviado. Revisa tu bandeja."

                // --- AQUÍ EJECUTAMOS LA ACCIÓN DE ÉXITO (NAVEGAR) ---
                onSuccess()
                // ----------------------------------------------------
            }.onFailure {
                _mensajeError.value = it.message ?: "No se pudo enviar el correo"
            }

            _isLoading.value = false
        }
    }

    // --- COMPLETAR PERFIL ---
    fun completeProfile(request: CompleteProfileRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.completarPerfil(request)
            result.onSuccess {
                _loginExitoso.value = true
                _authState.value = AuthState.Authenticated
            }.onFailure {
                _mensajeError.value = it.message
            }
            _isLoading.value = false
        }
    }

    // --- GOOGLE SIGN IN (Stub) ---
    fun getGoogleSignInIntent(): Intent {
        return Intent() // Aquí iría tu lógica real de Google client.signInIntent
    }

    fun handleGoogleSignInResult(intent: Intent?) {
        // Lógica futura
    }

    // --- LOGOUT ---
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Idle
            _loginExitoso.value = false
            _email.value = ""
            _password.value = ""
        }
    }
}