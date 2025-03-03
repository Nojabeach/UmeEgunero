// Ruta: com/tfg/umeegunero/data/model/UserType.kt
package com.tfg.umeegunero.data.model

/**
 * Tipos de usuario para navegación y acceso al sistema
 */
enum class UserType {
    ADMIN_APP,      // Administrador de la aplicación
    ADMIN_CENTRO,     // Administrador de centro educativo
    PROFESOR,   // Profesor
    FAMILIAR    // Padre, madre o tutor
}