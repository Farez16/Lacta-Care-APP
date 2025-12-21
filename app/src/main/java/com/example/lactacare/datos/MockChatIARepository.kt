package com.example.lactacare.datos

import com.example.lactacare.dominio.model.ChatIA
import com.example.lactacare.dominio.repository.ChatIARepository
import kotlinx.coroutines.delay
import java.util.UUID

class MockChatIARepository : ChatIARepository {

    // CORRECCIÓN: Usamos 'override suspend fun enviarMensaje'
    override suspend fun enviarMensaje(mensaje: String): ChatIA {
        delay(2000) // Simula pensar

        val respuestaTexto = when {
            mensaje.contains("hola", ignoreCase = true) ->
                "¡Hola! Soy tu asistente IA de LactaCare. ¿En qué te ayudo?"
            mensaje.contains("dolor", ignoreCase = true) ->
                "El dolor suele deberse a un mal agarre. Asegúrate que el bebé cubra la areola."
            else ->
                "Entiendo. ¿Puedes darme más detalles?"
        }

        return ChatIA(
            id = UUID.randomUUID().toString(),
            texto = respuestaTexto,
            esUsuario = false
        )
    }
}