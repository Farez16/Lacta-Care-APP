package com.example.lactacare.vistas.admin.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.vistas.theme.*

@Composable
fun PantallaPerfilAdmin(
    onLogout: () -> Unit,
    viewModel: AdminProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Efecto para navegar al login si se cierra sesión
    LaunchedEffect(uiState.sessionClosed) {
        if (uiState.sessionClosed) {
            onLogout()
        }
    }

    PantallaPremiumAdmin(
        titulo = "Mi Perfil"
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Icono de Perfil con Estilo Premium
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(140.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                         Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            tint = NeonPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Información
                Text(
                    text = uiState.nombre,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuroClean
                )
                
                Surface(
                    color = MintPastel,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                     Text(
                        text = uiState.rol,
                        fontSize = 14.sp,
                        color = DarkCharcoal,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                TarjetaPremium(titulo = "Información Personal") {
                        ItemInfoPerfil(
                            label = "Cédula",
                            valor = uiState.cedula,
                            icon = Icons.Outlined.Badge
                        )
                    }


                Spacer(modifier = Modifier.weight(1f))

                // Botón Cerrar Sesión Rojo pero estilo Píldora
                Button(
                    onClick = { viewModel.cerrarSesion() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("LactaCare v1.0.0", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ItemInfoPerfil(label: String, valor: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(NeonPrimary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = NeonPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(valor, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextoOscuroClean)
        }
    }
}
