package com.example.lactacare.datos.repository
import com.example.lactacare.datos.dto.PreguntaRequest
import com.example.lactacare.datos.network.ApiService
import com.example.lactacare.dominio.repository.ChatRepository
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ChatRepository {

    override suspend fun preguntarChatbot(
        pregunta: String,
        latitud: Double?,
        longitud: Double?
    ): Result<String> {
        return try {
            val request = PreguntaRequest(pregunta, latitud, longitud)
            val response = apiService.preguntarChatbot(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener respuesta del chatbot"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}