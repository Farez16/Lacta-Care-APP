
package com.example.lactacare.vistas.admin.lactarios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.datos.dto.SalaLactanciaDto
import com.example.lactacare.vistas.theme.AdminPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLactarios(
    viewModel: LactariosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    var lactarioEditar by remember { mutableStateOf<SalaLactanciaDto?>(null) }

    if (mostrarDialogo) {
        DialogoGuardarLactario(
            lactarioEditar = lactarioEditar,
            onDismiss = {
                mostrarDialogo = false
                lactarioEditar = null
            },
            onGuardar = { sala ->
                if (lactarioEditar == null) {
                    viewModel.crearLactario(sala)
                } else {
                    viewModel.editarLactario(sala.id, sala)
                }
                mostrarDialogo = false
                lactarioEditar = null
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogo = true },
                containerColor = AdminPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Lactario")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                "Lactarios Disponibles",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AdminPrimary,
                modifier = Modifier.padding(16.dp)
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AdminPrimary)
                }
            } else if (uiState.error != null) {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error!!, color = Color.Red)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.salas) { sala ->
                        ItemSalaLactancia(
                            sala = sala,
                            onEditar = {
                                lactarioEditar = sala
                                mostrarDialogo = true
                            },
                            onEliminar = { viewModel.eliminarLactario(sala.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemSalaLactancia(
    sala: SalaLactanciaDto,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = AdminPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(sala.nombre ?: "Sin Nombre", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Dirección: ${sala.direccion ?: "N/D"}", fontSize = 14.sp)
            Text("Teléfono: ${sala.telefono ?: "N/D"}", fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Editar")
                }
                TextButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Eliminar", color = Color.Red)
                }
            }
        }
    }
}

