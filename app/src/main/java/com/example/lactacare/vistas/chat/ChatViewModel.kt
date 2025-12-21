package com.example.lactacare.vistas.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.dominio.model.ChatIA
import com.example.lactacare.dominio.repository.ChatIARepository
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val chatIARepository: ChatIARepository
) : ViewModel() {

    // Ahora la lista es de tipo ChatIA
    val mensajes = mutableStateListOf<ChatIA>()

    init {
        mensajes.add(
            ChatIA("0", "¡Hola! Soy tu asistente de Lactancia.", false)
        )
    }

    fun enviarMensaje(texto: String) {
        if (texto.isBlank()) return

        val mensajeUsuario = ChatIA(
            id = UUID.randomUUID().toString(),
            texto = texto,
            esUsuario = true
        )
        mensajes.add(mensajeUsuario)

        // Loading
        val loadingMsg = ChatIA("load", "", false, true)
        mensajes.add(loadingMsg)

        viewModelScope.launch {
            try {
                // Ahora llamamos a enviarMensaje (que devuelve un ChatIA)
                val respuesta = chatIARepository.enviarMensaje(texto)

                mensajes.remove(loadingMsg)
                mensajes.add(respuesta)
            } catch (e: Exception) {
                mensajes.remove(loadingMsg)
            }
        }
    }
}