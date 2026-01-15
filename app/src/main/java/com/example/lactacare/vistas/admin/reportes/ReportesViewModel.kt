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
    val filtroEstado: FiltroEstado = FiltroEstado.Todos,
    val filtroTipoReporte: FiltroTipoReporte = FiltroTipoReporte.Reservas,
    val tipoGrafico: TipoGrafico = TipoGrafico.Barras,
    val fechaInicio: LocalDate = LocalDate.now().minusMonths(1),
    val fechaFin: LocalDate = LocalDate.now(),
    val error: String? = null
)

enum class FiltroFecha { Dia, Semana, Mes, Personalizado }
enum class FiltroEstado { Todos, Activo, Inactivo, Pendiente, Finalizada, Cancelada } // Combined status for users/reservations
enum class FiltroTipoReporte { Reservas, Usuarios, Doctores, Inventario }
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
                // 2. Calcular estado inicial
                filtrarYCalcular(
                    _uiState.value.filtroFecha, 
                    _uiState.value.filtroInstitucion, 
                    _uiState.value.filtroEstado, 
                    _uiState.value.filtroTipoReporte, 
                    instituciones
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun setFiltroFecha(filtro: FiltroFecha) {
        filtrarYCalcular(filtro, _uiState.value.filtroInstitucion, _uiState.value.filtroEstado, _uiState.value.filtroTipoReporte, _uiState.value.instituciones)
    }
    
    fun setRangoFechas(inicio: LocalDate, fin: LocalDate) {
        _uiState.value = _uiState.value.copy(fechaInicio = inicio, fechaFin = fin, filtroFecha = FiltroFecha.Personalizado)
        // Re-trigger calculation with new custom range
        filtrarYCalcular(FiltroFecha.Personalizado, _uiState.value.filtroInstitucion, _uiState.value.filtroEstado, _uiState.value.filtroTipoReporte, _uiState.value.instituciones)
    }

    fun setFiltroInstitucion(institucion: Institucion?) {
        filtrarYCalcular(_uiState.value.filtroFecha, institucion, _uiState.value.filtroEstado, _uiState.value.filtroTipoReporte, _uiState.value.instituciones)
    }

    fun setFiltroEstado(estado: FiltroEstado) {
        filtrarYCalcular(_uiState.value.filtroFecha, _uiState.value.filtroInstitucion, estado, _uiState.value.filtroTipoReporte, _uiState.value.instituciones)
    }

    fun setFiltroTipoReporte(tipo: FiltroTipoReporte) {
        filtrarYCalcular(_uiState.value.filtroFecha, _uiState.value.filtroInstitucion, _uiState.value.filtroEstado, tipo, _uiState.value.instituciones)
    }

    fun setTipoGrafico(tipo: TipoGrafico) {
        _uiState.value = _uiState.value.copy(tipoGrafico = tipo)
    }

    // Helpers de Fecha (Robustos)
    private fun parsearFecha(fechaStr: String?): LocalDate? {
        if (fechaStr.isNullOrEmpty()) return null
        return try {
            if (fechaStr.contains("T")) {
                 java.time.LocalDateTime.parse(fechaStr, java.time.format.DateTimeFormatter.ISO_DATE_TIME).toLocalDate()
            } else {
                 java.time.LocalDate.parse(fechaStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            }
        } catch (e: Exception) {
            try {
                java.time.LocalDate.parse(fechaStr)
            } catch (e2: Exception) { null }
        }
    }

    private fun isDateInWeek(dateStr: String?): Boolean {
        val date = parsearFecha(dateStr) ?: return false
        val now = LocalDate.now()
        val inicioSemana = now.minusDays(now.dayOfWeek.value.toLong() - 1)
        val finSemana = inicioSemana.plusDays(6)
        return !date.isBefore(inicioSemana) && !date.isAfter(finSemana)
    }

    private fun isDateInMonth(dateStr: String?): Boolean {
        val date = parsearFecha(dateStr) ?: return false
        val now = LocalDate.now()
        return date.month == now.month && date.year == now.year
    }
    
    private fun isDateInRange(dateStr: String?, inicio: LocalDate, fin: LocalDate): Boolean {
        val date = parsearFecha(dateStr) ?: return false
        return !date.isBefore(inicio) && !date.isAfter(fin)
    }

    private fun filtrarYCalcular(
        filtroFecha: FiltroFecha, 
        filtroInstitucion: Institucion?, 
        filtroEstado: FiltroEstado,
        filtroTipoReporte: FiltroTipoReporte,
        instituciones: List<Institucion>
    ) {
        if (rawData == null) return

        viewModelScope.launch {
            // A. Filtrar Reservas (Lógica principal)
            // Si el tipo es Usuarios, deberíamos filtrar usuarios, pero por ahora dashboard se basa en Reservas.
            // Ajustaremos Actividad Reciente según el tipo.
            
            val filteredReservas = rawData!!.reservas.filter { reserva ->
                val cumpleInstitucion = if (filtroInstitucion == null) true else {
                    // Comparación por nombre ya que DTO aplanado no trae ID de institución
                    reserva.nombreInstitucion == filtroInstitucion.nombreInstitucion
                }

                val cumpleFecha = when (filtroFecha) {
                    FiltroFecha.Dia -> parsearFecha(reserva.fecha) == LocalDate.now()
                    FiltroFecha.Semana -> isDateInWeek(reserva.fecha)
                    FiltroFecha.Mes -> isDateInMonth(reserva.fecha)
                    FiltroFecha.Personalizado -> isDateInRange(reserva.fecha, _uiState.value.fechaInicio, _uiState.value.fechaFin)
                }

                val cumpleEstado = when(filtroEstado) {
                    FiltroEstado.Todos -> true
                    FiltroEstado.Pendiente -> reserva.estado == "Pendiente"
                    FiltroEstado.Finalizada -> reserva.estado == "Finalizada"
                    FiltroEstado.Cancelada -> reserva.estado == "Cancelada"
                    else -> true // Estados de usuario ignorados para reservas
                }

                cumpleInstitucion && cumpleFecha && cumpleEstado
            }

            // B. Filtrar Usuarios/Docs si aplica (Solo para PDF o Stats)
            // Por simplicidad, en UI mostramos Stats calculados.
            
            // C. Mapear para PDF
            val actividades = when(filtroTipoReporte) {
                FiltroTipoReporte.Reservas -> {
                     filteredReservas.take(20).map { reserva ->
                        com.example.lactacare.dominio.model.ActividadReciente(
                            titulo = "Reserva: ${reserva.nombreSala ?: "Sala"}",
                            subtitulo = "${reserva.nombrePaciente ?: "Paciente"} ${reserva.apellidoPaciente ?: ""} - ${reserva.fecha} [${reserva.estado ?: "N/A"}]",
                            esAlerta = false
                        )
                    }
                }
                FiltroTipoReporte.Usuarios -> {
                     rawData!!.pacientes.take(20).map { usuario ->
                        com.example.lactacare.dominio.model.ActividadReciente(
                            titulo = "Paciente: ${usuario.nombreCompleto}",
                            subtitulo = "Registrado - ${usuario.correo}",
                            esAlerta = false
                        )
                    }
                }
                FiltroTipoReporte.Doctores -> {
                     rawData!!.doctores.take(20).map { doc ->
                        com.example.lactacare.dominio.model.ActividadReciente(
                            titulo = "Dr. ${doc.nombreCompleto}",
                            subtitulo = "Especialista - ${doc.correo}",
                            esAlerta = false
                        )
                    }
                }
                else -> emptyList()
            }

            val countCitas = filteredReservas.size

            // Stats Calculadas
            val reservasSemanaList = rawData!!.reservas.filter { isDateInWeek(it.fecha) }
            
            // Agrupar por día para el gráfico (List<Pair<String, Int>>)
            val citasSemanaAgg = reservasSemanaList
                .groupBy { parsearFecha(it.fecha)?.dayOfWeek }
                .mapNotNull { (day, list) ->
                    day?.let {
                        val nombreDia = when(it) {
                            java.time.DayOfWeek.MONDAY -> "Lun"
                            java.time.DayOfWeek.TUESDAY -> "Mar"
                            java.time.DayOfWeek.WEDNESDAY -> "Mie"
                            java.time.DayOfWeek.THURSDAY -> "Jue"
                            java.time.DayOfWeek.FRIDAY -> "Vie"
                            java.time.DayOfWeek.SATURDAY -> "Sab"
                            java.time.DayOfWeek.SUNDAY -> "Dom"
                        }
                        nombreDia to list.size
                    }
                }

            val statsCalculadas = DashboardAdminStats(
                totalUsuarios = rawData!!.pacientes.size,
                totalDoctores = rawData!!.doctores.size,
                citasHoy = countCitas, // Total Filtrado por el usuario
                alertasActivas = 0,
                actividadesRecientes = actividades,
                citasSemana = citasSemanaAgg, // Correct type: List<Pair<String, Int>>
                crecimientoCitas = 0.0,
                institucion = filtroInstitucion ?: rawData!!.institucion
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                stats = statsCalculadas,
                instituciones = instituciones,
                filtroFecha = filtroFecha,
                filtroInstitucion = filtroInstitucion,
                filtroEstado = filtroEstado,
                filtroTipoReporte = filtroTipoReporte
            )
        }
    }
}
