package com.example.lactacare.datos.dto

import com.google.gson.annotations.SerializedName
data class VerifyCodeRequest(
    @SerializedName("correo")
    val correo: String,

    @SerializedName("codigo")
    val codigo: String
)
data class VerifyCodeResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("resetToken")
    val resetToken: String? = null
)
data class ResetPasswordWithCodeRequest(
    @SerializedName("resetToken")
    val resetToken: String,

    @SerializedName("newPassword")
    val newPassword: String
)
data class ApiResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String
)