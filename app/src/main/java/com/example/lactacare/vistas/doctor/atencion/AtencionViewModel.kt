package com.example.lactacare.vistas.doctor.atencion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.CrearAtencionRequest
import com.example.lactacare.datos.dto.EmpleadoIdDto
import com.example.lactacare.datos.dto.ReservaIdDto
import com.example.lactacare.datos.local.SessionManager
import com.example.lactacare.dominio.repository.IDoctorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class AtencionUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AtencionViewModel @Inject constructor(
    private val repository: IDoctorRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AtencionUiState())
    val uiState = _uiState.asStateFlow()

    fun registrarAtencion(reservaId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val doctorId = sessionManager.userId.first() ?: -1L // ID del doctor logueado

            val request = CrearAtencionRequest(
                fecha = LocalDate.now().toString(),
                hora = LocalTime.now().toString().substringBefore("."), // HH:mm:ss
                reserva = ReservaIdDto(reservaId),
                empleado = if (doctorId != -1L) EmpleadoIdDto(doctorId) else null
            )

            val result = repository.crearAtencion(request)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error al registrar atención"
                )
            }
        }
    }
}
