package com.example.lactacare.vistas.doctor.reportes

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.vistas.theme.DoctorPrimary
import com.example.lactacare.vistas.theme.TextoOscuroClean

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaReportesDoctor(
    onVolver: () -> Unit,
    idDoctor: Int,
    viewModel: DoctorReportesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val snackbarHostState = remember { SnackbarHostState() }

    // Cargar datos al iniciar
    LaunchedEffect(idDoctor) {
        viewModel.cargarEstadisticas(idDoctor)
    }

    // Escuchar eventos de PDF
    LaunchedEffect(Unit) {
        viewModel.pdfEvent.collect { event ->
            when (event) {
                is PdfEvent.Success -> {
                    // Mostrar Snackbar con opción de abrir
                    val result = snackbarHostState.showSnackbar(
                        message = "✓ PDF guardado en ${event.ruta}",
                        actionLabel = "Abrir",
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed && activity != null) {
                        viewModel.abrirPdf(event.uri, activity)
                    }
                }
                is PdfEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        message = "✗ ${event.message}",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes y Estadísticas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Botón de exportar PDF
                    IconButton(
                        onClick = { viewModel.exportarPdf("Dr. Usuario") }, // TODO: Obtener nombre real
                        enabled = !uiState.isExportingPdf && uiState.estadisticas != null
                    ) {
                        if (uiState.isExportingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = "Exportar PDF",
                                tint = if (uiState.estadisticas != null) Color.White else Color.Gray
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DoctorPrimary)
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (data.visuals.message.startsWith("✓")) 
                        Color(0xFF4CAF50) else Color(0xFFEF5350),
                    contentColor = Color.White,
                    actionColor = Color.White
                )
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Filtros de Fecha
                FiltrosFecha(
                    filtroActual = uiState.filtroFecha,
                    onFiltroChange = { filtro ->
                        viewModel.setFiltroFecha(filtro, idDoctor)
                    }
                )

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DoctorPrimary)
                    }
                } else if (uiState.error != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = Color(0xFFC62828),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else if (uiState.estadisticas != null) {
                    val stats = uiState.estadisticas!!

                    // Grid de Métricas (2x3)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StatCardDoc(
                                modifier = Modifier.weight(1f),
                                title = "Atenciones",
                                value = stats.totalAtenciones.toString(),
                                icon = Icons.Outlined.Assignment,
                                color = DoctorPrimary
                            )
                            StatCardDoc(
                                modifier = Modifier.weight(1f),
                                title = "Leche (L)",
                                value = String.format("%.2f", stats.totalLecheLitros),
                                icon = Icons.Outlined.LocalDrink,
                                color = Color(0xFF66BB6A)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StatCardDoc(
                                modifier = Modifier.weight(1f),
                                title = "Pacientes",
                                value = stats.totalPacientes.toString(),
                                icon = Icons.Outlined.People,
                                color = Color(0xFF42A5F5)
                            )
                            StatCardDoc(
                                modifier = Modifier.weight(1f),
                                title = "Contenedores",
                                value = stats.totalContenedores.toString(),
                                icon = Icons.Outlined.Inventory,
                                color = Color(0xFFFFB74D)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StatCardDoc(
                                modifier = Modifier.weight(1f),
                                title = "Pendientes",
                                value = stats.solicitudesPendientes.toString(),
                                icon = Icons.Outlined.PendingActions,
                                color = Color(0xFFFF7043)
                            )
                            StatCardDoc(
                                modifier = Modifier.weight(1f),
                                title = "Cumplimiento",
                                value = "${stats.tasaCumplimiento.toInt()}%",
                                icon = Icons.Outlined.CheckCircle,
                                color = Color(0xFF26A69A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Producción Semanal
                    if (stats.produccionSemanal.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Producción Diaria",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextoOscuroClean
                                )
                                Spacer(Modifier.height(12.dp))

                                // Lista simple de producción
                                stats.produccionSemanal.forEach { dia ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = dia.fecha,
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "${String.format("%.2f", dia.cantidadLitros)} L (${dia.numeroContenedores})",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = DoctorPrimary
                                        )
                                    }
                                    if (dia != stats.produccionSemanal.last()) {
                                        Divider(color = Color(0xFFEEEEEE))
                                    }
                                }
                            }
                        }
                    }

                    // Estados de Contenedores
                    if (stats.contenedoresPorEstado.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Estados de Contenedores",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextoOscuroClean
                                )
                                Spacer(Modifier.height(12.dp))

                                stats.contenedoresPorEstado.forEach { (estado, cantidad) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .background(
                                                        color = when (estado) {
                                                            "REFRIGERADA" -> Color(0xFF42A5F5)
                                                            "ALMACENADO" -> Color(0xFF66BB6A)
                                                            "RETIRADA" -> Color.Gray
                                                            "CADUCADA" -> Color(0xFFEF5350)
                                                            "POR RETIRAR" -> Color(0xFFFFB74D)
                                                            else -> Color.LightGray
                                                        },
                                                        shape = RoundedCornerShape(2.dp)
                                                    )
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = estado,
                                                fontSize = 14.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        Text(
                                            text = cantidad.toString(),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextoOscuroClean
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun FiltrosFecha(
    filtroActual: FiltroFecha,
    onFiltroChange: (FiltroFecha) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        FiltroFecha.values().forEach { filtro ->
            FilterChip(
                selected = filtroActual == filtro,
                onClick = { onFiltroChange(filtro) },
                label = {
                    Text(
                        text = when (filtro) {
                            FiltroFecha.Hoy -> "Hoy"
                            FiltroFecha.Semana -> "Semana"
                            FiltroFecha.Mes -> "Mes"
                        }
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DoctorPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun StatCardDoc(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextoOscuroClean)
            Text(title, fontSize = 12.sp, color = Color.Gray)
        }
    }
}
