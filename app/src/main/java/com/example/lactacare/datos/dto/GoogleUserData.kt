package com.example.lactacare.datos.dto

data class GoogleUserData(
    val googleId: String,
    val email: String,
    val name: String,
    val givenName: String?,  // Nombre (Juan)
    val familyName: String?, // Apellido (Perez)
    val picture: String?     // URL de la foto
)