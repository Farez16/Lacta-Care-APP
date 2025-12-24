package com.example.lactacare.vistas.admin.inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.datos.dto.ContenedorLecheDto
import com.example.lactacare.vistas.theme.AdminPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInventario(
    viewModel: InventarioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.cargarInventario() },
                containerColor = AdminPrimary
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Recargar", tint = Color.White)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Inventario de Leche",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AdminPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AdminPrimary)
                    }
                } else if (uiState.error != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = uiState.error!!, color = Color.Red)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.contenedores) { contenedor ->
                            ItemContenedor(contenedor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemContenedor(contenedor: ContenedorLecheDto) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Frasco #${contenedor.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Badge(
                    containerColor = if (contenedor.estado == "Disponible") Color(0xFF4CAF50) else Color.Gray,
                    contentColor = Color.White
                ) {
                    Text(contenedor.estado ?: "Desconocido", modifier = Modifier.padding(4.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Volumen: ${contenedor.cantidadMililitros ?: 0} ml", color = Color.Gray)
                Text(
                    text = "Caduca: ${contenedor.fechaHoraCaducidad?.take(10) ?: "--"}",
                    color = Color.Red,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
