package com.example.lactacare.datos.repository

import com.example.lactacare.datos.network.ApiService
import com.example.lactacare.dominio.model.Institucion
import com.example.lactacare.dominio.repository.IInstitucionRepository
import javax.inject.Inject

class InstitucionRepository @Inject constructor(
    private val apiService: ApiService
) : IInstitucionRepository {

    override suspend fun obtenerInstituciones(): Result<List<Institucion>> {
        return try {
            val response = apiService.obtenerInstituciones()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener instituciones: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerInstitucion(id: Long): Result<Institucion> {
        return try {
            val response = apiService.obtenerInstitucion(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener institución: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun crearInstitucion(institucion: Institucion): Result<Institucion> {
        return try {
            val response = apiService.crearInstitucion(institucion)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al crear institución: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun editarInstitucion(id: Long, institucion: Institucion): Result<Institucion> {
         return try {
            val response = apiService.editarInstitucion(id, institucion)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al editar institución: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun eliminarInstitucion(id: Long): Result<Unit> {
        return try {
            val response = apiService.eliminarInstitucion(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar institución: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
