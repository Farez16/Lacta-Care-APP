package com.example.lactacare.vistas.doctor.reportes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.ContenedorLecheDto
import com.example.lactacare.datos.network.ApiService
import com.example.lactacare.vistas.admin.reportes.TipoGrafico
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DoctorReporteUiState(
    val isLoading: Boolean = false,
    val totalLecheLitros: Double = 0.0,
    val totalContenedores: Int = 0,
    val pacientesAtendidos: Int = 0,
    val dataGraficoSemanal: List<Pair<String, Float>> = emptyList(), // Día -> Litros
    val error: String? = null,
    val tipoGrafico: TipoGrafico = TipoGrafico.Barras
)

@HiltViewModel
class DoctorReportesViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorReporteUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarDatosReporte()
    }

    fun cargarDatosReporte() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // 1. Fetch Contenedores (Fuente de verdad para producción de leche)
                val respContenedores = apiService.obtenerContenedoresLeche()
                val contenedores = respContenedores.body() ?: emptyList()

                // 2. Fetch Reservas (Para contar pacientes/atenciones)
                // Nota: Usamos endpoint global y asumimos filtrado por contexto o mostramos global de la sala
                val respReservas = apiService.obtenerReservas()
                val reservas = respReservas.body() ?: emptyList()
                
                // --- CÁLCULOS ---
                
                // A. Total Leche (Convertir ml a Litros)
                val totalMl = contenedores.sumOf { it.cantidadMililitros ?: 0.0 }
                val totalLitros = totalMl / 1000.0

                // B. Gráfico Semanal (Simulado agrupando por fecha dummy o real si existe)
                // Como contenedor tiene fechaHoraExtraccion (String ISO), intentamos parsear
                val graficoData = agruparLechePorDia(contenedores)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalLecheLitros = String.format("%.2f", totalLitros).toDouble(),
                    totalContenedores = contenedores.size,
                    pacientesAtendidos = reservas.count { it.estado == "FINALIZADA" || it.estado == "ATENDIDA" },
                    dataGraficoSemanal = graficoData
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun agruparLechePorDia(contenedores: List<ContenedorLecheDto>): List<Pair<String, Float>> {
        // Mock rápido o agrupación real
        // Asumiendo que queremos mostrar últimos 7 días con datos dummy si no hay fechas parseables
        // Si hay fechas reales:
        val map = mutableMapOf<String, Double>()
        
        contenedores.forEach { c ->
            val fecha = c.fechaHoraExtraccion?.take(10) ?: "Desconocido" // yyyy-MM-dd
            val cant = c.cantidadMililitros ?: 0.0
            map[fecha] = (map[fecha] ?: 0.0) + cant
        }
        
        // Convert to List<Pair>
        return map.entries.map { it.key to (it.value / 1000f).toFloat() }.takeLast(7)
    }
    
    fun setTipoGrafico(tipo: TipoGrafico) {
        _uiState.value = _uiState.value.copy(tipoGrafico = tipo)
    }
}
