package com.example.lactacare.vistas.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- COLORES PREMIUM NEON (High Voltage) ---
// Definidos ahora en Color.kt para acceso global y evitar conflictos:
// NeonPrimary, NeonSecondary, DarkCharcoal, CleanBackground, White
// MintPrimary, MintPastel aliases also handled via global colors or manual replacement below

// Alias local para este archivo si se requieren (opcional, pero mejor usar directos)
val MintPastel = NeonSecondary
val BackgroundPastel = CleanBackground
// TextoOscuroClean was alias to DarkCharcoal locally, but in Color.kt it is Gray. 
// We should use DarkCharcoal explicitly in this file for Premium look.

// --- TARJETAS ---
@Composable
fun TarjetaPremium(
    titulo: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp), 
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0)), // Borde muy sutil
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (titulo != null) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = OliveTextPrimary
                )
                Spacer(Modifier.height(12.dp))
            }
            content()
        }
    }
}

// --- BOTONES ---
@Composable
fun BotonPildora(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = NeonPrimary,
    contentColor: Color = DarkCharcoal // Texto oscuro sobre neón para legibilidad
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(50),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        ),
        modifier = modifier.height(48.dp)
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
fun BotonPildoraSecundario(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    BotonPildora(
        text = text,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        containerColor = NeonSecondary,
        contentColor = DarkCharcoal // Texto oscuro para contraste
    )
}

// --- SELECTORES ---
@Composable
fun BotonPildoraSeleccionable(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (seleccionado) OliveAdmin else Color.Transparent
    val contentColor = if (seleccionado) Color.White else OliveTextSecondary
    val borderStroke = if (!seleccionado) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)) else null

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = containerColor,
        border = borderStroke,
        contentColor = contentColor,
        modifier = modifier.height(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if(seleccionado) FontWeight.Bold else FontWeight.Medium),
                color = contentColor
            )
        }
    }
}

// --- CONTENEDOR DE PANTALLA ESTÁNDAR ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPremiumAdmin(
    titulo: String,
    onVolver: (() -> Unit)? = null,
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = CleanBackground,
        snackbarHost = snackbarHost,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(titulo, fontWeight = FontWeight.Bold, color = DarkCharcoal) },
                navigationIcon = {
                    if (onVolver != null) {
                        IconButton(onClick = onVolver) {
                            Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Volver", tint = DarkCharcoal)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CleanBackground)
            )
        },
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        content(padding)
    }
}

// --- CAMPOS DE TEXTO ---
@Composable
fun CampoTextoClean(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary) },
        leadingIcon = if (icon != null) { { Icon(icon, null, tint = NeonPrimary) } } else null,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonPrimary,
            unfocusedBorderColor = Color.LightGray,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor = NeonPrimary,
            focusedLabelColor = NeonPrimary
        ),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}
