package com.example.lactacare.datos.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

// Creamos la extensión una sola vez fuera de la clase
private val Context.dataStore by preferencesDataStore("lactacare_session")

class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_TOKEN = stringPreferencesKey("auth_token")
        val KEY_USER_ID = longPreferencesKey("user_id")
        val KEY_FULL_NAME = stringPreferencesKey("user_fullname")
        val KEY_ROLE = stringPreferencesKey("user_role") // "PACIENTE" o "EMPLEADO"
        val KEY_PROFILE_COMPLETED = booleanPreferencesKey("profile_completed")
    }

    // Guardar todos los datos del usuario al hacer login
    suspend fun saveAuthData(token: String, id: Long, name: String, role: String, completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_USER_ID] = id
            prefs[KEY_FULL_NAME] = name
            prefs[KEY_ROLE] = role
            prefs[KEY_PROFILE_COMPLETED] = completed
        }
    }

    // Obtener Token
    val authToken: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }

    // Obtener Rol
    val userRole: Flow<String?> = context.dataStore.data.map { it[KEY_ROLE] }

    // --- AGREGADO: Obtener Nombre (Para que el HomeViewModel pueda leerlo) ---
    val userName: Flow<String?> = context.dataStore.data.map { it[KEY_FULL_NAME] }
    // -----------------------------------------------------------------------

    // Obtener si el perfil está completo
    val isProfileCompleted: Flow<Boolean> = context.dataStore.data.map { it[KEY_PROFILE_COMPLETED] ?: false }

    // Cerrar Sesión
    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}