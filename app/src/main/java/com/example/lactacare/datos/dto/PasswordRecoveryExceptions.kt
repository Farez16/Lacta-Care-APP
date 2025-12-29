package com.example.lactacare.datos.dto

/**
 * Excepción para cuentas registradas con Google
 */
class GoogleAccountException(message: String) : Exception(message)
/**
 * Excepción para rol incorrecto en recuperación de contraseña
 */
class RolMismatchRecoveryException(message: String) : Exception(message)
/**
 * Excepción para correos no autorizados (empleados)
 */
class UnauthorizedEmailException(message: String) : Exception(message)
/**
 * Excepción para usuarios no encontrados (pacientes)
 */
class UserNotFoundException(message: String) : Exception(message)