package com.example.lactacare.vistas.admin.usuarios

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.lactacare.datos.dto.CrearEmpleadoRequest
import com.example.lactacare.datos.dto.DiasLaborablesEmpleadoDto
import com.example.lactacare.datos.dto.HorariosEmpleadoDto
import com.example.lactacare.datos.dto.SalaLactanciaDto
import com.example.lactacare.vistas.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun DialogoCrearEmpleado(
    salasDisponibles: List<SalaLactanciaDto>,
    empleadoEditar: com.example.lactacare.datos.dto.UsuarioResponseDto? = null,
    onDismiss: () -> Unit,
    onConfirm: (CrearEmpleadoRequest, HorariosEmpleadoDto?, DiasLaborablesEmpleadoDto?, Int?) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    // Pre-procesamiento de nombre si es edición
    val nombreParts = empleadoEditar?.nombreCompleto?.split(" ") ?: emptyList()
    val initNombre = nombreParts.getOrNull(0) ?: ""
    val initApellido = nombreParts.getOrNull(1) ?: ""
    
    // Paso 1: Datos Personales
    var cedula by remember { mutableStateOf(empleadoEditar?.cedula ?: "") }
    var nombre by remember { mutableStateOf(initNombre) }
    var apellido by remember { mutableStateOf(initApellido) } // Usaremos este para Primer Apellido. Para simpleza UI.
    var segundoApellido by remember { mutableStateOf("") } // Perdido en lista simple
    var correo by remember { mutableStateOf(empleadoEditar?.correo ?: "") }
    var telefono by remember { mutableStateOf(empleadoEditar?.telefono ?: "") }
    
    // Paso 2: Rol y Ubicación
    var selectedRole by remember { mutableStateOf(empleadoEditar?.rol ?: "DOCTOR") } // DOCTOR o ADMINISTRADOR
    var selectedSala by remember { mutableStateOf<SalaLactanciaDto?>(null) }
    var expandedSalas by remember { mutableStateOf(false) }

    // Paso 3: Horario
    var horaEntrada by remember { mutableStateOf("08:00") }
    var horaSalida by remember { mutableStateOf("17:00") }
    val diasSeleccionados = remember { mutableStateMapOf(
        "Lunes" to true, "Martes" to true, "Miércoles" to true, 
        "Jueves" to true, "Viernes" to true, "Sábado" to false, "Domingo" to false
    )}

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize() // Fullscreen immersive experience
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = CleanBackground
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (empleadoEditar != null) "Editar Empleado" else "Nuevo Empleado", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary)
                    }
                }

                // Steps Indicator
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.Center) {
                    StepIndicator(1, "Datos", step >= 1)
                    Spacer(Modifier.width(8.dp))
                    StepIndicator(2, "Rol", step >= 2)
                    Spacer(Modifier.width(8.dp))
                    StepIndicator(3, "Horario", step >= 3)
                }

                HorizontalDivider(modifier = Modifier.padding(16.dp), color = Color.LightGray)

                // Content
                Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    when (step) {
                        1 -> StepDatosPersonales(
                            cedula, { cedula = it },
                            nombre, { nombre = it },
                            apellido, { apellido = it },
                            segundoApellido, { segundoApellido = it },
                            correo, { correo = it },
                            telefono, { telefono = it }
                        )
                        2 -> StepRolUbicacion(
                            selectedRole, { selectedRole = it },
                            salasDisponibles, selectedSala, { selectedSala = it },
                            expandedSalas, { expandedSalas = it }
                        )
                        3 -> StepHorario(
                            horaEntrada, { horaEntrada = it },
                            horaSalida, { horaSalida = it },
                            diasSeleccionados
                        )
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (step > 1) {
                        OutlinedButton(onClick = { step-- }, modifier = Modifier.padding(end = 8.dp)) {
                            Text("Atrás", color = DarkCharcoal)
                        }
                    }
                    
                    Button(
                        onClick = {
                            if (step < 3) {
                                step++
                            } else {
                                // CONFIRMACION FINAL
                                val request = CrearEmpleadoRequest(
                                    cedula = cedula,
                                    primerNombre = nombre,
                                    segundoNombre = "", // Opcional no mapeado en UI simple
                                    primerApellido = apellido,
                                    segundoApellido = segundoApellido,
                                    correo = correo,
                                    telefono = telefono,
                                    fechaNacimiento = "1990-01-01", // Default placeholder
                                    rol = if (selectedRole == "DOCTOR") "MEDICO" else selectedRole
                                )
                                
                                val horario = if (selectedRole == "DOCTOR") HorariosEmpleadoDto(
                                    horaInicio = "$horaEntrada:00",
                                    horaFin = "$horaSalida:00"
                                ) else null

                                val dias = if (selectedRole == "DOCTOR") DiasLaborablesEmpleadoDto(
                                    lunes = diasSeleccionados["Lunes"] ?: false,
                                    martes = diasSeleccionados["Martes"] ?: false,
                                    miercoles = diasSeleccionados["Miércoles"] ?: false,
                                    jueves = diasSeleccionados["Jueves"] ?: false,
                                    viernes = diasSeleccionados["Viernes"] ?: false,
                                    sabado = diasSeleccionados["Sábado"] ?: false,
                                    domingo = diasSeleccionados["Domingo"] ?: false
                                ) else null
                                
                                val salaId = if (selectedRole == "DOCTOR") selectedSala?.id?.toInt() else null

                                onConfirm(request, horario, dias, salaId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPrimary)
                    ) {
                        Text(if (step == 3) (if (empleadoEditar != null) "Guardar Cambios" else "Crear Empleado") else "Siguiente", color = DarkCharcoal)
                    }
                }
            }
        }
    }
}

