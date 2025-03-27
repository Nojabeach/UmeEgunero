package com.tfg.umeegunero.navigation

sealed class AppScreens(val route: String) {
    // Pantallas de acceso/autenticación
    object Welcome : AppScreens("welcome")
    object Login : AppScreens("login/{userType}") {
        fun createRoute(userType: String) = "login/$userType"
    }
    object Registro : AppScreens("registro")
    object SoporteTecnico : AppScreens("soporte_tecnico")
    object FAQ : AppScreens("faq")

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
    object EditUser : AppScreens("edit_user/{dni}") {
        fun createRoute(dni: String) = "edit_user/$dni"
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
    object AddCurso : AppScreens("add_curso/{centroId}?cursoId={cursoId}") {
        fun createRoute(centroId: String, cursoId: String? = null) = 
            "add_curso/$centroId${if (cursoId != null) "?cursoId=$cursoId" else ""}"
    }
    object GestionClases : AppScreens("gestion_clases/{cursoId}") {
        fun createRoute(cursoId: String) = "gestion_clases/$cursoId"
    }
    object AddClase : AppScreens("add_clase/{cursoId}?claseId={claseId}") {
        fun createRoute(cursoId: String, claseId: String? = null) = 
            "add_clase/$cursoId${if (claseId != null) "?claseId=$claseId" else ""}"
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

    object GestionCentros : AppScreens("gestion_centros")
    object GestionAcademica : AppScreens("gestion_academica")
    object Configuracion : AppScreens("configuracion")
    object ListaAlumnos : AppScreens("lista_alumnos")
    object ListaProfesores : AppScreens("lista_profesores")
    object ListaFamiliares : AppScreens("lista_familiares")
    object DetalleUsuario : AppScreens("detalle_usuario/{userId}") {
        fun createRoute(userId: String) = "detalle_usuario/$userId"
    }
    object DetalleAlumno : AppScreens("detalle_alumno/{alumnoId}") {
        fun createRoute(alumnoId: String) = "detalle_alumno/$alumnoId"
    }
    object DetalleFamiliar : AppScreens("detalle_familiar/{familiarId}/{alumnoId}") {
        fun createRoute(familiarId: String, alumnoId: String) = "detalle_familiar/$familiarId/$alumnoId"
    }
} 