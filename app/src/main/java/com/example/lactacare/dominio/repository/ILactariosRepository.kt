package com.example.lactacare.dominio.repository

import com.example.lactacare.datos.dto.SalaLactanciaDto

// Interface para el Repositorio de Lactarios
interface ILactariosRepository {
    suspend fun obtenerSalas(): Result<List<SalaLactanciaDto>>
}
