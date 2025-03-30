package com.tfg.umeegunero.data.model

/**
 * Enumeración que define los diferentes tipos de usuarios en el sistema UmeEgunero.
 * 
 * Esta enumeración establece los roles fundamentales que determinan los permisos, accesos
 * y capacidades de cada usuario dentro de la aplicación. Cada valor representa un perfil
 * específico con un conjunto definido de responsabilidades y privilegios.
 * 
 * Los tipos de usuario son utilizados en múltiples componentes del sistema, incluyendo:
 * - Control de acceso y navegación basada en roles
 * - Filtrado de contenido según permisos del usuario
 * - Asignación de responsabilidades en la gestión del centro educativo
 * - Personalización de la interfaz según el rol del usuario
 * 
 * @property ADMIN_APP Administrador global del sistema con acceso completo a todas las
 *                    funcionalidades, configuración técnica y gestión de centros.
 * @property ADMIN_CENTRO Administrador de un centro educativo específico. Puede gestionar
 *                       profesores, aulas, cursos y clases dentro de su centro.
 * @property PROFESOR Personal docente que imparte clases, gestiona alumnos y registra
 *                  actividades, asistencias y comunicaciones con familiares.
 * @property FAMILIAR Padre, madre o tutor legal de un alumno. Puede consultar información
 *                  sobre sus hijos/tutelados y comunicarse con el centro/profesores.
 * @property ALUMNO Usuario representando a un estudiante (generalmente no accede directamente
 *                al sistema, pero se usa para establecer relaciones en la base de datos).
 * @property DESCONOCIDO Tipo utilizado cuando no se puede determinar el tipo de usuario o
 *                     para valores por defecto en ciertos contextos.
 * 
 * @see Perfil Modelo que utiliza esta enumeración para definir roles de usuario
 * @see UserType Enumeración usada para navegación específica basada en tipos de usuario
 */
enum class TipoUsuario {
    ADMIN_APP, ADMIN_CENTRO, PROFESOR, FAMILIAR, ALUMNO, DESCONOCIDO
} 