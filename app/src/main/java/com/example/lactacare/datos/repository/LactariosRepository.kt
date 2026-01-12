package com.example.lactacare.datos.repository

import com.example.lactacare.datos.dto.SalaLactanciaDto
import com.example.lactacare.datos.network.ApiService
import com.example.lactacare.dominio.repository.ILactariosRepository
import javax.inject.Inject
import com.example.lactacare.datos.dto.BloqueHorarioDto

class LactariosRepository @Inject constructor(
    private val apiService: ApiService
) : ILactariosRepository {

    override suspend fun obtenerSalas(): Result<List<SalaLactanciaDto>> {
        return try {
            val response = apiService.obtenerLactarios() // Corregido nombre metodo
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun crearSala(sala: SalaLactanciaDto): Result<SalaLactanciaDto> {
        return try {
            val response = apiService.crearLactario(sala)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al crear: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun editarSala(id: Long, sala: SalaLactanciaDto): Result<SalaLactanciaDto> {
        return try {
            val response = apiService.editarLactario(id, sala)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al editar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun eliminarSala(id: Long): Result<Unit> {
        return try {
            val response = apiService.eliminarLactario(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun obtenerDisponibilidad(
        idSala: Long,
        fecha: String
    ): Result<List<BloqueHorarioDto>> {
        return try {
            val response = apiService.obtenerDisponibilidad(idSala, fecha)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
