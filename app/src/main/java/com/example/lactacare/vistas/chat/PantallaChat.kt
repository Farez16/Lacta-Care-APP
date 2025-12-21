package com.example.lactacare.vistas.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lactacare.datos.MockChatIARepository
// CORRECCIÓN: Importamos la entidad correcta 'ChatIA'
import com.example.lactacare.dominio.model.ChatIA
import com.example.lactacare.vistas.auth.BordeGrisClean
import com.example.lactacare.vistas.auth.TextoOscuroClean
import com.example.lactacare.vistas.theme.MomPrimary

@Composable
fun PantallaChat(
    // Inyectamos el Factory para usar el MockChatIARepository
    viewModel: ChatViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatViewModel(chatIARepository = MockChatIARepository()) as T
            }
        }
    )
) {
    var textoInput by remember { mutableStateOf("") }
    // Esta lista es de tipo 'ChatIA' y es reactiva
    val mensajes = viewModel.mensajes

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)) // background-light
    ) {
        // --- 1. CABECERA ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Tu Asistente de Lactancia",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuroClean
            )
        }
        Divider(color = BordeGrisClean)

        // --- 2. LISTA DE MENSAJES ---
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ilustración Inicial (Solo si hay pocos mensajes)
            if (mensajes.size <= 1) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.SmartToy, null, tint = MomPrimary, modifier = Modifier.size(80.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "¡Hola! Estoy aquí para ayudarte.",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoOscuroClean
                        )
                        Text(
                            "Pregúntame sobre técnicas, horarios o dudas.",
                            fontSize = 14.sp,
                            color = TextoOscuroClean.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            items(mensajes) { msg ->
                BurbujaMensaje(msg)
            }
        }

        // --- 3. SUGERENCIAS Y INPUT ---
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            // Mostramos sugerencias solo si hay poco historial
            if (mensajes.size < 4) {
                Text("Sugerencias", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextoOscuroClean)
                Spacer(Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val suggestions = listOf("Aumentar producción", "Extraer leche", "Almacenamiento")
                    items(suggestions) { text ->
                        SuggestionChip(
                            onClick = { viewModel.enviarMensaje(text) },
                            label = { Text(text, color = TextoOscuroClean, fontSize = 12.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MomPrimary.copy(alpha = 0.2f), labelColor = TextoOscuroClean),
                            border = null
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Campo de Texto
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = textoInput,
                    onValueChange = { textoInput = it },
                    placeholder = { Text("Escribe tu pregunta...", fontSize = 14.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3F4F6),
                        unfocusedContainerColor = Color(0xFFF3F4F6),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    maxLines = 3
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        viewModel.enviarMensaje(textoInput)
                        textoInput = ""
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MomPrimary, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White)
                }
            }
        }
    }
}

// CORRECCIÓN: Recibe 'ChatIA' en lugar de 'MensajeChat'
@Composable
fun BurbujaMensaje(msg: ChatIA) {
    val alignment = if (msg.esUsuario) Alignment.End else Alignment.Start
    val colorBurbuja = if (msg.esUsuario) MomPrimary else Color.White
    val colorTexto = if (msg.esUsuario) Color.White else TextoOscuroClean
    val shape = if (msg.esUsuario) RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp) else RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Text(
            text = if (msg.esUsuario) "Tú" else "Asistente AI",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextoOscuroClean.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Surface(
            color = colorBurbuja,
            shape = shape,
            shadowElevation = 1.dp
        ) {
            if (msg.esAnimacionEscribiendo) {
                Text("Escribiendo...", modifier = Modifier.padding(12.dp), fontSize = 14.sp, color = colorTexto, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            } else {
                Text(
                    text = msg.texto,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 15.sp,
                    color = colorTexto,
                    lineHeight = 20.sp
                )
            }
        }
    }
}