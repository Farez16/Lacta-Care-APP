package com.example.lactacare.dominio.repository

import com.example.lactacare.datos.dto.SalaLactanciaDto
import com.example.lactacare.datos.dto.SalaLactanciaConCubiculosDTO

// Interface para el Repositorio de Lactarios
interface ILactariosRepository {
    suspend fun obtenerSalas(): Result<List<SalaLactanciaDto>>
    suspend fun crearSala(sala: SalaLactanciaDto): Result<SalaLactanciaDto>
    suspend fun crearSalaConCubiculos(dto: SalaLactanciaConCubiculosDTO): Result<Any>
    suspend fun editarSala(id: Long, sala: SalaLactanciaDto): Result<SalaLactanciaDto>
    suspend fun eliminarSala(id: Long): Result<Unit>
}
