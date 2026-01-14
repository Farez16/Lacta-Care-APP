package com.example.lactacare.vistas.admin.refrigeradores

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.lactacare.datos.dto.SalaLactanciaDto
import com.example.lactacare.vistas.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRefrigeradores(
    viewModel: RefrigeradorViewModel = hiltViewModel(),
    esEditable: Boolean = true,
    primaryColor: Color = NeonPrimary
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var refriAEditar by remember { mutableStateOf<RefrigeradorDto?>(null) }
    
    // Feedback handling
    LaunchedEffect(uiState.mensaje, uiState.error) {
       // Optional: Snackbar logic here
    }

    PantallaPremiumAdmin(
        titulo = "Control de Frío",
        floatingActionButton = {
            if (esEditable) {
                BotonPildora(
                    text = "Nuevo Equipo",
                    icon = Icons.Default.Add,
                    onClick = { refriAEditar = null; showDialog = true },
                    modifier = Modifier.padding(bottom = 16.dp).height(56.dp),
                    containerColor = primaryColor
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = primaryColor)
            } else if (uiState.refrigeradores.isEmpty()) {
                Text(
                    "No hay refrigeradores registrados.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.refrigeradores) { refri ->
                        RefriVisualCardPremium(
                            refri = refri,
                            onEdit = { refriAEditar = refri; showDialog = true },
                            onDelete = { viewModel.eliminarRefrigerador(refri.id) },
                            esEditable = esEditable,
                            primaryColor = primaryColor
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
            
            if (showDialog) {
                DialogoRefrigerador(
                    refrigerador = refriAEditar,
                    salas = uiState.salas,
                    onDismiss = { showDialog = false },
                    onConfirm = { refri ->
                         if (refriAEditar == null) viewModel.crearRefrigerador(refri)
                         else viewModel.actualizarRefrigerador(refriAEditar!!.id, refri)
                         showDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun RefriVisualCardPremium(
    refri: RefrigeradorDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    esEditable: Boolean = true,
    primaryColor: Color = NeonPrimary
) {
    TarjetaPremium {
        Column {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MintPastel,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Kitchen, null, tint = primaryColor)
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Refrigerador #${refri.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuroClean
                    )
                    Text(
                        text = refri.sala?.nombre ?: "Sin Asignación",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                
                if (esEditable) {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Editar", tint = primaryColor) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Eliminar", tint = Color(0xFFEF5350)) }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BackgroundPastel)
            Spacer(modifier = Modifier.height(16.dp))
            
                // Info Chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Cap: ${refri.capacidad}L") },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = CleanBackground, labelColor = DarkCharcoal),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonPrimary)
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Ubic: P${refri.piso}-F${refri.fila}-C${refri.columna}") },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = CleanBackground, labelColor = DarkCharcoal),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grid Visualization (Simulated - Premium Look)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonSecondary, RoundedCornerShape(16.dp)) // Neon Secondary Background
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Esquema de Almacenamiento", fontSize = 10.sp, color = DarkCharcoal, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    
                    val filas = refri.fila
                    val cols = refri.columna
                    
                    repeat(filas) { r ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            repeat(cols) { c ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .border(1.dp, NeonPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Optional: Show if full or empty
                                }
                            }
                        }
                        if (r < filas - 1) Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DialogoRefrigerador(
        refrigerador: RefrigeradorDto?,
        salas: List<SalaLactanciaDto>,
        onDismiss: () -> Unit,
        onConfirm: (RefrigeradorDto) -> Unit
    ) {
        var capacidad by remember { mutableStateOf(refrigerador?.capacidad?.toString() ?: "") }
        var piso by remember { mutableStateOf(refrigerador?.piso?.toString() ?: "") }
        var fila by remember { mutableStateOf(refrigerador?.fila?.toString() ?: "") }
        var columna by remember { mutableStateOf(refrigerador?.columna?.toString() ?: "") }
        var salaSeleccionada by remember { mutableStateOf(refrigerador?.sala) } 
        
        var expanded by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = if (refrigerador == null) "Nuevo Refrigerador" else "Editar Refrigerador",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkCharcoal
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = capacidad, onValueChange = { capacidad = it },
                        label = { Text("Capacidad Max (Lt)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPrimary,
                            focusedLabelColor = DarkCharcoal
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = piso, onValueChange = { piso = it },
                            label = { Text("Piso") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonPrimary,
                                focusedLabelColor = DarkCharcoal
                            )
                        )
                        OutlinedTextField(
                            value = fila, onValueChange = { fila = it },
                            label = { Text("Fila") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonPrimary,
                                focusedLabelColor = DarkCharcoal
                            )
                        )
                        OutlinedTextField(
                            value = columna, onValueChange = { columna = it },
                            label = { Text("Col") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonPrimary,
                                focusedLabelColor = DarkCharcoal
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Dropdown Sala
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = salaSeleccionada?.nombre ?: "Seleccionar Sala",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonPrimary,
                                focusedLabelColor = DarkCharcoal
                            ),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            salas.forEach { sala ->
                                DropdownMenuItem(
                                    text = { Text(sala.nombre ?: "Sin nombre") },
                                    onClick = {
                                        salaSeleccionada = sala
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (capacidad.isNotBlank() && piso.isNotBlank()) {
                                    onConfirm(
                                        RefrigeradorDto(
                                            capacidad = capacidad.toInt(),
                                            piso = piso.toInt(),
                                            fila = fila.toIntOrNull() ?: 1,
                                            columna = columna.toIntOrNull() ?: 1,
                                            sala = salaSeleccionada
                                        )
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPrimary, contentColor = DarkCharcoal),
                            shape = RoundedCornerShape(50)
                        ) { Text("Guardar", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
