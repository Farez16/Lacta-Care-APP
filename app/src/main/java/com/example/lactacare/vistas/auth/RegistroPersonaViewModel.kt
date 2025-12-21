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

// UI State simplificado
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

    // Variable para guardar el rol que viene desde la MainActivity
    private var currentRol: RolUsuario = RolUsuario.PACIENTE

    // --- 3. NUEVA FUNCIÓN NECESARIA PARA MAINACTIVITY ---
    fun setRol(rol: RolUsuario) {
        this.currentRol = rol
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
    fun onPasswordChange(v: String) { _uiState.update { it.copy(password = v) } }

    // --- FUNCIÓN REGISTRAR ---
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

            if (datos.password.length < 8) {
                _mensaje.value = "La contraseña debe tener al menos 8 caracteres"
                _isLoading.value = false
                return@launch
            }

            try {
                // Solo permitimos registrar Pacientes por la App (Regla de Negocio)
                if (currentRol != RolUsuario.PACIENTE) {
                    _mensaje.value = "El registro de ${currentRol.name} no está permitido desde la app móvil. Contacte a administración."
                    _isLoading.value = false
                    return@launch
                }

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

                // Llamada al repositorio
                val resultado = authRepository.registrarPaciente(paciente)

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
}