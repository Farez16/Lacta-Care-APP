package com.example.lactacare.vistas.admin.usuarios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.vistas.theme.AdminPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCrearEmpleado(
    onVolver: () -> Unit,
    viewModel: CrearEmpleadoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var pasoActual by remember { mutableStateOf(1) } // 1: Datos, 2: Configuración (Rol/Sala/Horario)

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onVolver()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (pasoActual == 1) "Nuevo Empleado (1/2)" else "Configuración (2/2)") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (pasoActual == 2) pasoActual = 1 else onVolver()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminPrimary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Error Global
                uiState.error?.let {
                    Text(it, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
                }

                if (pasoActual == 1) {
                    // --- PASO 1: DATOS PERSONALES ---
                    Text("Datos Personales", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = uiState.cedula,
                        onValueChange = { viewModel.onPersonalDataChange(cedula = it) },
                        label = { Text("Cédula *") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = uiState.primerNombre, onValueChange = { viewModel.onPersonalDataChange(primerNombre = it) }, label = { Text("1er Nombre *") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = uiState.segundoNombre, onValueChange = { viewModel.onPersonalDataChange(segundoNombre = it) }, label = { Text("2do Nombre") }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = uiState.primerApellido, onValueChange = { viewModel.onPersonalDataChange(primerApellido = it) }, label = { Text("1er Apellido *") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = uiState.segundoApellido, onValueChange = { viewModel.onPersonalDataChange(segundoApellido = it) }, label = { Text("2do Apellido") }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = uiState.correo, onValueChange = { viewModel.onPersonalDataChange(correo = it) }, label = { Text("Correo *") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                    OutlinedTextField(value = uiState.telefono, onValueChange = { viewModel.onPersonalDataChange(telefono = it) }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                    OutlinedTextField(value = uiState.fechaNacimiento, onValueChange = { viewModel.onPersonalDataChange(fechaNacimiento = it) }, label = { Text("Fecha Nacimiento (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = { pasoActual = 2 },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary)
                    ) { Text("Siguiente") }
                
                } else {
                    // --- PASO 2: CONFIGURACIÓN ---
                    Text("Rol y Ubicación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    // Selector de Rol
                    var expandedRol by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expandedRol, onExpandedChange = { expandedRol = !expandedRol }) {
                        val rolName = uiState.roles.find { it.id == uiState.idRolSeleccionado }?.nombre ?: "Seleccionar Rol"
                        OutlinedTextField(
                            value = rolName, onValueChange = {}, readOnly = true,
                            label = { Text("Rol de Usuario") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRol) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(expanded = expandedRol, onDismissRequest = { expandedRol = false }) {
                            uiState.roles.forEach { rol ->
                                DropdownMenuItem(
                                    text = { Text(rol.nombre) },
                                    onClick = { viewModel.onRolSalaChange(idRol = rol.id); expandedRol = false }
                                )
                            }
                        }
                    }

                    // Selector de Sala (Solo si es Medico o Admin)
                    var expandedSala by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expandedSala, onExpandedChange = { expandedSala = !expandedSala }) {
                        val salaName = uiState.salas.find { it.id?.toInt() == uiState.idSalaSeleccionada }?.nombre ?: "Sin Sala Asignada"
                        OutlinedTextField(
                            value = salaName, onValueChange = {}, readOnly = true,
                            label = { Text("Asignar a Sala de Lactancia") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSala) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(expanded = expandedSala, onDismissRequest = { expandedSala = false }) {
                            DropdownMenuItem(text = { Text("Ninguna") }, onClick = { viewModel.onRolSalaChange(idSala = null); expandedSala = false })
                            uiState.salas.forEach { sala ->
                                DropdownMenuItem(
                                    text = { Text(sala.nombre ?: "Sala ${sala.id}") },
                                    onClick = { 
                                        sala.id?.let { id -> viewModel.onRolSalaChange(idSala = id.toInt()) }
                                        expandedSala = false 
                                    }
                                )
                            }
                        }
                    }

                    Divider()
                    Text("Horario Laboral", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = uiState.horaInicio, onValueChange = { viewModel.onHorarioChange(inicio = it) }, label = { Text("Entrada (HH:mm)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = uiState.horaFin, onValueChange = { viewModel.onHorarioChange(fin = it) }, label = { Text("Salida (HH:mm)") }, modifier = Modifier.weight(1f))
                    }

                    Text("Días Laborables", style = MaterialTheme.typography.bodyMedium)
                    val days = listOf("Lun" to uiState.lunes, "Mar" to uiState.martes, "Mie" to uiState.miercoles, "Jue" to uiState.jueves, "Vie" to uiState.viernes, "Sab" to uiState.sabado, "Dom" to uiState.domingo)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        days.forEach { (dia, checked) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(dia, style = MaterialTheme.typography.bodySmall)
                                Checkbox(checked = checked, onCheckedChange = { viewModel.onDiaChange(dia, it) })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.guardarEmpleado() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) CircularProgressIndicator(color = Color.White) else Text("Guardar Empleado")
                    }
                }
            }
        }
    }
}
