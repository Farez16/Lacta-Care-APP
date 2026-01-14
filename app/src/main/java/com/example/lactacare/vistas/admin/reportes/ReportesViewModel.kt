package com.example.lactacare.vistas.admin.reportes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.repository.AdminRepository
import com.example.lactacare.datos.repository.InstitucionRepository
import com.example.lactacare.dominio.model.DashboardAdminStats
import com.example.lactacare.dominio.model.Institucion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ReporteUiState(
    val isLoading: Boolean = false,
    val stats: DashboardAdminStats? = null,
    val instituciones: List<Institucion> = emptyList(),
    val filtroFecha: FiltroFecha = FiltroFecha.Mes,
    val filtroInstitucion: Institucion? = null,
    val tipoGrafico: TipoGrafico = TipoGrafico.Barras,
    val fechaInicio: LocalDate = LocalDate.now().minusMonths(1),
    val fechaFin: LocalDate = LocalDate.now(),
    val error: String? = null
)

enum class FiltroFecha { Dia, Semana, Mes, Personalizado }
enum class TipoGrafico { Barras, Pastel, Lineas }

@HiltViewModel
class ReportesViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val institucionRepository: InstitucionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReporteUiState())
    val uiState: StateFlow<ReporteUiState> = _uiState.asStateFlow()
    
    // Datos crudos en memoria para filtrado cliente
    private var rawData: com.example.lactacare.datos.repository.ReporteDataRaw? = null

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // 1. Obtener datos crudos y catálogo de instituciones
                rawData = adminRepository.obtenerDatosCrudosReporte()
                val institucionesResult = institucionRepository.obtenerInstituciones()

                val instituciones = institucionesResult.getOrNull() ?: emptyList()

                // 2. Calcular estado inicial
                filtrarYCalcular(_uiState.value.filtroFecha, _uiState.value.filtroInstitucion, instituciones)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun setFiltroFecha(filtro: FiltroFecha) {
        filtrarYCalcular(filtro, _uiState.value.filtroInstitucion, _uiState.value.instituciones)
    }
    
    fun setRangoFechas(inicio: LocalDate, fin: LocalDate) {
        _uiState.value = _uiState.value.copy(fechaInicio = inicio, fechaFin = fin, filtroFecha = FiltroFecha.Personalizado)
        // Re-trigger calculation with new custom range
        filtrarYCalcular(FiltroFecha.Personalizado, _uiState.value.filtroInstitucion, _uiState.value.instituciones)
    }

    fun setFiltroInstitucion(institucion: Institucion?) {
        filtrarYCalcular(_uiState.value.filtroFecha, institucion, _uiState.value.instituciones)
    }

    fun setTipoGrafico(tipo: TipoGrafico) {
        _uiState.value = _uiState.value.copy(tipoGrafico = tipo)
    }

    private fun filtrarYCalcular(filtroFecha: FiltroFecha, filtroInstitucion: Institucion?, instituciones: List<Institucion>) {
        if (rawData == null) return

        viewModelScope.launch {
            // A. Filtrar Reservas
            val filteredReservas = rawData!!.reservas.filter { reserva ->
                val cumpleInstitucion = if (filtroInstitucion == null) true else {
                    reserva.salaLactancia?.institucion?.idInstitucion == filtroInstitucion.idInstitucion
                }

                val cumpleFecha = when (filtroFecha) {
                    FiltroFecha.Dia -> reserva.fecha == LocalDate.now().toString()
                    FiltroFecha.Semana -> isDateInWeek(reserva.fecha)
                    FiltroFecha.Mes -> isDateInMonth(reserva.fecha)
                    FiltroFecha.Personalizado -> isDateInRange(reserva.fecha, _uiState.value.fechaInicio, _uiState.value.fechaFin)
                }

                cumpleInstitucion && cumpleFecha
            }

            // B. Recalcular Estadísticas (Totales)
            // Nota: TotalUsuarios y TotalDoctores son globales, pero CitasHoy y Crecimiento dependen del filtro
            
            // Calculo de grafico de barras (ultimos 7 items filtrados o categorias)
            // Simplificación: Grafico muestra distribución por estado de las reservas filtradas
            
            val statsCalculadas = DashboardAdminStats(
                totalUsuarios = rawData!!.pacientes.size, // Global
                totalDoctores = rawData!!.doctores.size,   // Global
                citasHoy = filteredReservas.size,          // "Citas en Periodo" (reusing field)
                alertasActivas = 0,
                actividadesRecientes = emptyList(), // No recalculamos esto para reporte
                citasSemana = emptyList(), // Se usará para el Gráfico
                crecimientoCitas = 0.0
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                stats = statsCalculadas,
                instituciones = instituciones,
                filtroFecha = filtroFecha,
                filtroInstitucion = filtroInstitucion
            )
        }
    }
    
    // Helpers de Fecha (Simplificados, asumiendo "yyyy-MM-dd")
    private fun isDateInWeek(dateStr: String?): Boolean {
        if (dateStr == null) return false
        return try {
            val date = LocalDate.parse(dateStr)
            val now = LocalDate.now()
            date.isAfter(now.minusDays(7)) && !date.isAfter(now)
        } catch (e: Exception) { false }
    }

    private fun isDateInMonth(dateStr: String?): Boolean {
         if (dateStr == null) return false
        return try {
            val date = LocalDate.parse(dateStr)
            val now = LocalDate.now()
            date.month == now.month && date.year == now.year
        } catch (e: Exception) { false }
    }
    
    private fun isDateInRange(dateStr: String?, inicio: LocalDate, fin: LocalDate): Boolean {
         if (dateStr == null) return false
        return try {
            val date = LocalDate.parse(dateStr)
            !date.isBefore(inicio) && !date.isAfter(fin)
        } catch (e: Exception) { false }
    }
}
