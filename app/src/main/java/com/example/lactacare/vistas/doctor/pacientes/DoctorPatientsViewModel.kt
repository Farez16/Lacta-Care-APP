package com.example.lactacare.vistas.doctor.pacientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.lactacare.datos.dto.UsuarioResponseDto

data class DoctorPacientesUiState(
    val isLoading: Boolean = false,
    val pacientes: List<UsuarioResponseDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DoctorPatientsViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorPacientesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarPacientes()
    }

    fun cargarPacientes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Reutilizamos el repositorio de Admin para obtener la lista RAW de pacientes
                val resultado = adminRepository.obtenerListaPacientes()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pacientes = resultado
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar pacientes"
                )
            }
        }
    }
}
