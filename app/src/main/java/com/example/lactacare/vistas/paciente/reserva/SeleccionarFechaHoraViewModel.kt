package com.example.lactacare.vistas.paciente.reserva

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.BloqueHorarioDto
import com.example.lactacare.datos.dto.CrearReservaRequest
import com.example.lactacare.datos.dto.CubiculoIdDto
import com.example.lactacare.datos.dto.PacienteIdDto
import com.example.lactacare.datos.dto.SalaIdDto
import com.example.lactacare.datos.local.SessionManager
import com.example.lactacare.dominio.repository.ILactariosRepository
import com.example.lactacare.dominio.repository.IPatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
data class SeleccionarFechaHoraUiState(
    val lactarioId: Long = 0,
    val nombreSala: String = "",
    val cubiculoId: Long = 0,
    val fechaSeleccionada: LocalDate = LocalDate.now(),
    val bloques: List<BloqueHorarioDto> = emptyList(),
    val bloqueSeleccionado: BloqueHorarioDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val reservaCreada: Boolean = false
)
@HiltViewModel
class SeleccionarFechaHoraViewModel @Inject constructor(
    private val lactariosRepository: ILactariosRepository,
    private val patientRepository: IPatientRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(SeleccionarFechaHoraUiState())
    val uiState = _uiState.asStateFlow()
    fun inicializar(lactarioId: Long, nombreSala: String, cubiculoId: Long) {
        _uiState.value = _uiState.value.copy(
            lactarioId = lactarioId,
            nombreSala = nombreSala,
            cubiculoId = cubiculoId
        )
        cargarDisponibilidad()
    }
    fun seleccionarFecha(fecha: LocalDate) {
        _uiState.value = _uiState.value.copy(
            fechaSeleccionada = fecha,
            bloqueSeleccionado = null
        )
        cargarDisponibilidad()
    }
    fun cargarDisponibilidad() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val fecha = _uiState.value.fechaSeleccionada.toString()
            val result = lactariosRepository.obtenerDisponibilidad(
                _uiState.value.lactarioId,
                fecha
            )

            if (result.isSuccess) {
                val bloques = result.getOrDefault(emptyList())
                
                // Filtrar horas pasadas si es HOY
                val bloquesFiltrados = if (_uiState.value.fechaSeleccionada == LocalDate.now()) {
                    val horaActual = java.time.LocalTime.now()
                    bloques.map { bloque ->
                        val horaInicio = java.time.LocalTime.parse(bloque.horaInicio)
                        // Marcar como no disponible si la hora ya pasó
                        if (horaInicio.isBefore(horaActual)) {
                            bloque.copy(disponible = false)
                        } else {
                            bloque
                        }
                    }
                } else {
                    bloques
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    bloques = bloquesFiltrados
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }
    fun seleccionarBloque(bloque: BloqueHorarioDto) {
        if (bloque.disponible) {
            _uiState.value = _uiState.value.copy(bloqueSeleccionado = bloque)
        }
    }
    fun confirmarReserva() {
        viewModelScope.launch {
            val bloque = _uiState.value.bloqueSeleccionado
            if (bloque == null) {
                _uiState.value = _uiState.value.copy(error = "Selecciona un horario")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val pacienteId = sessionManager.userId.first()
            if (pacienteId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Usuario no identificado"
                )
                return@launch
            }
            val request = CrearReservaRequest(
                fecha = _uiState.value.fechaSeleccionada.toString(),
                horaInicio = bloque.horaInicio,
                horaFin = bloque.horaFin,
                paciente = PacienteIdDto(pacienteId),
                sala = SalaIdDto(_uiState.value.lactarioId),
                cubiculo = CubiculoIdDto(_uiState.value.cubiculoId),
                estado = "EN RESERVA"
            )
            val result = patientRepository.crearReserva(request)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    reservaCreada = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al crear reserva: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun setError(mensaje: String) {
        _uiState.value = _uiState.value.copy(error = mensaje)
    }
}