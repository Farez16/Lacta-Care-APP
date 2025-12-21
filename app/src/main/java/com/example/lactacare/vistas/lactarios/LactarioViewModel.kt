package com.example.lactacare.vistas.lactarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.dominio.model.Lactario
import com.example.lactacare.dominio.repository.LactarioRepository
// 1. NUEVO IMPORT
import com.example.lactacare.dominio.repository.ReservasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LactarioUiState {
    object Loading : LactarioUiState()
    data class Success(val lactarios: List<Lactario>) : LactarioUiState()
    data class Error(val mensaje: String) : LactarioUiState()
}

class LactarioViewModel(
    private val lactarioRepository: LactarioRepository,
    // 2. INYECTAMOS EL REPOSITORIO DE RESERVAS TAMBIÉN
    private val reservasRepository: ReservasRepository
) : ViewModel() {

    private var listaCompleta: List<Lactario> = emptyList()
    // 3. NUEVA LISTA PARA GUARDAR IDs RESERVADOS
    private var idsLactariosReservados: List<Int> = emptyList()

    private val _uiState = MutableStateFlow<LactarioUiState>(LactarioUiState.Loading)
    val uiState: StateFlow<LactarioUiState> = _uiState.asStateFlow()

    var textoBusqueda = MutableStateFlow("")
    var filtroEstado = MutableStateFlow("Todos")

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = LactarioUiState.Loading
            try {
                // 4. CARGAMOS AMBOS DATOS EN PARALELO
                listaCompleta = lactarioRepository.obtenerLactarios()
                // Obtenemos TODAS las reservas del médico (agenda global simulada) para saber qué salas están ocupadas
                val reservasActivas = reservasRepository.obtenerReservasPorMedico(999) // ID dummy para traer "todas" las del mock

                // Guardamos solo los IDs de las salas que están confirmadas o pendientes
                idsLactariosReservados = reservasActivas
                    .filter { it.estado == "Confirmada" || it.estado == "Pendiente" }
                    .map { it.idLactario }

                aplicarFiltros()
            } catch (e: Exception) {
                _uiState.value = LactarioUiState.Error("Error al cargar salas: ${e.message}")
            }
        }
    }

    fun actualizarFiltros(nuevoTexto: String? = null, nuevoEstado: String? = null) {
        if (nuevoTexto != null) textoBusqueda.value = nuevoTexto
        if (nuevoEstado != null) filtroEstado.value = nuevoEstado
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val texto = textoBusqueda.value.lowercase()
        val estadoFiltro = filtroEstado.value

        val listaFiltrada = listaCompleta.filter { lactario ->
            val coincideTexto = lactario.nombre.lowercase().contains(texto) ||
                    lactario.direccion.lowercase().contains(texto)

            // Usamos la nueva función de simulación que consulta la lista real
            val estadoReal = calcularEstadoReal(lactario.id)
            val coincideEstado = if (estadoFiltro == "Todos") true else estadoReal.equals(estadoFiltro, ignoreCase = true)

            coincideTexto && coincideEstado
        }
        // Ordenamos para que los disponibles salgan primero
        _uiState.value = LactarioUiState.Success(listaFiltrada.sortedBy { calcularEstadoReal(it.id) != "Disponible" })
    }

    // 5. NUEVA FUNCIÓN: Calcula el estado basado en datos REALES del Mock de Reservas
    fun calcularEstadoReal(idLactario: Int): String {
        return if (idsLactariosReservados.contains(idLactario)) {
            "Reservado"
        } else {
            "Disponible"
        }
        // Nota: "Ocupado" lo omitimos por ahora para simplificar, o podrías añadir lógica de horario.
    }

    fun simularDistancia(id: Int): Int {
        return (id * 150) + 50
    }
}