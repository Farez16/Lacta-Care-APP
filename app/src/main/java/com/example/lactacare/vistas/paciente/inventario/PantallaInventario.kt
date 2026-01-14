package com.example.lactacare.vistas.paciente.inventario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInventario(
    idPaciente: Long,
    viewModel: InventarioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Cargar inventario al iniciar
    LaunchedEffect(idPaciente) {
        viewModel.cargarInventario(idPaciente)
    }

    // Mostrar mensaje de éxito
    uiState.mensajeExito?.let { mensaje ->
        LaunchedEffect(mensaje) {
            kotlinx.coroutines.delay(2000)
            viewModel.limpiarMensajeExito()
        }
        
        Snackbar(
            modifier = Modifier.padding(16.dp),
            containerColor = Color(0xFF4CAF50)
        ) {
            Text(mensaje, color = Color.White)
        }
    }

    // Mostrar mensaje de error
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            kotlinx.coroutines.delay(3000)
            viewModel.limpiarError()
        }
        
        Snackbar(
            modifier = Modifier.padding(16.dp),
            containerColor = Color(0xFFF44336)
        ) {
            Text(error, color = Color.White)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Inventario") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.cargarInventario(idPaciente) }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Recargar",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Filtros
            FilasChipsFiltro(
                filtroActual = uiState.filtroActual,
                onFiltroClick = { viewModel.aplicarFiltro(it) }
            )

            // Contenido
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2196F3))
                    }
                }

                uiState.contenedoresFiltrados.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay contenedores",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.contenedoresFiltrados) { contenedor ->
                            TarjetaContenedor(
                                contenedor = contenedor,
                                onRetirarClick = { viewModel.mostrarDialogRetirar(contenedor) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog de confirmación
    if (uiState.mostrarDialogRetirar && uiState.contenedorSeleccionado != null) {
        DialogConfirmarRetiro(
            contenedor = uiState.contenedorSeleccionado!!,
            onConfirmar = { viewModel.confirmarRetiro(idPaciente) },
            onCancelar = { viewModel.cancelarRetiro() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilasChipsFiltro(
    filtroActual: FiltroInventario,
    onFiltroClick: (FiltroInventario) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Filtros:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FiltroInventario.values().forEach { filtro ->
                FilterChip(
                    selected = filtroActual == filtro,
                    onClick = { onFiltroClick(filtro) },
                    label = {
                        Text(
                            text = when (filtro) {
                                FiltroInventario.TODO -> "Todo"
                                FiltroInventario.CADUCADA -> "Caducada"
                                FiltroInventario.REFRIGERADA -> "Refrigerada"
                                FiltroInventario.CONGELADA -> "Congelada"
                                FiltroInventario.RETIRADA -> "Retirada"
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2196F3),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}
