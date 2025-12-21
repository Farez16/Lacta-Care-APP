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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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



@Composable
fun PantallaPerfil(
    rolUsuario: RolUsuario,
    onLogout: () -> Unit,
    viewModel: PerfilViewModel = viewModel(
        factory = PerfilViewModel.Factory(LocalContext.current)
    )
) {
    // Lógica de Colores Dinámicos según Rol
    val (colorPrincipal, colorFondoSuave) = when (rolUsuario) {
        RolUsuario.PACIENTE -> Pair(MomPrimary, MomAccent)
        RolUsuario.DOCTOR -> Pair(DoctorPrimary, DoctorBackground)
        RolUsuario.ADMINISTRADOR -> Pair(AdminPrimary, AdminBackground)
    }

    // Cargar datos
    LaunchedEffect(rolUsuario) {
        viewModel.cargarPerfil(rolUsuario)
    }

    val uiState by viewModel.uiState.collectAsState()
    val telefono by viewModel.telefono.collectAsState()
    val nuevaImagenUri by viewModel.nuevaImagenUri.collectAsState()

    // Launcher para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onImagenSeleccionada(uri)
    }

    // Snackbar para mensajes
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.cambiosGuardados) {
        if (uiState.cambiosGuardados) {
            snackbarHostState.showSnackbar("Cambios guardados correctamente")
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
            // --- CABECERA PERSONALIZADA ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    // Foto Perfil
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(colorFondoSuave, CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                if (uiState.modoEdicion) {
                                    imagePickerLauncher.launch("image/*")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            nuevaImagenUri != null -> {
                                AsyncImage(
                                    model = nuevaImagenUri,
                                    contentDescription = "Nueva foto de perfil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            uiState.imagenPerfil != null -> {
                                AsyncImage(
                                    model = uiState.imagenPerfil,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                Icon(
                                    Icons.Default.Person,
                                    null,
                                    tint = colorPrincipal,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }
                    }

                    // Botón Editar (Con color del rol)
                    if (!uiState.modoEdicion) {
                        IconButton(
                            onClick = { viewModel.activarEdicion() },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .background(colorPrincipal, CircleShape)
                                .padding(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .background(colorPrincipal, CircleShape)
                                .padding(4.dp)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colorPrincipal
                    )
                } else if (uiState.error != null) {
                    Text(
                        uiState.error!!,
                        color = Color.Red,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    Text(
                        uiState.nombreCompleto,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuroClean
                    )
                    Text(
                        uiState.correo,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    // Badge del Rol
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = colorFondoSuave,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = rolUsuario.name,
                            color = colorPrincipal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- DETALLES ---
            if (!uiState.isLoading && uiState.error == null && uiState.detalles.isNotEmpty()) {
                SeccionPerfil("DETALLES DEL PERFIL")
                CardOpciones {
                    uiState.detalles.entries.forEachIndexed { index, entry ->
                        val icono = when (entry.key) {
                            "Teléfono" -> Icons.Outlined.Phone
                            "Cédula" -> Icons.Outlined.Badge
                            "Departamento" -> Icons.Outlined.Business
                            "Código Empleado" -> Icons.Outlined.QrCode
                            "Especialidad" -> Icons.Outlined.MedicalServices
                            "Licencia Médica" -> Icons.Outlined.VerifiedUser
                            "Fecha Nacimiento" -> Icons.Outlined.Cake
                            "Discapacidad" -> Icons.Outlined.Accessible
                            else -> Icons.Outlined.Info
                        }

                        // Campos editables
                        if (entry.key == "Teléfono" && uiState.modoEdicion) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    entry.key,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = telefono,
                                    onValueChange = { viewModel.onTelefonoChange(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colorPrincipal,
                                        focusedLabelColor = colorPrincipal,
                                        cursorColor = colorPrincipal
                                    ),
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(icono, null, tint = colorPrincipal)
                                    }
                                )
                            }
                        } else {
                            ItemPerfil(icono, entry.key, entry.value, colorPrincipal)
                        }

                        if (index < uiState.detalles.size - 1) {
                            Divider(color = BordeGrisClean.copy(alpha = 0.5f))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Botones de edición
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
                            enabled = !uiState.guardandoCambios
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = { viewModel.guardarCambios() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorPrincipal
                            ),
                            enabled = !uiState.guardandoCambios
                        ) {
                            if (uiState.guardandoCambios) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text("Guardar", color = if (rolUsuario == RolUsuario.PACIENTE) TextoOscuroClean else Color.White)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // --- GESTIÓN ---
            SeccionPerfil("GESTIÓN DE CUENTA")
            CardOpciones {
                ItemPerfil(Icons.Outlined.Security, "Privacidad", "", colorPrincipal)
                Divider(color = BordeGrisClean.copy(alpha = 0.5f))
                ItemPerfil(Icons.Outlined.HelpOutline, "Ayuda y Soporte", "", colorPrincipal)
                Divider(color = BordeGrisClean.copy(alpha = 0.5f))

                // Logout
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        content()
    }
}

@Composable
fun ItemPerfil(
    icon: ImageVector,
    titulo: String,
    subtitulo: String,
    colorTema: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(colorTema.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = colorTema)
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontSize = 12.sp, color = Color.Gray)
            if (subtitulo.isNotEmpty()) {
                Text(
                    subtitulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextoOscuroClean
                )
            }
        }
        Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
    }
}