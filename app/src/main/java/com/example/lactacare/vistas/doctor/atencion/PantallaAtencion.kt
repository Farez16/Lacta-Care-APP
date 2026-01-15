package com.example.lactacare.vistas.doctor.atencion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.vistas.theme.DoctorPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAtencion(
    idReserva: Long,
    onVolver: () -> Unit,
    onContinuar: (Long, List<ContenedorItem>, Int) -> Unit, // idReserva, contenedores, idSala
    viewModel: AtencionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(idReserva) {
        viewModel.cargarDatosIniciales(idReserva)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Atención") },
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Información de la reserva
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Información de la Atención",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))

                        InfoRow("Doctor", uiState.nombreDoctor)
                        InfoRow("Paciente", uiState.reserva?.paciente?.let {
                            "${it.primerNombre} ${it.primerApellido}"
                        } ?: "Sin paciente")
                        InfoRow("Sala", uiState.reserva?.sala?.nombre ?: "Sin sala")
                        InfoRow("Hora", "${uiState.reserva?.horaInicio} - ${uiState.reserva?.horaFin}")
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Formulario de contenedores
                Text(
                    text = "Registro de Contenedores",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    OutlinedTextField(
                        value = uiState.cantidadActual,
                        onValueChange = { viewModel.actualizarCantidad(it) },
                        label = { Text("Cantidad (ml)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = uiState.errorCantidad != null,
                        supportingText = {
                            if (uiState.errorCantidad != null) {
                                Text(uiState.errorCantidad!!, color = Color.Red)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = { viewModel.agregarContenedor() },
                        modifier = Modifier
                            .height(56.dp)
                            .padding(top = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DoctorPrimary
                        )
                    ) {
                        Icon(Icons.Default.Add, "Agregar")
                        Spacer(Modifier.width(4.dp))
                        Text("Agregar")
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Lista de contenedores
                if (uiState.contenedores.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Contenedores Agregados",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.contenedores) { contenedor ->
                                    ContenedorCard(
                                        contenedor = contenedor,
                                        onEliminar = { viewModel.eliminarContenedor(contenedor.id) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Total
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = DoctorPrimary
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Extraído",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${uiState.totalMl} ml",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Botón continuar
                    Button(
                        onClick = { 
                            val idSala = uiState.reserva?.sala?.id ?: 1
                            onContinuar(idReserva, uiState.contenedores, idSala)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF66BB6A)
                        )
                    ) {
                        Icon(Icons.Default.ArrowForward, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Continuar a Ubicación", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "No hay contenedores agregados",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Agregue al menos un contenedor para continuar",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            color = Color.Gray
        )
    }
}

@Composable
fun ContenedorCard(
    contenedor: ContenedorItem,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalDrink,
                    contentDescription = null,
                    tint = DoctorPrimary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "${contenedor.cantidadMl} ml",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.Red
                )
            }
        }
    }
}
