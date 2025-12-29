package com.example.lactacare.vistas.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.dominio.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class CambiarPasswordTemporalViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _passwordActual = MutableStateFlow("")
    val passwordActual = _passwordActual.asStateFlow()
    private val _nuevaPassword = MutableStateFlow("")
    val nuevaPassword = _nuevaPassword.asStateFlow()
    private val _confirmarPassword = MutableStateFlow("")
    val confirmarPassword = _confirmarPassword.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()
    fun setPasswordActual(value: String) {
        _passwordActual.value = value
        _mensaje.value = null
    }
    fun setNuevaPassword(value: String) {
        _nuevaPassword.value = value
        _mensaje.value = null
    }
    fun setConfirmarPassword(value: String) {
        _confirmarPassword.value = value
        _mensaje.value = null
    }
    fun cambiarPassword(
        token: String,
        correo: String,
        onSuccess: () -> Unit
    ) {
        // Validar que las contraseñas coincidan
        if (_nuevaPassword.value != _confirmarPassword.value) {
            _mensaje.value = "Las contraseñas no coinciden"
            return
        }
        // Validar longitud mínima
        if (_nuevaPassword.value.length < 8) {
            _mensaje.value = "La contraseña debe tener al menos 8 caracteres"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _mensaje.value = null
            val result = authRepository.changeTemporaryPassword(
                token = token,
                correo = correo,
                currentPassword = _passwordActual.value,
                newPassword = _nuevaPassword.value
            )
            result.onSuccess {
                _mensaje.value = "Contraseña actualizada exitosamente"
                onSuccess()
            }.onFailure { error ->
                _mensaje.value = error.message ?: "Error al cambiar contraseña"
            }
            _isLoading.value = false
        }
    }
}