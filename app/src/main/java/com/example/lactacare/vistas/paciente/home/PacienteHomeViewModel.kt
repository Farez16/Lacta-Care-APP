package com.example.lactacare.vistas.paciente.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.ReservaPacienteDto
import com.example.lactacare.datos.local.SessionManager
import com.example.lactacare.dominio.repository.IPatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class PatientHomeUiState(
    val isLoading: Boolean = false,
    val proximaCita: ReservaPacienteDto? = null,
    val nombreBebe: String? = null,
    val sugerencias: List<com.example.lactacare.datos.dto.SugerenciaDto> = emptyList(), // Nuevo campo
    val error: String? = null
)

@HiltViewModel
class PatientHomeViewModel @Inject constructor(
    private val repository: IPatientRepository,
    private val sugerenciasRepo: com.example.lactacare.datos.repository.SugerenciasRepository, // Inyectado
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientHomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarDatosDashboard()
    }

    fun cargarDatosDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 1. Cargar datos del Bebé (Local)
            val bebe = sessionManager.babyName.first()
            
            // 2. Cargar Reservas (Remoto + Real)
            val idPaciente = sessionManager.userId.first()
            var proxima: ReservaPacienteDto? = null
            
            if (idPaciente != null) {
                val result = repository.obtenerMisReservas(idPaciente)
                if (result.isSuccess) {
                    val reservas = result.getOrDefault(emptyList())
                    // Filtramos las futuras y PENDIENTES
                    val ahora = LocalTime.now()
                    val hoy = LocalDate.now().toString()
                    
                    proxima = reservas
                        .filter { it.estado == "PENDIENTE" }
                        // Simplificación: Tomamos la primera que encontremos (idealmente ordenar por fecha)
                        .sortedBy { it.fecha } 
                        .firstOrNull()
                }
            }

            // 3. Cargar Sugerencias (Tips) Aleatorias
            var tips: List<com.example.lactacare.datos.dto.SugerenciaDto> = emptyList()
            val resultTips = sugerenciasRepo.obtenerSugerencias()
            if (resultTips.isSuccess) {
                tips = resultTips.getOrDefault(emptyList()).shuffled() // Aleatorio
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                nombreBebe = bebe,
                proximaCita = proxima,
                sugerencias = tips
            )
        }
    }
    
    fun cerrarSesion() {
        viewModelScope.launch { sessionManager.clearSession() }
    }
}
