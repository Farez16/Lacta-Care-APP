package com.example.lactacare.vistas.admin.sugerencias

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.datos.dto.SugerenciaDto
import com.example.lactacare.vistas.theme.AdminPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSugerencias(
    viewModel: SugerenciasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var mostrarDialogo by remember { mutableStateOf(false) }

    if (mostrarDialogo) {
        DialogoCrearSugerencia(
            onDismiss = { mostrarDialogo = false },
            onConfirm = { nuevaSugerencia ->
                viewModel.crear(nuevaSugerencia)
                mostrarDialogo = false
            }
        )
    }

    Scaffold(
        topBar = {
             CenterAlignedTopAppBar(
                 title = { Text("Tips Informativos (CMS)", fontWeight = FontWeight.Bold, color = AdminPrimary) }
             )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogo = true },
                containerColor = AdminPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Tip")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AdminPrimary)
            } else if (uiState.error != null) {
                Text(
                    text = "Error: ${uiState.error}",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.sugerencias.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Feedback, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Text("No hay sugerencias nuevas", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.sugerencias) { sugerencia ->
                        if (sugerencia.id != null) {
                            ItemSugerencia(sugerencia, onEliminar = { viewModel.eliminar(sugerencia.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemSugerencia(
    sugerencia: SugerenciaDto,
    onEliminar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(sugerencia.titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(sugerencia.detalle, fontSize = 14.sp, color = Color.DarkGray)
            
            if (!sugerencia.imagenUrl.isNullOrEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Adjunto: ${sugerencia.imagenUrl}", fontSize = 12.sp, color = Color.Blue)
                // Aquí podrías usar Coil para cargar la imagen real si es una URL válida
            }
        }
    }
}
