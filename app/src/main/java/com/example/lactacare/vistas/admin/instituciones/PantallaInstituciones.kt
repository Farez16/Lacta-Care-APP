package com.example.lactacare.vistas.admin.instituciones

import androidx.compose.material.icons.filled.Apartment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.lactacare.dominio.model.Institucion
import com.example.lactacare.vistas.theme.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext

@Composable
fun PantallaInstituciones(
    viewModel: InstitucionesViewModel = hiltViewModel(),
    esEditable: Boolean = true,
    primaryColor: Color = NeonPrimary
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    var institucionAEditar by remember { mutableStateOf<Institucion?>(null) }

    PantallaPremiumAdmin(
        titulo = "Instituciones Aliadas",
        floatingActionButton = {
            if (esEditable) {
                BotonPildora(
                    text = "Nueva Institución",
                    icon = Icons.Default.Add,
                    onClick = {
                        institucionAEditar = null
                        mostrarDialogo = true
                    },
                    modifier = Modifier.padding(bottom = 16.dp).height(56.dp),
                    containerColor = primaryColor
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = primaryColor)
            } else {
                if (uiState.instituciones.isEmpty()) {
                    Text(
                        text = "No hay instituciones registradas",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.instituciones) { inst ->
                            ItemInstitucionPremium(
                                institucion = inst,
                                onEdit = {
                                    institucionAEditar = inst
                                    mostrarDialogo = true
                                },
                                onDelete = {
                                    inst.idInstitucion?.let { viewModel.eliminarInstitucion(it) }
                                },
                                esEditable = esEditable,
                                primaryColor = primaryColor
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
            
            // Diálogo para Crear/Editar
            if (mostrarDialogo) {
                DialogoCrearInstitucion(
                    institucion = institucionAEditar,
                    onDismiss = { mostrarDialogo = false },
                    onConfirm = { nombre, logo ->
                        if (institucionAEditar == null) {
                            viewModel.crearInstitucion(nombre, logo)
                        } else {
                            institucionAEditar!!.idInstitucion?.let {
                                viewModel.actualizarInstitucion(it, nombre, logo)
                            }
                        }
                        mostrarDialogo = false
                    }
                )
            }
        }
    }
}

@Composable
fun ItemInstitucionPremium(
    institucion: Institucion,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    esEditable: Boolean = true,
    primaryColor: Color = NeonPrimary
) {
    TarjetaPremium {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo Container
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CleanBackground,
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                     if (!institucion.logoInstitucion.isNullOrEmpty()) {
                        if (institucion.logoInstitucion!!.startsWith("http")) {
                             AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(institucion.logoInstitucion)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val bitmap = remember(institucion.logoInstitucion) {
                                try {
                                    val decodedString = Base64.decode(institucion.logoInstitucion, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Apartment, null, tint = primaryColor, modifier = Modifier.size(32.dp))
                            }
                        }
                    } else {
                         Icon(Icons.Default.Apartment, null, tint = primaryColor, modifier = Modifier.size(32.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = institucion.nombreInstitucion,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextoOscuroClean,
                modifier = Modifier.weight(1f)
            )

            if (esEditable) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = primaryColor)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF5350))
                }
            }
        }
    }
}
