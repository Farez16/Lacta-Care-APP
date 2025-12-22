package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName

data class GoogleLoginRequest(
    @SerializedName("idToken") val idToken: String,
    @SerializedName("platform") val platform: String = "ANDROID" // Agrega este valor por defecto
)
