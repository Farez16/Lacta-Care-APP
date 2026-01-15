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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.lactacare.datos.dto.ReservaPacienteDto
import com.example.lactacare.vistas.chat.BurbujaChatFlotante
import com.example.lactacare.vistas.theme.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.time.LocalTime

@Composable
fun PantallaHomePaciente(
    nombreUsuario: String,
    onLogout: () -> Unit,
    onNavReservas: () -> Unit,
    onNavBebe: () -> Unit,
    onNavInfo: (String) -> Unit,
    onNavChat: () -> Unit,
    onNavMisReservas: () -> Unit,
    viewModel: PatientHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarChat by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargarDatosDashboard()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavChat,
                    containerColor = MomPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Filled.SmartToy,
                        contentDescription = "Chat IA",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        ) { padding ->
            DashboardPacienteContent(
                modifier = Modifier.padding(padding),
                nombreCompleto = uiState.nombreCompleto,
                fotoPerfil = uiState.fotoPerfil,
                proximaCita = uiState.proximaCita,
                nombreBebe = uiState.nombreBebe,
                sugerencias = uiState.sugerencias,
                isLoading = uiState.isLoading,
                onRefresh = { viewModel.cargarDatosDashboard() },
                onNavReservas = onNavReservas,
                onNavBebe = onNavBebe,
                onNavInfo = onNavInfo,
                onNavMisReservas = onNavMisReservas
            )
        }

        // Burbuja flotante del chat
        if (mostrarChat) {
            BurbujaChatFlotante(
                onCerrar = { mostrarChat = false }
            )
        }
    }
}

@Composable
fun DashboardPacienteContent(
    modifier: Modifier = Modifier,
    nombreCompleto: String?,
    fotoPerfil: String?,
    proximaCita: ReservaPacienteDto?,
    nombreBebe: String?,
    sugerencias: List<com.example.lactacare.datos.dto.SugerenciaDto> = emptyList(),
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onNavReservas: () -> Unit,
    onNavBebe: () -> Unit,
    onNavInfo: (String) -> Unit,
    onNavMisReservas: () -> Unit
) {
    // Estado para el dialog
    var tipSeleccionado by remember { mutableStateOf<com.example.lactacare.datos.dto.SugerenciaDto?>(null) }

    if (tipSeleccionado != null) {
        DialogoDetalleTip(
            tip = tipSeleccionado!!,
            onDismiss = { tipSeleccionado = null }
        )
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isLoading),
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DashboardBg),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // HEADER SALUDO DINÁMICO
            item {
                HeaderSaludo(
                    nombreCompleto = nombreCompleto,
                    fotoPerfil = fotoPerfil
                )
            }

            // 1. TARJETA PRINCIPAL (Estado Actual) - TAMAÑO AUMENTADO
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "Lactario Principal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DashboardTextLight
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (proximaCita != null) {
                            // CASO: TIENE CITA
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    null,
                                    tint = DashboardPinkIcon,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Sala ${proximaCita.nombreSala}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DashboardTextDark
                                )
                            }
                            Text(
                                "Tu reserva es el ${proximaCita.fecha} a las ${proximaCita.horaInicio}",
                                fontSize = 16.sp,
                                color = DashboardTextLight
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onNavReservas,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MomAccent,
                                    contentColor = Color(0xFFC13B84)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Text(
                                    "Reservar Otra Sala",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        } else {
                            // CASO: NO TIENE CITA
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.EventAvailable,
                                    null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Sin reservas activas",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DashboardTextDark
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Reserva tu espacio en el lactario ahora.",
                                fontSize = 16.sp,
                                color = DashboardTextLight
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Botón principal para Reservar
                            Button(
                                onClick = onNavReservas,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MomAccent,
                                    contentColor = Color(0xFFC13B84)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Text(
                                    "Reservar Ahora",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // 2. TARJETA MIS RESERVAS - TAMAÑO AUMENTADO (ELIMINADO CARD "AÑADIR BEBÉ")
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clickable { onNavMisReservas() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icono
                        Surface(
                            shape = CircleShape,
                            color = MomAccent.copy(alpha = 0.3f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.CalendarMonth,
                                    contentDescription = null,
                                    tint = DashboardPinkIcon,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Texto
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Mis Reservas",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DashboardTextDark
                            )
                            Text(
                                "Ver todas mis reservas",
                                fontSize = 14.sp,
                                color = DashboardTextLight,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // Flecha
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // 3. INFORMATIVO (REAL TIPS)
            if (sugerencias.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            "Tips Informativos",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DashboardTextDark,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(sugerencias) { tip ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(1.dp),
                                    modifier = Modifier
                                        .width(200.dp)
                                        .height(180.dp)
                                        .clickable { tipSeleccionado = tip }
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Top
                                    ) {
                                        Icon(
                                            Icons.Outlined.Lightbulb,
                                            null,
                                            tint = DashboardPinkIcon,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            tip.titulo,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = DashboardTextDark,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            tip.detalle ?:"",
                                            fontSize = 12.sp,
                                            color = DashboardTextLight,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun DialogoDetalleTip(
    tip: com.example.lactacare.datos.dto.SugerenciaDto,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Lightbulb,
                        null,
                        tint = DashboardPinkIcon,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Tip Informativo",
                        fontSize = 14.sp,
                        color = DashboardTextLight,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = tip.titulo,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DashboardTextDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Contenido Scrollable
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = tip.detalle?: "Sin detalles disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray,
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MomAccent,
                        contentColor = Color(0xFFC13B84)
                    )
                ) {
                    Text("Entendido")
                }
            }
        }
    }
}

@Composable
fun HeaderSaludo(
    nombreCompleto: String?,
    fotoPerfil: String?
) {
    val saludo = when (LocalTime.now().hour) {
        in 0..11 -> "Buenos días"
        in 12..18 -> "Buenas tardes"
        else -> "Buenas noches"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Foto de perfil
        AsyncImage(
            model = fotoPerfil,
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Saludo y nombre
        Column {
            Text(
                text = saludo,
                fontSize = 14.sp,
                color = DashboardTextLight,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = nombreCompleto ?: "Paciente",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DashboardTextDark
            )
        }
    }
}