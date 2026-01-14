package com.example.lactacare.vistas.paciente.reserva

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.CrearReservaRequest
import com.example.lactacare.datos.dto.PacienteIdDto
import com.example.lactacare.datos.dto.SalaIdDto
import com.example.lactacare.datos.local.SessionManager
import com.example.lactacare.dominio.model.Lactario
import com.example.lactacare.dominio.repository.ILactariosRepository
import com.example.lactacare.dominio.repository.IPatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class AgendarUiState(
    val isLoading: Boolean = false,
    val lactarios: List<Lactario> = emptyList(),
    val error: String? = null,
    val reservaExitosa: Boolean = false
)

@HiltViewModel
class AgendarViewModel @Inject constructor(
    private val lactariosRepository: ILactariosRepository,
    private val patientRepository: IPatientRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgendarUiState())
    val uiState = _uiState.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda = _busqueda.asStateFlow()

    init {
        cargarLactarios()
    }

    fun onBusquedaChanged(query: String) {
        _busqueda.value = query
        // Aquí se podría filtrar la lista localmente si se desea
    }

    private fun cargarLactarios() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = lactariosRepository.obtenerSalas()
            if (result.isSuccess) {
                val dtos = result.getOrDefault(emptyList())
                val domainList = dtos.map { dto ->
                     Lactario(
                        id = dto.id?.toInt() ?: 0,
                        nombre = dto.nombre ?: "Sala Lactancia",
                        direccion = dto.direccion ?: "Sin dirección",
                        correo = dto.correo ?: "",
                        telefono = dto.telefono ?: "",
                        latitud = "0.0",
                        longitud = "0.0",
                        idInstitucion = 0
                     )
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lactarios = domainList
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun reservar(lactarioId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val pacienteId = sessionManager.userId.first()
            if (pacienteId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Usuario no identificado")
                return@launch
            }

            // Lógica simple: Reservar para "HOY" a las "AHORA + 1 hora" 
            // En una app real, aquí iría un selector de hora
            val fecha = LocalDate.now().toString()
            val horaInicio = LocalTime.now().plusMinutes(10).toString().substring(0, 5) // HH:mm
            val horaFin = LocalTime.now().plusMinutes(40).toString().substring(0, 5)

            val request = CrearReservaRequest(
                fecha = fecha,
                horaInicio = horaInicio, // El backend espera string HH:mm:ss o HH:mm
                horaFin = horaFin,
                paciente = PacienteIdDto(pacienteId),
                sala = SalaIdDto(lactarioId),
                estado = "PENDIENTE"
            )

            val result = patientRepository.crearReserva(request)
            if (result.isSuccess) {
                 _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    reservaExitosa = true
                )
            } else {
                 _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al reservar: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }
    
    fun resetReservaExitosa() {
        _uiState.value = _uiState.value.copy(reservaExitosa = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
