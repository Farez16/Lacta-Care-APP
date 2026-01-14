package com.example.lactacare.vistas.admin.ia

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.datos.dto.DocumentoDto
import com.example.lactacare.vistas.theme.*

@Composable
fun PantallaGestionIA(
    viewModel: IAViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // File Picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.subirDocumento(it) }
    }
    
    PantallaPremiumAdmin(
        titulo = "Inteligencia Artificial",
        floatingActionButton = {
            BotonPildora(
                text = "Subir PDF",
                icon = Icons.Default.Add,
                onClick = { launcher.launch("application/pdf") },
                modifier = Modifier.padding(bottom = 16.dp).height(56.dp)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = NeonPrimary)
            } else if (uiState.documentos.isEmpty()) {
                Text(
                    "No hay documentos de entrenamiento.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            "Documentos de Entrenamiento (PDF)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextoOscuroClean,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(uiState.documentos) { doc ->
                        ItemDocumentoPremium(
                            documento = doc,
                            onDelete = { viewModel.eliminarDocumento(doc.idDocumento) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
            
            // Error overlay simple (si aplica)
            uiState.error?.let {
                Card(
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                     modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(it, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onErrorContainer)
                        TextButton(onClick = { viewModel.limpiarMensaje() }) {
                             Text("Cerrar", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemDocumentoPremium(
    documento: DocumentoDto,
    onDelete: () -> Unit
) {
    TarjetaPremium {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MintPastel,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Description, null, tint = NeonPrimary, modifier = Modifier.size(24.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = documento.nombreArchivo,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = TextoOscuroClean
                )
                if (documento.tamano != null) {
                    Text(
                        text = "${documento.tamano} bytes",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF5350))
            }
        }
    }
}
