package com.example.lactacare.vistas.doctor.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.datos.dto.DoctorReservaDto
import com.example.lactacare.vistas.theme.DoctorPrimary
import com.example.lactacare.vistas.theme.TextoOscuroClean
import com.example.lactacare.vistas.home.DashboardStatCard // Reutilizamos si es posible, o definimos localmente si no es pública

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHomeDoctor(
    onLogout: () -> Unit,
    onAtender: (Long, String) -> Unit, // (idReserva, nombrePaciente)
    viewModel: DoctorHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorPrimary = DoctorPrimary
    val colorAccent = Color(0xFFE1F5FE) // Un azulito claro para fondo de iconos

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.cargarAgendaHoy() },
                containerColor = colorPrimary
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Recargar", tint = Color.White)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF9F9F9))) {
            
            if (uiState.isLoading) {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorPrimary)
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Groups, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error!!,
                            color = Color.Red,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = { viewModel.verificarSalaYCargarAgenda() }) {
                            Text("Reintentar")
                        }
                    }
                }
            } else {
                 DashboardDoctorContent(
                    uiState = uiState,
                    colorPrimary = colorPrimary,
                    colorAccent = colorAccent,
                    onAtender = onAtender,
                    onConfirmar = { reserva -> viewModel.confirmarAsistencia(reserva) }
                )
            }
        }
    }
}

@Composable
fun DashboardDoctorContent(
    uiState: DoctorHomeUiState,
    colorPrimary: Color,
    colorAccent: Color,
    onAtender: (Long, String) -> Unit,
    onConfirmar: (DoctorReservaDto) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ... (resto igual hasta items) ...
        
        // Resumen Rápido - SIN CAMBIOS (omitido por brevedad en diff, conservar código original)

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val totalCitas = uiState.agenda.size.toString()
                val pendientes = uiState.agenda.count { it.estado == "PENDIENTE" }.toString()
                DashboardStatCardLocal(modifier = Modifier.weight(1f), "Citas Hoy", totalCitas, Icons.Outlined.Groups, colorPrimary)
                DashboardStatCardLocal(modifier = Modifier.weight(1f), "Pendientes", pendientes, Icons.Outlined.PendingActions, Color(0xFFFFB74D))
            }
        }
        
        item {
             Text("Agenda de Hoy (${uiState.fechaHoy})", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextoOscuroClean)
             Spacer(modifier = Modifier.height(12.dp))
             if (uiState.agenda.isEmpty()) {
                 Text("No hay citas programadas para hoy.", color = Color.Gray)
             }
        }

        items(uiState.agenda) { reserva ->
            ItemReservaDoctor(reserva, colorPrimary, colorAccent, onAtender, onConfirmar)
        }
    }
}

@Composable
fun ItemReservaDoctor(
    reserva: DoctorReservaDto,  
    colorPrimary: Color, 
    colorAccent: Color,
    onAtender: (Long, String) -> Unit,
    onConfirmar: (DoctorReservaDto) -> Unit
) {
    val nombrePaciente = reserva.paciente?.nombreCompleto() ?: "Paciente Desconocido"
    val inicial = nombrePaciente.firstOrNull()?.toString() ?: "?"
    
    // Habilitar clic solo si es PENDIENTE (opcional)
    val esPendiente = reserva.estado == "PENDIENTE"
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = {
            if (esPendiente) {
                onAtender(reserva.id, nombrePaciente)
            }
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(50.dp).background(colorAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(inicial, fontWeight = FontWeight.Bold, color = colorPrimary, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(nombrePaciente, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextoOscuroClean)
                Text("Estado: ${reserva.estado}", fontSize = 12.sp, color = Color.Gray)
            }
            Box(
                modifier = Modifier.background(colorPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(reserva.horaInicio, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorPrimary)
            }
            // Check de Asistencia
             if (esPendiente) {
                 IconButton(onClick = { onConfirmar(reserva) }) {
                     Icon(
                         Icons.Default.CheckCircle, 
                         contentDescription = "Confirmar Asistencia",
                         tint = Color.Gray
                     )
                 }
             } else if (reserva.estado == "CONFIRMADA" || reserva.estado == "ASISTIO") {
                 Icon(
                     Icons.Default.CheckCircle,
                     contentDescription = "Asistió",
                     tint = Color.Green,
                     modifier = Modifier.padding(start = 8.dp)
                 )
             }
        }
    }
}

// Copia local del componente de tarjeta para evitar problemas de visibilidad si el original es interno
@Composable
fun DashboardStatCardLocal(
    modifier: Modifier = Modifier,
    titulo: String,
    valor: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    colorIcono: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(valor, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextoOscuroClean)
            Text(titulo, fontSize = 12.sp, color = Color.Gray)
        }
    }
}
