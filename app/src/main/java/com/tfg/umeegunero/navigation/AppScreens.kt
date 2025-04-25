package com.tfg.umeegunero.navigation

import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Representa las diferentes pantallas y rutas de navegación de la aplicación.
 * 
 * Esta clase sealed contiene objetos que definen todas las rutas posibles dentro
 * de la aplicación, organizadas por funcionalidad y tipo de usuario. Cada objeto
 * tiene un parámetro [route] que define la ruta URL utilizada por Navigation Compose.
 * 
 * Las rutas pueden incluir:
 * - Rutas simples: Solo contienen el nombre de la ruta
 * - Rutas parametrizadas: Contienen partes variables indicadas entre llaves {param}
 * - Rutas con argumentos opcionales: Utilizan notación de query parameters con ?param=value
 * 
 * Para rutas parametrizadas, se incluyen funciones de ayuda [createRoute] que facilitan
 * la construcción de la ruta con los parámetros adecuados.
 */
sealed class AppScreens(val route: String) {
    /**
     * Sección: Pantallas de acceso y autenticación
     * 
     * Incluye pantallas para el inicio, autenticación y soporte al usuario
     */
    /** Pantalla de bienvenida inicial de la aplicación */
    object Welcome : AppScreens("welcome")
    
    /** 
     * Pantalla de inicio de sesión con parámetro de tipo de usuario 
     * @param userType Tipo de usuario (admin, centro, profesor, familiar)
     */
    object Login : AppScreens("login/{userType}") {
        fun createRoute(userType: String) = "login/$userType"
    }
    
    /** Pantalla de registro de nuevos usuarios */
    object Registro : AppScreens("registro")
    
    /** Pantalla de soporte técnico con formulario de contacto */
    object SoporteTecnico : AppScreens("soporte_tecnico")
    
    /** Pantalla de preguntas frecuentes */
    object FAQ : AppScreens("faq")

    /**
     * Sección: Pantallas principales (dashboards)
     * 
     * Pantallas principales para cada tipo de usuario que actúan como punto de entrada
     * después de la autenticación
     */
    /** Dashboard principal para administradores del sistema */
    object AdminDashboard : AppScreens("admin_dashboard")
    
    /** Dashboard principal para responsables de centros educativos */
    object CentroDashboard : AppScreens("centro_dashboard")
    
    /** Dashboard principal para profesores */
    object ProfesorDashboard : AppScreens("profesor_dashboard")
    
    /** Dashboard principal para familiares */
    object FamiliarDashboard : AppScreens("familia_dashboard")

    /**
     * Sección: Pantallas de administración
     * 
     * Pantallas para la gestión administrativa de centros y usuarios
     */
    /** Formulario para añadir un nuevo centro educativo */
    object AddCentro : AppScreens("add_centro")
    
    /** 
     * Formulario para editar un centro educativo existente
     * @param centroId Identificador único del centro a editar
     */
    object EditCentro : AppScreens("edit_centro/{centroId}") {
        fun createRoute(centroId: String) = "edit_centro/$centroId"
    }
    
    /** 
     * Pantalla de detalles de un centro educativo
     * @param centroId Identificador único del centro
     */
    object DetalleCentro : AppScreens("detalle_centro/{centroId}") {
        fun createRoute(centroId: String) = "detalle_centro/$centroId"
    }
    
    /**
     * Formulario para añadir un nuevo usuario al sistema
     * @param isAdminApp Indica si el usuario será administrador de la aplicación
     * @param tipoUsuario Tipo de usuario (opcional): centro, profesor, familiar
     */
    object AddUser : AppScreens("add_user/{isAdminApp}") {
        fun createRoute(isAdminApp: Boolean) = "add_user/$isAdminApp"
        fun createRoute(isAdminApp: Boolean, tipoUsuario: String) = "add_user/$isAdminApp?tipo=$tipoUsuario"
    }
    
    /**
     * Formulario para editar un usuario existente
     * @param dni Identificador único del usuario (DNI)
     */
    object EditUser : AppScreens("edit_user/{dni}") {
        fun createRoute(dni: String) = "edit_user/$dni"
    }
    
    /** Pantalla de configuración del sistema de correo electrónico */
    object EmailConfig : AppScreens("email_config")

    /**
     * Sección: Rutas anidadas para los dashboards
     * 
     * Subrutas que se utilizan dentro de los dashboards para navegación interna
     */
    /** Lista de cursos dentro del dashboard de administración */
    object Cursos : AppScreens("admin_dashboard/cursos")
    
