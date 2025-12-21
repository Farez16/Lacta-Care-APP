package com.example.lactacare.vistas.lactarios

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lactacare.datos.MockLactarioRepository
import com.example.lactacare.datos.MockReservasRepository

@Composable
fun PantallaLactario(
    onVolver: () -> Unit,

    viewModel: LactarioViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LactarioViewModel(
                    // 1. El nombre correcto es 'lactarioRepository' (no 'repository')
                    lactarioRepository = MockLactarioRepository(),

                    // 2. Y necesitamos 'reservasRepository' para que funcione la sincronización
                    reservasRepository = MockReservasRepository()
                ) as T
            }
        }
    )
) {
    // Reutilizamos la UI de PantallaReservas
    PantallaReservas(
        onVolver = onVolver,
        viewModel = viewModel
    )
}