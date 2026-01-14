package com.example.lactacare.vistas.admin.sugerencias

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.datos.dto.SugerenciaDto
import com.example.lactacare.vistas.theme.*

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

    PantallaPremiumAdmin(
        titulo = "Tips Informativos (CMS)",
        floatingActionButton = {
            BotonPildora(
                text = "Crear Tip",
                icon = Icons.Default.Add,
                onClick = { mostrarDialogo = true },
                modifier = Modifier.padding(bottom = 16.dp).height(56.dp)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = NeonPrimary)
            } else if (uiState.error != null) {
                Text(
                    text = "Error: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.sugerencias) { sugerencia ->
                        if (sugerencia.id != null) {
                            ItemSugerenciaPremium(sugerencia, onEliminar = { viewModel.eliminar(sugerencia.id) })
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun ItemSugerenciaPremium(
    sugerencia: SugerenciaDto,
    onEliminar: () -> Unit
) {
    TarjetaPremium {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MintPastel,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Feedback, null, tint = NeonPrimary, modifier = Modifier.size(20.dp))
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sugerencia.titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextoOscuroClean
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = sugerencia.detalle,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, null, tint = Color(0xFFEF5350))
                }
            }
            
            if (!sugerencia.imagenUrl.isNullOrEmpty()) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = BackgroundPastel,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(start = 52.dp) // Indent to align with text
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        // Placeholder icon for image
                        Icon(Icons.Default.Feedback, null, tint = NeonPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Imagen adjunta disponible",
                            fontSize = 12.sp,
                            color = DarkCharcoal,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
