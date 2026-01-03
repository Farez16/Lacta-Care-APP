package com.example.lactacare.vistas.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.location.LocationHelper
import com.example.lactacare.dominio.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MensajeChat(
    val texto: String,
    val esUsuario: Boolean,
    val esAnimacionEscribiendo: Boolean = false
)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val locationHelper: LocationHelper
) : ViewModel() {
    val mensajes = mutableStateListOf<MensajeChat>()

    private val _usarUbicacion = MutableStateFlow(false)
    val usarUbicacion = _usarUbicacion.asStateFlow()
    init {
        mensajes.add(MensajeChat("¡Hola! Soy LactaBot. ¿Cómo puedo ayudarte hoy?", false))
    }
    fun toggleUbicacion() {
        _usarUbicacion.value = !_usarUbicacion.value
    }
    fun enviarMensaje(texto: String) {
        if (texto.isBlank()) return
        // 1. Agregar mensaje del usuario
        mensajes.add(MensajeChat(texto, true))
        // 2. Mostrar "Escribiendo..."
        viewModelScope.launch {
            val loadingMsg = MensajeChat("", false, true)
            mensajes.add(loadingMsg)

            // 3. Obtener ubicación si está activada
            var latitud: Double? = null
            var longitud: Double? = null

            if (_usarUbicacion.value && locationHelper.hasLocationPermission()) {
                val coords = locationHelper.getCoordinates()
                latitud = coords?.first
                longitud = coords?.second

                // LOG para debug
                println("📍 Enviando ubicación: lat=$latitud, lon=$longitud")
            }

            // 4. Llamar al backend
            val result = chatRepository.preguntarChatbot(texto, latitud, longitud)

            mensajes.remove(loadingMsg)

            result.onSuccess { respuesta ->
                mensajes.add(MensajeChat(respuesta, false))
            }.onFailure { error ->
                mensajes.add(MensajeChat(
                    "Lo siento, hubo un error: ${error.message}",
                    false
                ))
            }
        }
    }

    fun hasLocationPermission(): Boolean {
        return locationHelper.hasLocationPermission()
    }

    /**
     * Limpia el chat y reinicia con mensaje de bienvenida
     */
    fun limpiarChat() {
        mensajes.clear()
        mensajes.add(MensajeChat("¡Hola! Soy LactaBot. ¿Cómo puedo ayudarte hoy?", false))
        _usarUbicacion.value = false
    }
}
