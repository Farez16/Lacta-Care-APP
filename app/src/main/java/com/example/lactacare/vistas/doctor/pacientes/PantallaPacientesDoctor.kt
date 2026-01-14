package com.example.lactacare.vistas.doctor.pacientes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.datos.dto.UsuarioResponseDto
import com.example.lactacare.vistas.theme.* // Asumiendo componentes de tema existen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPacientesDoctor(
    onVolver: () -> Unit,
    viewModel: DoctorPatientsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // --- SEARCH STATE ---
    var searchQuery by remember { mutableStateOf("") }
    
    // Validar si componentes de tema están disponibles, si no, usar defaults
    val bgColor = Color(0xFFF5F5F5)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Directorio de Pacientes", fontWeight = FontWeight.Bold) },
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
                    .background(bgColor)
            ) {
                // --- SEARCH BAR ---
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Buscar por nombre o cédula...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // --- CONTENT ---
                Box(modifier = Modifier.fillMaxSize()) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (uiState.error != null) {
                        Text(
                            text = uiState.error!!,
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        val filteredList = uiState.pacientes.filter {
                            it.nombreCompleto.contains(searchQuery, ignoreCase = true) ||
                            (it.cedula?.contains(searchQuery, ignoreCase = true) ?: false)
                        }

                        if (filteredList.isEmpty()) {
                            Text(
                                "No se encontraron pacientes.",
                                modifier = Modifier.align(Alignment.Center),
                                color = Color.Gray
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredList) { paciente ->
                                    ItemPacienteDoctor(paciente)
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
fun ItemPacienteDoctor(usuario: UsuarioResponseDto) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                color = Color(0xFFE0F7FA), // Soft Cyan
                shape = CircleShape,
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = usuario.nombreCompleto.take(1),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0097A7), // Cyan Dark
                        fontSize = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = usuario.nombreCompleto,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                if (!usuario.cedula.isNullOrEmpty()) {
                    Text(
                        text = "C.I.: ${usuario.cedula}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = usuario.correo,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (!usuario.telefono.isNullOrEmpty()) {
                    Text(
                        text = "Tel: ${usuario.telefono}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
