package com.example.lactacare.vistas.perfil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lactacare.dominio.model.RolUsuario
import com.example.lactacare.vistas.auth.BordeGrisClean
import com.example.lactacare.vistas.auth.TextoOscuroClean
import com.example.lactacare.vistas.theme.AdminBackground
import com.example.lactacare.vistas.theme.AdminPrimary
import com.example.lactacare.vistas.theme.DoctorBackground
import com.example.lactacare.vistas.theme.DoctorPrimary
import com.example.lactacare.vistas.theme.MomAccent
import com.example.lactacare.vistas.theme.MomPrimary
import androidx.hilt.navigation.compose.hiltViewModel



@Composable
fun PantallaPerfil(
    rolUsuario: RolUsuario,
    onLogout: () -> Unit,
    viewModel: PerfilViewModel = hiltViewModel()
) {
    // Colores según Rol
    val (colorPrincipal, colorFondoSuave) = when (rolUsuario) {
        RolUsuario.PACIENTE -> Pair(MomPrimary, MomAccent)
        RolUsuario.DOCTOR -> Pair(DoctorPrimary, DoctorBackground)
        RolUsuario.ADMINISTRADOR -> Pair(AdminPrimary, AdminBackground)
    }

    // Cargar datos (Sin pasar rol, el VM ya no lo necesita para lógica, solo para UI colors)
    LaunchedEffect(Unit) {
        viewModel.cargarPerfil()
    }

    val uiState by viewModel.uiState.collectAsState()
    val nombreEdit by viewModel.nombreEdit.collectAsState()
    val nuevaImagenUri by viewModel.nuevaImagenUri.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.onImagenSeleccionada(uri) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.cambiosGuardados) {
        if (uiState.cambiosGuardados) {
            snackbarHostState.showSnackbar("Perfil actualizado correctamente")
            viewModel.limpiarMensajeExito()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
        ) {
            // --- CABECERA ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    // FOTO DE PERFIL
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(colorFondoSuave, CircleShape)
                            .clip(CircleShape)
                            .clickable(enabled = uiState.modoEdicion) {
                                imagePickerLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            nuevaImagenUri != null -> {
                                AsyncImage(
                                    model = nuevaImagenUri,
                                    contentDescription = "Nueva foto",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            uiState.imagenPerfil != null -> {
                                AsyncImage(
                                    model = uiState.imagenPerfil,
                                    contentDescription = "Foto actual",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                Icon(Icons.Default.Person, null, tint = colorPrincipal, modifier = Modifier.size(60.dp))
                            }
                        }

                        // Icono de cámara superpuesto si estamos editando
                        if (uiState.modoEdicion) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, null, tint = Color.White)
                            }
                        }
                    }

                    // BOTÓN EDITAR (Lápiz flotante)
                    if (!uiState.modoEdicion) {
                        IconButton(
                            onClick = { viewModel.activarEdicion() },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .background(colorPrincipal, CircleShape)
                                .padding(4.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (uiState.isLoading) {
                    CircularProgressIndicator(color = colorPrincipal, modifier = Modifier.size(24.dp))
                } else {
                    // NOMBRE (Editable vs Lectura)
                    if (uiState.modoEdicion) {
                        OutlinedTextField(
                            value = nombreEdit,
                            onValueChange = { viewModel.onNombreChange(it) },
                            label = { Text("Primer Nombre") },
                            modifier = Modifier.fillMaxWidth(0.7f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorPrincipal,
                                focusedLabelColor = colorPrincipal,
                                cursorColor = colorPrincipal
                            )
                        )
                        Text(
                            text = uiState.apellido, // El apellido se muestra abajo solo lectura
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Text(
                            text = "${uiState.primerNombre} ${uiState.apellido}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoOscuroClean
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // ROL (Badge)
                    Surface(
                        color = colorFondoSuave,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = uiState.detalles["Rol"] ?: rolUsuario.name,
                            color = colorPrincipal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                if (uiState.error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(uiState.error!!, color = Color.Red, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- LISTA DE DETALLES (SOLO LECTURA) ---
            if (!uiState.isLoading) {
                SeccionPerfil("INFORMACIÓN PERSONAL")
                CardOpciones {
                    uiState.detalles.entries.forEachIndexed { index, entry ->
                        // Filtramos el Rol de la lista porque ya está en el header
                        if (entry.key != "Rol") {
                            val icono = when (entry.key) {
                                "Cédula" -> Icons.Outlined.Badge
                                "Fecha Nacimiento" -> Icons.Outlined.Cake
                                else -> Icons.Outlined.Info
                            }

                            ItemPerfil(icono, entry.key, entry.value, colorPrincipal)

                            if (index < uiState.detalles.size - 1) {
                                Divider(color = BordeGrisClean.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // BOTONES DE GUARDAR (Solo en modo edición)
                if (uiState.modoEdicion) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.cancelarEdicion() },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.guardandoCambios,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextoOscuroClean)
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = { viewModel.guardarCambios() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = colorPrincipal),
                            enabled = !uiState.guardandoCambios
                        ) {
                            if (uiState.guardandoCambios) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Guardar Cambios", color = if (rolUsuario == RolUsuario.PACIENTE) TextoOscuroClean else Color.White)
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            // --- GESTIÓN DE CUENTA ---
            SeccionPerfil("GESTIÓN DE CUENTA")
            CardOpciones {
                ItemPerfil(Icons.Outlined.Security, "Privacidad", "", colorPrincipal)
                Divider(color = BordeGrisClean.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLogout() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Logout, null, tint = Color.Red)
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Cerrar Sesión",
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

// --- COMPONENTES AUXILIARES ---
@Composable
fun SeccionPerfil(titulo: String) {
    Text(
        text = titulo,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
fun CardOpciones(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        content()
    }
}

@Composable
fun ItemPerfil(icon: ImageVector, titulo: String, subtitulo: String, colorTema: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(colorTema.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = colorTema)
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontSize = 12.sp, color = Color.Gray)
            if (subtitulo.isNotEmpty()) {
                Text(subtitulo, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextoOscuroClean)
            }
        }
    }
}