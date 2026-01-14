package com.example.lactacare.vistas.doctor.atencion

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.lactacare.vistas.theme.DoctorPrimary
import com.example.lactacare.vistas.theme.TextoOscuroClean

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAtencion(
    idReserva: Long,
    nombrePaciente: String,
    onVolver: () -> Unit,
    viewModel: AtencionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle Success Navigation
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onVolver() // Return to Home on success
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Atención Clínica", fontWeight = FontWeight.Bold, color = TextoOscuroClean) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextoOscuroClean)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Header
                Text(
                    text = "Paciente: $nombrePaciente",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DoctorPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Reserva #$idReserva",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                HorizontalDivider()
                
                Text(
                    text = "Detalles de Atención",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Placeholder for Vital Signs (Backend doesn't support them yet apparently)
                Text(
                     text = "Actualmente el sistema registra la fecha y hora de la atención automáticamente al finalizar. Próximamente podrá ingresar signos vitales.",
                     style = MaterialTheme.typography.bodyMedium,
                     color = Color.Gray
                )
            }

            // Action Button
            Button(
                onClick = { viewModel.finalizarAtencion(idReserva) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DoctorPrimary),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Finalizar Atención", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
