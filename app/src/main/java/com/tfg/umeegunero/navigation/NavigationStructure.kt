package com.tfg.umeegunero.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.Announcement
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.ui.graphics.vector.ImageVector
import com.tfg.umeegunero.data.model.TipoUsuario

object NavigationStructure {
    data class NavItem(
        val title: String,
        val icon: ImageVector,
        val route: String,
        val dividerAfter: Boolean = false,
        val badge: Int? = null,
        val subItems: List<NavItem> = emptyList(),
        val isImplemented: Boolean = true,
        val description: String = "" // Descripción corta para ayudar al usuario
    )

    // PERFIL ADMINISTRADOR DE LA APLICACIÓN
    fun getAdminNavItems(): List<NavItem> = listOf(
        NavItem(
            title = "Inicio",
            icon = Icons.Filled.Home,
            route = AppScreens.AdminDashboard.route,
            description = "Panel principal del administrador"
        ),
        NavItem(
            title = "Centros",
            icon = Icons.Filled.Business,
            route = AppScreens.GestionCentros.route,
            description = "Administración de centros educativos",
            subItems = listOf(
                NavItem(
                    title = "Lista de Centros",
                    icon = Icons.AutoMirrored.Outlined.List,
                    route = AppScreens.GestionCentros.route,
                    description = "Ver todos los centros registrados",
                    isImplemented = true
                ),
                NavItem(
                    title = "Añadir Centro",
                    icon = Icons.Outlined.Add,
                    route = AppScreens.AddCentro.route,
                    description = "Registrar un nuevo centro",
                    isImplemented = true
                )
            )
        ),
        NavItem(
            title = "Usuarios",
            icon = Icons.Filled.Group,
            route = "usuarios",
            description = "Administración de usuarios del sistema",
            subItems = listOf(
                NavItem(
                    title = "Administradores",
                    icon = Icons.Outlined.AdminPanelSettings,
                    route = AppScreens.AdminList.route,
                    description = "Gestión de administradores"
                ),
                NavItem(
                    title = "Profesores",
                    icon = Icons.Outlined.School,
                    route = AppScreens.ProfesorList.route,
                    description = "Gestión de profesores"
                ),
                NavItem(
                    title = "Alumnos",
                    icon = Icons.Outlined.Face,
                    route = AppScreens.AlumnoList.route,
                    description = "Gestión de alumnos"
                ),
                NavItem(
                    title = "Familiares",
                    icon = Icons.Outlined.People,
                    route = AppScreens.FamiliarList.route,
                    description = "Gestión de familiares"
                )
            )
        ),
        NavItem(
            title = "Comunicaciones",
            icon = Icons.Filled.Email,
            route = "comunicaciones",
            description = "Sistema de mensajería administrativa",
            subItems = listOf(
                NavItem(
                    title = "Comunicados",
                    icon = Icons.AutoMirrored.Outlined.Announcement,
                    route = AppScreens.Comunicados.route,
                    description = "Enviar comunicados generales"
                ),
                NavItem(
                    title = "Notificaciones",
                    icon = Icons.Outlined.Notifications,
                    route = AppScreens.Notificaciones.route,
                    description = "Gestionar notificaciones del sistema"
                ),
                NavItem(
                    title = "Config. Email",
                    icon = Icons.Outlined.MailOutline,
                    route = AppScreens.EmailConfig.route,
                    description = "Configurar sistema de email"
                )
            )
        ),
        NavItem(
            title = "Reportes",
            icon = Icons.Filled.Assessment,
            route = "reportes",
            description = "Informes y estadísticas globales",
            subItems = listOf(
                NavItem(
                    title = "Uso de Plataforma",
                    icon = Icons.Outlined.Insights,
                    route = AppScreens.ReporteUso.route,
                    description = "Estadísticas de uso"
                ),
                NavItem(
                    title = "Rendimiento",
                    icon = Icons.AutoMirrored.Outlined.TrendingUp,
                    route = AppScreens.ReporteRendimiento.route,
                    description = "Métricas de rendimiento"
                ),
                NavItem(
                    title = "Estadísticas",
                    icon = Icons.Outlined.BarChart,
                    route = AppScreens.Estadisticas.route,
                    description = "Estadísticas generales"
                )
            ),
            dividerAfter = true
        ),
        NavItem(
            title = "Perfil",
            icon = Icons.Filled.Person,
            route = AppScreens.Perfil.route,
            description = "Gestión de datos personales"
        ),
        NavItem(
            title = "Configuración",
            icon = Icons.Filled.Settings,
            route = AppScreens.Configuracion.route,
            description = "Ajustes técnicos del sistema",
            subItems = listOf(
                NavItem(
                    title = "Parámetros",
                    icon = Icons.Outlined.Tune,
                    route = AppScreens.Configuracion.route,
                    description = "Parámetros generales"
                ),
                NavItem(
                    title = "Seguridad",
                    icon = Icons.Outlined.Security,
                    route = "configuracion/seguridad",
                    description = "Configuración de seguridad"
                ),
                NavItem(
                    title = "Email",
                    icon = Icons.Outlined.Email,
                    route = AppScreens.EmailConfig.route,
                    description = "Configuración de email"
                ),
                NavItem(
                    title = "Soporte Técnico",
                    icon = Icons.AutoMirrored.Outlined.Help,
                    route = AppScreens.SoporteTecnico.route,
                    description = "Contactar con soporte técnico"
                ),
                NavItem(
                    title = "FAQ",
                    icon = Icons.Outlined.Info,
                    route = AppScreens.FAQ.route, 
                    description = "Preguntas frecuentes"
                )
            ),
            dividerAfter = true
        )
    )

