package com.example.lactacare.vistas.paciente.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.datos.dto.ReservaPacienteDto
import com.example.lactacare.vistas.theme.*

@Composable
fun PantallaHomePaciente(
    nombreUsuario: String,
    onLogout: () -> Unit, // Para el perfil
    onNavReservas: () -> Unit,
    onNavBebe: () -> Unit,
    onNavInfo: (String) -> Unit,
    onNavChat: () -> Unit, // Agregado para el Chat
    viewModel: PatientHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Si queremos cargar al volver a la pantalla
    LaunchedEffect(Unit) {
        viewModel.cargarDatosDashboard()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavChat,
                containerColor = MomPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.SmartToy, contentDescription = "Chat IA")
            }
        }
    ) { padding ->
        DashboardPacienteContent(
            modifier = Modifier.padding(padding),
            proximaCita = uiState.proximaCita,
            nombreBebe = uiState.nombreBebe,
            onNavReservas = onNavReservas,
            onNavBebe = onNavBebe,
            onNavInfo = onNavInfo
        )
    }
}

@Composable
fun DashboardPacienteContent(
    modifier: Modifier = Modifier,
    proximaCita: ReservaPacienteDto?,
    nombreBebe: String?,
    onNavReservas: () -> Unit,
    onNavBebe: () -> Unit,
    onNavInfo: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DashboardBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        // 1. TARJETA PRINCIPAL (Estado Actual)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Lactario Principal", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DashboardTextLight)

                    Spacer(modifier = Modifier.height(12.dp))

                    if (proximaCita != null) {
                        // CASO: TIENE CITA
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, null, tint = DashboardPinkIcon, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Sala ${proximaCita.sala.nombre ?: proximaCita.sala.id}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = DashboardTextDark)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Tu reserva es el ${proximaCita.fecha} a las ${proximaCita.horaInicio}", fontSize = 16.sp, color = DashboardTextLight)

                    } else {
                        // CASO: NO TIENE CITA
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.EventAvailable, null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Sin reservas activas", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = DashboardTextDark)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Reserva tu espacio en el lactario ahora.", fontSize = 16.sp, color = DashboardTextLight)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botón principal para Reservar
                        Button(
                            onClick = onNavReservas, // Va al buscador
                            colors = ButtonDefaults.buttonColors(containerColor = MomAccent, contentColor = Color(0xFFC13B84)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text("Reservar Ahora", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // 2. TARJETA MI BEBÉ
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MomAccent.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth().clickable { onNavBebe() }
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(shape = CircleShape, color = Color.White, shadowElevation = 2.dp, modifier = Modifier.size(64.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.ChildCare, null, tint = DashboardPinkIcon, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (!nombreBebe.isNullOrEmpty()) nombreBebe else "Mi Bebé", 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = DashboardTextDark
                    )
                    Text(
                        text = if (!nombreBebe.isNullOrEmpty()) "Ver registro" else "Registra y sigue el crecimiento.", 
                        fontSize = 14.sp, 
                        color = DashboardTextLight, 
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavBebe,
                        colors = ButtonDefaults.buttonColors(containerColor = MomAccent, contentColor = Color(0xFFC13B84)),
                        shape = RoundedCornerShape(50),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(if (!nombreBebe.isNullOrEmpty()) "Editar Perfil" else "Añadir Bebé", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. INFORMATIVO
        item {
            Column {
                Text("Informativo", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DashboardTextDark, modifier = Modifier.padding(bottom = 12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val items = listOf(
                        Triple(Icons.Outlined.HealthAndSafety, "Beneficios", "Descubre ventajas"),
                        Triple(Icons.Outlined.WaterDrop, "Extracción", "Técnicas y consejos"),
                        Triple(Icons.Outlined.MenuBook, "Guía 101", "Guía para lactar")
                    )
                    items(items) { (icon, title, subtitle) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(1.dp),
                            modifier = Modifier.width(160.dp).height(170.dp).clickable { onNavInfo(title) }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(icon, null, tint = DashboardPinkIcon, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = DashboardTextDark)
                                Text(subtitle, fontSize = 13.sp, color = DashboardTextLight, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
