package com.example.lactacare.vistas.auth

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class GoogleAuthManager(private val context: Context) {

    // Configura el cliente de Google
    fun getSignInIntent(): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("TU_CLIENT_ID_DE_GOOGLE_CLOUD.apps.googleusercontent.com") // <--- PON TU CLIENT ID AQUÍ
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        return googleSignInClient.signInIntent
    }

    // Procesa el resultado cuando el usuario vuelve de elegir su cuenta
    fun handleSignInResult(task: Task<GoogleSignInAccount>): String? {
        return try {
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            // Retornamos el ID Token para enviarlo a TU backend Spring Boot
            account.idToken
        } catch (e: ApiException) {
            e.printStackTrace()
            null
        }
    }
}