    /** Lista de clases dentro del dashboard de administración */
    object Clases : AppScreens("admin_dashboard/clases")
    
    /** Lista de profesores dentro del dashboard de administración */
    object ProfesorList : AppScreens("admin_dashboard/profesores")
    
    /** Lista de alumnos dentro del dashboard de administración */
    object AlumnoList : AppScreens("admin_dashboard/alumnos")
    
    /** Lista de familiares dentro del dashboard de administración */
    object FamiliarList : AppScreens("admin_dashboard/familiares")
    
    /** Lista de administradores dentro del dashboard de administración */
    object AdminList : AppScreens("admin_dashboard/administradores")
    
    /** Lista de administradores de centro dentro del dashboard de administración */
    object AdminCentroList : AppScreens("admin_dashboard/administradores_centro")

    /**
     * Sección: Pantallas de gestión académica
     * 
     * Pantallas para la gestión de cursos, clases y estructura académica
     */
    /**
     * Pantalla de gestión de cursos para un centro específico
     * @param centroId Identificador único del centro
     */
    object GestionCursos : AppScreens("gestion_cursos/{centroId}") {
        fun createRoute(centroId: String) = "gestion_cursos/$centroId"
    }
    
    /**
     * Formulario para añadir o editar un curso
     * @param centroId Identificador único del centro
     * @param cursoId Identificador del curso (opcional, para edición)
     */
    object AddCurso : AppScreens("add_curso/{centroId}?cursoId={cursoId}") {
        fun createRoute(centroId: String) = "add_curso/$centroId"
        fun createRoute(centroId: String, cursoId: String) = "add_curso/$centroId?cursoId=$cursoId"
    }
    
    /**
     * Pantalla de gestión de clases para un curso específico
     * @param cursoId Identificador único del curso
     */
    object GestionClases : AppScreens("gestion_clases/{cursoId}") {
        fun createRoute(cursoId: String) = "gestion_clases/$cursoId"
    }
    
    /**
     * Formulario para añadir una clase a un curso
     * @param cursoId Identificador único del curso
     * @param centroId Identificador único del centro
     */
    object AddClase : AppScreens("add_clase/{cursoId}/{centroId}") {
        fun createRoute(cursoId: String, centroId: String) = "add_clase/$cursoId/$centroId"
    }
    
    /**
     * Formulario para editar una clase existente
     * @param cursoId Identificador único del curso
     * @param claseId Identificador de la clase a editar
     */
    object EditClase : AppScreens("edit_clase/{claseId}") {
        fun createRoute(claseId: String) = "edit_clase/$claseId"
    }
    
    /**
     * Detalles de una clase específica
     * @param claseId Identificador único de la clase
     */
    object DetalleClase : AppScreens("detalle_clase/{claseId}") {
        fun createRoute(claseId: String) = "detalle_clase/$claseId"
    }
    
    /** Pantalla de calendario y eventos académicos */
    object Calendario : AppScreens("calendario")
    
    /**
     * Pantalla de detalle y edición de evento
     * @param eventoId Identificador único del evento
     */
    object DetalleEvento : AppScreens("detalle_evento/{eventoId}") {
        fun createRoute(eventoId: String) = "detalle_evento/$eventoId"
    }
    
    /**
     * Pantalla de detalle de día con eventos
     * @param fecha Fecha del día a mostrar
     */
    object DetalleDiaEvento : AppScreens("detalle_dia_evento/{fecha}") {
        const val Fecha = "fecha"
        fun createRoute(fecha: String) = "detalle_dia_evento/$fecha"
    }
    
    /** Pantalla de estadísticas y análisis de datos */
    object Estadisticas : AppScreens("estadisticas")
    
    /** Pantalla de notificaciones del sistema */
    object Notificaciones : AppScreens("notificaciones")
    
    /** Pantalla de configuración de la aplicación */
    object Config : AppScreens("config")
    
    /** Pantalla de perfil del usuario actual */
    object Perfil : AppScreens("perfil")
    
    /** 
     * Pantalla de cambio de contraseña
     * @param dni DNI del usuario que cambiará su contraseña
     */
    object CambioContrasena : AppScreens("cambio_contrasena/{dni}") {
        fun createRoute(dni: String) = "cambio_contrasena/$dni"
        
        val arguments = listOf(
            navArgument("dni") { type = NavType.StringType }
        )
    }