@Composable
fun StepIndicator(num: Int, label: String, active: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = if (active) NeonPrimary else Color.LightGray,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(num.toString(), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if(active) DarkCharcoal else Color.White)
            }
        }
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, fontWeight = if(active) FontWeight.Bold else FontWeight.Normal, color = if(active) DarkCharcoal else TextSecondary)
    }
}

@Composable
fun StepDatosPersonales(
    cedula: String, onCedula: (String) -> Unit,
    nombre: String, onNombre: (String) -> Unit,
    apellido: String, onApellido: (String) -> Unit,
    segundoApellido: String, onSegundoApellido: (String) -> Unit,
    correo: String, onCorreo: (String) -> Unit,
    telefono: String, onTelefono: (String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { CampoTextoClean("Cédula", cedula, onCedula, Icons.Default.Person, keyboardType = KeyboardType.Number) }
        item { CampoTextoClean("Primer Nombre", nombre, onNombre, Icons.Default.Person) }
        item { CampoTextoClean("Primer Apellido", apellido, onApellido, Icons.Default.Person) }
        item { CampoTextoClean("Segundo Apellido", segundoApellido, onSegundoApellido, Icons.Default.Person) }
        item { CampoTextoClean("Correo Electrónico", correo, onCorreo, Icons.Default.Email, keyboardType = KeyboardType.Email) }
        item { CampoTextoClean("Teléfono", telefono, onTelefono, Icons.Default.Phone, keyboardType = KeyboardType.Phone) }
    }
}

@Composable
fun StepRolUbicacion(
    selectedRole: String, onRoleChange: (String) -> Unit,
    salas: List<SalaLactanciaDto>, selectedSala: SalaLactanciaDto?, onSalaChange: (SalaLactanciaDto) -> Unit,
    expanded: Boolean, onExpandedChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Seleccione el Rol", fontWeight = FontWeight.Bold, color = DarkCharcoal)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedRole == "DOCTOR",
                onClick = { onRoleChange("DOCTOR") },
                label = { Text("DOCTOR") },
                leadingIcon = { if (selectedRole == "DOCTOR") Icon(Icons.Default.Check, null) else null }
            )
            FilterChip(
                selected = selectedRole == "ADMINISTRADOR",
                onClick = { onRoleChange("ADMINISTRADOR") },
                label = { Text("ADMINISTRADOR") },
                leadingIcon = { if (selectedRole == "ADMINISTRADOR") Icon(Icons.Default.Check, null) else null }
            )
        }

        if (selectedRole == "DOCTOR") {
            HorizontalDivider()
            Text("Asignar Lactario (Sede)", fontWeight = FontWeight.Bold, color = DarkCharcoal)
            
            Box {
                OutlinedTextField(
                    value = selectedSala?.nombre ?: "Seleccionar Sala...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sala de Lactancia") },
                    trailingIcon = { Icon(Icons.Default.Business, null) },
                    modifier = Modifier.fillMaxWidth().clickable { onExpandedChange(true) },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = DarkCharcoal,
                        disabledBorderColor = Color.Gray,
                        disabledLabelColor = TextSecondary,
                        disabledTrailingIconColor = NeonPrimary
                    )
                )
                // Overlay invisible click handler
                Box(modifier = Modifier.matchParentSize().clickable { onExpandedChange(true) })
                
                DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                    salas.forEach { sala ->
                        DropdownMenuItem(
                            text = { Text(sala.nombre ?: "Sin Nombre") },
                            onClick = {
                                onSalaChange(sala)
                                onExpandedChange(false)
                            }
                        )
                    }
                }
            }
        } else {
             Text("Los administradores tienen acceso global.", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = TextSecondary)
        }
    }
}

@Composable
fun StepHorario(
    horaEntrada: String, onEntrada: (String) -> Unit,
    horaSalida: String, onSalida: (String) -> Unit,
    dias: MutableMap<String, Boolean>
) {
    Column {
        Text("Definir Horario Laboral", fontWeight = FontWeight.Bold, color = DarkCharcoal, modifier = Modifier.padding(bottom = 16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Simplified Time Inputs (Text for now, TimePicker ideal but expensive to implement quickly)
            CampoTextoClean("Entrada (HH:mm)", horaEntrada, onEntrada, Icons.Default.AccessTime, Modifier.weight(1f))
            CampoTextoClean("Salida (HH:mm)", horaSalida, onSalida, Icons.Default.AccessTime, Modifier.weight(1f))
        }
        
        Spacer(Modifier.height(16.dp))
        Text("Días Laborables", fontWeight = FontWeight.Bold, color = DarkCharcoal)
        
        Column {
            dias.keys.chunked(2).forEach { rowDays ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    rowDays.forEach { dia ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(4.dp)
                        ) {
                            Checkbox(
                                checked = dias[dia] == true,
                                onCheckedChange = { dias[dia] = it },
                                colors = CheckboxDefaults.colors(checkedColor = NeonPrimary)
                            )
                            Text(dia, fontSize = 14.sp, color = TextoOscuroClean)
                        }
                    }
                }
            }
        }
    }
}
