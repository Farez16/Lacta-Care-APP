package com.example.lactacare.datos.dto

// Asegúrate de que GoogleUserData existe en el mismo paquete,
// o impórtalo si está en otro lado.

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()

    // Este es vital para tu flujo de Google
    object ProfileIncomplete : AuthState()
}

// Esta clase sirve para pasar los datos de Google a la pantalla de completar perfil
data class ProfileIncompleteData(
    val googleUserData: GoogleUserData,
    val googleToken: String
)