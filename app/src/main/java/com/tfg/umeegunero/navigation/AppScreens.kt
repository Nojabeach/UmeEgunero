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
    object GestionCursos : AppScreens("gestion_cursos/{centroId}") {
        fun createRoute(centroId: String) = "gestion_cursos/$centroId"
    }
    object GestionClases : AppScreens("gestion_clases/{cursoId}") {
        fun createRoute(cursoId: String) = "gestion_clases/$cursoId"
    }
    object EditClase : AppScreens("edit_clase/{cursoId}?claseId={claseId}") {
        fun createRoute(cursoId: String, claseId: String? = null) = 
            if (claseId != null) "edit_clase/$cursoId?claseId=$claseId" else "edit_clase/$cursoId"
    }
    object Calendario : AppScreens("calendario")
    object Estadisticas : AppScreens("estadisticas")
    object Notificaciones : AppScreens("notificaciones")
    object Config : AppScreens("config")
    object Perfil : AppScreens("perfil")
    object Dummy : AppScreens("dummy/{title}") {
        fun createRoute(title: String) = "dummy/$title"
    }

    // Pantallas de detalle y chat
    object UserDetail : AppScreens("user_detail/{dni}") {
        fun createRoute(dni: String) = "user_detail/$dni"
    }
    object StudentDetail : AppScreens("student_detail/{alumnoId}") {
        fun createRoute(alumnoId: String) = "student_detail/$alumnoId"
    }
    object Chat : AppScreens("chat/{familiarId}/{alumnoId}") {
        fun createRoute(familiarId: String, alumnoId: String? = null) = 
            if (alumnoId != null) "chat/$familiarId/$alumnoId" else "chat/$familiarId"
    }
} 