package com.example.lactacare.dominio.model

data class ChatIA(
    val id: String,
    val texto: String,
    val esUsuario: Boolean, // true = Usuario, false = Bot
    val esAnimacionEscribiendo: Boolean = false
)