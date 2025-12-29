package com.example.lactacare.vistas.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Apartment // Para Admin (Domain)
import androidx.compose.material.icons.outlined.ChildCare // Para Usuario
import androidx.compose.material.icons.outlined.HealthAndSafety // Para Doctor
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lactacare.dominio.model.RolUsuario

// --- COLORES EXTRAÍDOS DE TU HTML ---

val FondoTarjeta = Color(0xFFF9F6F2)  // Igual al fondo o un poco más claro
val BeigeClaro = Color(0xFFEFEBE4)    // bg-beige-light (Fondo de los iconos)
val TextoPrimario = Color(0xFF4A4A4A) // text-primary
val TextoSecundario = Color(0xFF7D7D7D)

// Colores de Acento (Iconos)
val AccentAdmin = Color(0xFFA3C9A8)   // Verde pastel
val AccentDoctor = Color(0xFFB0C4DE)  // Azul acero claro
val AccentUser = Color(0xFFF4C2C2)    // Rosa pastel

@Composable
fun PantallaBienvenida(
    // Este evento nos devolverá QUÉ rol eligió el usuario
    onRolSeleccionado: (RolUsuario) -> Unit
) {
    // Contenedor Principal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        // 1. SECCIÓN SUPERIOR (HEADER)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "LactaCare",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextoPrimario
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ILUSTRACIÓN (Placeholder)
            // Cuando tengas tu imagen: Image(painter = painterResource(id = R.drawable.tu_imagen)...)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color(0xFFFFF0E0), shape = RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Icono temporal
                Icon(
                    imageVector = Icons.Outlined.ChildCare,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = Color(0xFFFFA726)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Equilibrando trabajo, estudios y\nmaternidad con apoyo a tu alcance.",
                fontSize = 16.sp,
                color = TextoSecundario,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }

        // 2. LISTA DE TARJETAS (ROLES)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // Espacio entre tarjetas
        ) {
            // Tarjeta Paciente
            TarjetaRol(
                titulo = "Paciente",
                icono = Icons.Outlined.ChildCare,
                colorIcono = AccentUser,
                onClick = { onRolSeleccionado(RolUsuario.PACIENTE) }
            )


            // Tarjeta DOCTOR
            TarjetaRol(
                titulo = "Medico",
                icono = Icons.Outlined.HealthAndSafety,
                colorIcono = AccentDoctor,
                onClick = { onRolSeleccionado(RolUsuario.MEDICO) }
            )

            // Tarjeta ADMINISTRADOR
            TarjetaRol(
                titulo = "Administrador",
                icono = Icons.Outlined.Apartment, // Icono similar a "domain"
                colorIcono = AccentAdmin,
                onClick = { onRolSeleccionado(RolUsuario.ADMINISTRADOR) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 3. FOOTER
        Text(
            text = "©2025 | LactaCare.",
            fontSize = 12.sp,
            color = TextoSecundario,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp)
        )
    }
}

// --- COMPONENTE REUTILIZABLE: TARJETA DE ROL ---
@Composable
fun TarjetaRol(
    titulo: String,
    icono: ImageVector,
    colorIcono: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
        shape = RoundedCornerShape(24.dp),
        // Simulamos la sombra "neumorphic" suave del HTML con una elevación ligera y borde sutil
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono con fondo cuadrado redondeado
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(BeigeClaro, shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = colorIcono,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Texto
            Text(
                text = titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextoPrimario,
                modifier = Modifier.weight(1f)
            )

            // Flechita
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewBienvenida() {
    PantallaBienvenida(onRolSeleccionado = {})
}