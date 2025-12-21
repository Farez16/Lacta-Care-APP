package com.example.lactacare.vistas.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.repository.AuthRepositoryImpl
import com.example.lactacare.dominio.model.RolUsuario
import com.example.lactacare.dominio.model.Administrador
import com.example.lactacare.dominio.model.Medico
import com.example.lactacare.dominio.model.Paciente
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// UI State unificado
data class RegistroUiState(
    val cedula: String = "",
    val primerNombre: String = "",
    val segundoNombre: String = "",
    val primerApellido: String = "",
    val segundoApellido: String = "",
    val correo: String = "",
    val telefono: String = "",
    val fechaNacimiento: String = "",
    val discapacidad: String = "",
    // Campos Específicos
    val licenciaMedica: String = "",
    val codigoEmpleado: String = "",
    val password: String = ""
)

class RegistroPersonaViewModel(
    private val authRepository: AuthRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState = _uiState.asStateFlow()

    private val _rolActual = MutableStateFlow(RolUsuario.PACIENTE)
    val rolActual = _rolActual.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()

    private val _registroExitoso = MutableStateFlow(false)
    val registroExitoso = _registroExitoso.asStateFlow()

    fun setRol(rol: RolUsuario) {
        _rolActual.value = rol
        _mensaje.value = null
    }

    // --- SETTERS ---
    fun onCedulaChange(v: String) { _uiState.update { it.copy(cedula = v) } }
    fun onPrimerNombreChange(v: String) { _uiState.update { it.copy(primerNombre = v) } }
    fun onSegundoNombreChange(v: String) { _uiState.update { it.copy(segundoNombre = v) } }
    fun onPrimerApellidoChange(v: String) { _uiState.update { it.copy(primerApellido = v) } }
    fun onSegundoApellidoChange(v: String) { _uiState.update { it.copy(segundoApellido = v) } }
    fun onCorreoChange(v: String) { _uiState.update { it.copy(correo = v) } }
    fun onTelefonoChange(v: String) { _uiState.update { it.copy(telefono = v) } }
    fun onFechaNacimientoChange(v: String) { _uiState.update { it.copy(fechaNacimiento = v) } }
    fun onDiscapacidadChange(v: String) { _uiState.update { it.copy(discapacidad = v) } }
    fun onLicenciaChange(v: String) { _uiState.update { it.copy(licenciaMedica = v) } }
    fun onCodigoEmpleadoChange(v: String) { _uiState.update { it.copy(codigoEmpleado = v) } }
    fun onPasswordChange(v: String) { _uiState.update { it.copy(password = v) } }

    // --- FUNCIÓN MAESTRA: REGISTRAR ---
    fun registrar() {
        viewModelScope.launch {
            _isLoading.value = true
            _mensaje.value = null
            _registroExitoso.value = false

            val datos = _uiState.value

            // Validaciones básicas
            if (datos.cedula.isEmpty() || datos.primerNombre.isEmpty() ||
                datos.primerApellido.isEmpty() || datos.correo.isEmpty() ||
                datos.password.isEmpty()) {
                _mensaje.value = "Por favor completa todos los campos obligatorios"
                _isLoading.value = false
                return@launch
            }

            // Validar contraseña
            if (datos.password.length < 8) {
                _mensaje.value = "La contraseña debe tener al menos 8 caracteres"
                _isLoading.value = false
                return@launch
            }

            try {
                val resultado = when (_rolActual.value) {

                    RolUsuario.PACIENTE -> {
                        val paciente = Paciente(
                            cedula = datos.cedula,
                            primerNombre = datos.primerNombre,
                            segundoNombre = datos.segundoNombre,
                            primerApellido = datos.primerApellido,
                            segundoApellido = datos.segundoApellido,
                            correo = datos.correo,
                            password = datos.password,
                            telefono = datos.telefono,
                            fechaNacimiento = datos.fechaNacimiento,
                            discapacidad = datos.discapacidad.ifBlank { null }
                        )
                        authRepository.registrar(paciente)
                    }

                    RolUsuario.DOCTOR -> {
                        if (datos.licenciaMedica.isBlank()) {
                            _mensaje.value = "Completa licencia médica "
                            _isLoading.value = false
                            return@launch
                        }

                        val medico = Medico(
                            cedula = datos.cedula,
                            primerNombre = datos.primerNombre,
                            segundoNombre = datos.segundoNombre,
                            primerApellido = datos.primerApellido,
                            segundoApellido = datos.segundoApellido,
                            correo = datos.correo,
                            password = datos.password,
                            telefono = datos.telefono,
                            fechaNacimiento = datos.fechaNacimiento,
                            licenciaMedica = datos.licenciaMedica,
                        )
                        authRepository.registrar(medico)
                    }

                    RolUsuario.ADMINISTRADOR -> {
                        if (datos.codigoEmpleado.isBlank()) {
                            _mensaje.value = "Completa código de empleado"
                            _isLoading.value = false
                            return@launch
                        }

                        val admin = Administrador(
                            cedula = datos.cedula,
                            primerNombre = datos.primerNombre,
                            segundoNombre = datos.segundoNombre,
                            primerApellido = datos.primerApellido,
                            segundoApellido = datos.segundoApellido,
                            correo = datos.correo,
                            password = datos.password,
                            telefono = datos.telefono,
                            fechaNacimiento = datos.fechaNacimiento,
                            codigoEmpleado = datos.codigoEmpleado,
                        )
                        authRepository.registrar(admin)
                    }
                }

                resultado.onSuccess {
                    _registroExitoso.value = true
                    _mensaje.value = "Registro exitoso"
                }.onFailure {
                    _mensaje.value = it.message ?: "Error al registrar"
                }

            } catch (e: Exception) {
                _mensaje.value = e.message ?: "Error inesperado"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetRegistroExitoso() {
        _registroExitoso.value = false
    }

    /**
     * Factory para crear el ViewModel con dependencias
     */
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegistroPersonaViewModel::class.java)) {
                return RegistroPersonaViewModel(
                    AuthRepositoryImpl(context)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

