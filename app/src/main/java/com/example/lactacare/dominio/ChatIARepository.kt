package com.example.lactacare.dominio

interface ChatIARepository {
    // La función se debe llamar 'enviarMensaje' para coincidir con tu ViewModel y el error
    suspend fun enviarMensaje(mensaje: String): ChatIA
}