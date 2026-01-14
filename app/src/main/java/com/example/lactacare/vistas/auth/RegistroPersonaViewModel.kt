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
import com.example.lactacare.datos.ValidationUtils
import android.util.Patterns
import java.time.LocalDate

// UI State con campos de error para validación en tiempo real
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
    val password: String = "",

    // NUEVOS: Estados de error para validación en tiempo real
    val errorCedula: String? = null,
    val errorCorreo: String? = null,
    val errorTelefono: String? = null,
    val errorPassword: String? = null
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
    fun onCedulaChange(v: String) {
        // Solo permitir dígitos y máximo 10 caracteres
        val cedulaLimpia = v.filter { it.isDigit() }.take(10)

        // Validar en tiempo real solo si tiene 10 dígitos
        val error = when {
            cedulaLimpia.isEmpty() -> null // No mostrar error si está vacío
            cedulaLimpia.length < 10 -> "La cédula debe tener 10 dígitos"
            !ValidationUtils.esCedulaEcuatorianaValida(cedulaLimpia) -> "Cédula ecuatoriana inválida"
            else -> null
        }

        _uiState.update {
            it.copy(
                cedula = cedulaLimpia,
                errorCedula = error
            )
        }
    }
    fun onPrimerNombreChange(v: String) {
        // Solo permitir letras y espacios
        val nombreLimpio = v.filter { it.isLetter() || it.isWhitespace() }
        // Capitalizar automáticamente
        val nombreCapitalizado = ValidationUtils.capitalizarNombre(nombreLimpio)

        _uiState.update { it.copy(primerNombre = nombreCapitalizado) }
    }
    fun onSegundoNombreChange(v: String) {
        val nombreLimpio = v.filter { it.isLetter() || it.isWhitespace() }
        val nombreCapitalizado = ValidationUtils.capitalizarNombre(nombreLimpio)

        _uiState.update { it.copy(segundoNombre = nombreCapitalizado) }
    }
    fun onPrimerApellidoChange(v: String) {
        val apellidoLimpio = v.filter { it.isLetter() || it.isWhitespace() }
        val apellidoCapitalizado = ValidationUtils.capitalizarNombre(apellidoLimpio)

        _uiState.update { it.copy(primerApellido = apellidoCapitalizado) }
    }
    fun onSegundoApellidoChange(v: String) {
        val apellidoLimpio = v.filter { it.isLetter() || it.isWhitespace() }
        val apellidoCapitalizado = ValidationUtils.capitalizarNombre(apellidoLimpio)

        _uiState.update { it.copy(segundoApellido = apellidoCapitalizado) }
    }
    fun onCorreoChange(v: String) {
        // Sanitizar: eliminar espacios al inicio/final
        val correoLimpio = ValidationUtils.sanitizarTexto(v)

        // Validar formato solo si no está vacío
        val error = if (correoLimpio.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()) {
            "Formato de correo inválido"
        } else null

        _uiState.update {
            it.copy(
                correo = correoLimpio,
                errorCorreo = error
            )
        }
    }
    fun onTelefonoChange(v: String) {
        // Solo permitir dígitos y máximo 10 caracteres
        val telefonoLimpio = v.filter { it.isDigit() }.take(10)

        // Validar solo si no está vacío
        val error = if (telefonoLimpio.isNotBlank() && telefonoLimpio.length != 10) {
            "El teléfono debe tener 10 dígitos"
        } else null

        _uiState.update {
            it.copy(
                telefono = telefonoLimpio,
                errorTelefono = error
            )
        }
    }
    fun onFechaNacimientoChange(v: String) { _uiState.update { it.copy(fechaNacimiento = v) } }
    fun onDiscapacidadChange(v: String) { _uiState.update { it.copy(discapacidad = v) } }
    fun onPasswordChange(v: String) {
        // Sanitizar: eliminar espacios
        val passwordLimpio = v.replace(" ", "")

        // Validar usando la función de ValidationUtils
        val error = if (passwordLimpio.isNotBlank()) {
            ValidationUtils.validarPassword(passwordLimpio)
        } else null

        _uiState.update {
            it.copy(
                password = passwordLimpio,
                errorPassword = error
            )
        }
    }

    // --- FUNCIÓN REGISTRAR ---
    fun registrar() {
        viewModelScope.launch {
            _isLoading.value = true
            _mensaje.value = null
            _registroExitoso.value = false

            val datos = _uiState.value

            // 1. Validar que no haya errores de validación pendientes
            if (datos.errorCedula != null || datos.errorCorreo != null ||
                datos.errorTelefono != null || datos.errorPassword != null) {
                _mensaje.value = "Por favor corrige los errores antes de continuar"
                _isLoading.value = false
                return@launch
            }
// 2. Validaciones básicas de campos vacíos
            if (datos.cedula.isBlank() || datos.primerNombre.isBlank() ||
                datos.primerApellido.isBlank() || datos.correo.isBlank() ||
                datos.password.isBlank() || datos.telefono.isBlank() ||
                datos.fechaNacimiento.isBlank()) {
                _mensaje.value = "Por favor completa todos los campos obligatorios"
                _isLoading.value = false
                return@launch
            }
// 3. Validación final de cédula (por si acaso)
            if (!ValidationUtils.esCedulaEcuatorianaValida(datos.cedula)) {
                _mensaje.value = "La cédula ingresada no es válida"
                _isLoading.value = false
                return@launch
            }
// 4. Validación final de edad
            try {
                val fechaNac = LocalDate.parse(datos.fechaNacimiento)
                if (!ValidationUtils.esMayorDeEdad(fechaNac)) {
                    _mensaje.value = "Debes ser mayor de 18 años para registrarte"
                    _isLoading.value = false
                    return@launch
                }
            } catch (e: Exception) {
                _mensaje.value = "Fecha de nacimiento inválida"
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