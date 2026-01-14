package com.example.lactacare.vistas.doctor.reportes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.dominio.repository.AuthRepository
import com.example.lactacare.dominio.repository.IDoctorRepository
import com.example.lactacare.datos.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DoctorReportesUiState(
    val isLoading: Boolean = false,
    val totalCitas: Int = 0,
    val citasAtendidas: Int = 0,
    val citasPendientes: Int = 0,
    val citasCanceladas: Int = 0,
    val salaNombre: String = "Detectando...",
    val error: String? = null
)

@HiltViewModel
class DoctorReportsViewModel @Inject constructor(
    private val repository: IDoctorRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorReportesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        generarReporteDiario()
    }

    fun generarReporteDiario() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // 1. Obtener Sala
            var salaId = sessionManager.userSalaId.first()
             if (salaId == null) {
                 val email = sessionManager.userEmail.first()
                 if (!email.isNullOrEmpty()) {
                     val result = authRepository.getEmpleadoData(email)
                     if (result.isSuccess) {
                         salaId = result.getOrNull()?.salaLactanciaId
                         if (salaId != null) sessionManager.saveSalaId(salaId)
                     }
                 }
            }

            // 2. Obtener Agenda de Hoy
            val hoy = LocalDate.now().toString()
            val result = repository.obtenerAgendaDelDia(hoy)

            if (result.isSuccess) {
                val todas = result.getOrDefault(emptyList())
                // Filtrar por Sala
                val agenda = if (salaId != null) {
                    todas.filter { it.sala?.id == salaId }
                } else {
                    emptyList()
                }

                // 3. Calcular Estadísticas
                val total = agenda.size
                val atendidas = agenda.count { it.estado == "CONFIRMADA" || it.estado == "ASISTIO" }
                val pendientes = agenda.count { it.estado == "PENDIENTE" }
                val canceladas = agenda.count { it.estado == "CANCELADA" }
                
                // Nombre Sala (Hack: Sacarlo de la primera reserva si existe)
                val nombreSala = agenda.firstOrNull()?.sala?.nombre ?: "Mi Sala"

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalCitas = total,
                    citasAtendidas = atendidas,
                    citasPendientes = pendientes,
                    citasCanceladas = canceladas,
                    salaNombre = nombreSala,
                    error = if (salaId == null) "Sin sala asignada" else null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error al cargar datos"
                )
            }
        }
    }
}
