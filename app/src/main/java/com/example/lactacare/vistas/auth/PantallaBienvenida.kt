package com.example.lactacare.vistas.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lactacare.dominio.model.RolUsuario

// --- COLORES MINIMALISTAS ---
val BackgroundMinimal = Color(0xFFFEFEFE) // Casi blanco absoluto
val TextDark = Color(0xFF2D3436)
val TextGray = Color(0xFF636E72)

// Acentos Suaves
val SoftPink = Color(0xFFFCE4EC)  // Fondo Icono Paciente
val PinkAccent = Color(0xFFE91E63) // Icono Paciente

val SoftBlue = Color(0xFFE3F2FD)  // Fondo Icono Medico
val BlueAccent = Color(0xFF2196F3) // Icono Medico

val SoftGreen = Color(0xFFE8F5E9) // Fondo Icono Admin
val GreenAccent = Color(0xFF4CAF50) // Icono Admin

@Composable
fun PantallaBienvenida(
    onRolSeleccionado: (RolUsuario) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundMinimal)
            .verticalScroll(rememberScrollState())
            .padding(24.dp), // Margen global consistente
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        // 1. LOGO & HEADER
        Image(
            painter = painterResource(id = com.example.lactacare.R.drawable.app_logo),
            contentDescription = "LactaCare Logo",
            modifier = Modifier.size(160.dp), // Tamaño equilibrado
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "LactaCare",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Equilibrando trabajo, estudios y\nmaternidad con apoyo a tu alcance.",
            fontSize = 16.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 2. SELECCIÓN DE ROL (GRID VERTICAL)
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TarjetaRolMinimal(
                titulo = "Paciente",
                subtitulo = "Madres y usuarias",
                icono = Icons.Outlined.ChildCare,
                colorFondoIcono = SoftPink,
                colorIcono = PinkAccent,
                onClick = { onRolSeleccionado(RolUsuario.PACIENTE) }
            )

            TarjetaRolMinimal(
                titulo = "Médico",
                subtitulo = "Personal de salud",
                icono = Icons.Outlined.HealthAndSafety,
                colorFondoIcono = SoftBlue,
                colorIcono = BlueAccent,
                onClick = { onRolSeleccionado(RolUsuario.MEDICO) }
            )

            TarjetaRolMinimal(
                titulo = "Administrador",
                subtitulo = "Gestión del sistema",
                icono = Icons.Outlined.Apartment,
                colorFondoIcono = SoftGreen,
                colorIcono = GreenAccent,
                onClick = { onRolSeleccionado(RolUsuario.ADMINISTRADOR) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 3. FOOTER
        Text(
            text = "© 2025 LactaCare",
            fontSize = 12.sp,
            color = Color.LightGray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun TarjetaRolMinimal(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    colorFondoIcono: Color,
    colorIcono: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F2F6)), // Borde muy sutil
        elevation = CardDefaults.cardElevation(0.dp), // FLAT DESIGN
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono Circular
            Surface(
                shape = CircleShape,
                color = colorFondoIcono,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = colorIcono,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Textos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = subtitulo,
                    fontSize = 13.sp,
                    color = TextGray
                )
            }

            // Chevron sutil
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFDFE6E9),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewBienvenidaMinimal() {
    PantallaBienvenida(onRolSeleccionado = {})
}