    /**
     * Pantalla de visualización de documentos
     * @param url URL del documento a visualizar (codificada)
     * @param nombre Nombre opcional del documento (codificado)
     */
    object VisualizadorDocumento : AppScreens("documento/{url}?nombre={nombre}") {
        fun createRoute(url: String, nombre: String? = null): String {
            val urlEncoded = android.net.Uri.encode(url)
            return if (nombre != null) {
                "documento/$urlEncoded?nombre=${android.net.Uri.encode(nombre)}"
            } else {
                "documento/$urlEncoded"
            }
        }
    }

    /**
     * Sección: Pantallas de detalle y chat
     * 
     * Pantallas para ver detalles de usuarios y comunicación
     */
    /**
     * Detalles de un usuario
     * @param dni Identificador único del usuario
     */
    object UserDetail : AppScreens("user_detail/{dni}") {
        fun createRoute(dni: String) = "user_detail/$dni"
    }
    
    /**
     * Detalles de un estudiante
     * @param alumnoId Identificador único del alumno
     */
    object StudentDetail : AppScreens("student_detail/{alumnoId}") {
        fun createRoute(alumnoId: String) = "student_detail/$alumnoId"
    }
    
    /**
     * Pantalla de chat entre usuarios
     * @param familiarId Identificador del familiar
     * @param alumnoId Identificador del alumno (opcional)
     */
    object Chat : AppScreens("chat/{familiarId}/{alumnoId}") {
        fun createRoute(familiarId: String, alumnoId: String? = null) = 
            if (alumnoId != null) "chat/$familiarId/$alumnoId" else "chat/$familiarId"
    }

    /** Pantalla de gestión de centros educativos */
    object GestionCentros : AppScreens("gestion_centros")
    
    /** Pantalla de gestión académica general */
    object GestionAcademica : AppScreens("gestion_academica")
    
    /** Pantalla de configuración general */
    object Configuracion : AppScreens("configuracion")
    
    /** Lista de alumnos del sistema */
    object ListaAlumnos : AppScreens("lista_alumnos")
    
    /** Lista de profesores del sistema */
    object ListaProfesores : AppScreens("lista_profesores")
    
    /** Lista de familiares del sistema */
    object ListaFamiliares : AppScreens("lista_familiares")
    
    /**
     * Detalles de un usuario general
     * @param userId Identificador único del usuario
     */
    object DetalleUsuario : AppScreens("detalle_usuario/{userId}") {
        fun createRoute(userId: String) = "detalle_usuario/$userId"
    }
    
    /**
     * Detalles de un alumno
     * @param alumnoId Identificador único del alumno
     */
    object DetalleAlumno : AppScreens("detalle_alumno/{alumnoId}") {
        fun createRoute(alumnoId: String) = "detalle_alumno/$alumnoId"
    }
    
    /**
     * Detalles de un familiar y su relación con un alumno
     * @param familiarId Identificador único del familiar
     * @param alumnoId Identificador único del alumno relacionado
     */
    object DetalleFamiliar : AppScreens("detalle_familiar/{familiarId}/{alumnoId}") {
        fun createRoute(familiarId: String, alumnoId: String) = "detalle_familiar/$familiarId/$alumnoId"
    }

    /**
     * Sección: Pantallas de comunicaciones y reportes
     */
    /** Pantalla de comunicados y circulares */
    object ComunicadosCirculares : AppScreens("comunicaciones/comunicados")
    
    /** Reporte de uso del sistema */
    object ReporteUso : AppScreens("reportes/uso")
    
    /**
     * Sección: Gestión de alumnos y vinculación familiar
     */
    /** Formulario para añadir un nuevo alumno */
    object AddAlumno : AppScreens("add_alumno")
    
    /**
     * Formulario para editar un alumno existente
     * @param alumnoId Identificador único del alumno
     */
    object EditAlumno : AppScreens("edit_alumno/{alumnoId}") {
        fun createRoute(alumnoId: String) = "edit_alumno/$alumnoId"
    }
    
    /** Pantalla para vincular alumnos con familiares */
    object VincularAlumnoFamiliar : AppScreens("vincular_alumno_familiar")
    
    /** Gestión unificada de cursos y clases */
    object GestionCursosYClases : AppScreens("gestion_cursos_clases")
    
    /** Gestión de notificaciones a nivel de centro */
    object GestionNotificacionesCentro : AppScreens("gestion_notificaciones_centro")

    /**
     * Pantalla para que el familiar entregue una tarea en nombre del alumno
     * @param tareaId Identificador único de la tarea
     * @param alumnoId Identificador único del alumno
     */
    object EntregaTarea : AppScreens("entrega_tarea/{tareaId}/{alumnoId}") {
        fun createRoute(tareaId: String, alumnoId: String) = "entrega_tarea/$tareaId/$alumnoId"
    }

