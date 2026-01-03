package com.example.lactacare.dominio.repository

interface ChatRepository {
    suspend fun preguntarChatbot(
        pregunta: String,
        latitud: Double? = null,
        longitud: Double? = null
    ): Result<String>
}