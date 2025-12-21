package com.example.lactacare.dominio

import com.example.lactacare.dominio.model.Institucion
import com.example.lactacare.dominio.model.Lactario

interface LactarioRepository {
    suspend fun obtenerLactarios(): List<Lactario>
    suspend fun obtenerLactarioPorId(id: Int): Lactario?
    suspend fun obtenerInstituciones(): List<Institucion>
    suspend fun obtenerTodos(): List<Lactario>

}