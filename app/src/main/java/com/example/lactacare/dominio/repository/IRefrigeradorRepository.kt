package com.example.lactacare.dominio.repository

import com.example.lactacare.datos.dto.RefrigeradorDto

interface IRefrigeradorRepository {
    suspend fun obtenerRefrigeradores(): Result<List<RefrigeradorDto>>
    suspend fun crearRefrigerador(refri: RefrigeradorDto): Result<RefrigeradorDto>
    suspend fun editarRefrigerador(id: Long, refri: RefrigeradorDto): Result<RefrigeradorDto>
    suspend fun eliminarRefrigerador(id: Long): Result<Unit>
}
