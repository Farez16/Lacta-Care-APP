package com.example.lactacare.datos.dto
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileIncompleteData(
    val googleUserData: GoogleUserData,
    val googleToken: String? = null // Opcional, dependiendo de tu backend
) : Parcelable

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()

    // --- CORRECCIÓN CLAVE ---
    // Cambiamos 'object' por 'data class' para que pueda llevar los datos
    data class ProfileIncomplete(val data: ProfileIncompleteData) : AuthState()
}