package com.example.lactacare.vistas.admin.lactarios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.lactacare.datos.dto.DiasLaborablesSalaDto
import com.example.lactacare.datos.dto.HorariosSalaDto
import com.example.lactacare.datos.dto.SalaLactanciaDto
import com.example.lactacare.dominio.model.Institucion
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.lactacare.vistas.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoGuardarLactario(
    lactarioEditar: SalaLactanciaDto? = null,
    instituciones: List<Institucion>,
    onDismiss: () -> Unit,
    onGuardar: (SalaLactanciaDto, Int) -> Unit
) {
    // Campos Básicos
    var nombre by remember(lactarioEditar) { mutableStateOf(lactarioEditar?.nombre ?: "") }
    var direccion by remember(lactarioEditar) { mutableStateOf(lactarioEditar?.direccion ?: "") }
    var telefono by remember(lactarioEditar) { mutableStateOf(lactarioEditar?.telefono ?: "") }
    var correo by remember(lactarioEditar) { mutableStateOf(lactarioEditar?.correo ?: "") }
    var numCubiculos by remember(lactarioEditar) { mutableStateOf(lactarioEditar?.numeroCubiculos?.toString() ?: "1") }
    
    // Institución
    var institucionExpanded by remember { mutableStateOf(false) }
    var institucionSeleccionada by remember(lactarioEditar) { mutableStateOf(lactarioEditar?.institucion) }
// ...


    // Ubicación (Default: Centro de la ciudad o 0,0)
    val defaultLocation = LatLng(-0.180653, -78.467834) // Quito Example
    var markerPosition by remember { 
        mutableStateOf(
            if (lactarioEditar?.latitud != null && lactarioEditar.longitud != null)
                LatLng(lactarioEditar.latitud.toDouble(), lactarioEditar.longitud.toDouble())
            else defaultLocation
        )
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerPosition, 15f)
    }

    // Horarios
    var horaInicio by remember { mutableStateOf(lactarioEditar?.horario?.horaInicio ?: "08:00") }
    var horaFin by remember { mutableStateOf(lactarioEditar?.horario?.horaFin ?: "17:00") }
    
    // Días
    var dias by remember { mutableStateOf(lactarioEditar?.dias ?: DiasLaborablesSalaDto(lunes = true, martes = true, miercoles = true, jueves = true, viernes = true)) }
    
    // Estado
    var esActivo by remember { mutableStateOf(lactarioEditar?.estado == "Activo" || lactarioEditar?.estado == null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Full width allowed
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (lactarioEditar == null) "Nuevo Lactario" else "Editar Lactario",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Divider(Modifier.padding(vertical = 8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 1. Institución (Dropdown)
                    ExposedDropdownMenuBox(
                        expanded = institucionExpanded,
                        onExpandedChange = { institucionExpanded = !institucionExpanded }
                    ) {
                        OutlinedTextField(
                            value = institucionSeleccionada?.nombreInstitucion ?: "Seleccione Institución",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = institucionExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            label = { Text("Institución") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OliveAdmin,
                                focusedLabelColor = OliveAdmin,
                                cursorColor = OliveAdmin
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = institucionExpanded,
                            onDismissRequest = { institucionExpanded = false },
                             modifier = Modifier.background(Color.White)
                        ) {
                            instituciones.forEach { inst ->
                                DropdownMenuItem(
                                    text = { Text(inst.nombreInstitucion) },
                                    onClick = {
                                        institucionSeleccionada = inst
                                        institucionExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    val textFieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OliveAdmin,
                        focusedLabelColor = OliveAdmin,
                        cursorColor = OliveAdmin
                    )
                    val fieldShape = RoundedCornerShape(12.dp)

                    OutlinedTextField(
                        value = nombre, 
                        onValueChange = { nombre = it }, 
                        label = { Text("Nombre Sala") }, 
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        shape = fieldShape
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = direccion, 
                        onValueChange = { direccion = it }, 
                        label = { Text("Dirección") }, 
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        shape = fieldShape
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = telefono, 
                            onValueChange = { telefono = it }, 
                            label = { Text("Teléfono") }, 
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = textFieldColors,
                            shape = fieldShape
                        )
                        OutlinedTextField(
                            value = numCubiculos, 
                            onValueChange = { numCubiculos = it }, 
                            label = { Text("Cubículos") }, 
                            modifier = Modifier.weight(0.5f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = textFieldColors,
                            shape = fieldShape
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = correo, 
                        onValueChange = { correo = it }, 
                        label = { Text("Correo") }, 
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = textFieldColors,
                        shape = fieldShape
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    // Switch Estado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estado: ${if (esActivo) "Activo" else "Inactivo"}", style = MaterialTheme.typography.bodyMedium, color = OliveTextSecondary)
                        Switch(
                            checked = esActivo, 
                            onCheckedChange = { esActivo = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OliveAdmin,
                                checkedTrackColor = OliveAdmin.copy(alpha = 0.5f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("Horario de Atención", style = MaterialTheme.typography.titleSmall, color = OliveTextPrimary)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = horaInicio, 
                            onValueChange = { horaInicio = it }, 
                            label = { Text("Apertura (HH:mm)") }, 
                            modifier = Modifier.weight(1f),
                            colors = textFieldColors,
                            shape = fieldShape
                        )
                        OutlinedTextField(
                            value = horaFin, 
                            onValueChange = { horaFin = it }, 
                            label = { Text("Cierre (HH:mm)") }, 
                            modifier = Modifier.weight(1f),
                            colors = textFieldColors,
                            shape = fieldShape
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("Días Laborables", style = MaterialTheme.typography.titleSmall, color = OliveTextPrimary)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                         // Simple checkboxes layout (could be improved)
                         Column {
                             CheckboxWithLabel("Lun", dias.lunes) { dias = dias.copy(lunes = it) }
                             CheckboxWithLabel("Mar", dias.martes) { dias = dias.copy(martes = it) }
                         }
                         Column {
                             CheckboxWithLabel("Mié", dias.miercoles) { dias = dias.copy(miercoles = it) }
                             CheckboxWithLabel("Jue", dias.jueves) { dias = dias.copy(jueves = it) }
                         }
                         Column {
                             CheckboxWithLabel("Vie", dias.viernes) { dias = dias.copy(viernes = it) }
                             CheckboxWithLabel("Sáb", dias.sabado) { dias = dias.copy(sabado = it) }
                         }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("Ubicación (Toque para seleccionar)", style = MaterialTheme.typography.titleSmall, color = OliveTextPrimary)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        GoogleMap(
                            cameraPositionState = cameraPositionState,
                            onMapClick = { latLng ->
                                markerPosition = latLng
                            }
                        ) {
                            Marker(
                                state = MarkerState(position = markerPosition),
                                title = "Ubicación Sala"
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = OliveTextSecondary)) { Text("Cancelar") }
                    Button(
                        onClick = {
                            if (nombre.isBlank()) {
                                return@Button
                            }
                             if (institucionSeleccionada == null) {
                                return@Button
                            }
                            val nuevoLactario = SalaLactanciaDto(
                                id = lactarioEditar?.id ?: 0,
                                nombre = nombre,
                                direccion = direccion,
                                telefono = telefono,
                                correo = correo,
                                latitud = markerPosition.latitude.toString(),
                                longitud = markerPosition.longitude.toString(),
                                institucion = institucionSeleccionada,
                                horario = HorariosSalaDto(
                                    id = lactarioEditar?.horario?.id,
                                    horaInicio = horaInicio, 
                                    horaFin = horaFin
                                ),
                                dias = dias,
                                estado = if (esActivo) "Activo" else "Inactivo"
                            )
                            onGuardar(nuevoLactario, numCubiculos.toIntOrNull() ?: 1)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OliveAdmin, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (lactarioEditar == null) "Guardar" else "Actualizar")
                    }
                }
            }
        }
    }
}

@Composable
fun CheckboxWithLabel(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = OliveAdmin)
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = OliveTextSecondary)
    }
}
