package com.example.lactacare.vistas.doctor.almacenamiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.ContenedorLecheDto
import com.example.lactacare.datos.dto.CrearContenedorRequest
import com.example.lactacare.datos.dto.RefrigeradorDto
import com.example.lactacare.datos.repository.RefrigeradorRepository
import com.example.lactacare.datos.network.ApiService // Direct usage for now or create InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import com.example.lactacare.dominio.repository.AuthRepository
import com.example.lactacare.datos.local.SessionManager
import kotlinx.coroutines.flow.first

data class AlmacenamientoUiState(
    val isLoading: Boolean = false,
    val refrigeradores: List<RefrigeradorDto> = emptyList(),
    val contenedores: List<ContenedorLecheDto> = emptyList(),
    val selectedFridge: RefrigeradorDto? = null,
    val temperatura: Double = 4.0, // Mock IoT
    val error: String? = null
)



@HiltViewModel
class AlmacenamientoViewModel @Inject constructor(
    private val refrigeradorRepository: RefrigeradorRepository,
    private val apiService: ApiService,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlmacenamientoUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarDatos()
        iniciarSimulacionIoT()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // 1. Obtener salaId (Local o Remoto)
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

                // 2. Obtener todos los refris
                val refrisResult = refrigeradorRepository.obtenerRefrigeradores()
                val todosRefris = refrisResult.getOrDefault(emptyList())

                // 3. Filtrar por Sala
                val refrisFiltrados = if (salaId != null) {
                    todosRefris.filter { it.sala?.id == salaId.toLong() }
                } else {
                    todosRefris // Si no hay sala (no debería pasar), mostramos todo o vacío.
                }

                // 4. Obtener Contenedores
                val contenedoresResp = apiService.obtenerContenedoresLeche()
                val contenedores = if (contenedoresResp.isSuccessful) contenedoresResp.body() ?: emptyList() else emptyList()

                // Filtrar contenedores para que solo sean de los refris visibles
                val idsRefrisVisibles = refrisFiltrados.map { it.id }
                val contenedoresFiltrados = contenedores.filter { it.refrigeradorId in idsRefrisVisibles }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    refrigeradores = refrisFiltrados,
                    contenedores = contenedoresFiltrados,
                    selectedFridge = refrisFiltrados.firstOrNull(), // Auto-select first
                    error = if (salaId == null) "No tienes sala asignada" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun seleccionarRefrigerador(refri: RefrigeradorDto) {
        _uiState.value = _uiState.value.copy(selectedFridge = refri)
    }

    fun guardarContenedor(cantidad: Double, piso: Int, fila: Int, columna: Int, atencionId: Long = 0) { // atencionId hardcoded for demo if not passed
        val refri = uiState.value.selectedFridge ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val request = CrearContenedorRequest(
                    fechaHoraExtraccion = LocalDateTime.now().toString(),
                    cantidadMililitros = cantidad,
                    refrigeradorId = refri.id,
                    piso = piso,
                    fila = fila,
                    columna = columna, 
                    atencionId = atencionId
                )
                
                // Optimistic Update or Refetch
                apiService.crearContenedorLeche(request)
                
                cargarDatos() // Refetch to sync
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Error guardando: ${e.message}")
            }
        }
    }

    private fun iniciarSimulacionIoT() {
        viewModelScope.launch {
            while (true) {
                delay(5000)
                // Fluctuate between 3.5 and 4.5
                val temp = 3.5 + Math.random()
                _uiState.value = _uiState.value.copy(temperatura = String.format("%.1f", temp).toDouble())
            }
        }
    }
}
