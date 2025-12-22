package com.example.lactacare.vistas.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// --- CORRECCIÓN IMPORTANTE AQUÍ ABAJO ---
import com.example.lactacare.dominio.repository.AuthRepository
// ----------------------------------------
import com.example.lactacare.dominio.model.Paciente
import com.example.lactacare.dominio.model.RolUsuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI State (Se mantiene igual, incluye 'discapacidad' que es campo de paciente)
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
    val password: String = ""
)

@HiltViewModel
class RegistroPersonaViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()

    private val _registroExitoso = MutableStateFlow(false)
    val registroExitoso = _registroExitoso.asStateFlow()

    // --- YA NO NECESITAMOS 'currentRol' NI 'setRol' ---
    // Este ViewModel asume implícitamente que siempre es un Paciente.

    // --- SETTERS (Actualizan el estado mientras el usuario escribe) ---
    fun onCedulaChange(v: String) { _uiState.update { it.copy(cedula = v) } }
    fun onPrimerNombreChange(v: String) { _uiState.update { it.copy(primerNombre = v) } }
    fun onSegundoNombreChange(v: String) { _uiState.update { it.copy(segundoNombre = v) } }
    fun onPrimerApellidoChange(v: String) { _uiState.update { it.copy(primerApellido = v) } }
    fun onSegundoApellidoChange(v: String) { _uiState.update { it.copy(segundoApellido = v) } }
    fun onCorreoChange(v: String) { _uiState.update { it.copy(correo = v) } }
    fun onTelefonoChange(v: String) { _uiState.update { it.copy(telefono = v) } }
    fun onFechaNacimientoChange(v: String) { _uiState.update { it.copy(fechaNacimiento = v) } }
    fun onDiscapacidadChange(v: String) { _uiState.update { it.copy(discapacidad = v) } }
    fun onPasswordChange(v: String) { _uiState.update { it.copy(password = v) } }

    // --- FUNCIÓN REGISTRAR ---
    fun registrar() {
        viewModelScope.launch {
            _isLoading.value = true
            _mensaje.value = null
            _registroExitoso.value = false

            val datos = _uiState.value

            // 1. Validaciones básicas de campos vacíos
            if (datos.cedula.isBlank() || datos.primerNombre.isBlank() ||
                datos.primerApellido.isBlank() || datos.correo.isBlank() ||
                datos.password.isBlank()) {
                _mensaje.value = "Por favor completa todos los campos obligatorios"
                _isLoading.value = false
                return@launch
            }

            // 2. Validación de contraseña
            if (datos.password.length < 8) {
                _mensaje.value = "La contraseña debe tener al menos 8 caracteres"
                _isLoading.value = false
                return@launch
            }

            try {
                // 3. Creación del objeto Paciente
                // Nota: discapacidad se envía como null si está vacío (String.isBlank)
                val paciente = Paciente(
                    cedula = datos.cedula.trim(),
                    primerNombre = datos.primerNombre.trim(),
                    segundoNombre = datos.segundoNombre.trim(),
                    primerApellido = datos.primerApellido.trim(),
                    segundoApellido = datos.segundoApellido.trim(),
                    correo = datos.correo.trim(),
                    password = datos.password,
                    telefono = datos.telefono.trim(),
                    fechaNacimiento = datos.fechaNacimiento.trim(),
                    discapacidad = datos.discapacidad.ifBlank { null }
                )

                // 4. Llamada al repositorio (Específica para Pacientes)
                val resultado = authRepository.registrarPaciente(paciente)

                resultado.onSuccess {
                    _registroExitoso.value = true
                    _mensaje.value = "Registro exitoso"
                }.onFailure {
                    // Manejo de errores que vienen del backend (Ej: "Cédula ya existe")
                    _mensaje.value = it.message ?: "Error al registrar"
                }

            } catch (e: Exception) {
                _mensaje.value = e.message ?: "Error inesperado de conexión"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Limpiar el estado de éxito para evitar navegaciones repetidas si se vuelve a la pantalla
    fun resetRegistroExitoso() {
        _registroExitoso.value = false
    }
}