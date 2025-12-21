package com.example.lactacare.datos.network

import com.example.lactacare.datos.dto.ErrorResponse
import com.example.lactacare.datos.dto.Resource
import com.google.gson.Gson
import retrofit2.Response

/**
 * Clase para manejar respuestas de la API
 */
object ApiResponseHandler {

    /**
     * Maneja respuestas exitosas (200-299)
     */
    fun <T> handleSuccess(response: Response<T>): Resource<T> {
        return if (response.isSuccessful && response.body() != null) {
            Resource.Success(response.body()!!)
        } else {
            Resource.Error("Respuesta vacía del servidor")
        }
    }

    /**
     * Maneja errores de la API
     */
    fun <T> handleError(response: Response<T>): Resource<T> {
        val errorBody = response.errorBody()?.string()

        return try {
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            Resource.Error(
                message = errorResponse.message,
                code = errorResponse.status
            )
        } catch (e: Exception) {
            Resource.Error(
                message = "Error del servidor: ${response.code()}",
                code = response.code()
            )
        }
    }

    /**
     * Maneja excepciones
     */
    fun <T> handleException(exception: Exception): Resource<T> {
        return Resource.Error(
            message = exception.message ?: "Error desconocido"
        )
    }

    /**
     * Ejecuta una petición de forma segura
     */
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>
    ): Resource<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                handleSuccess(response)
            } else {
                handleError(response)
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }
}