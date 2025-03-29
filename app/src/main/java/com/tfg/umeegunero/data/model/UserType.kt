// Ruta: com/tfg/umeegunero/data/model/UserType.kt
package com.tfg.umeegunero.data.model

/**
 * Enumeración que define los tipos de usuario para la navegación y acceso al sistema UmeEgunero.
 * 
 * Esta enumeración se utiliza específicamente para el control de la navegación y la experiencia
 * de usuario dentro de la aplicación. Define las rutas disponibles, pantallas accesibles y flujos
 * de navegación según el rol del usuario que ha iniciado sesión.
 * 
 * A diferencia de [TipoUsuario], que se enfoca en los permisos y roles a nivel de base de datos,
 * esta enumeración está orientada a la experiencia de usuario y la navegación en la interfaz.
 * 
 * Los valores de esta enumeración se utilizan principalmente para:
 * - Determinar el punto de entrada después del inicio de sesión
 * - Configurar los destinos de navegación disponibles
 * - Personalizar los menús y opciones visibles
 * - Filtrar las funcionalidades accesibles en la interfaz
 * 
 * @property ADMIN_APP Administrador principal que accede al panel global del sistema con
 *                    estadísticas generales y gestión de todos los centros educativos.
 * @property ADMIN_CENTRO Administrador de un centro específico con acceso al panel de
 *                       administración de su centro, gestión de profesores y alumnos.
 * @property PROFESOR Usuario docente con acceso a sus clases asignadas, herramientas de 
 *                  registro de actividades y comunicación con familias.
 * @property FAMILIAR Usuario familiar (padre, madre o tutor) con acceso a información de
 *                  sus hijos/tutelados y comunicación con el centro/profesores.
 * 
 * @see TipoUsuario Enumeración relacionada que define los roles en el sistema a nivel de permisos
 */
enum class UserType {
    ADMIN_APP,      // Administrador de la aplicación
    ADMIN_CENTRO,   // Administrador de centro educativo
    PROFESOR,       // Profesor
    FAMILIAR        // Padre, madre o tutor
}