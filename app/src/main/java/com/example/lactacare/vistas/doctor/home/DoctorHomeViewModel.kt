package com.example.lactacare.vistas.doctor.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.DoctorReservaDto
import com.example.lactacare.datos.dto.DoctorEstadisticasDto
import com.example.lactacare.dominio.repository.IDoctorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

import com.example.lactacare.dominio.repository.AuthRepository
import com.example.lactacare.datos.local.SessionManager
import kotlinx.coroutines.flow.first

data class DoctorHomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val estadisticas: DoctorEstadisticasDto? = null,
    val nombreDoctor: String = "",
    val imagenDoctor: String? = null,
    val error: String? = null,
    val fechaHoy: String = LocalDate.now().toString()
)



@HiltViewModel
class DoctorHomeViewModel @Inject constructor(
    private val repository: IDoctorRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorHomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarDatosIniciales()
    }
    
    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            // Cargar nombre e imagen del doctor
            val email = sessionManager.userEmail.first()
            if (!email.isNullOrEmpty()) {
                val result = authRepository.getEmpleadoData(email)
                if (result.isSuccess) {
                    val empleado = result.getOrNull()
                    _uiState.value = _uiState.value.copy(
                        nombreDoctor = "${empleado?.primerNombre ?: ""} ${empleado?.primerApellido ?: ""}".trim()
                        // TODO: Agregar imagenDoctor cuando esté disponible en el DTO
                    )
                    // Guardar salaId si no existe
                    if (empleado?.salaLactanciaId != null) {
                        sessionManager.saveSalaId(empleado.salaLactanciaId)
                    }
                }
            }
            cargarEstadisticas()
        }
    }

    
    fun cargarEstadisticas(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }
            
            val hoy = LocalDate.now().toString()
            val result = repository.obtenerEstadisticasDoctor(hoy)
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    estadisticas = result.getOrNull(),
                    fechaHoy = hoy
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = result.exceptionOrNull()?.message ?: "Error al cargar estadísticas"
                )
            }
        }
    }

    fun confirmarAsistencia(reserva: DoctorReservaDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Cambiamos estado a CONFIRMADA (o el valor que use tu backend para asistencia verificada)
            val reservaActualizada = reserva.copy(estado = "CONFIRMADA")
            
            val result = repository.actualizarReserva(reserva.id, reservaActualizada)
            
            if (result.isSuccess) {
                // Recargar estadísticas para reflejar cambios
                cargarEstadisticas()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error al confirmar asistencia"
                )
            }
        }
    }
}
