package com.example.lactacare.vistas.doctor.atencion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.CrearAtencionRequest
import com.example.lactacare.datos.dto.EmpleadoIdDto
import com.example.lactacare.datos.dto.ReservaIdDto
import com.example.lactacare.dominio.repository.IDoctorRepository
import com.example.lactacare.datos.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class AtencionUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AtencionViewModel @Inject constructor(
    private val repository: IDoctorRepository,
    private val sessionManager: SessionManager // Required to get Doctor ID
) : ViewModel() {

    private val _uiState = MutableStateFlow(AtencionUiState())
    val uiState = _uiState.asStateFlow()

    fun finalizarAtencion(idReserva: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Generate timestamps
            val fecha = LocalDate.now().toString()
            val hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            
            // Get Current Doctor
            val empleadoId = sessionManager.userId.first() ?: run {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "No se pudo identificar al doctor")
                return@launch
            }

            val request = CrearAtencionRequest(
                fecha = fecha,
                hora = hora,
                reserva = ReservaIdDto(idReserva),
                empleado = EmpleadoIdDto(empleadoId.toLong())
            )

            val result = repository.crearAtencion(request)
            
            if (result.isSuccess) {
                 _uiState.value = _uiState.value.copy(isLoading = false, success = true)
            } else {
                 _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }
    
    fun resetState() {
        _uiState.value = AtencionUiState()
    }
}
