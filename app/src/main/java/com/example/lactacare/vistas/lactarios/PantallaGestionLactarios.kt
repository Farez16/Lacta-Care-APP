package com.example.lactacare.vistas.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lactacare.datos.MockLactarioRepository
import com.example.lactacare.datos.MockRefrigeradorRepository
import com.example.lactacare.dominio.model.Lactario
import com.example.lactacare.vistas.theme.AdminPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionLactarios(
    onVolver: () -> Unit
) {
    // 1. Instanciamos el ViewModel con sus dependencias (Mocks)
    val viewModel: GestionLactariosViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return GestionLactariosViewModel(
                    lactarioRepo = MockLactarioRepository(),
                    refrigeradorRepo = MockRefrigeradorRepository()
                ) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    // 2. FORZAR CARGA: Asegura que al entrar veamos los datos más recientes
    LaunchedEffect(Unit) {
        viewModel.cargarLactarios()
    }

    // Estados para controlar el Diálogo (Ventana emergente)
    var mostrarDialogo by remember { mutableStateOf(false) }
    var lactarioEnEdicion by remember { mutableStateOf<Lactario?>(null) }

    Scaffold(
        containerColor = Color(0xFFF5F5F5), // Fondo gris claro
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Lactarios", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    lactarioEnEdicion = null // null significa "Modo Crear Nuevo"
                    mostrarDialogo = true
                },
                containerColor = AdminPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Agregar")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {

            // A. BARRA DE CARGA
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AdminPrimary)
            }

            // B. LISTA DE SALAS
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.listaLactarios) { lactario ->
                    ItemLactarioAdmin(
                        lactario = lactario,
                        onEditar = {
                            lactarioEnEdicion = lactario
                            mostrarDialogo = true
                        },
                        onEliminar = { viewModel.eliminarLactario(lactario.id) }
                    )
                }
            }

            // C. MENSAJE SI ESTÁ VACÍO
            if (!uiState.isLoading && uiState.listaLactarios.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No hay salas registradas", color = Color.Gray)
                }
            }

            // D. MENSAJE DE ERROR
            uiState.mensajeError?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
                )
            }
        }
    }

    // E. DIÁLOGO POP-UP (Para Crear o Editar)
    if (mostrarDialogo) {
        DialogoLactario(
            lactarioExistente = lactarioEnEdicion,
            onDismiss = { mostrarDialogo = false },
            onGuardar = { nuevoLactario ->
                viewModel.guardarLactario(nuevoLactario)
                mostrarDialogo = false
            }
        )
    }
}

// COMPONENTE 1: Tarjeta visual de la sala
@Composable
fun ItemLactarioAdmin(
    lactario: Lactario,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(lactario.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(lactario.direccion, fontSize = 14.sp, color = Color.Gray)
                Text("Tel: ${lactario.telefono}", fontSize = 12.sp, color = AdminPrimary)
            }
            Row {
                IconButton(onClick = onEditar) {
                    Icon(Icons.Outlined.Edit, "Editar", tint = AdminPrimary)
                }
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Outlined.Delete, "Eliminar", tint = Color(0xFFEF5350)) // Rojo suave
                }
            }
        }
    }
}

// COMPONENTE 2: Formulario flotante
@Composable
fun DialogoLactario(
    lactarioExistente: Lactario?,
    onDismiss: () -> Unit,
    onGuardar: (Lactario) -> Unit
) {
    // Variables temporales para escribir en el formulario
    var nombre by remember { mutableStateOf(lactarioExistente?.nombre ?: "") }
    var direccion by remember { mutableStateOf(lactarioExistente?.direccion ?: "") }
    var telefono by remember { mutableStateOf(lactarioExistente?.telefono ?: "") }

    // Datos que no pedimos en el form rápido (se mantienen ocultos o por defecto)
    val lat = lactarioExistente?.latitud ?: "0.0"
    val lon = lactarioExistente?.longitud ?: "0.0"
    val correo = lactarioExistente?.correo ?: ""

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (lactarioExistente == null) "Nueva Sala" else "Editar Sala") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre (Ej: Sala 3A)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Ubicación (Ej: Piso 2)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono / Ext.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary),
                onClick = {
                    if (nombre.isNotBlank()) {
                        val nuevo = Lactario(
                            id = lactarioExistente?.id ?: 0, // 0 = Crear nuevo
                            nombre = nombre,
                            direccion = direccion,
                            telefono = telefono,
                            correo = correo,
                            latitud = lat,
                            longitud = lon,
                            idInstitucion = 100 // Valor fijo por ahora
                        )
                        onGuardar(nuevo)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}