package com.example.lactacare.datos.dto
import com.google.gson.annotations.SerializedName

// Respuesta de GET /api/user/me
data class UserProfileDto(
    @SerializedName("imagen") val imagen: String?,
    @SerializedName("primerNombre") val primerNombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("cedula") val cedula: String,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String,
    @SerializedName("rol") val rol: String
)

// Petición para PUT /api/user/profile
data class UpdateProfileRequest(
    @SerializedName("primerNombre") val primerNombre: String?,
    @SerializedName("imagenPerfil") val imagenPerfil: String? // Base64 string
)
