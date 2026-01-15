package com.example.lactacare.vistas.paciente.reserva
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.ReservaPacienteDto
import com.example.lactacare.dominio.repository.IPatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1. UI State
data class MisReservasUiState(
    val isLoading: Boolean = false,
    val reservas: List<ReservaPacienteDto> = emptyList(),
    val error: String? = null,
    val pacienteId: Long = 0
)

// 2. ViewModel
@HiltViewModel
class MisReservasViewModel @Inject constructor(
    private val repository: IPatientRepository
) : ViewModel() {
    // 3. State Management
    private val _uiState = MutableStateFlow(MisReservasUiState())
    val uiState = _uiState.asStateFlow()
    
    // 4. Cargar Reservas
    fun cargarReservas(pacienteId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                pacienteId = pacienteId
            )
            
            val result = repository.obtenerMisReservas(pacienteId)
            
            if (result.isSuccess) {
                // Mostrar TODAS las reservas (incluyendo CANCELADO)
                val reservas = result.getOrDefault(emptyList())
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    reservas = reservas
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error al cargar reservas"
                )
            }
        }
    }
    
    // 5. Cancelar Reserva
    fun cancelarReserva(idReserva: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = repository.cancelarReserva(idReserva)
            
            if (result.isSuccess) {
                // Recargar lista
                cargarReservas(_uiState.value.pacienteId)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cancelar: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }
    
    // 6. Limpiar Error
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}