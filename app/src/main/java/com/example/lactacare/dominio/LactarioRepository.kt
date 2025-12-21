package com.example.lactacare.dominio

interface LactarioRepository {
    suspend fun obtenerLactarios(): List<Lactario>
    suspend fun obtenerLactarioPorId(id: Int): Lactario?
    suspend fun obtenerInstituciones(): List<Institucion>
    suspend fun obtenerTodos(): List<Lactario>

}