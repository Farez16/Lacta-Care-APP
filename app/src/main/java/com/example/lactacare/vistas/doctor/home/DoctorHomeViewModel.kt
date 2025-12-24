package com.example.lactacare.vistas.doctor.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.DoctorReservaDto
import com.example.lactacare.dominio.repository.IDoctorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DoctorHomeUiState(
    val isLoading: Boolean = false,
    val agenda: List<DoctorReservaDto> = emptyList(),
    val error: String? = null,
    val fechaHoy: String = LocalDate.now().toString()
)

@HiltViewModel
class DoctorHomeViewModel @Inject constructor(
    private val repository: IDoctorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorHomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarAgendaHoy()
    }

    fun cargarAgendaHoy() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Usar fecha real del dispositivo
            val hoy = LocalDate.now().toString()
            
            val result = repository.obtenerAgendaDelDia(hoy)
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    agenda = result.getOrDefault(emptyList()),
                    fechaHoy = hoy
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error al cargar agenda"
                )
            }
        }
    }
}
