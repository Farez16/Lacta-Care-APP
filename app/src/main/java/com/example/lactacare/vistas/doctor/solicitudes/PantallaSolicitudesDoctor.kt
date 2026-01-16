package com.example.lactacare.vistas.doctor.solicitudes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.vistas.theme.DoctorPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSolicitudesDoctor(
    onNavigateBack: () -> Unit,
    viewModel: SolicitudesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarSolicitudes()
    }

    // Mostrar mensaje de éxito y limpiar después de 2 segundos
    LaunchedEffect(uiState.mensajeExito) {
        uiState.mensajeExito?.let {
            kotlinx.coroutines.delay(2000)
            viewModel.limpiarMensajeExito()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitudes de Retiro") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DoctorPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = DoctorPrimary)
                            Spacer(Modifier.height(16.dp))
                            Text("Cargando solicitudes...")
                        }
                    }
                }
                
                uiState.solicitudes.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No hay solicitudes pendientes",
                                color = Color.Gray
                            )
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.solicitudes) { solicitud ->
                            SolicitudCard(
                                solicitud = solicitud,
                                onMarcarRetirada = { viewModel.mostrarDialogConfirmar(solicitud) }
                            )
                        }
                    }
                }
            }

            // Diálogo de confirmación
            if (uiState.mostrarDialogConfirmar && uiState.solicitudSeleccionada != null) {
                DialogConfirmarRetiro(
                    solicitud = uiState.solicitudSeleccionada!!,
                    onConfirmar = { viewModel.confirmarRetiro() },
                    onCancelar = { viewModel.cancelarConfirmacion() }
                )
            }

            // Snackbar de error
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.limpiarError() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(error)
                }
            }

            // Snackbar de éxito
            uiState.mensajeExito?.let { mensaje ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = Color(0xFF4CAF50)
                ) {
                    Text(mensaje, color = Color.White)
                }
            }
        }
    }
}
