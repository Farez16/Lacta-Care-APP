package com.example.lactacare.datos.repository

import com.example.lactacare.datos.dto.DocumentoDto
import com.example.lactacare.datos.network.ApiService
import com.example.lactacare.dominio.repository.IIARepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class IARepository @Inject constructor(
    private val api: ApiService
) : IIARepository {

    override suspend fun listarDocumentos(): Result<List<DocumentoDto>> {
        return try {
            val response = api.listarDocumentos()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al listar documentos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun subirDocumento(archivo: File): Result<String> {
        return try {
            val requestFile = archivo.asRequestBody("application/pdf".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("archivo", archivo.name, requestFile)
            
            val response = api.subirDocumento(body)
            if (response.isSuccessful) {
                Result.success("Documento subido correctamente")
            } else {
                Result.failure(Exception("Error al subir: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun eliminarDocumento(id: Long): Result<String> {
        return try {
            val response = api.eliminarDocumento(id)
            if (response.isSuccessful) {
                Result.success("Eliminado correctamente")
            } else {
                Result.failure(Exception("Error al eliminar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
