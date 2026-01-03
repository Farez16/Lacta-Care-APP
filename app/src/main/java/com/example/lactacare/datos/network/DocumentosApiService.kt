package com.example.lactacare.datos.network

import com.example.lactacare.datos.dto.ApiResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface DocumentosApiService {

    @Multipart
    @POST("api/documentos/upload")
    suspend fun subirArchivo(
        @Part archivo: MultipartBody.Part
    ): Response<Map<String, Any>> // El backend devuelve un Map genérico

    @GET("api/documentos")
    suspend fun listarDocumentos(): Response<List<com.example.lactacare.datos.dto.DocumentoDto>>

    @DELETE("api/documentos/{id}")
    suspend fun eliminarDocumento(@Path("id") id: Long): Response<Map<String, Any>>
}
