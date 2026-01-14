package com.example.lactacare.vistas.doctor.reportes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    viewModel: DoctorReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes del Día", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DoctorPrimary)
                    }
                } else if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    Text(
                        text = "Resumen: ${uiState.salaNombre}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuroClean
                    )

                    // Cards Grid
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatCardDoc(
                            modifier = Modifier.weight(1f),
                            title = "Total Citas",
                            value = uiState.totalCitas.toString(),
                            icon = Icons.Outlined.Assignment,
                            color = DoctorPrimary
                        )
                        StatCardDoc(
                            modifier = Modifier.weight(1f),
                            title = "Atendidas",
                            value = uiState.citasAtendidas.toString(),
                            icon = Icons.Outlined.CheckCircle,
                            color = Color(0xFF66BB6A) // Green
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatCardDoc(
                            modifier = Modifier.weight(1f),
                            title = "Pendientes",
                            value = uiState.citasPendientes.toString(),
                            icon = Icons.Outlined.PendingActions,
                            color = Color(0xFFFFB74D) // Orange
                        )
                        StatCardDoc(
                            modifier = Modifier.weight(1f),
                            title = "Canceladas",
                            value = uiState.citasCanceladas.toString(),
                            icon = Icons.Outlined.Cancel,
                            color = Color(0xFFEF5350) // Red
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Simple Performance Text
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Rendimiento", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.height(8.dp))
                            val porcentaje = if (uiState.totalCitas > 0) {
                                (uiState.citasAtendidas.toFloat() / uiState.totalCitas.toFloat()) * 100
                            } else 0f
                            
                            LinearProgressIndicator(
                                progress = porcentaje / 100f,
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = DoctorPrimary,
                                trackColor = Color(0xFFE0E0E0)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${porcentaje.toInt()}% Completado",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }
    )
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
