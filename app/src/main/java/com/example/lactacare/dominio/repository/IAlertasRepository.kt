package com.example.lactacare.dominio.repository

import com.example.lactacare.datos.dto.SistemaAlertaDto

interface IAlertasRepository {
    suspend fun obtenerAlertas(): Result<List<SistemaAlertaDto>>
}
