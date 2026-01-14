package com.example.lactacare.vistas.admin.lactarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MeetingRoom
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
import com.example.lactacare.datos.dto.SalaLactanciaDto
import com.example.lactacare.vistas.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLactarios(
    viewModel: LactariosViewModel = hiltViewModel(),
    esEditable: Boolean = true,
    primaryColor: Color = NeonPrimary
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    var salaAEditar by remember { mutableStateOf<SalaLactanciaDto?>(null) }
    
    // Feedback handling
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensajeExito, uiState.error) {
        uiState.mensajeExito?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajes()
        }
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajes()
        }
    }

    PantallaPremiumAdmin(
        titulo = "Salas de Lactancia",
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (esEditable) {
                BotonPildora(
                    text = "Nueva Sala",
                    icon = Icons.Default.Add,
                    onClick = {
                        salaAEditar = null
                        mostrarDialogo = true
                    },
                    modifier = Modifier.padding(bottom = 16.dp).height(56.dp),
                    containerColor = primaryColor
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = primaryColor)
            } else {
                if (uiState.salas.isEmpty()) {
                    Text(
                        text = "No hay salas de lactancia registradas",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.salas) { sala ->
                            ItemSalaLactanciaPremium(
                                sala = sala,
                                onEdit = {
                                    salaAEditar = sala
                                    mostrarDialogo = true
                                },
                                onDelete = {
                                    sala.id?.let { viewModel.eliminarLactario(it) }
                                },
                                esEditable = esEditable,
                                primaryColor = primaryColor
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
            
            if (mostrarDialogo) {
                DialogoGuardarLactario(
                    lactarioEditar = salaAEditar,
                    instituciones = uiState.instituciones,
                    onDismiss = { mostrarDialogo = false },
                    onGuardar = { sala, cubiculos ->
                        if (salaAEditar == null) {
                            viewModel.crearLactario(sala, cubiculos)
                        } else {
                            sala.id?.let { viewModel.editarLactario(it, sala) }
                        }
                        mostrarDialogo = false
                    }
                )
            }
        }
    }
}

@Composable
fun ItemSalaLactanciaPremium(
    sala: SalaLactanciaDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    esEditable: Boolean = true,
    primaryColor: Color = NeonPrimary
) {
    TarjetaPremium {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icono Institución / Sala
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeonSecondary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MeetingRoom, null, tint = primaryColor)
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sala.nombre ?: "Sin Nombre", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuroClean
                    )
                    Text(
                        text = sala.institucion?.nombreInstitucion ?: "Sin Institución", 
                        style = MaterialTheme.typography.bodySmall, 
                        color = Color.Gray
                    )
                }
                
                // Chip de Estado
                val esActivo = sala.estado == "Activo"
                Surface(
                    color = if (esActivo) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (esActivo) "Activo" else "Inactivo",
                        color = if (esActivo) Color(0xFF2E7D32) else Color(0xFFC62828),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = CleanBackground)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Detalles de Dirección y Coordenadas
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = primaryColor)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = sala.direccion ?: "Sin Dirección", 
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    color = TextoOscuroClean
                )
            }
            
            if (sala.latitud != null && sala.longitud != null) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Map, null, modifier = Modifier.size(16.dp), tint = primaryColor)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Ubicación Georeferenciada", 
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Botones de Acción
            if (esEditable) {
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    FilledTonalButton(
                        onClick = onEdit, 
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = CleanBackground, contentColor = DarkCharcoal),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Editar")
                    }
                    Spacer(Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = onDelete,
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFD32F2F)),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}
