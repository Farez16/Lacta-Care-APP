package com.example.lactacare.vistas.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.GoogleAccountException
import com.example.lactacare.datos.dto.RolMismatchRecoveryException
import com.example.lactacare.datos.dto.UnauthorizedEmailException
import com.example.lactacare.datos.dto.UserNotFoundException
import com.example.lactacare.datos.repository.AuthRepositoryImpl
import com.example.lactacare.dominio.model.RolUsuario
import com.example.lactacare.dominio.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class RecuperarPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Estados del formulario
    private val _correo = MutableStateFlow("")
    val correo = _correo.asStateFlow()

    private val _codigo = MutableStateFlow("")
    val codigo = _codigo.asStateFlow()

    private val _nuevaPassword = MutableStateFlow("")
    val nuevaPassword = _nuevaPassword.asStateFlow()

    private val _confirmarPassword = MutableStateFlow("")
    val confirmarPassword = _confirmarPassword.asStateFlow()

    private val _rol = MutableStateFlow(RolUsuario.PACIENTE)
    val rol = _rol.asStateFlow()

    // Estados de UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()

    // Token temporal después de verificar código
    private var resetToken: String? = null

    // Setters
    fun setCorreo(value: String) {
        _correo.value = value
        _mensaje.value = null
    }

    fun setCodigo(value: String) {
        _codigo.value = value
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

    fun setRol(value: RolUsuario) {
        _rol.value = value
    }

    /**
     * Paso 1: Solicitar código de recuperación
     */
    fun solicitarCodigo(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _mensaje.value = null

            val result = authRepository.solicitarCodigoRecuperacion(
                _correo.value,
                _rol.value
            )

            result.onSuccess { message ->
                _mensaje.value = message
                onSuccess()
            }.onFailure { error ->
                // Manejar diferentes tipos de errores
                _mensaje.value = when (error) {
                    is GoogleAccountException -> {
                        "GOOGLE_ACCOUNT: ${error.message}"
                    }
                    is RolMismatchRecoveryException -> {
                        "ROL_MISMATCH: ${error.message}"
                    }
                    is UnauthorizedEmailException -> {
                        "UNAUTHORIZED_EMAIL: ${error.message}"
                    }
                    is UserNotFoundException -> {
                        "USER_NOT_FOUND: ${error.message}"
                    }
                    else -> {
                        error.message ?: "Error al enviar código"
                    }
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Paso 2: Verificar código y obtener token
     */
    fun verificarCodigo(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _mensaje.value = null

            val result = authRepository.verificarCodigo(
                _correo.value,
                _codigo.value,
                _rol.value
            )

            result.onSuccess { token ->
                resetToken = token
                _mensaje.value = "Código válido"
                onSuccess()
            }.onFailure { error ->
                _mensaje.value = error.message ?: "Código inválido"
            }

            _isLoading.value = false
        }
    }

    /**
     * Paso 3: Cambiar contraseña con token
     */
    fun cambiarPassword(onSuccess: () -> Unit) {
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

        if (resetToken == null) {
            _mensaje.value = "Error: Token no válido. Vuelve a verificar el código."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _mensaje.value = null

            val result = authRepository.cambiarPassword(
                resetToken!!,
                _nuevaPassword.value,
                _rol.value
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

    /**
     * Resetear estado del ViewModel
     */
    fun resetState() {
        _correo.value = ""
        _codigo.value = ""
        _nuevaPassword.value = ""
        _confirmarPassword.value = ""
        _mensaje.value = null
        resetToken = null
    }
}