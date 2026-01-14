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
        val KEY_EMAIL = stringPreferencesKey("user_email") // <--- NUEVO
        val KEY_ROLE = stringPreferencesKey("user_role") // "PACIENTE" o "EMPLEADO"
        val KEY_PROFILE_COMPLETED = booleanPreferencesKey("profile_completed")
        val KEY_SALA_ID = intPreferencesKey("user_sala_id") // <--- NUEVO
        
        // --- DATOS DEL BEBÉ (Local Persistence) ---
        val KEY_BABY_NAME = stringPreferencesKey("baby_name")
        val KEY_BABY_DOB = stringPreferencesKey("baby_dob")
        val KEY_BABY_GENDER = stringPreferencesKey("baby_gender")
    }

    // Guardar todos los datos del usuario al hacer login
    suspend fun saveAuthData(token: String, id: Long, name: String, email: String, role: String, completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_USER_ID] = id
            prefs[KEY_FULL_NAME] = name
            prefs[KEY_EMAIL] = email
            prefs[KEY_ROLE] = role
            prefs[KEY_PROFILE_COMPLETED] = completed
        }
    }

    // Obtener Token
    val authToken: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }

    // Obtener Rol
    val userRole: Flow<String?> = context.dataStore.data.map { it[KEY_ROLE] }

    // --- AGREGADO: Obtener Nombre y ID ---
    val userName: Flow<String?> = context.dataStore.data.map { it[KEY_FULL_NAME] }
    val userEmail: Flow<String?> = context.dataStore.data.map { it[KEY_EMAIL] } // <--- NUEVO
    val userId: Flow<Long?> = context.dataStore.data.map { it[KEY_USER_ID] }
    val userSalaId: Flow<Int?> = context.dataStore.data.map { it[KEY_SALA_ID] } // <--- NUEVO
    // -----------------------------------------------------------------------

    // Datos del Bebé
    val babyName: Flow<String?> = context.dataStore.data.map { it[KEY_BABY_NAME] }
    
    suspend fun saveBabyData(name: String, dob: String, gender: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BABY_NAME] = name
            prefs[KEY_BABY_DOB] = dob
            prefs[KEY_BABY_GENDER] = gender
        }
    }

    suspend fun saveSalaId(salaId: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SALA_ID] = salaId
        }
    }

    // Obtener si el perfil está completo
    val isProfileCompleted: Flow<Boolean> = context.dataStore.data.map { it[KEY_PROFILE_COMPLETED] ?: false }

    // Cerrar Sesión
    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}