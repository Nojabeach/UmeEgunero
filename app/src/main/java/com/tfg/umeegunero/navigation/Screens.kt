package com.tfg.umeegunero.navigation

/**
 * Clase que define las rutas de navegación en la aplicación.
 */
sealed class Screens(val route: String) {
    // Pantallas base
    object Welcome : Screens("welcome")
    object SoporteTecnico : Screens("soporte_tecnico")
    object FAQ : Screens("faq")
    
    // Autenticación
    object Login : Screens("login/{userType}") {
        fun createRoute(userType: String) = "login/$userType"
    }
    object Registro : Screens("registro")
    
    // Dashboards
    object AdminDashboard : Screens("admin_dashboard")
    object CentroDashboard : Screens("centro_dashboard")
    object ProfesorDashboard : Screens("profesor_dashboard")
    object FamiliarDashboard : Screens("familiar_dashboard")
    
    // Pantallas Admin
    object GestionCentros : Screens("gestion_centros")
    object Estadisticas : Screens("estadisticas")
    object Notificaciones : Screens("notificaciones")
    object EmailConfig : Screens("email_config")
    object Comunicados : Screens("comunicados")
    object ReporteUso : Screens("reporte_uso")
    object ReporteRendimiento : Screens("reporte_rendimiento")
    object Seguridad : Screens("seguridad")
    
    // Gestión de entidades
    object AddCentro : Screens("add_centro")
    object DetalleCentro : Screens("detalle_centro/{centroId}") {
        fun createRoute(centroId: String) = "detalle_centro/$centroId"
    }
    object EditCentro : Screens("edit_centro/{centroId}") {
        fun createRoute(centroId: String) = "edit_centro/$centroId"
    }
    
    object GestionCursos : Screens("gestion_cursos/{centroId}") {
        fun createRoute(centroId: String) = "gestion_cursos/$centroId"
    }
    object GestionClases : Screens("gestion_clases/{cursoId}") {
        fun createRoute(cursoId: String) = "gestion_clases/$cursoId"
    }
    object AddCurso : Screens("add_curso/{centroId}") {
        fun createRoute(centroId: String) = "add_curso/$centroId"
    }
    object EditCurso : Screens("edit_curso/{cursoId}") {
        fun createRoute(cursoId: String) = "edit_curso/$cursoId"
    }
    object AddClase : Screens("add_clase/{cursoId}") {
        fun createRoute(cursoId: String) = "add_clase/$cursoId"
    }
    object EditClase : Screens("edit_clase/{claseId}") {
        fun createRoute(claseId: String) = "edit_clase/$claseId"
    }
    
    // Gestión de usuarios
    object ProfesorList : Screens("profesores")
    object AlumnoList : Screens("alumnos")
    object FamiliarList : Screens("familiares")
    object AdminList : Screens("administradores")
    object UserDetail : Screens("user_detail/{dni}") {
        fun createRoute(dni: String) = "user_detail/$dni"
    }
    object AddUser : Screens("add_user/{userType}") {
        fun createRoute(userType: String) = "add_user/$userType"
    }
    
    // Pantallas Profesor
    object Calendario : Screens("calendario")
    object Asistencia : Screens("asistencia")
    object Tareas : Screens("tareas")
    object RegistroDiario : Screens("registro_diario")
    
    // Pantallas Familia
    object DetalleAlumno : Screens("detalle_alumno/{alumnoId}") {
        fun createRoute(alumnoId: String) = "detalle_alumno/$alumnoId"
    }
    object DetalleFamiliar : Screens("detalle_familiar/{familiarId}/{alumnoId}") {
        fun createRoute(familiarId: String, alumnoId: String) = "detalle_familiar/$familiarId/$alumnoId"
    }
    object CalendarioFamilia : Screens("calendario_familia")
    object TareasFamilia : Screens("tareas_familia")
    object ChatFamilia : Screens("chat_familia")
    object NotificacionesFamilia : Screens("notificaciones_familia")
    object ConsultaRegistro : Screens("consulta_registro")
    
    // Pantallas Centro
    object GestionAcademica : Screens("gestion_academica")
    object GestionProfesores : Screens("gestion_profesores")
    object VinculacionFamiliar : Screens("vinculacion_familiar")
    object AddAlumno : Screens("add_alumno")
    object NotificacionesCentro : Screens("notificaciones_centro")
    
    // Comunes
    object Configuracion : Screens("configuracion")
    object Perfil : Screens("perfil")
    
    // Dummys para desarrollo
    object Cursos : Screens("cursos")
    object Clases : Screens("clases")
} 