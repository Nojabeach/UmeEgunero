package com.tfg.umeegunero.navigation

sealed class AppScreens(val route: String) {
    // Pantallas de acceso/autenticación
    object Welcome : AppScreens("welcome")
    object Login : AppScreens("login/{userType}") {
        fun createRoute(userType: String) = "login/$userType"
    }
    object Registro : AppScreens("registro")
    object SoporteTecnico : AppScreens("soporte_tecnico")

    // Pantallas principales según tipo de usuario
    object AdminDashboard : AppScreens("admin_dashboard")
    object CentroDashboard : AppScreens("centro_dashboard")
    object ProfesorDashboard : AppScreens("profesor_dashboard")
    object FamiliarDashboard : AppScreens("familiar_dashboard")

    // Pantallas de administración
    object AddCentro : AppScreens("add_centro")
    object EditCentro : AppScreens("edit_centro/{centroId}") {
        fun createRoute(centroId: String) = "edit_centro/$centroId"
    }
    object DetalleCentro : AppScreens("detalle_centro/{centroId}") {
        fun createRoute(centroId: String) = "detalle_centro/$centroId"
    }
    object AddUser : AppScreens("add_user/{isAdminApp}") {
        fun createRoute(isAdminApp: Boolean) = "add_user/$isAdminApp"
        fun createRoute(isAdminApp: Boolean, tipoUsuario: String) = "add_user/$isAdminApp?tipo=$tipoUsuario"
    }
    object EmailConfig : AppScreens("email_config")

    // Rutas anidadas para los dashboards
    object Cursos : AppScreens("admin_dashboard/cursos")
    object Clases : AppScreens("admin_dashboard/clases")
    object ProfesorList : AppScreens("admin_dashboard/profesores")
    object AlumnoList : AppScreens("admin_dashboard/alumnos")
    object FamiliarList : AppScreens("admin_dashboard/familiares")

    // Pantallas de gestión académica
    object AddCurso : AppScreens("add_curso/{centroId}") {
        fun createRoute(centroId: String) = "add_curso/$centroId"
    }
    object EditCurso : AppScreens("edit_curso/{centroId}/{cursoId}") {
        fun createRoute(centroId: String, cursoId: String) = "edit_curso/$centroId/$cursoId"
    }
    object AddClase : AppScreens("add_clase/{centroId}") {
        fun createRoute(centroId: String) = "add_clase/$centroId"
    }
    object EditClase : AppScreens("edit_clase/{centroId}/{claseId}") {
        fun createRoute(centroId: String, claseId: String) = "edit_clase/$centroId/$claseId"
    }
    
    // Pantallas de configuración
    object Config : AppScreens("config")

    // Otras pantallas
    object StudentDetail : AppScreens("student_detail/{studentId}") {
        fun createRoute(studentId: String) = "student_detail/$studentId"
    }
    object ReportDetail : AppScreens("report_detail/{reportId}") {
        fun createRoute(reportId: String) = "report_detail/$reportId"
    }
    object Chat : AppScreens("chat/{familiarId}/{alumnoId?}") {
        fun createRoute(familiarId: String, alumnoId: String? = null): String {
            return if (alumnoId != null) {
                "chat/$familiarId/$alumnoId"
            } else {
                "chat/$familiarId"
            }
        }
    }
    
    // Pantalla de detalles de usuario
    object UserDetail : AppScreens("user_detail/{dni}") {
        fun createRoute(dni: String) = "user_detail/$dni"
    }
    
    // Pantallas de estadísticas y notificaciones
    object Estadisticas : AppScreens("estadisticas")
    object Notificaciones : AppScreens("notificaciones")
    object Calendario : AppScreens("calendario")
    
    // Pantalla de perfil
    object Perfil : AppScreens("perfil")

    // Ruta para pantalla dummy de funcionalidades en desarrollo
    object Dummy : AppScreens("dummy/{title}") {
        fun createRoute(title: String): String {
            return "dummy/$title"
        }
    }

    // Pantalla de recuperación de contraseña
    object RecuperarPassword : AppScreens("recuperar_password")

    // Pantalla de soporte técnico
    object FAQ : AppScreens("preguntas_frecuentes")
} 