package com.example.lactacare.dominio.model

/**
* Enum para tipos de rol de usuario
* ÚNICO archivo de RolUsuario en el proyecto
*/
enum class RolUsuario {
    PACIENTE,
    DOCTOR,
    ADMINISTRADOR;

    companion object {
        /**
         * Convierte un String al enum correspondiente
         * Útil para deserializar desde JSON o backend
         */
        fun fromString(rol: String): RolUsuario {
            return when (rol.uppercase()) {
                "PACIENTE" -> PACIENTE
                "DOCTOR" -> DOCTOR
                "ADMINISTRADOR", "ADMIN" -> ADMINISTRADOR
                else -> PACIENTE // Por defecto
            }
        }
    }
}
