package com.example.lactacare.vistas.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.MockPacienteRepository
import com.example.lactacare.dominio.model.RolUsuario
import com.example.lactacare.dominio.repository.PacienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: PacienteRepository = MockPacienteRepository()
) : ViewModel() {

    private val _rolActual = MutableStateFlow(RolUsuario.PACIENTE) // Valor por defecto
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

    fun setRol(rol: RolUsuario) {
        _rolActual.value = rol
    }
    val loginExitoso = _loginExitoso.asStateFlow()

    fun onEmailChange(text: String) { _email.value = text }
    fun onPasswordChange(text: String) { _password.value = text }



    fun login() {
        viewModelScope.launch {
            _isLoading.value = true
            _mensajeError.value = null

            val result = repository.login(_email.value, _password.value)

            _isLoading.value = false
            result.onSuccess {
                _loginExitoso.value = true
            }.onFailure {
                _mensajeError.value = it.message
            }
        }
    }
}