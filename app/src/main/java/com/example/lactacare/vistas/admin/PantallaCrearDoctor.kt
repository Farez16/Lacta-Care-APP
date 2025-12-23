package com.example.lactacare.vistas.admin.creardoctor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday // Icono Calendario
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.vistas.theme.AdminPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCrearDoctor(
    onVolver: () -> Unit,
    viewModel: CrearDoctorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Efecto para navegar al volver cuando sea exitoso
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onVolver() // Volver a la lista al terminar
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Doctor") },
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
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // --- DIÁLOGO DE ERROR ---
                if (uiState.error != null) {
                    AlertDialog(
                        onDismissRequest = { viewModel.resetState() }, // Reset state on dismiss (or just clear error)
                        title = { Text("Error", color = MaterialTheme.colorScheme.error) },
                        text = { Text(uiState.error!!) },
                        confirmButton = {
                            TextButton(onClick = { 
                                // Opción: Crear función limpiarError en ViewModel, por ahora reseteamos todo o solo cerramos
                                // Para simplicidad, al dar OK, podríamos limpiar solo el mensaje de error.
                                viewModel.onFieldChange() // Hack: esto no limpia error.
                                // Idealmente agregar: fun limpiarError() { _uiState.value = _uiState.value.copy(error = null) }
                                // Por ahora usaré:
                                viewModel.limpiarError()
                            }) {
                                Text("Entendido")
                            }
                        },
                        containerColor = Color.White
                    )
                }

                // Campos del Formulario
                OutlinedTextField(
                    value = uiState.cedula,
                    onValueChange = { viewModel.onFieldChange(cedula = it) },
                    label = { Text("Cédula *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.primerNombre,
                        onValueChange = { viewModel.onFieldChange(primerNombre = it) },
                        label = { Text("Primer Nombre *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = uiState.segundoNombre,
                        onValueChange = { viewModel.onFieldChange(segundoNombre = it) },
                        label = { Text("Segundo Nombre") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.primerApellido,
                        onValueChange = { viewModel.onFieldChange(primerApellido = it) },
                        label = { Text("Primer Apellido *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = uiState.segundoApellido,
                        onValueChange = { viewModel.onFieldChange(segundoApellido = it) },
                        label = { Text("Segundo Apellido") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }

                OutlinedTextField(
                    value = uiState.correo,
                    onValueChange = { viewModel.onFieldChange(correo = it) },
                    label = { Text("Correo Electrónico *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = uiState.telefono,
                    onValueChange = { viewModel.onFieldChange(telefono = it) },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
                )

                // --- CAMPO FECHA CON DATEPICKER ---
                val datePickerState = rememberDatePickerState()
                var showDatePicker by remember { mutableStateOf(false) }

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val fecha = java.time.Instant.ofEpochMilli(millis)
                                        .atZone(java.time.ZoneId.of("UTC")) // Usar UTC para evitar desfases
                                        .toLocalDate()
                                        .toString() // Formato YYYY-MM-DD por defecto
                                    viewModel.onFieldChange(fechaNacimiento = fecha)
                                    showDatePicker = false
                                }
                            }) { Text("OK", color = AdminPrimary) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = Color.Gray) }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                // Campo de texto de solo lectura que abre el calendario
                Box(modifier = Modifier.clickable { showDatePicker = true }) {
                    OutlinedTextField(
                        value = uiState.fechaNacimiento,
                        onValueChange = { },
                        label = { Text("Fecha Nacimiento (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Seleccione una fecha") },
                        readOnly = true, // No permitir escritura manual
                        enabled = false, // Deshabilitar para que el click lo capture la Box
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.Gray,
                            disabledLabelColor = Color.Black,
                            disabledContainerColor = Color.White
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha", tint = AdminPrimary)
                            }
                        }
                    )
                }


                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.guardarDoctor() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Guardar Doctor")
                    }
                }
            }
        }
    }
}
