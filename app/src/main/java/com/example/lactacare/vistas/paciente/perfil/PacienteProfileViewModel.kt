package com.example.lactacare.vistas.paciente.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.ActualizarPerfilRequest
import com.example.lactacare.datos.local.SessionManager
import com.example.lactacare.datos.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PatientProfileUiState(
    val id: Long = 0,
    val cedula: String = "",
    val imagenPerfil: String? = null,
    val nombreCompleto: String = "Cargando...",
    val primerNombre: String = "",
    val segundoNombre: String? = null,
    val correo: String = "",
    val telefono: String = "",
    val fechaNacimiento: String = "",
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val sessionClosed: Boolean = false,
    // Validaciones
    val nombreError: String? = null,
    val telefonoError: String? = null
)

@HiltViewModel
class PatientProfileViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarPerfil()
    }

    private fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val userId = sessionManager.userId.first()
            if (userId == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Usuario no identificado"
                    )
                }
                return@launch
            }
            
            try {
                val response = apiService.obtenerPerfilPaciente(userId)
                
                if (response.isSuccessful && response.body() != null) {
                    val perfil = response.body()!!
                    
                    val nombreCompleto = buildString {
                        append(perfil.primerNombre)
                        if (!perfil.segundoNombre.isNullOrBlank()) {
                            append(" ${perfil.segundoNombre}")
                        }
                        append(" ${perfil.primerApellido}")
                        if (!perfil.segundoApellido.isNullOrBlank()) {
                            append(" ${perfil.segundoApellido}")
                        }
                    }
                    
                    _uiState.update {
                        it.copy(
                            id = perfil.id,
                            cedula = perfil.cedula,
                            imagenPerfil = perfil.imagenPerfil,
                            nombreCompleto = nombreCompleto,
                            primerNombre = perfil.primerNombre,
                            segundoNombre = perfil.segundoNombre,
                            correo = perfil.correo,
                            telefono = perfil.telefono ?: "",
                            fechaNacimiento = perfil.fechaNacimiento,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar perfil: ${response.message()}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleEditing() {
        _uiState.update { 
            it.copy(
                isEditing = !it.isEditing, 
                error = null, 
                successMessage = null,
                nombreError = null,
                telefonoError = null
            ) 
        }
    }

    fun validarNombre(nombre: String): String? {
        return when {
            nombre.isBlank() -> "El nombre no puede estar vacío"
            !nombre.all { it.isLetter() || it.isWhitespace() } -> "El nombre solo puede contener letras"
            nombre.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            else -> null
        }
    }

    fun validarTelefono(telefono: String): String? {
        return when {
            telefono.isBlank() -> null // Teléfono es opcional
            !telefono.all { it.isDigit() } -> "El teléfono solo puede contener números"
            telefono.length != 10 -> "El teléfono debe tener exactamente 10 dígitos"
            else -> null
        }
    }

    fun formatearNombre(nombre: String): String {
        return nombre.trim()
            .split(" ")
            .joinToString(" ") { palabra ->
                palabra.lowercase().replaceFirstChar { it.uppercase() }
            }
    }

    fun actualizarPerfil(
        nombre: String,
        telefono: String,
        imagenBase64: String? = null
    ) {
        // Validar antes de enviar
        val nombreError = validarNombre(nombre)
        val telefonoError = validarTelefono(telefono)

        if (nombreError != null || telefonoError != null) {
            _uiState.update {
                it.copy(
                    nombreError = nombreError,
                    telefonoError = telefonoError,
                    error = "Por favor corrige los errores en el formulario"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val nombreFormateado = formatearNombre(nombre)
                
                val request = ActualizarPerfilRequest(
                    primerNombre = nombreFormateado,
                    segundoNombre = _uiState.value.segundoNombre,
                    telefono = telefono.trim().ifBlank { null },
                    imagenPerfil = imagenBase64
                )
                
                val response = apiService.actualizarPerfilPaciente(
                    _uiState.value.id,
                    request
                )
                
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isEditing = false,
                            successMessage = "Perfil actualizado exitosamente",
                            nombreError = null,
                            telefonoError = null
                        )
                    }
                    cargarPerfil()
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al actualizar: ${response.message()}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _uiState.update { it.copy(sessionClosed = true) }
        }
    }
}