    // PERFIL ADMINISTRADOR DE CENTRO
    fun getCentroNavItems(): List<NavItem> = listOf(
        NavItem(
            title = "Inicio",
            icon = Icons.Filled.Home,
            route = AppScreens.CentroDashboard.route,
            description = "Dashboard administrativo del centro"
        ),
        NavItem(
            title = "Centro",
            icon = Icons.Filled.Business,
            route = "centro",
            description = "Información y gestión del centro",
            subItems = listOf(
                NavItem(
                    title = "Datos Generales",
                    icon = Icons.Outlined.Info,
                    route = "centro/datos",
                    description = "Información general del centro"
                ),
                NavItem(
                    title = "Cursos y Clases",
                    icon = Icons.AutoMirrored.Outlined.MenuBook,
                    route = "centro/cursos",
                    description = "Configuración de cursos y clases"
                ),
                NavItem(
                    title = "Calendario Escolar",
                    icon = Icons.Outlined.Event,
                    route = "centro/calendario",
                    description = "Gestión del calendario escolar"
                ),
                NavItem(
                    title = "Añadir Centro",
                    icon = Icons.Outlined.Add,
                    route = AppScreens.AddCentro.route,
                    description = "Registrar un nuevo centro",
                    isImplemented = true
                )
            )
        ),
        NavItem(
            title = "Profesores",
            icon = Icons.Filled.Person,
            route = "admin_dashboard/profesores",
            description = "Gestión del personal docente",
            subItems = listOf(
                NavItem(
                    title = "Lista de Profesores",
                    icon = Icons.AutoMirrored.Outlined.List,
                    route = AppScreens.GestionProfesores.route,
                    description = "Ver y gestionar todos los profesores",
                    isImplemented = true
                ),
                NavItem(
                    title = "Añadir Profesor",
                    icon = Icons.Outlined.PersonAdd,
                    route = "admin_dashboard/profesores/add",
                    description = "Registrar un nuevo profesor"
                ),
                NavItem(
                    title = "Asignar Clases",
                    icon = Icons.Outlined.School,
                    route = "admin_dashboard/profesores/clases",
                    description = "Asignar profesores a clases"
                )
            )
        ),
        NavItem(
            title = "Estudiantes",
            icon = Icons.Filled.Face,
            route = "estudiantes",
            description = "Gestión de estudiantes",
            subItems = listOf(
                NavItem(
                    title = "Lista de Estudiantes",
                    icon = Icons.AutoMirrored.Outlined.List,
                    route = "estudiantes/lista",
                    description = "Ver todos los estudiantes"
                ),
                NavItem(
                    title = "Añadir Estudiante",
                    icon = Icons.Outlined.PersonAdd,
                    route = "estudiantes/añadir",
                    description = "Registrar nuevo estudiante"
                ),
                NavItem(
                    title = "Asignar a Clases",
                    icon = Icons.AutoMirrored.Outlined.Assignment,
                    route = "estudiantes/asignar",
                    description = "Asignar estudiantes a clases"
                )
            )
        ),
        NavItem(
            title = "Familias",
            icon = Icons.Filled.Group,
            route = "familias",
            description = "Administración de familiares",
            subItems = listOf(
                NavItem(
                    title = "Lista de Familias",
                    icon = Icons.AutoMirrored.Outlined.List,
                    route = "familias/lista",
                    description = "Ver todas las familias"
                ),
                NavItem(
                    title = "Asignar Estudiantes",
                    icon = Icons.Outlined.Link,
                    route = "familias/asignar",
                    description = "Vincular familias con estudiantes"
                ),
                NavItem(
                    title = "Gestionar Accesos",
                    icon = Icons.Outlined.VpnKey,
                    route = "familias/accesos",
                    description = "Administrar permisos de acceso"
                )
            )
        ),
        NavItem(
            title = "Comunicaciones",
            icon = Icons.Filled.Email,
            route = AppScreens.GestionAcademica.route,
            description = "Sistema de mensajería",
            subItems = listOf(
                NavItem(
                    title = "Bandeja de Entrada",
                    icon = Icons.Outlined.Mail,
                    route = "comunicaciones/bandeja",
                    description = "Ver mensajes recibidos"
                ),
                NavItem(
                    title = "Comunicados",
                    icon = Icons.AutoMirrored.Outlined.Announcement,
                    route = "comunicaciones/comunicados",
                    description = "Enviar comunicados generales"
                ),
                NavItem(
                    title = "Notificaciones",
                    icon = Icons.Outlined.Notifications,
                    route = AppScreens.GestionNotificacionesCentro.route,
                    description = "Gestionar notificaciones del centro",
                    isImplemented = true
                ),
                NavItem(
                    title = "Mensajes Directos",
                    icon = Icons.AutoMirrored.Outlined.Send,
                    route = "comunicaciones/mensajes",
                    description = "Enviar mensajes a profesores o familias"
                )
            )
        ),
        NavItem(
            title = "Informes",
            icon = Icons.Filled.Description,
            route = "informes",
            description = "Generación y acceso a informes",
            subItems = listOf(
                NavItem(
                    title = "Informes Académicos",
                    icon = Icons.Outlined.School,
                    route = "informes/academicos",
                    description = "Informes de rendimiento académico"
                ),
                NavItem(
                    title = "Informes Administrativos",
                    icon = Icons.Outlined.Settings,
                    route = "informes/administrativos",
                    description = "Informes de gestión del centro"
                ),
                NavItem(
                    title = "Estadísticas",
                    icon = Icons.Outlined.BarChart,
                    route = "informes/estadisticas",
                    description = "Estadísticas generales"
                )
            )
        ),
        NavItem(
            title = "Calendario",
            icon = Icons.Filled.CalendarToday,
            route = AppScreens.Calendario.route,
            description = "Gestión del calendario escolar",
            dividerAfter = true
        ),
        NavItem(
            title = "Configuración",
            icon = Icons.Filled.Settings,
            route = AppScreens.Configuracion.route,
            description = "Ajustes del centro educativo",
            subItems = listOf(
                NavItem(
                    title = "Parámetros",
                    icon = Icons.Outlined.Tune,
                    route = AppScreens.Configuracion.route,
                    description = "Parámetros generales"
                ),
                NavItem(
                    title = "Soporte Técnico",
                    icon = Icons.AutoMirrored.Outlined.Help,
                    route = AppScreens.SoporteTecnico.route,
                    description = "Contactar con soporte técnico"
                ),
                NavItem(
                    title = "FAQ",
                    icon = Icons.Outlined.Info,
                    route = AppScreens.FAQ.route, 
                    description = "Preguntas frecuentes"
                )
            ),
            dividerAfter = true
        ),
        NavItem(
            title = "Gestión Académica",
            icon = Icons.Outlined.School,
            route = "admin_dashboard/academico",
            isImplemented = true,
            subItems = listOf(
                NavItem(
                    title = "Cursos",
                    icon = Icons.Outlined.List,
                    route = Screens.ListCursos.route,
                    isImplemented = true
                ),
                NavItem(
                    title = "Clases",
                    icon = Icons.Outlined.Group,
                    route = "admin_dashboard/clases",
                    isImplemented = true
                )
            )
        )
    )

