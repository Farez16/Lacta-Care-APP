package com.example.lactacare.vistas.doctor.ubicacion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.datos.dto.RefrigeradorDisponibleDto
import com.example.lactacare.vistas.doctor.atencion.ContenedorItem
import com.example.lactacare.vistas.theme.DoctorPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSeleccionUbicacion(
    idReserva: Long,
    contenedores: List<ContenedorItem>,
    idSala: Int,
    onVolver: () -> Unit,
    onAtencionGuardada: () -> Unit,
    viewModel: UbicacionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.inicializar(idReserva, contenedores, idSala)
    }

    LaunchedEffect(uiState.atencionGuardada) {
        if (uiState.atencionGuardada != null) {
            onAtencionGuardada()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar Ubicación") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DoctorPrimary)
            }
        } else if (uiState.refrigeradorSeleccionado == null) {
            // Pantalla 1: Seleccionar Refrigerador
            PantallaListaRefrigeradores(
                refrigeradores = uiState.refrigeradores,
                onSeleccionar = { viewModel.seleccionarRefrigerador(it) },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            // Pantalla 2: Asignar Coordenadas
            PantallaAsignarCoordenadas(
                refrigerador = uiState.refrigeradorSeleccionado!!,
                contenedores = uiState.contenedores,
                contenedorActualIndex = uiState.contenedorActualIndex,
                onAsignarUbicacion = { piso, fila, columna ->
                    viewModel.asignarUbicacion(piso, fila, columna)
                },
                onSiguiente = { viewModel.siguienteContenedor() },
                onAnterior = { viewModel.anteriorContenedor() },
                onFinalizar = { viewModel.guardarAtencionCompleta(idReserva) },
                modifier = Modifier.padding(paddingValues)
            )
        }

        // Mostrar error si existe
        uiState.error?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.limpiarError() },
                title = { Text("Error") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { viewModel.limpiarError() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun PantallaListaRefrigeradores(
    refrigeradores: List<RefrigeradorDisponibleDto>,
    onSeleccionar: (RefrigeradorDisponibleDto) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Seleccione un Refrigerador",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        if (refrigeradores.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Kitchen,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No hay refrigeradores disponibles", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(refrigeradores) { refrigerador ->
                    RefrigeradorCard(
                        refrigerador = refrigerador,
                        onClick = { onSeleccionar(refrigerador) }
                    )
                }
            }
        }
    }
}

@Composable
fun RefrigeradorCard(
    refrigerador: RefrigeradorDisponibleDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = refrigerador.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Dimensiones: ${refrigerador.pisos}P x ${refrigerador.filas}F x ${refrigerador.columnas}C",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Espacios disponibles: ${refrigerador.capacidadDisponible}",
                    fontSize = 14.sp,
                    color = if (refrigerador.capacidadDisponible > 0) Color(0xFF66BB6A) else Color.Red
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = DoctorPrimary
            )
        }
    }
}
