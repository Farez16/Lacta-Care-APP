package com.example.lactacare.datos.repository

import com.example.lactacare.dominio.model.RolUsuario

/**
 * Validador de roles para asegurar que los usuarios solo puedan
 * iniciar sesión desde el portal correcto
 */
object RolValidator {

    /**
     * Valida que el rol del usuario coincida con el rol esperado
     * @param rolUsuario El rol del usuario autenticado
     * @param rolEsperado El rol desde el cual está intentando iniciar sesión
     * @return true si los roles coinciden, false en caso contrario
     */
    fun validarRol(rolUsuario: RolUsuario, rolEsperado: RolUsuario): Boolean {
        return rolUsuario == rolEsperado
    }

    /**
     * Obtiene un mensaje de error descriptivo cuando el rol no coincide
     */
    fun obtenerMensajeError(rolUsuario: RolUsuario): String {
        return when (rolUsuario) {
            RolUsuario.PACIENTE -> "Esta cuenta está registrada como PACIENTE. Por favor, inicie sesión desde el portal de pacientes."
            RolUsuario.DOCTOR -> "Esta cuenta está registrada como DOCTOR. Por favor, inicie sesión desde el portal de doctores."
            RolUsuario.ADMINISTRADOR -> "Esta cuenta está registrada como ADMINISTRADOR. Por favor, inicie sesión desde el portal de administradores."
        }
    }
}

/**
 * Excepción personalizada para errores de rol
 */
class RolMismatchException(val rolCorrecto: RolUsuario, message: String) : Exception(message)
