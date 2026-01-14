package com.example.lactacare.dominio.repository

import com.example.lactacare.dominio.model.Institucion

interface IInstitucionRepository {
    suspend fun obtenerInstituciones(): Result<List<Institucion>>
    suspend fun obtenerInstitucion(id: Long): Result<Institucion>
    suspend fun crearInstitucion(institucion: Institucion): Result<Institucion>
    suspend fun editarInstitucion(id: Long, institucion: Institucion): Result<Institucion>
    suspend fun eliminarInstitucion(id: Long): Result<Unit>
}
