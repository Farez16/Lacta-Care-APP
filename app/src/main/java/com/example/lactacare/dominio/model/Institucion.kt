package com.example.lactacare.dominio.model

import com.google.gson.annotations.SerializedName

data class Institucion(
    @SerializedName("idInstitucion") val idInstitucion: Long? = null,
    @SerializedName("nombreInstitucion") val nombreInstitucion: String,
    @SerializedName("logoInstitucion") val logoInstitucion: String? = null // Base64 string
)