    /**
     * Sección: Pantallas específicas para profesores
     */
    /** Gestión de alumnos por parte del profesor */
    object GestionAlumnos : AppScreens("gestion_alumnos_profesor")
    
    /** Calendario específico para profesores */
    object CalendarioProfesor : AppScreens("calendario_profesor")
    
    /** Gestión de tareas para profesores */
    object TareasProfesor : AppScreens("tareas_profesor")
    
    /** Control de asistencia de clase */
    object AsistenciaClase : AppScreens("asistencia_clase")

    /** Sección de asistencia en el dashboard del profesor */
    object ProfesorAsistencia : AppScreens("profesor_asistencia")
    
    /** Sección de tareas en el dashboard del profesor */
    object ProfesorTareas : AppScreens("profesor_tareas")
    
    /** Sección de calendario en el dashboard del profesor */
    object ProfesorCalendario : AppScreens("profesor_calendario")

    /** Pantalla de registro de asistencia para el profesor */
    object AsistenciaProfesor : AppScreens("asistencia_profesor")
    
    /** Pantalla de chat para el profesor */
    object ChatProfesor : AppScreens("chat_profesor/{conversacionId}/{participanteId}") {
        fun createRoute(conversacionId: String, participanteId: String) = 
            "chat_profesor/$conversacionId/$participanteId"
    }
    
    /** Pantalla de conversaciones para el profesor */
    object ConversacionesProfesor : AppScreens("conversaciones_profesor")
    
    /**
     * Detalles de un alumno desde la perspectiva del profesor
     * @param alumnoId Identificador único del alumno
     */
    object DetalleAlumnoProfesor : AppScreens("detalle_alumno_profesor/{alumnoId}") {
        fun createRoute(alumnoId: String) = "detalle_alumno_profesor/$alumnoId"
    }
    
    /**
     * Sección: Registro diario de actividades
     */
    /**
     * Pantalla para registrar actividad diaria de un alumno
     * @param alumnoId Identificador del alumno
     * @param claseId Identificador de la clase
     * @param profesorId Identificador del profesor
     * @param alumnoNombre Nombre del alumno (para mostrar)
     * @param claseNombre Nombre de la clase (para mostrar)
     */
    object RegistroDiario : AppScreens("registro_diario/{alumnoId}/{claseId}/{profesorId}/{alumnoNombre}/{claseNombre}") {
        fun createRoute(
            alumnoId: String, 
            claseId: String, 
            profesorId: String,
            alumnoNombre: String,
            claseNombre: String
        ) = "registro_diario/$alumnoId/$claseId/$profesorId/$alumnoNombre/$claseNombre"
    }
    
    /**
     * Consulta del historial de registros diarios
     * @param alumnoId Identificador del alumno
     * @param alumnoNombre Nombre del alumno (para mostrar)
     */
    object ConsultaRegistroDiario : AppScreens("consulta_registro_diario/{alumnoId}/{alumnoNombre}") {
        fun createRoute(alumnoId: String, alumnoNombre: String) = 
            "consulta_registro_diario/$alumnoId/$alumnoNombre"
    }
    
    /**
     * Sección: Pantallas específicas para familiares
     */
    /**
     * Detalles de un alumno desde la perspectiva del familiar
     * @param alumnoId Identificador único del alumno
     */
    object DetalleAlumnoFamilia : AppScreens("detalle_alumno_familia/{alumnoId}") {
        fun createRoute(alumnoId: String) = "detalle_alumno_familia/$alumnoId"
    }
    
    /** Calendario específico para familiares */
    object CalendarioFamilia : AppScreens("calendario_familia")
    
    /** Vista de tareas para familiares */
    object TareasFamilia : AppScreens("tareas_familia")
    
    /** Pantalla de Chat para la familia */
    object ChatFamilia : AppScreens("chat_familia/{conversacionId}/{participanteId}") {
        fun createRoute(conversacionId: String, participanteId: String) = 
            "chat_familia/$conversacionId/$participanteId"
    }
    
    /** Pantalla de conversaciones para la familia */
    object ConversacionesFamilia : AppScreens("conversaciones_familia")
    
    /** Notificaciones para familiares */
    object NotificacionesFamilia : AppScreens("notificaciones_familia")
    
    /** Comunicados para familiares */
    object ComunicadosFamilia : AppScreens("comunicados_familia")