    // PERFIL PROFESOR
    fun getProfesorNavItems(): List<NavItem> = listOf(
        NavItem(
            title = "Inicio",
            icon = Icons.Filled.Home,
            route = AppScreens.ProfesorDashboard.route,
            description = "Dashboard principal del profesor"
        ),
        NavItem(
            title = "Mis Clases",
            icon = Icons.Filled.Class,
            route = AppScreens.AsistenciaProfesor.route,
            description = "Gestión de mis clases",
            subItems = listOf(
                NavItem(
                    title = "Lista de Clases",
                    icon = Icons.AutoMirrored.Outlined.List,
                    route = AppScreens.AsistenciaProfesor.route,
                    description = "Ver todas mis clases asignadas"
                ),
                NavItem(
                    title = "Asistencia",
                    icon = Icons.Outlined.CheckCircle,
                    route = AppScreens.AsistenciaProfesor.route,
                    description = "Registrar asistencia"
                )
            )
        ),
        NavItem(
            title = "Alumnos",
            icon = Icons.Filled.ChildCare,
            route = "alumnos_profesor",
            description = "Gestión de alumnos",
            subItems = listOf(
                NavItem(
                    title = "Lista de Alumnos",
                    icon = Icons.AutoMirrored.Outlined.List,
                    route = "alumnos_profesor/lista",
                    description = "Ver todos los alumnos"
                ),
                NavItem(
                    title = "Actividades",
                    icon = Icons.AutoMirrored.Outlined.DirectionsRun,
                    route = "alumnos_profesor/actividades",
                    description = "Registro de actividades"
                ),
                NavItem(
                    title = "Evaluaciones",
                    icon = Icons.Outlined.Grade,
                    route = "alumnos_profesor/evaluaciones",
                    description = "Gestión de evaluaciones"
                )
            )
        ),
        NavItem(
            title = "Tareas",
            icon = Icons.AutoMirrored.Filled.Assignment,
            route = AppScreens.TareasProfesor.route,
            description = "Gestión de tareas y deberes",
            subItems = listOf(
                NavItem(
                    title = "Ver Tareas",
                    icon = Icons.AutoMirrored.Outlined.List,
                    route = AppScreens.TareasProfesor.route,
                    description = "Ver todas las tareas"
                ),
                NavItem(
                    title = "Crear Tarea",
                    icon = Icons.Outlined.Add,
                    route = AppScreens.TareasProfesor.route,
                    description = "Crear nueva tarea"
                ),
                NavItem(
                    title = "Tarea Recurrente",
                    icon = Icons.Outlined.Repeat,
                    route = "tareas_profesor/recurrente",
                    description = "Crear tarea recurrente"
                )
            )
        ),
        NavItem(
            title = "Comunicaciones",
            icon = Icons.Filled.Email,
            route = AppScreens.ChatProfesor.route,
            description = "Mensajería con familias",
            subItems = listOf(
                NavItem(
                    title = "Bandeja de Entrada",
                    icon = Icons.Outlined.Mail,
                    route = AppScreens.ChatProfesor.route,
                    description = "Ver mensajes recibidos"
                ),
                NavItem(
                    title = "Mensaje Individual",
                    icon = Icons.Outlined.Person,
                    route = "comunicaciones_profesor/individual",
                    description = "Enviar mensaje a una familia"
                ),
                NavItem(
                    title = "Mensaje Grupal",
                    icon = Icons.Outlined.People,
                    route = "comunicaciones_profesor/grupal",
                    description = "Enviar mensaje a varias familias"
                )
            )
        ),
        NavItem(
            title = "Calendario",
            icon = Icons.Filled.CalendarToday,
            route = AppScreens.CalendarioProfesor.route,
            description = "Gestión de eventos y actividades"
        ),
        NavItem(
            title = "Perfil",
            icon = Icons.Filled.Person,
            route = AppScreens.Perfil.route,
            description = "Gestión de datos personales"
        ),
        NavItem(
            title = "Configuración",
            icon = Icons.Filled.Settings,
            route = AppScreens.Config.route,
            description = "Ajustes de la aplicación"
        ),
        NavItem(
            title = "Cerrar Sesión",
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            route = "logout",
            description = "Cerrar sesión"
        )
    )

