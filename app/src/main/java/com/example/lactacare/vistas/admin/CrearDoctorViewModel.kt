package com.example.lactacare.vistas.admin.creardoctor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.CrearEmpleadoRequest
import com.example.lactacare.datos.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CrearDoctorUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    
    // Form fields
    val cedula: String = "",
    val primerNombre: String = "",
    val segundoNombre: String = "",
    val primerApellido: String = "",
    val segundoApellido: String = "",
    val correo: String = "",
    val telefono: String = "",
    val fechaNacimiento: String = "" // Formato YYYY-MM-DD
)

@HiltViewModel
class CrearDoctorViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrearDoctorUiState())
    val uiState = _uiState.asStateFlow()

    fun onFieldChange(
        cedula: String? = null,
        primerNombre: String? = null,
        segundoNombre: String? = null,
        primerApellido: String? = null,
        segundoApellido: String? = null,
        correo: String? = null,
        telefono: String? = null,
        fechaNacimiento: String? = null
    ) {
        _uiState.value = _uiState.value.copy(
            cedula = cedula ?: _uiState.value.cedula,
            primerNombre = primerNombre ?: _uiState.value.primerNombre,
            segundoNombre = segundoNombre ?: _uiState.value.segundoNombre,
            primerApellido = primerApellido ?: _uiState.value.primerApellido,
            segundoApellido = segundoApellido ?: _uiState.value.segundoApellido,
            correo = correo ?: _uiState.value.correo,
            telefono = telefono ?: _uiState.value.telefono,
            fechaNacimiento = fechaNacimiento ?: _uiState.value.fechaNacimiento
        )
    }

    fun guardarDoctor() {
        val state = _uiState.value
        
        // Validación básica
        if (state.cedula.isBlank() || state.primerNombre.isBlank() || state.primerApellido.isBlank() || state.correo.isBlank()) {
            _uiState.value = state.copy(error = "Complete los campos obligatorios")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            
            val request = CrearEmpleadoRequest(
                cedula = state.cedula,
                primerNombre = state.primerNombre,
                segundoNombre = state.segundoNombre,
                primerApellido = state.primerApellido,
                segundoApellido = state.segundoApellido,
                correo = state.correo,
                telefono = state.telefono,
                fechaNacimiento = formatearFecha(state.fechaNacimiento.ifBlank { "1990-01-01" }), 
                rol = "DOCTOR"
            )

            val result = repository.crearDoctor(request)
            
            if (result.isSuccess) {
                _uiState.value = state.copy(isLoading = false, success = true)
            } else {
                _uiState.value = state.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    private fun formatearFecha(fecha: String): String {
        // Intenta convertir YYYYMMDD a YYYY-MM-DD si es necesario
        return if (fecha.length == 8 && !fecha.contains("-")) {
            "${fecha.substring(0, 4)}-${fecha.substring(4, 6)}-${fecha.substring(6, 8)}"
        } else {
            fecha
        }
    }
    
    fun resetState() {
        _uiState.value = CrearDoctorUiState()
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
