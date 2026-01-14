package com.example.lactacare.vistas.doctor.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.DoctorReservaDto
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
    val agenda: List<DoctorReservaDto> = emptyList(),
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
        verificarSalaYCargarAgenda()
    }

    fun verificarSalaYCargarAgenda() {
        viewModelScope.launch {
            // 1. Verificar si tenemos salaId local
            var salaId = sessionManager.userSalaId.first()

            // 2. Si no, buscarla en el servidor
            if (salaId == null) {
                val email = sessionManager.userEmail.first()
                if (!email.isNullOrEmpty()) {
                    val result = authRepository.getEmpleadoData(email)
                    if (result.isSuccess) {
                        val empleado = result.getOrNull()
                        salaId = empleado?.salaLactanciaId
                        if (salaId != null) {
                            sessionManager.saveSalaId(salaId)
                        }
                    }
                }
            }
            
            cargarAgendaHoy(salaId)
        }
    }

    fun cargarAgendaHoy(salaId: Int? = null) {
        viewModelScope.launch {
             // Si no pasaron salaId, intentamos leerlo de nuevo (refresh manual)
            val finalSalaId = salaId ?: sessionManager.userSalaId.first()

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Usar fecha real del dispositivo
            val hoy = LocalDate.now().toString()
            
            val result = repository.obtenerAgendaDelDia(hoy)
            
            if (result.isSuccess) {
                val todasLasReservas = result.getOrDefault(emptyList())
                
                // FILTRADO: Solo reservas de la sala asignada
                val agendaFiltrada = if (finalSalaId != null) {
                    todasLasReservas.filter { it.sala?.id == finalSalaId }
                } else {
                    // Si no hay sala asignada, ¿mostramos todo o nada?
                    // Por seguridad, si no tiene sala asignada, mejor no mostrar nada o todo.
                    // Mostremos todo por ahora para no bloquear, o vacio.
                    // El requerimiento dice que DEBE ver solo SU sala.
                    emptyList() 
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    agenda = agendaFiltrada,
                    fechaHoy = hoy,
                    error = if (finalSalaId == null) "No tienes una sala de lactancia asignada." else null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error al cargar agenda"
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
                // Recargar agenda para reflejar cambios
                cargarAgendaHoy()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error al confirmar asistencia"
                )
            }
        }
    }
}