    // PERFIL FAMILIAR
    fun getFamiliarNavItems(): List<NavItem> = listOf(
        NavItem(
            title = "Inicio",
            icon = Icons.Filled.Home,
            route = AppScreens.FamiliaDashboard.route,
            description = "Panel principal con resumen de actividad"
        ),
        NavItem(
            title = "Estudiantes",
            icon = Icons.Filled.ChildCare,
            route = "estudiantes_familiar",
            description = "Información de mis hijos",
            subItems = listOf(
                NavItem(
                    title = "Perfiles",
                    icon = Icons.Outlined.Person,
                    route = AppScreens.DetalleAlumnoFamilia.createRoute(""),
                    description = "Ver perfil de mis hijos"
                ),
                NavItem(
                    title = "Historial Académico",
                    icon = Icons.Outlined.History,
                    route = "estudiantes_familiar/historial",
                    description = "Ver historial académico"
                ),
                NavItem(
                    title = "Asistencia",
                    icon = Icons.Outlined.CheckCircle,
                    route = "estudiantes_familiar/asistencia",
                    description = "Control de asistencia"
                ),
                NavItem(
                    title = "Evaluaciones",
                    icon = Icons.Outlined.Assessment,
                    route = "estudiantes_familiar/evaluaciones",
                    description = "Notas y evaluaciones"
                )
            )
        ),
        NavItem(
            title = "Calendario",
            icon = Icons.Filled.CalendarToday,
            route = AppScreens.CalendarioFamilia.route,
            description = "Calendario de eventos escolares"
        ),
        NavItem(
            title = "Tareas",
            icon = Icons.AutoMirrored.Filled.Assignment,
            route = AppScreens.TareasFamilia.route,
            description = "Tareas y deberes"
        ),
        NavItem(
            title = "Comunicaciones",
            icon = Icons.Filled.Email,
            route = AppScreens.ChatFamilia.route,
            description = "Mensajería con profesores"
        ),
        NavItem(
            title = "Notificaciones",
            icon = Icons.Filled.Notifications,
            route = AppScreens.NotificacionesFamilia.route,
            description = "Centro de notificaciones"
        ),
        NavItem(
            title = "Perfil",
            icon = Icons.Filled.Person,
            route = AppScreens.Perfil.route,
            description = "Gestión de datos personales"
        ),
        NavItem(
            title = "Configuración",
            icon = Icons.Filled.Settings,
            route = AppScreens.Config.route,
            description = "Ajustes de la aplicación"
        ),
        NavItem(
            title = "Cerrar Sesión",
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            route = "logout",
            description = "Cerrar sesión"
        )
    )

    fun getNavItemsByTipo(tipo: TipoUsuario): List<NavItem> = when (tipo) {
        TipoUsuario.ADMIN_APP -> getAdminNavItems()
        TipoUsuario.ADMIN_CENTRO -> getCentroNavItems()
        TipoUsuario.PROFESOR -> getProfesorNavItems()
        TipoUsuario.FAMILIAR -> getFamiliarNavItems()
        TipoUsuario.ALUMNO -> emptyList() // Los alumnos no tienen navegación por ahora
    }

    fun getNavItemsByTipo(tipoString: String): List<NavItem> = when (tipoString.uppercase()) {
        "ADMIN_APP", "ADMIN" -> getAdminNavItems()
        "ADMIN_CENTRO", "CENTRO" -> getCentroNavItems()
        "PROFESOR" -> getProfesorNavItems()
        "FAMILIAR" -> getFamiliarNavItems()
        else -> emptyList()
    }
} 