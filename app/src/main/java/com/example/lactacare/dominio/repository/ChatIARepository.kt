package com.example.lactacare.dominio.repository

import com.example.lactacare.dominio.model.ChatIA

interface ChatIARepository {
    // La función se debe llamar 'enviarMensaje' para coincidir con tu ViewModel y el error
    suspend fun enviarMensaje(mensaje: String): ChatIA
}