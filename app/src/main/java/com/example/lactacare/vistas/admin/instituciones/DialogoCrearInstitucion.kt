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
import java.io.InputStream

@Composable
fun DialogoCrearInstitucion(
    institucion: Institucion? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var nombre by remember { mutableStateOf(institucion?.nombreInstitucion ?: "") }
    var logoBase64 by remember { mutableStateOf(institucion?.logoInstitucion) }
    
    val context = LocalContext.current
    
    // Image Picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream: InputStream? = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            logoBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (institucion == null) "Nueva Institución" else "Editar Institución",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Botón Logo
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Upload, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (logoBase64 == null) "Subir Logo" else "Cambiar Logo")
                }
                
                // Preview Logo
                if (logoBase64 != null) {
                    Spacer(Modifier.height(8.dp))
                    
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
                            contentDescription = "Preview",
                            modifier = Modifier.size(100.dp).fillMaxWidth()
                        )
                    } else {
                        Text("Error al cargar imagen", color = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { onConfirm(nombre, logoBase64) },
                        enabled = nombre.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
