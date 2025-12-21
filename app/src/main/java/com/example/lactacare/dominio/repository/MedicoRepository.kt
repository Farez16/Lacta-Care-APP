package com.example.lactacare.dominio.repository

import com.example.lactacare.dominio.model.Medico

interface MedicoRepository {
    suspend fun registrar(medico: Medico): Result<Boolean>
    suspend fun login(correo: String, clave: String): Result<Boolean>
    suspend fun obtenerMedicoPorId(id: Int): Medico?
}