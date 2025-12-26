package com.example.lactacare.datos.network

import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class ApiResponseHandler @Inject constructor() {

    suspend fun <T> handleSuccess(
        response: Response<T>,
        onSuccess: suspend (T) -> Boolean
    ): Result<Boolean> {
        return if (response.isSuccessful && response.body() != null) {
            onSuccess(response.body()!!)
            Result.success(true)
        } else {
            // Aquí capturamos el mensaje de error de tu backend Spring Boot
            val errorMsg = parseErrorBody(response)
            Result.failure(Exception(errorMsg))
        }
    }

    fun parseErrorBody(response: Response<*>): String {
        return try {
            val errorJson = response.errorBody()?.string()
            if (errorJson != null) {
                // Tu backend devuelve algo como: { "message": "Usuario no registrado...", "code": "USUARIO_NO_REGISTRADO" }
                val jsonObject = JSONObject(errorJson)
                // Priorizamos leer el campo "message" de tu JSON
                jsonObject.optString("message", "Error desconocido del servidor")
            } else {
                "Error en la respuesta del servidor"
            }
        } catch (e: Exception) {
            "Error al leer respuesta: ${e.message}"
        }
    }
}