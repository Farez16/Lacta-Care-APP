package com.example.lactacare.vistas.paciente.reserva

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.vistas.theme.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMisReservas(
    pacienteId: Long,
    onNavigateBack: () -> Unit,
    viewModel: MisReservasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var reservaACancelar by remember { mutableStateOf<Long?>(null) }

    // Estado para SwipeRefresh
    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = uiState.isLoading
    )

    // Cargar reservas al iniciar
    LaunchedEffect(pacienteId) {
        viewModel.cargarReservas(pacienteId)
    }

    // Diálogo de confirmación
    if (reservaACancelar != null) {
        AlertDialog(
            onDismissRequest = { reservaACancelar = null },
            title = { Text("Cancelar Reserva") },
            text = { Text("¿Estás seguro de que deseas cancelar esta reserva?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelarReserva(reservaACancelar!!)
                        reservaACancelar = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text("Sí, cancelar")
                }
            },
            dismissButton = {
                TextButton(onClick = { reservaACancelar = null }) {
                    Text("No")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mis Reservas",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MomPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = DashboardBg
    ) { padding ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.cargarReservas(pacienteId) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.reservas.isEmpty() -> {
                        // Loading inicial
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MomPrimary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Cargando reservas...",
                                color = DashboardTextLight,
                                fontSize = 14.sp
                            )
                        }
                    }

                    uiState.error != null && uiState.reservas.isEmpty() -> {
                        // Error sin datos
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                uiState.error ?: "Error desconocido",
                                color = DashboardTextDark,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.cargarReservas(pacienteId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MomPrimary
                                )
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reintentar")
                            }
                        }
                    }

                    uiState.reservas.isEmpty() -> {
                        // Sin reservas
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.EventBusy,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No tienes reservas",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = DashboardTextDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Desliza hacia abajo para actualizar",
                                fontSize = 14.sp,
                                color = DashboardTextLight
                            )
                        }
                    }

                    else -> {
                        // Lista de reservas
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Header con contador
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MomAccent.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = DashboardPinkIcon,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                "Total de Reservas",
                                                fontSize = 14.sp,
                                                color = DashboardTextLight
                                            )
                                            Text(
                                                "${uiState.reservas.size}",
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DashboardTextDark
                                            )
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(
                                            Icons.Default.SwipeDown,
                                            contentDescription = null,
                                            tint = DashboardTextLight,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            // Lista de reservas ✅ CORREGIDO
                            items(uiState.reservas) { reserva ->
                                ReservaCard(
                                    reserva = reserva,
                                    onCancelar = { reservaACancelar = it }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReservaCard(
    reserva: com.example.lactacare.datos.dto.ReservaPacienteDto,
    onCancelar: (Long) -> Unit
) {
    // Colores según estado
    val estadoColor = when (reserva.estado.uppercase()) {
        "EN RESERVA" -> Color(0xFF9E9E9E)  // Plomo (gris)
        "CANCELADO", "CANCELADA" -> Color(0xFFFFC107)  // Amarillo
        "FINALIZADO", "FINALIZADA" -> Color(0xFFE91E63)  // Rosado
        else -> Color.Gray
    }

    val estadoIcon = when (reserva.estado.uppercase()) {
        "EN RESERVA" -> Icons.Default.Schedule
        "CANCELADO", "CANCELADA" -> Icons.Default.Cancel
        "FINALIZADO", "FINALIZADA" -> Icons.Default.CheckCircle
        else -> Icons.Default.Info
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header - Solo nombre de la sala e institución
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MomAccent.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.MeetingRoom,
                            contentDescription = null,
                            tint = DashboardPinkIcon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        reserva.nombreSala,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DashboardTextDark
                    )
                    reserva.nombreInstitucion?.let {
                        Text(
                            it,
                            fontSize = 12.sp,
                            color = DashboardTextLight
                        )
                    }
                    
                    // Estado debajo del nombre de la sala
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = estadoColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                estadoIcon,
                                contentDescription = null,
                                tint = estadoColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                reserva.estado,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = estadoColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            // Información de fecha y hora
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Fecha
                InfoItem(
                    icon = Icons.Default.CalendarToday,
                    label = "Fecha",
                    value = reserva.fecha
                )

                // Hora
                InfoItem(
                    icon = Icons.Default.AccessTime,
                    label = "Hora",
                    value = reserva.horaInicio
                )
            }

            // Botón Cancelar (solo para EN RESERVA)
            if (reserva.estado.uppercase() == "EN RESERVA") {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { onCancelar(reserva.id) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFF44336)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Cancel,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancelar Reserva", fontSize = 14.sp)
                }
            }
        }
    }
}

// ✅ InfoItem FUERA de ReservaCard
@Composable
fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = DashboardPinkIcon,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                label,
                fontSize = 12.sp,
                color = DashboardTextLight
            )
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = DashboardTextDark
            )
        }
    }
}