package com.example.lactacare.dominio.repository

import com.example.lactacare.dominio.model.Institucion
import com.example.lactacare.dominio.model.Lactario

interface LactarioRepository {
    // Lectura (Ya tenías estas)
    suspend fun obtenerLactarios(): List<Lactario>
    suspend fun obtenerLactarioPorId(id: Int): Lactario?
    suspend fun obtenerInstituciones(): List<Institucion>
    suspend fun obtenerTodos(): List<Lactario> // (Esta era redundante pero la dejaste)

    // --- AGREGA ESTAS DOS PARA ARREGLAR EL ERROR ---
    suspend fun guardarLactario(lactario: Lactario): Boolean
    suspend fun eliminarLactario(id: Int): Boolean
}