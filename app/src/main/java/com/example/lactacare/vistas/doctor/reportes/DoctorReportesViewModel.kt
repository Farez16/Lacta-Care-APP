package com.example.lactacare.vistas.doctor.reportes

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.EstadisticasDoctorDto
import com.example.lactacare.datos.network.ApiService
import com.example.lactacare.servicios.PdfService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Estados del UI para reportes del doctor
 */
data class DoctorReporteUiState(
    val isLoading: Boolean = false,
    val isExportingPdf: Boolean = false,
    val estadisticas: EstadisticasDoctorDto? = null,
    val filtroFecha: FiltroFecha = FiltroFecha.Semana,
    val error: String? = null
)

/**
 * Filtros de fecha disponibles
 */
enum class FiltroFecha {
    Hoy, Semana, Mes;
    
    fun toDisplayString(): String = when (this) {
        Hoy -> "Hoy"
        Semana -> "Última Semana"
        Mes -> "Último Mes"
    }
}

/**
 * Eventos de PDF para comunicación con la UI
 */
sealed class PdfEvent {
    data class Success(val uri: Uri, val ruta: String) : PdfEvent()
    data class Error(val message: String) : PdfEvent()
}

/**
 * ViewModel para reportes y estadísticas del doctor
 * Optimizado con query backend que responde en <1s
 */
@HiltViewModel
class DoctorReportesViewModel @Inject constructor(
    private val apiService: ApiService,
    private val pdfService: PdfService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorReporteUiState())
    val uiState = _uiState.asStateFlow()

    private val _pdfEvent = MutableSharedFlow<PdfEvent>()
    val pdfEvent = _pdfEvent.asSharedFlow()

    /**
     * Carga estadísticas del doctor
     * @param idDoctor ID del doctor (Integer)
     */
    fun cargarEstadisticas(idDoctor: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Calcular fechas según filtro
                val (fechaInicio, fechaFin) = calcularRangoFechas(_uiState.value.filtroFecha)
                
                // Llamar al endpoint optimizado
                val response = apiService.obtenerEstadisticasDoctor(
                    idDoctor = idDoctor,
                    fechaInicio = fechaInicio,
                    fechaFin = fechaFin
                )

                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        estadisticas = response.body(),
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al cargar estadísticas: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Cambia el filtro de fecha y recarga datos
     */
    fun setFiltroFecha(filtro: FiltroFecha, idDoctor: Int) {
        _uiState.value = _uiState.value.copy(filtroFecha = filtro)
        cargarEstadisticas(idDoctor)
    }

    /**
     * Exporta las estadísticas a PDF
     */
    fun exportarPdf(nombreDoctor: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExportingPdf = true)
            
            val estadisticas = _uiState.value.estadisticas
            if (estadisticas == null) {
                _pdfEvent.emit(PdfEvent.Error("No hay datos para exportar"))
                _uiState.value = _uiState.value.copy(isExportingPdf = false)
                return@launch
            }

            val filtroTexto = _uiState.value.filtroFecha.toDisplayString()
            val result = pdfService.generarPdfEstadisticas(estadisticas, nombreDoctor, filtroTexto)
            
            result.fold(
                onSuccess = { uri ->
                    val ruta = pdfService.obtenerRutaLegible(uri)
                    _pdfEvent.emit(PdfEvent.Success(uri, ruta))
                },
                onFailure = { error ->
                    _pdfEvent.emit(PdfEvent.Error(error.message ?: "Error al generar PDF"))
                }
            )
            
            _uiState.value = _uiState.value.copy(isExportingPdf = false)
        }
    }

    /**
     * Abre el PDF con una aplicación externa
     */
    fun abrirPdf(uri: Uri, activity: Activity) {
        pdfService.abrirPdf(uri, activity)
    }

    /**
     * Calcula el rango de fechas según el filtro seleccionado
     */
    private fun calcularRangoFechas(filtro: FiltroFecha): Pair<String, String> {
        val hoy = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        return when (filtro) {
            FiltroFecha.Hoy -> {
                hoy.format(formatter) to hoy.format(formatter)
            }
            FiltroFecha.Semana -> {
                val hace7Dias = hoy.minusDays(7)
                hace7Dias.format(formatter) to hoy.format(formatter)
            }
            FiltroFecha.Mes -> {
                val hace30Dias = hoy.minusDays(30)
                hace30Dias.format(formatter) to hoy.format(formatter)
            }
        }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
