package com.example.lactacare.vistas.admin.instituciones

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.lactacare.dominio.model.Institucion
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.example.lactacare.vistas.theme.*
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.InputStream

@Composable
fun DialogoCrearInstitucion(
    institucion: Institucion? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var nombre by remember { mutableStateOf(institucion?.nombreInstitucion ?: "") }
    var logoBase64 by remember { mutableStateOf(institucion?.logoInstitucion) }
    var errorImagen by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    
    // Image Picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                inputStream?.use { stream ->
                    val bytes = stream.readBytes()
                    // Compress if needed or standard encode
                    // For safety, enforce NO_WRAP to avoid line breaks ruining the string
                    logoBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    errorImagen = null
                }
            } catch (e: Exception) {
                errorImagen = "Error al leer imagen: ${e.message}"
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (institucion == null) "Nueva Institución" else "Editar Institución",
                    style = MaterialTheme.typography.headlineSmall,
                     color = OliveTextPrimary
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OliveAdmin,
                        focusedLabelColor = OliveAdmin,
                        cursorColor = OliveAdmin
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botón Logo
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = OliveAdmin),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Upload, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (logoBase64 == null) "Subir Logo" else "Cambiar Logo")
                }
                
                // Preview Logo
                if (!logoBase64.isNullOrBlank()) {
                    Spacer(Modifier.height(16.dp))
                    
                    if (logoBase64!!.startsWith("http")) {
                        // Es una URL (del backend)
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(logoBase64)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Preview Web",
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.CenterHorizontally),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        // Es Base64 (local)
                        val bitmap = remember(logoBase64) {
                            try {
                                val decodedString = Base64.decode(logoBase64, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Preview Local",
                                modifier = Modifier
                                    .size(100.dp)
                                    .align(Alignment.CenterHorizontally),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text("Error visualizando imagen", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                
                if (errorImagen != null) {
                   Text(errorImagen!!, color = Color.Red, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = OliveTextSecondary)) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { onConfirm(nombre, logoBase64) },
                        enabled = nombre.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = OliveAdmin, disabledContainerColor = Color.Gray),
                         shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
