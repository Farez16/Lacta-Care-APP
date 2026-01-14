package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

data class RolDto(
    @SerializedName("idRoles") val id: Int,
    @SerializedName("nombreRol") val nombre: String,
    @SerializedName("descripcion") val descripcion: String? = null
)
