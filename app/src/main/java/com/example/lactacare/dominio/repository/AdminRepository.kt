package com.example.lactacare.dominio.repository

import com.example.lactacare.dominio.model.DashboardAdminStats
import com.example.lactacare.dominio.model.Administrador

interface AdminRepository {
    // Tus métodos existentes de Auth
    suspend fun registrar(admin: Administrador): Result<Boolean>
    suspend fun login(correo: String, clave: String): Result<Boolean>
    suspend fun obtenerAdminPorId(id: Int): Administrador?

    // --- NUEVO: El método que alimentará el Dashboard ---
    suspend fun obtenerEstadisticas(): DashboardAdminStats
}