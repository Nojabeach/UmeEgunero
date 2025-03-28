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
    object AdminList : AppScreens("admin_dashboard/administradores")

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

    // Pantallas de comunicaciones
    object Comunicados : AppScreens("comunicaciones/comunicados")
    
    // Pantallas de reportes
    object ReporteUso : AppScreens("reportes/uso")
    object ReporteRendimiento : AppScreens("reportes/rendimiento")
    
    // Nuevas pantallas para gestión de alumnos y vinculación familiar
    object AddAlumno : AppScreens("add_alumno")
    object EditAlumno : AppScreens("edit_alumno/{alumnoId}") {
        fun createRoute(alumnoId: String) = "edit_alumno/$alumnoId"
    }
    object VinculacionFamiliar : AppScreens("vinculacion_familiar")
    object GestionCursosYClases : AppScreens("gestion_cursos_clases")
    object GestionNotificacionesCentro : AppScreens("gestion_notificaciones_centro")

    // Pantallas para el profesor
    object GestionAlumnos : AppScreens("gestion_alumnos_profesor")
    object CalendarioProfesor : AppScreens("calendario_profesor")
    object TareasProfesor : AppScreens("tareas_profesor")
    object AsistenciaClase : AppScreens("asistencia_clase") // Nueva pantalla de asistencia

    // Rutas del profesor
    object ProfesorAsistencia : AppScreens("profesor_asistencia")
    object ProfesorTareas : AppScreens("profesor_tareas")
    object ProfesorCalendario : AppScreens("profesor_calendario")

    // Pantallas del profesor
    object AsistenciaProfesor : AppScreens("asistencia_profesor")
    object ChatProfesor : AppScreens("chat_profesor")
    object DetalleAlumnoProfesor : AppScreens("detalle_alumno_profesor/{alumnoId}") {
        fun createRoute(alumnoId: String) = "detalle_alumno_profesor/$alumnoId"
    }
    
    // Pantallas para los registros diarios
    object RegistroDiario : AppScreens("registro_diario/{alumnoId}/{claseId}/{profesorId}/{alumnoNombre}/{claseNombre}") {
        fun createRoute(
            alumnoId: String, 
            claseId: String, 
            profesorId: String,
            alumnoNombre: String,
            claseNombre: String
        ) = "registro_diario/$alumnoId/$claseId/$profesorId/$alumnoNombre/$claseNombre"
    }
    
    object ConsultaRegistroDiario : AppScreens("consulta_registro_diario/{alumnoId}/{alumnoNombre}") {
        fun createRoute(alumnoId: String, alumnoNombre: String) = 
            "consulta_registro_diario/$alumnoId/$alumnoNombre"
    }
    
    // Pantallas de la familia
    object FamiliaDashboard : AppScreens("familia_dashboard")
    object DetalleAlumnoFamilia : AppScreens("detalle_alumno_familia/{alumnoId}") {
        fun createRoute(alumnoId: String) = "detalle_alumno_familia/$alumnoId"
    }
    object CalendarioFamilia : AppScreens("calendario_familia")
    object TareasFamilia : AppScreens("tareas_familia")
    object ChatFamilia : AppScreens("chat_familia")
    object NotificacionesFamilia : AppScreens("notificaciones_familia")

    object GestionProfesores : AppScreens("gestion_profesores")
} 