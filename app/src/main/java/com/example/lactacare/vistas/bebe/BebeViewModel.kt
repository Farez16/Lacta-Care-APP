package com.example.lactacare.vistas.bebe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.dominio.model.Bebe
import com.example.lactacare.dominio.repository.BebeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BebeUiState {
    object Loading : BebeUiState()
    data class Success(val bebes: List<Bebe>) : BebeUiState()
    data class Error(val mensaje: String) : BebeUiState()
}

class BebeViewModel(
    private val bebeRepository: BebeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BebeUiState>(BebeUiState.Loading)
    val uiState: StateFlow<BebeUiState> = _uiState.asStateFlow()

    init {
        cargarBebes()
    }

    fun cargarBebes() {
        viewModelScope.launch {
            _uiState.value = BebeUiState.Loading
            try {
                val lista = bebeRepository.obtenerBebesPorFamiliar(1)
                _uiState.value = BebeUiState.Success(lista)
            } catch (e: Exception) {
                _uiState.value = BebeUiState.Error("Error al cargar bebés")
            }
        }
    }

    // --- NUEVA FUNCIÓN PARA GUARDAR ---
    fun registrarBebe(nombre: String, fecha: String, genero: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val nuevoBebe = Bebe(
                    id = (10..1000).random(), // ID temporal
                    idFamiliar = 1,
                    nombre = nombre,
                    fechaNacimiento = fecha,
                    genero = genero
                )
                // Guardamos en el Mock
                bebeRepository.registrarBebe(nuevoBebe)

                // Recargamos la lista para que al volver se vea el nuevo bebé
                cargarBebes()

                // Avisamos a la UI que terminó bien
                onSuccess()
            } catch (e: Exception) {
                // Manejo de error si fuera necesario
            }
        }
    }
}