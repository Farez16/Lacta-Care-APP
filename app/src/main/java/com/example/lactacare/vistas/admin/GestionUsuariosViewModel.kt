package com.example.lactacare.vistas.admin.usuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.PacienteDto
import com.example.lactacare.datos.dto.UsuarioResponseDto
import com.example.lactacare.datos.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GestionUiState(
    val isLoading: Boolean = false,
    val listaDoctores: List<UsuarioResponseDto> = emptyList(),
    val listaPacientes: List<PacienteDto> = emptyList(),
    val tabSeleccionado: Int = 0 // 0 = Doctores, 1 = Pacientes
)

@HiltViewModel
class GestionUsuariosViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GestionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarListas()
    }

    fun cargarListas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Cargamos ambas listas en paralelo
            val doctores = repository.obtenerListaDoctores()
            val pacientes = repository.obtenerListaPacientes()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                listaDoctores = doctores,
                listaPacientes = pacientes
            )
        }
    }

    fun cambiarTab(index: Int) {
        _uiState.value = _uiState.value.copy(tabSeleccionado = index)
    }
}