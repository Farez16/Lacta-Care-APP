package com.example.lactacare.vistas.paciente.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.ReservaPacienteDto
import com.example.lactacare.datos.local.SessionManager
import com.example.lactacare.datos.network.ApiService
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
    val nombreCompleto: String? = null,
    val fotoPerfil: String? = null,
    val proximaCita: ReservaPacienteDto? = null,
    val nombreBebe: String? = null,
    val sugerencias: List<com.example.lactacare.datos.dto.SugerenciaDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class PatientHomeViewModel @Inject constructor(
    private val apiService: ApiService,
    private val repository: IPatientRepository,
    private val sugerenciasRepo: com.example.lactacare.datos.repository.SugerenciasRepository,
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

            // 1. Cargar datos del Paciente (desde API)
            val idPaciente = sessionManager.userId.first()
            var nombreCompleto: String? = null
            var fotoPerfil: String? = null
            
            if (idPaciente != null) {
                try {
                    val perfilResponse = apiService.obtenerPerfilPaciente(idPaciente)
                    if (perfilResponse.isSuccessful && perfilResponse.body() != null) {
                        val perfil = perfilResponse.body()!!
                        nombreCompleto = buildString {
                            append(perfil.primerNombre)
                            if (!perfil.segundoNombre.isNullOrBlank()) {
                                append(" ${perfil.segundoNombre}")
                            }
                            append(" ${perfil.primerApellido}")
                            if (!perfil.segundoApellido.isNullOrBlank()) {
                                append(" ${perfil.segundoApellido}")
                            }
                        }
                        fotoPerfil = perfil.imagenPerfil
                    }
                } catch (e: Exception) {
                    // Si falla, continuar sin nombre/foto
                }
            }
            
            // 2. Cargar datos del Bebé (Local)
            val bebe = sessionManager.babyName.first()
            
            // 3. Cargar Reservas (Remoto + Real)
            var proxima: ReservaPacienteDto? = null
            
            if (idPaciente != null) {
                val result = repository.obtenerMisReservas(idPaciente)
                if (result.isSuccess) {
                    val reservas = result.getOrDefault(emptyList())
                    // Filtramos las futuras y EN RESERVA
                    val ahora = java.time.LocalTime.now()
                    val hoy = java.time.LocalDate.now().toString()
                    
                    proxima = reservas
                        .filter { it.estado.equals("EN RESERVA", ignoreCase = true) }
                        .sortedBy { it.fecha } 
                        .firstOrNull()
                }
            }

            // 4. Cargar Sugerencias (Tips) Aleatorias
            var tips: List<com.example.lactacare.datos.dto.SugerenciaDto> = emptyList()
            val resultTips = sugerenciasRepo.obtenerSugerencias()
            if (resultTips.isSuccess) {
                tips = resultTips.getOrDefault(emptyList()).shuffled()
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                nombreCompleto = nombreCompleto,
                fotoPerfil = fotoPerfil,
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
