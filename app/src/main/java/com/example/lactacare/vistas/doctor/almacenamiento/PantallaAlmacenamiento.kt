package com.example.lactacare.vistas.doctor.almacenamiento

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.datos.dto.ContenedorLecheDto
import com.example.lactacare.vistas.theme.DoctorPrimary
import com.example.lactacare.vistas.theme.NeonSecondary
import com.example.lactacare.vistas.theme.TextoOscuroClean

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAlmacenamiento(
    onVolver: () -> Unit,
    viewModel: AlmacenamientoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedSlot by remember { mutableStateOf<Triple<Int,Int,Int>?>(null) } // Piso, Fila, Col
    var cantidadInput by remember { mutableStateOf("150") }

    if (showDialog && selectedSlot != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Guardar Leche") },
            text = {
                Column {
                    Text("Ubicación: Piso ${selectedSlot!!.first}, Fila ${selectedSlot!!.second}, Col ${selectedSlot!!.third}")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cantidadInput,
                        onValueChange = { cantidadInput = it },
                        label = { Text("Cantidad (ml)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.guardarContenedor(
                        cantidad = cantidadInput.toDoubleOrNull() ?: 0.0,
                        piso = selectedSlot!!.first,
                        fila = selectedSlot!!.second,
                        columna = selectedSlot!!.third
                    )
                    showDialog = false
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Almacenamiento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, "Volver") }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            
            // IoT Monitor
            Card(
                colors = CardDefaults.cardColors(containerColor = NeonSecondary.copy(alpha=0.2f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Thermostat, null, tint = DoctorPrimary, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Temperatura Actual", fontSize = 12.sp, color = Color.Gray)
                        Text("${uiState.temperatura}°C", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextoOscuroClean)
                    }
                    Spacer(Modifier.weight(1f))
                    Box(modifier = Modifier.background(Color.Green, CircleShape).size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Online", fontSize = 12.sp, color = Color.Green)
                }
            }

            // Fridge Selector
            if (uiState.refrigeradores.isNotEmpty()) {
                Text("Refrigerador Seleccionado: ${uiState.selectedFridge?.piso ?: 1} Pisos", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
            } else {
                Text("No hay refrigeradores asignados.", color = Color.Red)
            }

            // Grid Visualization
            if (uiState.selectedFridge != null) {
                val refri = uiState.selectedFridge!!
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(refri.piso) { pisoIdx ->
                        val pisoNum = pisoIdx + 1
                        Text("Piso $pisoNum", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                        
                        Column {
                            repeat(refri.fila) { filaIdx -> 
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    repeat(refri.columna) { colIdx ->
                                        // Check ocupancy
                                        val filaNum = filaIdx + 1
                                        val colNum = colIdx + 1
                                        
                                        val ocupado = uiState.contenedores.find { 
                                            it.refrigeradorId == refri.id && it.piso == pisoNum && it.fila == filaNum && it.columna == colNum 
                                        }

                                        FridgeSlot(
                                            ocupado = ocupado != null,
                                            container = ocupado,
                                            onClick = {
                                                if (ocupado == null) {
                                                    selectedSlot = Triple(pisoNum, filaNum, colNum)
                                                    showDialog = true
                                                }
                                            }
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                        Divider(Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FridgeSlot(
    ocupado: Boolean,
    container: ContenedorLecheDto?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (ocupado) DoctorPrimary else Color.LightGray.copy(alpha = 0.3f))
            .clickable(enabled = !ocupado, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (ocupado) {
            Icon(Icons.Default.LocalDrink, null, tint = Color.White)
        } else {
            Icon(Icons.Default.Add, null, tint = Color.Gray)
        }
    }
}
