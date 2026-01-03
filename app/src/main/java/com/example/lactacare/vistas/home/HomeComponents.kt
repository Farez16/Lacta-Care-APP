package com.example.lactacare.vistas.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lactacare.vistas.theme.TextoOscuroClean
import com.example.lactacare.vistas.navegacion.ItemMenu

@Composable
fun TopBarHome(
    saludo: String, 
    colorIcono: Color,
    onMenuClick: () -> Unit = {} // Nuevo callback
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Botón Hamburguesa (Nuevo)
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menú", tint = TextoOscuroClean)
            }
            Spacer(modifier = Modifier.width(8.dp))
            
            Box(
                modifier = Modifier
                    .size(40.dp) // Ligeramente más pequeño para ajustar espacio
                    .clip(CircleShape)
                    .background(Color.LightGray) // Placeholder foto
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Hola,", fontSize = 14.sp, color = Color.Gray)
                Text(
                    text = saludo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuroClean
                )
            }
        }

        IconButton(
            onClick = { },
            modifier = Modifier
                .size(42.dp)
                .shadow(2.dp, CircleShape)
                .background(Color.White, CircleShape)
        ) {
            Icon(Icons.Outlined.Notifications, contentDescription = "Alertas", tint = colorIcono)
        }
    }
}

@Composable
fun BottomNavBarFlotante(
    items: List<ItemMenu>,
    rutaActual: String?,
    colorActivo: Color,
    onItemClick: (ItemMenu) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(10.dp, RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(50))
            .height(65.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val seleccionado = rutaActual == item.ruta
                val colorIcono = if (seleccionado) colorActivo else Color.Gray.copy(alpha = 0.5f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onItemClick(item) }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = item.icono,
                        contentDescription = item.titulo,
                        tint = colorIcono,
                        modifier = Modifier.size(if (seleccionado) 30.dp else 26.dp)
                    )
                    if (seleccionado) {
                        Text(
                            text = item.titulo,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorIcono
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardStatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextoOscuroClean)
            Text(title, fontSize = 12.sp, color = TextoOscuroClean.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun PantallaEnConstruccion(titulo: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.Construction, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Sección $titulo", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoOscuroClean)
        Text("Próximamente disponible", fontSize = 14.sp, color = Color.Gray)
    }
}