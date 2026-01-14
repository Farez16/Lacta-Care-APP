package com.example.lactacare.vistas.paciente.perfil

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.lactacare.vistas.theme.MomPrimary

@Composable
fun PantallaPerfilPaciente(
    onLogout: () -> Unit,
    viewModel: PatientProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var nombreEdit by remember { mutableStateOf("") }
    var telefonoEdit by remember { mutableStateOf("") }
    var imagenSeleccionada by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // Launcher para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                
                if (bytes != null) {
                    val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                    imagenSeleccionada = base64
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    LaunchedEffect(uiState.isEditing) {
        if (uiState.isEditing) {
            nombreEdit = uiState.primerNombre
            telefonoEdit = uiState.telefono
            imagenSeleccionada = null
        }
    }

    LaunchedEffect(uiState.sessionClosed) {
        if (uiState.sessionClosed) {
            onLogout()
        }
    }

    // Mostrar mensajes
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearMessages() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMessages() }) {
                    Text("OK")
                }
            }
        )
    }

    uiState.successMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearMessages() },
            title = { Text("Éxito") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMessages() }) {
                    Text("OK")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Avatar / Imagen de perfil
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clickable(enabled = uiState.isEditing) {
                        imagePickerLauncher.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                // Mostrar imagen
                ImagenPerfil(
                    imagenSeleccionada = imagenSeleccionada,
                    imagenPerfil = uiState.imagenPerfil
                )

                // Icono de cámara si está en modo edición
                if (uiState.isEditing) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Cambiar foto",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(36.dp)
                            .background(MomPrimary, CircleShape)
                            .padding(8.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nombre
            Text(
                text = uiState.nombreCompleto,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Text(
                text = "Madre Lactante",
                fontSize = 16.sp,
                color = MomPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Modo Vista
            if (!uiState.isEditing) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Mis Datos",
                                fontWeight = FontWeight.SemiBold,
                                color = MomPrimary,
                                fontSize = 18.sp
                            )
                            IconButton(onClick = { viewModel.toggleEditing() }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = MomPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))

                        InfoRow(Icons.Default.Badge, "Cédula", uiState.cedula)
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(Icons.Default.Email, "Correo", uiState.correo)
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(Icons.Default.Phone, "Teléfono", uiState.telefono.ifEmpty { "No registrado" })
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(Icons.Default.Cake, "Fecha de Nacimiento", uiState.fechaNacimiento)
                    }
                }
            } else {
                // Modo Edición
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Editar Perfil",
                            fontWeight = FontWeight.SemiBold,
                            color = MomPrimary,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo Nombre con validación
                        OutlinedTextField(
                            value = nombreEdit,
                            onValueChange = { newValue ->
                                // Solo permitir letras y espacios
                                if (newValue.all { it.isLetter() || it.isWhitespace() }) {
                                    nombreEdit = newValue
                                }
                            },
                            label = { Text("Nombre") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = uiState.nombreError != null,
                            supportingText = {
                                uiState.nombreError?.let { Text(it, color = Color.Red) }
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Campo Teléfono con validación
                        OutlinedTextField(
                            value = telefonoEdit,
                            onValueChange = { newValue ->
                                // Solo permitir números y máximo 10 dígitos
                                if (newValue.all { it.isDigit() } && newValue.length <= 10) {
                                    telefonoEdit = newValue
                                }
                            },
                            label = { Text("Teléfono (10 dígitos)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = uiState.telefonoError != null,
                            supportingText = {
                                uiState.telefonoError?.let { Text(it, color = Color.Red) }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))

                        // Botones
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { 
                                    viewModel.toggleEditing()
                                    imagenSeleccionada = null
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancelar")
                            }

                            Button(
                                onClick = {
                                    viewModel.actualizarPerfil(
                                        nombre = nombreEdit,
                                        telefono = telefonoEdit,
                                        imagenBase64 = imagenSeleccionada
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MomPrimary),
                                enabled = !uiState.isLoading
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White
                                    )
                                } else {
                                    Text("Guardar")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Cerrar Sesión
            Button(
                onClick = { viewModel.cerrarSesion() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MomPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ImagenPerfil(
    imagenSeleccionada: String?,
    imagenPerfil: String?
) {
    when {
        // Imagen seleccionada para editar
        imagenSeleccionada != null -> {
            val bitmap = remember(imagenSeleccionada) {
                try {
                    val imageBytes = Base64.decode(imagenSeleccionada, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                } catch (e: Exception) {
                    null
                }
            }
            
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Imagen de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                IconoPorDefecto()
            }
        }
        // Imagen desde backend (URL o Base64)
        imagenPerfil != null -> {
            if (imagenPerfil.startsWith("http")) {
                // URL de Google o servidor
                AsyncImage(
                    model = imagenPerfil,
                    contentDescription = "Imagen de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Base64
                val bitmap = remember(imagenPerfil) {
                    try {
                        val imageBytes = Base64.decode(imagenPerfil, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    } catch (e: Exception) {
                        null
                    }
                }
                
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Imagen de perfil",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    IconoPorDefecto()
                }
            }
        }
        // Sin imagen - mostrar icono por defecto
        else -> {
            IconoPorDefecto()
        }
    }
}

@Composable
fun IconoPorDefecto() {
    Icon(
        imageVector = Icons.Default.Face,
        contentDescription = null,
        modifier = Modifier
            .size(120.dp)
            .background(MomPrimary.copy(alpha = 0.1f), CircleShape)
            .padding(24.dp),
        tint = MomPrimary
    )
}
