package com.example.lactacare.vistas.admin.refrigeradores

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.datos.dto.RefrigeradorDto
import com.example.lactacare.vistas.theme.AdminPrimary

@Composable
fun PantallaRefrigeradores(
    viewModel: RefrigeradoresViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    var refriEditar by remember { mutableStateOf<RefrigeradorDto?>(null) }

    if (mostrarDialogo) {
        DialogoRefrigerador(
            refri = refriEditar,
            onDismiss = { mostrarDialogo = false; refriEditar = null },
            onGuardar = { nuevoRefri ->
                if (refriEditar == null) viewModel.crear(nuevoRefri)
                else viewModel.editar(nuevoRefri)
                mostrarDialogo = false
                refriEditar = null
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
                Icon(Icons.Default.Add, contentDescription = "Nuevo")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                "Gestión de Refrigeradores",
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
                    Text(text = "Error: ${uiState.error}", color = Color.Red)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.refrigeradores) { refri ->
                        ItemRefrigerador(
                            refri = refri,
                            onEditar = { refriEditar = refri; mostrarDialogo = true },
                            onEliminar = { viewModel.eliminar(refri.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemRefrigerador(
    refri: RefrigeradorDto,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Kitchen, null, tint = AdminPrimary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Refrigerador #${refri.id}", fontWeight = FontWeight.Bold)
                Text("Capacidad: ${refri.filas} Filas x ${refri.columnas} Cols", fontSize = 14.sp)
                val salaNombre = refri.sala?.nombre ?: "Sin Sala Asignada"
                Text("Ubicación: $salaNombre", fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, null) }
            IconButton(onClick = onEliminar) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoRefrigerador(
    refri: RefrigeradorDto?,
    onDismiss: () -> Unit,
    onGuardar: (RefrigeradorDto) -> Unit
) {
    var filas by remember { mutableStateOf(refri?.filas?.toString() ?: "") }
    var columnas by remember { mutableStateOf(refri?.columnas?.toString() ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (refri == null) "Nuevo Refrigerador" else "Editar Refrigerador", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = filas, 
                    onValueChange = { filas = it }, 
                    label = { Text("Filas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = columnas, 
                    onValueChange = { columnas = it }, 
                    label = { Text("Columnas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                Spacer(Modifier.height(24.dp))
                Row {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(onClick = {
                        val f = filas.toIntOrNull() ?: 0
                        val c = columnas.toIntOrNull() ?: 0
                        if (f > 0 && c > 0) {
                            // Mantenemos la sala original si es edición, o null si es nuevo (por simplicidad en este paso)
                            onGuardar(RefrigeradorDto(
                                id = refri?.id ?: 0,
                                filas = f,
                                columnas = c,
                                sala = refri?.sala
                            ))
                        }
                    }) { Text("Guardar") }
                }
            }
        }
    }
}
