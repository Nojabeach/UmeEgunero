package com.tfg.umeegunero.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.List
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
                    icon = Icons.Outlined.List,
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
                ),
                NavItem(
                    title = "Estadísticas",
                    icon = Icons.Outlined.BarChart,
                    route = AppScreens.Estadisticas.route,
                    description = "Estadísticas de los centros",
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
                    icon = Icons.Outlined.Announcement,
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
                    icon = Icons.Outlined.TrendingUp,
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
                    icon = Icons.Outlined.Help,
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
                    icon = Icons.Outlined.MenuBook,
                    route = "centro/cursos",
                    description = "Configuración de cursos y clases"
                ),
                NavItem(
                    title = "Calendario Escolar",
                    icon = Icons.Outlined.Event,
                    route = "centro/calendario",
                    description = "Gestión del calendario escolar"
                )
            )
        ),
        NavItem(
            title = "Profesores",
            icon = Icons.Filled.School,
            route = "profesores",
            description = "Administración del personal docente",
            subItems = listOf(
                NavItem(
                    title = "Lista de Profesores",
                    icon = Icons.Outlined.List,
                    route = "profesores/lista",
                    description = "Ver todos los profesores"
                ),
                NavItem(
                    title = "Añadir Profesor",
                    icon = Icons.Outlined.PersonAdd,
                    route = "profesores/añadir",
                    description = "Registrar nuevo profesor"
                ),
                NavItem(
                    title = "Asignar Clases",
                    icon = Icons.Outlined.Assignment,
                    route = "profesores/asignar",
                    description = "Asignar clases a profesores"
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
                    icon = Icons.Outlined.List,
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
                    icon = Icons.Outlined.Assignment,
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
                    icon = Icons.Outlined.List,
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
                    icon = Icons.Outlined.Inbox,
                    route = "comunicaciones/bandeja",
                    description = "Ver mensajes recibidos"
                ),
                NavItem(
                    title = "Comunicados",
                    icon = Icons.Outlined.Announcement,
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
                    icon = Icons.Outlined.Send,
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
                    icon = Icons.Outlined.Help,
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

    // PERFIL PROFESOR
    fun getProfesorNavItems(): List<NavItem> = listOf(
        NavItem(
            title = "Inicio",
            icon = Icons.Filled.Home,
            route = AppScreens.ProfesorDashboard.route,
            description = "Panel principal con tareas pendientes"
        ),
        NavItem(
            title = "Estudiantes",
            icon = Icons.Filled.Face,
            route = "estudiantes_profesor",
            description = "Gestión de estudiantes asignados",
            subItems = listOf(
                NavItem(
                    title = "Lista de Estudiantes",
                    icon = Icons.Outlined.List,
                    route = "estudiantes_profesor/lista",
                    description = "Ver mis estudiantes"
                ),
                NavItem(
                    title = "Tomar Asistencia",
                    icon = Icons.Outlined.CheckCircle,
                    route = "estudiantes_profesor/asistencia",
                    description = "Registrar asistencia diaria"
                ),
                NavItem(
                    title = "Registrar Evaluaciones",
                    icon = Icons.Outlined.Assignment,
                    route = "estudiantes_profesor/evaluaciones",
                    description = "Gestionar evaluaciones"
                )
            )
        ),
        NavItem(
            title = "Clases",
            icon = Icons.Filled.MenuBook,
            route = "clases_profesor",
            description = "Administración de clases asignadas",
            subItems = listOf(
                NavItem(
                    title = "Mis Clases",
                    icon = Icons.Outlined.List,
                    route = "clases_profesor/lista",
                    description = "Ver mis clases asignadas"
                ),
                NavItem(
                    title = "Horarios",
                    icon = Icons.Outlined.Schedule,
                    route = "clases_profesor/horarios",
                    description = "Gestionar horarios"
                ),
                NavItem(
                    title = "Planificación",
                    icon = Icons.Outlined.Event,
                    route = "clases_profesor/planificacion",
                    description = "Planificar actividades"
                )
            )
        ),
        NavItem(
            title = "Registro Diario",
            icon = Icons.Filled.Edit,
            route = "registro_diario",
            description = "Creación de registros diarios",
            subItems = listOf(
                NavItem(
                    title = "Nuevo Registro",
                    icon = Icons.Outlined.Add,
                    route = "registro_diario/nuevo",
                    description = "Crear nuevo registro"
                ),
                NavItem(
                    title = "Editar Registros",
                    icon = Icons.Outlined.Edit,
                    route = "registro_diario/editar",
                    description = "Modificar registros existentes"
                ),
                NavItem(
                    title = "Histórico",
                    icon = Icons.Outlined.History,
                    route = "registro_diario/historico",
                    description = "Ver histórico de registros"
                )
            )
        ),
        NavItem(
            title = "Comunicaciones",
            icon = Icons.Filled.Email,
            route = "comunicaciones_profesor",
            description = "Mensajería con familias",
            subItems = listOf(
                NavItem(
                    title = "Bandeja de Entrada",
                    icon = Icons.Outlined.Inbox,
                    route = "comunicaciones_profesor/bandeja",
                    description = "Ver mensajes recibidos"
                ),
                NavItem(
                    title = "Mensaje Individual",
                    icon = Icons.Outlined.Send,
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
            route = AppScreens.Calendario.route,
            description = "Gestión de eventos y actividades"
        ),
        NavItem(
            title = "Informes",
            icon = Icons.Filled.Description,
            route = "informes_profesor",
            description = "Generación de informes académicos",
            subItems = listOf(
                NavItem(
                    title = "Informe de Progreso",
                    icon = Icons.Outlined.TrendingUp,
                    route = "informes_profesor/progreso",
                    description = "Generar informe de progreso"
                ),
                NavItem(
                    title = "Boletín de Calificaciones",
                    icon = Icons.Outlined.Grade,
                    route = "informes_profesor/boletin",
                    description = "Crear boletín de calificaciones"
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
            description = "Ajustes de la aplicación",
            subItems = listOf(
                NavItem(
                    title = "Parámetros",
                    icon = Icons.Outlined.Tune,
                    route = AppScreens.Configuracion.route,
                    description = "Parámetros generales"
                ),
                NavItem(
                    title = "Soporte Técnico",
                    icon = Icons.Outlined.Help,
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

    // PERFIL FAMILIAR
    fun getFamiliarNavItems(): List<NavItem> = listOf(
        NavItem(
            title = "Inicio",
            icon = Icons.Filled.Home,
            route = AppScreens.FamiliarDashboard.route,
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
                    route = "estudiantes_familiar/perfiles",
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
            route = AppScreens.Calendario.route,
            description = "Calendario de eventos escolares",
            subItems = listOf(
                NavItem(
                    title = "Eventos Próximos",
                    icon = Icons.Outlined.Event,
                    route = "calendario_familiar/proximos",
                    description = "Ver próximos eventos"
                ),
                NavItem(
                    title = "Filtrar por Tipo",
                    icon = Icons.Outlined.FilterList,
                    route = "calendario_familiar/filtrar",
                    description = "Filtrar eventos por tipo"
                )
            )
        ),
        NavItem(
            title = "Comunicaciones",
            icon = Icons.Filled.Email,
            route = "comunicaciones_familiar",
            description = "Mensajería con profesores",
            subItems = listOf(
                NavItem(
                    title = "Bandeja de Entrada",
                    icon = Icons.Outlined.Inbox,
                    route = "comunicaciones_familiar/bandeja",
                    description = "Ver mensajes recibidos"
                ),
                NavItem(
                    title = "Nuevo Mensaje",
                    icon = Icons.Outlined.Send,
                    route = "comunicaciones_familiar/nuevo",
                    description = "Enviar nuevo mensaje"
                ),
                NavItem(
                    title = "Mensajes Enviados",
                    icon = Icons.Outlined.Mail,
                    route = "comunicaciones_familiar/enviados",
                    description = "Ver mensajes enviados"
                )
            )
        ),
        NavItem(
            title = "Registro Diario",
            icon = Icons.Filled.Book,
            route = "registro_diario_familiar",
            description = "Información diaria sobre actividades",
            subItems = listOf(
                NavItem(
                    title = "Ver Registro",
                    icon = Icons.Outlined.Visibility,
                    route = "registro_diario_familiar/ver",
                    description = "Ver registro diario"
                ),
                NavItem(
                    title = "Histórico",
                    icon = Icons.Outlined.History,
                    route = "registro_diario_familiar/historico",
                    description = "Ver histórico de registros"
                )
            )
        ),
        NavItem(
            title = "Informes",
            icon = Icons.Filled.Description,
            route = "informes_familiar",
            description = "Acceso a informes y documentos",
            subItems = listOf(
                NavItem(
                    title = "Boletines",
                    icon = Icons.Outlined.Grade,
                    route = "informes_familiar/boletines",
                    description = "Boletines de calificaciones"
                ),
                NavItem(
                    title = "Informes de Progreso",
                    icon = Icons.Outlined.TrendingUp,
                    route = "informes_familiar/progreso",
                    description = "Informes de progreso"
                ),
                NavItem(
                    title = "Documentos",
                    icon = Icons.Outlined.InsertDriveFile,
                    route = "informes_familiar/documentos",
                    description = "Documentos del centro"
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
            description = "Ajustes de la aplicación",
            subItems = listOf(
                NavItem(
                    title = "Parámetros",
                    icon = Icons.Outlined.Tune,
                    route = AppScreens.Configuracion.route,
                    description = "Parámetros generales"
                ),
                NavItem(
                    title = "Soporte Técnico",
                    icon = Icons.Outlined.Help,
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