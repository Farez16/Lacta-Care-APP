package com.example.lactacare.dominio

enum class UserRole {
    PACIENTE,
    DOCTOR, // Asumiendo que EMPLEADO con rol médico es Doctor
    ADMIN,
    UNKNOWN
}

object RolValidator {

    fun parseRole(roleString: String?): UserRole {
        return when (roleString?.uppercase()) {
            "PACIENTE" -> UserRole.PACIENTE
            "ADMINISTRADOR" -> UserRole.ADMIN // Revisa cómo viene exactamente en tu BD
            "DOCTOR", "MEDICO" -> UserRole.DOCTOR
            "EMPLEADO" -> UserRole.DOCTOR // Si tu backend devuelve "EMPLEADO" genérico
            else -> UserRole.UNKNOWN
        }
    }

    // Regla: ¿A dónde va este usuario?
    fun getDestinationRoute(role: UserRole, profileCompleted: Boolean): String {
        if (!profileCompleted) return "complete_profile_screen"

        return when (role) {
            UserRole.PACIENTE -> "home_paciente"
            UserRole.DOCTOR -> "home_doctor"
            UserRole.ADMIN -> "home_admin"
            UserRole.UNKNOWN -> "login_screen"
        }
    }
}