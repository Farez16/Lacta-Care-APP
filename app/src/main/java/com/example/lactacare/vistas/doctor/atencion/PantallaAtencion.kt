package com.example.lactacare.vistas.doctor.atencion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAtencion(
    reservaId: Long,
    nombrePaciente: String,
    onVolver: () -> Unit,
    viewModel: AtencionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Si tiene éxito, volver automáticamente
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Atención") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DoctorPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle, 
                        contentDescription = null, 
                        tint = DoctorPrimary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Atender a Paciente",
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = nombrePaciente, fontSize = 18.sp, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = DoctorPrimary)
                    } else {
                        Button(
                            onClick = { viewModel.registrarAtencion(reservaId) },
                            colors = ButtonDefaults.buttonColors(containerColor = DoctorPrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Confirmar Atención Realizada")
                        }
                    }
                    
                    if (uiState.error != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = uiState.error ?: "", color = Color.Red)
                    }
                }
            }
        }
    }
}
