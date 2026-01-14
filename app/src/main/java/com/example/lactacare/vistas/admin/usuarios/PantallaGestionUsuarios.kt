package com.example.lactacare.vistas.admin.usuarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.datos.dto.UsuarioResponseDto
import com.example.lactacare.vistas.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionUsuarios(
    onVolver: () -> Unit,
    onCrearDoctor: () -> Unit, // Ya no se usa para navegación, pero lo mantenemos por compatibilidad
    viewModel: GestionUsuariosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var usuarioEditar by remember { mutableStateOf<UsuarioResponseDto?>(null) }
    var mostrarConfirmarEliminar by remember { mutableStateOf<UsuarioResponseDto?>(null) }
    
    val context = androidx.compose.ui.platform.LocalContext.current

    // --- DIALOGO DE CREACION / EDICION ---
    if (mostrarDialogoCrear) {
        DialogoCrearEmpleado(
            salasDisponibles = uiState.listaSalas,
            empleadoEditar = usuarioEditar,
            onDismiss = { 
                mostrarDialogoCrear = false 
                usuarioEditar = null
            },
            onConfirm = { datos, horario, dias, salaId ->
                if (usuarioEditar != null) {
                    viewModel.actualizarEmpleado(
                        id = usuarioEditar!!.id.toInt(),
                        datos = datos,
                        horario = horario,
                        dias = dias,
                        salaId = salaId,
                        onSuccess = { 
                            mostrarDialogoCrear = false 
                            usuarioEditar = null
                            android.widget.Toast.makeText(context, "Empleado actualizado", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        onError = { msg -> android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show() }
                    )
                } else {
                    viewModel.crearEmpleadoCompleto(datos, horario, dias, salaId, 
                        onSuccess = { mostrarDialogoCrear = false },
                        onError = { msg -> 
                             android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
        )
    }

    // --- DIALOGO CONFIRMAR ELIMINAR ---
    if (mostrarConfirmarEliminar != null) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarEliminar = null },
            title = { Text("¿Eliminar Empleado?") },
            text = { Text("Se eliminará a ${mostrarConfirmarEliminar?.nombreCompleto}. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = mostrarConfirmarEliminar!!.id.toInt()
                        viewModel.eliminarUsuario(id, 
                            onSuccess = {
                                mostrarConfirmarEliminar = null
                                android.widget.Toast.makeText(context, "Eliminado correctamente", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            onError = { msg -> android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show() }
                        )
                    }
                ) { Text("Eliminar", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    PantallaPremiumAdmin(
        titulo = "Gestión de Usuarios",
        floatingActionButton = {
            // Solo Mostrar Botón en Tabs Admin (0) y Doctor (1)
            if (uiState.tabSeleccionado == 0 || uiState.tabSeleccionado == 1) {
                BotonPildora(
                    text = "Nuevo Empleado",
                    icon = Icons.Default.Add,
                    onClick = { 
                        usuarioEditar = null
                        mostrarDialogoCrear = true 
                    },
                    modifier = Modifier.padding(bottom = 16.dp).height(56.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // --- TABS (Pestañas) Premium Style ---
            TabRow(
                selectedTabIndex = uiState.tabSeleccionado,
                containerColor = Color.White,
                contentColor = DarkCharcoal,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.tabSeleccionado]),
                        color = NeonPrimary,
                        height = 3.dp
                    )
                },
                divider = { HorizontalDivider(color = CleanBackground) }
            ) {
                Tab(
                    selected = uiState.tabSeleccionado == 0,
                    onClick = { viewModel.cambiarTab(0) },
                    text = { Text("Admins", fontWeight = if(uiState.tabSeleccionado==0) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Outlined.Security, null) },
                    selectedContentColor = DarkCharcoal,
                    unselectedContentColor = TextSecondary
                )
                Tab(
                    selected = uiState.tabSeleccionado == 1,
                    onClick = { viewModel.cambiarTab(1) },
                    text = { Text("Doctores", fontWeight = if(uiState.tabSeleccionado==1) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Outlined.MedicalServices, null) },
                    selectedContentColor = DarkCharcoal,
                    unselectedContentColor = TextSecondary
                )
                Tab(
                    selected = uiState.tabSeleccionado == 2,
                    onClick = { viewModel.cambiarTab(2) },
                    text = { Text("Pacientes", fontWeight = if(uiState.tabSeleccionado==2) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Outlined.Person, null) },
                    selectedContentColor = DarkCharcoal,
                    unselectedContentColor = TextSecondary
                )
            }

            // --- CONTENIDO DE LA LISTA ---
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = NeonPrimary)
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (uiState.tabSeleccionado) {
                            0 -> { // ADMINS
                                items(uiState.listaAdministradores) { admin ->
                                    ItemUsuarioPremium(
                                        usuario = admin, 
                                        esDoctor = false, 
                                        colorIcono = Color(0xFF6200EE), // Color distintivo
                                        onEdit = { 
                                            usuarioEditar = admin
                                            mostrarDialogoCrear = true
                                        },
                                        onDelete = { mostrarConfirmarEliminar = admin }
                                    )
                                }
                            }
                            1 -> { // DOCTORES
                                items(uiState.listaDoctores) { doctor ->
                                    ItemUsuarioPremium(
                                        usuario = doctor, 
                                        esDoctor = true, 
                                        colorIcono = NeonPrimary,
                                        onEdit = { 
                                            usuarioEditar = doctor
                                            mostrarDialogoCrear = true
                                        },
                                        onDelete = { mostrarConfirmarEliminar = doctor }
                                    )
                                }
                            }
                            2 -> { // PACIENTES
                                items(uiState.listaPacientes) { paciente ->
                                    ItemUsuarioPremium(
                                        usuario = paciente, 
                                        esDoctor = false, 
                                        colorIcono = NeonPrimary,
                                        onEdit = { 
                                            usuarioEditar = paciente
                                            mostrarDialogoCrear = true
                                        },
                                        onDelete = { mostrarConfirmarEliminar = paciente }
                                    )
                                }
                            }
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemUsuarioPremium(
    usuario: UsuarioResponseDto,
    esDoctor: Boolean,
    colorIcono: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    TarjetaPremium {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), // Added padding inside card for better touch targets
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = colorIcono.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = usuario.nombreCompleto.take(1),
                        fontWeight = FontWeight.Bold,
                        color = colorIcono,
                        fontSize = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = usuario.nombreCompleto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuroClean
                )
                Text(
                    text = usuario.correo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            
             Column(horizontalAlignment = Alignment.End) {
                  if (esDoctor) {
                     Text("Cédula:", fontSize = 10.sp, color = Color.Gray)
                     Text(usuario.cedula ?: "-", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextoOscuroClean)
                  } else {
                     Text("Teléfono:", fontSize = 10.sp, color = Color.Gray)
                     Text(usuario.telefono ?: "-", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextoOscuroClean)
                  }
             }
             
             // --- BOTONES DE ACCIÓN (Editar/Eliminar) ---
             Row(modifier = Modifier.padding(start = 8.dp)) {
                 IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                     Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                 }
                 IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                     Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                 }
             }
        }
    }
}