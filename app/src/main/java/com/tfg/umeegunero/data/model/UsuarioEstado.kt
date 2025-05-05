package com.tfg.umeegunero.data.model

/**
 * Enumeración que representa los posibles estados de un usuario en el sistema.
 * 
 * Esta enumeración se utiliza para controlar el ciclo de vida de las cuentas de usuario,
 * desde su creación hasta su posible desactivación o eliminación.
 * 
 * @property PENDIENTE El usuario ha sido creado pero aún no ha completado su registro o activación
 * @property ACTIVO El usuario está activo y puede acceder normalmente al sistema
 * @property BLOQUEADO El usuario ha sido temporalmente bloqueado
 * @property INACTIVO El usuario está desactivado pero sus datos se mantienen
 * @property ELIMINADO El usuario ha sido marcado para eliminación
 */
enum class UsuarioEstado {
    PENDIENTE,
    ACTIVO,
    BLOQUEADO,
    INACTIVO,
    ELIMINADO
} 