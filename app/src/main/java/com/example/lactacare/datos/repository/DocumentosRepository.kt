package com.example.lactacare.datos.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.lactacare.datos.dto.DocumentoDto
import com.example.lactacare.datos.network.DocumentosApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class DocumentosRepository @Inject constructor(
    private val apiService: DocumentosApiService,
    @ApplicationContext private val context: Context
) {

    suspend fun listarDocumentos(): List<DocumentoDto> {
        return try {
            val response = apiService.listarDocumentos()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun subirDocumento(uri: Uri): Result<String> {
        return try {
            val file = getFileFromUri(uri) ?: return Result.failure(Exception("No se pudo procesar el archivo"))
            
            val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("archivo", file.name, requestFile)
            
            val response = apiService.subirArchivo(body)
            
            if (response.isSuccessful) {
                Result.success("Archivo subido con éxito")
            } else {
                Result.failure(Exception("Error al subir el archivo: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarDocumento(id: Long): Boolean {
        return try {
            val response = apiService.eliminarDocumento(id)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Función auxiliar para obtener File desde Uri
    private fun getFileFromUri(uri: Uri): File? {
        try {
            val contentResolver = context.contentResolver
            val fileName = getFileName(uri)
            val tempFile = File(context.cacheDir, fileName)
            
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "temp_file.pdf"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }
}
