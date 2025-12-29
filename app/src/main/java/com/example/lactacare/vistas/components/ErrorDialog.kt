package com.example.lactacare.vistas.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lactacare.vistas.theme.SlateGray

/**
 * Sistema Unificado de Diálogos de Error
 * Componente reutilizable para mostrar errores de forma consistente en toda la app
 */
enum class ErrorType {
    ROL_MISMATCH,           // Usuario con rol incorrecto
    UNAUTHORIZED_EMAIL,      // Correo no autorizado (empleados)
    USER_NOT_FOUND,         // Usuario no existe
    INVALID_CREDENTIALS,    // Contraseña incorrecta
    GOOGLE_ACCOUNT,    // Cuenta de google
    GENERIC                 // Error genérico
}
data class ErrorDialogData(
    val tipo: ErrorType,
    val titulo: String,
    val mensaje: String,
    val rolCorrecto: String? = null  // Solo para ROL_MISMATCH
)
@Composable
fun ErrorDialog(
    data: ErrorDialogData,
    onDismiss: () -> Unit
) {
    // Configuración visual según el tipo de error
    val config = when (data.tipo) {
        ErrorType.ROL_MISMATCH -> DialogConfig(
            icono = Icons.Outlined.Warning,
            colorIcono = Color(0xFFFF9800),      // Naranja
            colorFondo = Color(0xFFFFF3E0),      // Amarillo claro
            colorTexto = Color(0xFFE65100)       // Naranja oscuro
        )
        ErrorType.UNAUTHORIZED_EMAIL -> DialogConfig(
            icono = Icons.Outlined.Lock,
            colorIcono = Color(0xFFF44336),      // Rojo
            colorFondo = Color(0xFFFFEBEE),      // Rojo claro
            colorTexto = Color(0xFFC62828)       // Rojo oscuro
        )
        ErrorType.USER_NOT_FOUND -> DialogConfig(
            icono = Icons.Outlined.PersonOff,
            colorIcono = Color(0xFF2196F3),      // Azul
            colorFondo = Color(0xFFE3F2FD),      // Azul claro
            colorTexto = Color(0xFF1565C0)       // Azul oscuro
        )
        ErrorType.INVALID_CREDENTIALS -> DialogConfig(
            icono = Icons.Outlined.Key,
            colorIcono = Color(0xFFFF5722),      // Naranja oscuro
            colorFondo = Color(0xFFFBE9E7),      // Naranja claro
            colorTexto = Color(0xFFD84315)       // Naranja muy oscuro
        )
        ErrorType.GOOGLE_ACCOUNT -> DialogConfig(  // ⭐ NUEVO
            icono = Icons.Outlined.AccountCircle,
            colorIcono = Color(0xFF4285F4),      // Azul Google
            colorFondo = Color(0xFFE8F0FE),      // Azul claro Google
            colorTexto = Color(0xFF1967D2)       // Azul oscuro Google
        )
        ErrorType.GENERIC -> DialogConfig(
            icono = Icons.Outlined.Error,
            colorIcono = Color(0xFF9E9E9E),      // Gris
            colorFondo = Color(0xFFF5F5F5),      // Gris claro
            colorTexto = Color(0xFF424242)       // Gris oscuro
        )
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = config.icono,
                contentDescription = null,
                tint = config.colorIcono,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = data.titulo,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column {
                Text(
                    text = data.mensaje,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                // Tarjeta especial para ROL_MISMATCH
                if (data.tipo == ErrorType.ROL_MISMATCH && data.rolCorrecto != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = config.colorFondo
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = config.colorIcono,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Tu rol correcto es: ${data.rolCorrecto}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = config.colorTexto
                            )
                        }
                    }
                }

                // ⭐ NUEVO: Tarjeta especial para GOOGLE_ACCOUNT
                if (data.tipo == ErrorType.GOOGLE_ACCOUNT) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = config.colorFondo
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = config.colorIcono,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Usa el botón 'Continuar con Google' para iniciar sesión",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = config.colorTexto,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)  // Verde
                )
            ) {
                Text("Entendido")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = SlateGray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
/**
 * Configuración visual de cada tipo de diálogo
 */
data class DialogConfig(
    val icono: ImageVector,
    val colorIcono: Color,
    val colorFondo: Color,
    val colorTexto: Color
)