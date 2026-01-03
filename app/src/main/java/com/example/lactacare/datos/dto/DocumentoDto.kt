package com.example.lactacare.datos.dto

data class DocumentoDto(
    val idDocumento: Long,
    val nombreArchivo: String,
    val tipoContenido: String? = null,
    val tamano: Long? = null
)