    /** Pantalla de gestión de profesores */
    object GestionProfesores : AppScreens("gestion_profesores")

    /** Registros de actividad diaria */
    object RegistroActividad : AppScreens("registro_actividad")
    
    /**
     * Sección: Actividades preescolares (2-3 años)
     */
    /** Visualización de actividades preescolares para familiares */
    object ActividadesPreescolar : AppScreens("actividades_preescolar")
    
    /** Gestión de actividades preescolares para profesores */
    object ActividadesPreescolarProfesor : AppScreens("actividades_preescolar_profesor")
    
    /**
     * Sección: Tareas académicas
     */
    /** 
     * Detalles de una tarea para profesores, muestra entregas de alumnos 
     * @param tareaId Identificador único de la tarea
     */
    object DetalleTareaProfesor : AppScreens("detalle_tarea_profesor/{tareaId}") {
        fun createRoute(tareaId: String) = "detalle_tarea_profesor/$tareaId"
    }
    
    /**
     * Detalles de una tarea para alumnos, muestra información de la tarea para entregar
     * @param tareaId Identificador único de la tarea
     */
    object DetalleTareaAlumno : AppScreens("detalle_tarea_alumno/{tareaId}") {
        fun createRoute(tareaId: String) = "detalle_tarea_alumno/$tareaId"
    }

    /**
     * Pantalla genérica para pruebas y desarrollo
     * @param title Título a mostrar en la pantalla
     */
    object Dummy : AppScreens("dummy/{title}") {
        fun createRoute(title: String) = "dummy/$title"
    }

    /**
     * Sección: Pantallas de mensajería
     */
    /** Pantalla de bandeja de entrada de mensajes */
    object BandejaEntrada : AppScreens("bandeja_entrada")
    
    /**
     * Pantalla para componer un nuevo mensaje
     * @param destinatarioId ID del destinatario (opcional)
     */
    object ComponerMensaje : AppScreens("componer_mensaje?destinatario={destinatarioId}") {
        fun createRoute() = "componer_mensaje"
        fun createRoute(destinatarioId: String) = "componer_mensaje?destinatario=$destinatarioId"
    }
    
    /**
     * Detalle de un mensaje recibido
     * @param mensajeId ID del mensaje
     */
    object DetalleMensaje : AppScreens("detalle_mensaje/{mensajeId}") {
        fun createRoute(mensajeId: String) = "detalle_mensaje/$mensajeId"
    }
    
    /**
     * Sección: Pantallas de tareas y evaluación
     */
    /** Lista de tareas académicas */
    object Tareas : AppScreens("tareas")
    
    /**
     * Detalle de una tarea académica
     * @param tareaId ID de la tarea
     */
    object DetalleTarea : AppScreens("detalle_tarea/{tareaId}") {
        fun createRoute(tareaId: String) = "detalle_tarea/$tareaId"
    }
    
    /** Pantalla de evaluaciones */
    object Evaluacion : AppScreens("evaluacion")

    /** Gestión de cursos para el centro educativo */
    object ListaCursos : AppScreens("gestion_centro/cursos")

    /** Gestión de clases para el centro educativo */
    object ListaClases : AppScreens("gestion_centro/clases")

    /** Lista de profesores para vincular a clases */
    object ListaProfesoresClases : AppScreens("gestion_centro/profesores_clases")

    /** Lista de alumnos para vincular a familiares */
    object ListaAlumnosFamilias : AppScreens("gestion_centro/alumnos_familias")

    /** Creación rápida de usuarios */
    object CrearUsuarioRapido : AppScreens("gestion_centro/crear_usuario")

    /** Pantalla para vincular profesores a clases */
    object VincularProfesorClase : AppScreens("vincular_profesor_clase")
    
    /** Pantalla de configuración de seguridad */
    object Seguridad : AppScreens("seguridad")
    
    /** Pantalla de configuración de email de soporte */
    object EmailConfigSoporte : AppScreens("email_config_soporte")
    
    /** Pantalla de gestión de usuarios */
    object GestionUsuarios : AppScreens("gestion_usuarios")

    // Pantallas dummy para funcionalidades en desarrollo
    object DummyGestionCursos : AppScreens("dummy_gestion_cursos")
    object DummyGestionClases : AppScreens("dummy_gestion_clases")
    object DummyGestionUsuarios : AppScreens("dummy_gestion_usuarios")
    object DummyEstadisticas : AppScreens("dummy_estadisticas")
    object DummyConfiguracion : AppScreens("dummy_configuracion")
} 