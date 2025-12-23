package com.example.lactacare.vistas.admin.usuarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.datos.dto.PacienteDto
import com.example.lactacare.datos.dto.UsuarioResponseDto
import com.example.lactacare.vistas.theme.AdminPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionUsuarios(
    onVolver: () -> Unit,
    onCrearDoctor: () -> Unit, // Navegación para crear nuevo
    viewModel: GestionUsuariosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AdminPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            // Solo mostramos botón de crear si estamos en la pestaña de Doctores
            if (uiState.tabSeleccionado == 0) {
                FloatingActionButton(
                    onClick = onCrearDoctor,
                    containerColor = AdminPrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo Doctor")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // --- TABS (Pestañas) ---
            TabRow(
                selectedTabIndex = uiState.tabSeleccionado,
                containerColor = Color.White,
                contentColor = AdminPrimary
            ) {
                Tab(
                    selected = uiState.tabSeleccionado == 0,
                    onClick = { viewModel.cambiarTab(0) },
                    text = { Text("Doctores (${uiState.listaDoctores.size})") },
                    icon = { Icon(Icons.Outlined.MedicalServices, null) }
                )
                Tab(
                    selected = uiState.tabSeleccionado == 1,
                    onClick = { viewModel.cambiarTab(1) },
                    text = { Text("Pacientes (${uiState.listaPacientes.size})") },
                    icon = { Icon(Icons.Outlined.Person, null) }
                )
            }

            // --- CONTENIDO DE LA LISTA ---
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AdminPrimary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.tabSeleccionado == 0) {
                        // LISTA DE DOCTORES
                        items(uiState.listaDoctores) { doctor ->
                            ItemDoctor(doctor)
                        }
                    } else {
                        // LISTA DE PACIENTES
                        items(uiState.listaPacientes) { paciente ->
                            ItemPaciente(paciente)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemDoctor(doctor: UsuarioResponseDto) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        ListItem(
            headlineContent = { Text(doctor.nombreCompleto, fontWeight = FontWeight.Bold) },
            supportingContent = { Text(doctor.correo) },
            leadingContent = {
                Surface(color = AdminPrimary.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(doctor.nombreCompleto.take(1), fontWeight = FontWeight.Bold, color = AdminPrimary)
                    }
                }
            },
            trailingContent = {
                Text("Cédula:\n${doctor.cedula}", fontSize = 10.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
        )
    }
}

@Composable
fun ItemPaciente(paciente: PacienteDto) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        ListItem(
            headlineContent = { Text(paciente.nombreCompleto, fontWeight = FontWeight.Bold) },
            supportingContent = { Text(paciente.correo) },
            leadingContent = {
                Surface(color = Color(0xFFE91E63).copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(paciente.nombreCompleto.take(1), fontWeight = FontWeight.Bold, color = Color(0xFFE91E63))
                    }
                }
            },
            trailingContent = {
                Text("Tel:\n${paciente.telefono ?: "-"}", fontSize = 10.sp, color = Color.Gray)
            }
        )
    }
}