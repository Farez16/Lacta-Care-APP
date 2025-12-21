package com.example.lactacare.datos.datos.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.lactacare.datos.dto.AuthResponse
import com.example.lactacare.datos.dto.UserSession
import com.example.lactacare.dominio.model.RolUsuario
import com.google.gson.Gson

/**
 * Gestor de sesión de usuario
 * Guarda y recupera la sesión del usuario de forma segura
 */
class SessionManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "lactacare_session"
        private const val KEY_USER_SESSION = "user_session"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback a SharedPreferences normal si EncryptedSharedPreferences falla
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val gson = Gson()

    /**
     * Guarda la respuesta de autenticación y crea una sesión
     */
    fun saveUserSession(authResponse: AuthResponse) {
        val userInfo = authResponse.userInfo

        val session = UserSession(
            userId = userInfo.id,
            correo = userInfo.correo,
            nombreCompleto = userInfo.nombreCompleto,
            rol = RolUsuario.Companion.fromString(userInfo.rol),
            accessToken = authResponse.accessToken,
            refreshToken = authResponse.refreshToken,
            tokenExpiration = System.currentTimeMillis() + (authResponse.expiresIn * 1000),
            imagenPerfil = userInfo.imagenPerfil
        )

        val sessionJson = gson.toJson(session)
        sharedPreferences.edit().putString(KEY_USER_SESSION, sessionJson).apply()
    }

    /**
     * Obtiene la sesión actual del usuario
     */
    fun getUserSession(): UserSession? {
        val sessionJson = sharedPreferences.getString(KEY_USER_SESSION, null) ?: return null

        return try {
            val session = gson.fromJson(sessionJson, UserSession::class.java)

            // Verificar si el token ha expirado
            if (session.isTokenExpired()) {
                clearSession()
                null
            } else {
                session
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Limpia la sesión del usuario (logout)
     */
    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }

    /**
     * Actualiza el access token (útil para refresh token)
     */
    fun updateAccessToken(newToken: String, expiresIn: Long) {
        val session = getUserSession() ?: return

        val updatedSession = session.copy(
            accessToken = newToken,
            tokenExpiration = System.currentTimeMillis() + (expiresIn * 1000)
        )

        val sessionJson = gson.toJson(updatedSession)
        sharedPreferences.edit().putString(KEY_USER_SESSION, sessionJson).apply()
    }

    /**
     * Verifica si existe una sesión válida
     */
    fun hasValidSession(): Boolean {
        return getUserSession() != null
    }

    /**
     * Obtiene el ID del usuario actual
     */
    fun getUserId(): Long? {
        return getUserSession()?.userId
    }

    /**
     * Obtiene el rol del usuario actual
     */
    fun getUserRole(): RolUsuario? {
        return getUserSession()?.rol
    }

    /**
     * Obtiene el access token actual
     */
    fun getAccessToken(): String? {
        return getUserSession()?.accessToken
    }
}