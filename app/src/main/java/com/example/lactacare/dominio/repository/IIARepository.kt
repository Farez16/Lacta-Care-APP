package com.example.lactacare.dominio.repository

import com.example.lactacare.datos.dto.DocumentoDto
import java.io.File

interface IIARepository {
    suspend fun listarDocumentos(): Result<List<DocumentoDto>>
    suspend fun subirDocumento(archivo: File): Result<String>
    suspend fun eliminarDocumento(id: Long): Result<String>
}
