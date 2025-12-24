package com.example.lactacare.vistas.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MensajeChat(
    val texto: String,
    val esUsuario: Boolean,
    val esAnimacionEscribiendo: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {

    // Lista observable de mensajes
    val mensajes = mutableStateListOf<MensajeChat>()

    init {
        // Mensaje de bienvenida
        mensajes.add(MensajeChat("¡Hola! Soy tu asistente de lactancia. ¿Cómo puedo ayudarte hoy?", false))
    }

    fun enviarMensaje(texto: String) {
        if (texto.isBlank()) return

        // 1. Agregar mensaje del usuario
        mensajes.add(MensajeChat(texto, true))

        // 2. Simular "Escribiendo..."
        viewModelScope.launch {
            val loadingMsg = MensajeChat("", false, true)
            mensajes.add(loadingMsg)
            
            delay(1500) // Simular red
            
            mensajes.remove(loadingMsg)

            // 3. Respuesta Mock Inteligente
            val respuesta = generarRespuestaMock(texto)
            mensajes.add(MensajeChat(respuesta, false))
        }
        //hola
    }

    private fun generarRespuestaMock(input: String): String {
        val q = input.lowercase()
        return when {
            q.contains("Dolor") -> "Si tienes dolor al amamantar, puede deberse a un mal agarre. Intenta asegurar que la boca del bebé cubra gran parte de la areola, no solo el pezón."
            q.contains("produc") || q.contains("leche") -> "Para aumentar la producción, lo más efectivo es la succión frecuente. Intenta amamantar a demanda y mantente bien hidratada."
            q.contains("guardar") || q.contains("congelar") -> "La leche materna dura: \n- 4 horas a temperatura ambiente.\n- 4 días en el refrigerador.\n- 6 meses en el congelador."
            q.contains("horario") -> "Se recomienda evitar horarios estrictos al inicio y amamantar a demanda, cada vez que el bebé muestre señales de hambre."
            else -> "Entiendo tu consulta. Como soy una IA en entrenamiento, te sugiero consultar la sección 'Informativo' o acudir a un especialista si es urgente."
        }
    }
    ///qaaa
    //aaaaa
    //aaa
    //aaaa
    ///aaaaaaa
}
