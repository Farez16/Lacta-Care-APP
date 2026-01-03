package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

data class SugerenciaDto(
    @SerializedName("idSugerencias") val id: Int? = null,
    @SerializedName("tituloSugerencias") val titulo: String,
    @SerializedName("detalleSugerencias") val detalle: String, // Corrected from "detalle Sugerencias"
    @SerializedName("linkImagen") val imagenUrl: String?      // Corrected from "link_Imagen"
)
