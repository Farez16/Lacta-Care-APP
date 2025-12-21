package com.example.lactacare.vistas.auth
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.lactacare.dev.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

/**
 * Manager para manejar la autenticación con Google
 * ACTUALIZADO: Fuerza la selección de cuenta en cada login
 */
class GoogleAuthManager(private val context: Context) {

    companion object {
        private const val TAG = "GoogleAuthManager"
        const val RC_SIGN_IN = 9001
    }

    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    /**
     * Obtiene el Intent para iniciar el flujo de Google Sign-In
     * IMPORTANTE: Llama a signOutLocally() antes para limpiar la caché
     */
    fun getSignInIntent(): Intent {
        // Limpiar la sesión local para forzar selección de cuenta
        signOutLocally()
        return googleSignInClient.signInIntent
    }

    /**
     * Inicia el flujo de Google Sign-In
     * FUERZA la selección de cuenta limpiando la caché primero
     */
    fun signIn(activity: Activity) {
        signOutLocally()
        val signInIntent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    /**
     * Maneja el resultado del Intent de Google Sign-In
     */
    fun handleSignInResult(data: Intent?): GoogleSignInAccount? {
        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)

        return try {
            val account = task.getResult(ApiException::class.java)
            Log.d(TAG, "✅ Google Sign-In exitoso: ${account?.email}")
            Log.d(TAG, "ID Token: ${account?.idToken?.take(50)}...")
            account
        } catch (e: ApiException) {
            Log.e(TAG, "❌ Google Sign-In falló. Status code: ${e.statusCode}", e)
            handleSignInError(e.statusCode)
            null
        }
    }

    /**
     * Obtiene el ID Token del account
     */
    fun getIdToken(account: GoogleSignInAccount?): String? {
        return account?.idToken
    }

    /**
     * Verifica si hay un usuario ya autenticado con Google
     */
    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    /**
     * Cierra la sesión de Google Y limpia la caché local
     * Esto permite que el usuario elija cuenta en el próximo login
     */
    fun signOut(onComplete: () -> Unit = {}) {
        googleSignInClient.signOut()
            .addOnCompleteListener {
                Log.d(TAG, "Google Sign-Out completado")
                onComplete()
            }
    }

    /**
     * Limpia SOLO la caché local sin cerrar sesión en Google
     * Esto fuerza la selección de cuenta sin hacer logout completo
     */
    private fun signOutLocally() {
        try {
            // Esto limpia la caché local pero no cierra sesión en Google
            googleSignInClient.silentSignIn().addOnCompleteListener { }
            Log.d(TAG, "Caché local limpiada - Se forzará selección de cuenta")
        } catch (e: Exception) {
            Log.w(TAG, "Error al limpiar caché local: ${e.message}")
        }
    }

    /**
     * Revoca el acceso de Google (desvincula la cuenta completamente)
     */
    fun revokeAccess(onComplete: () -> Unit = {}) {
        googleSignInClient.revokeAccess()
            .addOnCompleteListener {
                Log.d(TAG, "Google Revoke Access completado")
                onComplete()
            }
    }

    /**
     * Maneja los errores de Google Sign-In
     */
    private fun handleSignInError(statusCode: Int) {
        when (statusCode) {
            12501 -> Log.e(TAG, "El usuario canceló el sign-in")
            12500 -> Log.e(TAG, "Error de conexión con Google Services")
            7 -> Log.e(TAG, "Error de red - Sin conexión a internet")
            10 -> Log.e(TAG, "Error de configuración - Verifica el SHA-1 y Web Client ID")
            else -> Log.e(TAG, "Error desconocido: $statusCode")
        }
    }

    /**
     * Verifica si Google Play Services está disponible
     */
    fun isGooglePlayServicesAvailable(): Boolean {
        val account = getLastSignedInAccount()
        return account != null
    }
}