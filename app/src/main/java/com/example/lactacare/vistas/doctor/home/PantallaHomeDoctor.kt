package com.example.lactacare.vistas.doctor.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.vistas.theme.DoctorPrimary
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHomeDoctor(
    onLogout: () -> Unit,
    onNavigateToReservas: () -> Unit = {},
    onNavigateToSolicitudes: () -> Unit = {},
    onNavigateToReportes: () -> Unit = {},
    viewModel: DoctorHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            // Opcional: Puedes agregar un TopAppBar aquí si lo necesitas
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header dinámico
            HeaderDoctor(
                nombreDoctor = uiState.nombreDoctor,
                imagenDoctor = uiState.imagenDoctor,
                onRefresh = { viewModel.cargarEstadisticas(isRefresh = true) },
                isRefreshing = uiState.isRefreshing
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Cards de estadísticas
            if (uiState.estadisticas != null) {
                EstadisticasSection(
                    estadisticas = uiState.estadisticas!!,
                    onClickAtenciones = onNavigateToReservas,
                    onClickSolicitudes = onNavigateToSolicitudes,
                    onClickReportes = onNavigateToReportes
                )
            }

            // Error message
            if (uiState.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Text(
                        text = uiState.error!!,
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFC62828)
                    )
                }
            }

            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DoctorPrimary)
                }
            }
        }
    }
}

@Composable
fun HeaderDoctor(
    nombreDoctor: String,
    imagenDoctor: String?,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    val saludo = obtenerSaludo()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DoctorPrimary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Imagen del doctor o ícono por defecto
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Doctor",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = saludo,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = nombreDoctor.ifEmpty { "Doctor" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Botón de refresh
            IconButton(
                onClick = onRefresh,
                enabled = !isRefreshing
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun EstadisticasSection(
    estadisticas: com.example.lactacare.datos.dto.DoctorEstadisticasDto,
    onClickAtenciones: () -> Unit,
    onClickSolicitudes: () -> Unit,
    onClickReportes: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Resumen del Día",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Primera fila: Citas Hoy y Pendientes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CardEstadistica(
                titulo = "Citas Hoy",
                valor = estadisticas.citasHoy.toString(),
                icono = Icons.Default.CalendarToday,
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )

            CardEstadistica(
                titulo = "Pendientes",
                valor = estadisticas.pendientes.toString(),
                icono = Icons.Default.PendingActions,
                color = Color(0xFFFFA726),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Segunda fila: Atenciones y Solicitudes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CardEstadistica(
                titulo = "Atenciones",
                valor = if (estadisticas.proximaReserva != null) "Ver" else "0",
                icono = Icons.Default.MedicalServices,
                color = Color(0xFF66BB6A),
                modifier = Modifier.weight(1f),
                onClick = onClickAtenciones
            )

            CardEstadistica(
                titulo = "Solicitudes",
                valor = estadisticas.solicitudesRetiro.toString(),
                icono = Icons.Default.Inventory,
                color = Color(0xFFEC407A),
                modifier = Modifier.weight(1f),
                onClick = onClickSolicitudes
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tercera fila: Card de Reportes (centrado)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            CardEstadistica(
                titulo = "Reportes",
                valor = "Ver",
                icono = Icons.Default.Assessment,
                color = Color(0xFF1976D2),
                modifier = Modifier.width(170.dp),
                onClick = onClickReportes
            )
        }
    }
}

@Composable
fun CardEstadistica(
    titulo: String,
    valor: String,
    icono: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = titulo,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = titulo,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = valor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

fun obtenerSaludo(): String {
    val hora = LocalTime.now().hour
    return when {
        hora < 12 -> "Buenos días"
        hora < 18 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}
