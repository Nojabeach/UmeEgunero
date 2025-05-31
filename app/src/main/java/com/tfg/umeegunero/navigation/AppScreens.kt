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

    /** Pantalla de términos y condiciones */
    object TerminosCondiciones : AppScreens("terminos_condiciones")

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
     * @param centroId ID del centro (opcional, para preselección y bloqueo)
     * @param centroBloqueado Indica si el selector de centro debe estar bloqueado (opcional)
     * @param dniUsuario DNI del usuario a editar (opcional, si se proporciona se abrirá en modo edición)
     */
    object AddUser : AppScreens("add_user/{isAdminApp}?tipo={tipoUsuario}&centroId={centroId}&centroBloqueado={centroBloqueado}&dni={dniUsuario}") {
        const val ARG_IS_ADMIN_APP = "isAdminApp"
        const val ARG_TIPO_USUARIO = "tipoUsuario"
        const val ARG_CENTRO_ID = "centroId"
        const val ARG_CENTRO_BLOQUEADO = "centroBloqueado"
        const val ARG_DNI_USUARIO = "dniUsuario"

        fun createRoute(
            isAdminApp: Boolean,
            tipoUsuario: String? = null,
            centroId: String? = null,
            centroBloqueado: Boolean? = null,
            dniUsuario: String? = null
        ): String {
            var route = "add_user/$isAdminApp"
            val params = mutableListOf<String>()
            tipoUsuario?.let { params.add("tipo=$it") }
            centroId?.let { params.add("centroId=$it") }
            centroBloqueado?.let { params.add("centroBloqueado=$it") }
            dniUsuario?.let { params.add("dni=$it") }
            if (params.isNotEmpty()) {
                route += "?" + params.joinToString("&")
            }
            return route
        }

        val arguments = listOf(
            navArgument(ARG_IS_ADMIN_APP) { type = NavType.BoolType },
            navArgument(ARG_TIPO_USUARIO) { type = NavType.StringType; nullable = true },
            navArgument(ARG_CENTRO_ID) { type = NavType.StringType; nullable = true },
            navArgument(ARG_CENTRO_BLOQUEADO) { type = NavType.BoolType; defaultValue = false },
            navArgument(ARG_DNI_USUARIO) { type = NavType.StringType; nullable = true }
        )
    }
    
    /**
     * Formulario para editar un usuario existente
     * @param dni Identificador único del usuario (DNI)
     * @param tipoUsuario Tipo de usuario que se está editando (opcional)
     */
    object EditUser : AppScreens("edit_user/{dni}?tipoUsuario={tipoUsuario}") {
        fun createRoute(dni: String, tipoUsuario: String? = null): String {
            return if (tipoUsuario != null) {
                "edit_user/$dni?tipoUsuario=$tipoUsuario"
            } else {
                "edit_user/$dni"
            }
        }
        
        val arguments = listOf(
            navArgument("dni") { type = NavType.StringType },
            navArgument("tipoUsuario") { 
                type = NavType.StringType 
                nullable = true
                defaultValue = null
            }
        )
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
    object ProfesorList : AppScreens("admin_dashboard/profesores/{centroId}") {
        const val ARG_CENTRO_ID = "centroId"
        fun createRoute(centroId: String) = "admin_dashboard/profesores/$centroId"

        val arguments = listOf(
            navArgument(ARG_CENTRO_ID) { type = NavType.StringType }
        )
    }
    
    /** Lista de alumnos dentro del dashboard de administración */
    object AlumnoList : AppScreens("alumnos/{centroId}") {
        const val ARG_CENTRO_ID = "centroId"
        fun createRoute(centroId: String) = "alumnos/$centroId"

        val arguments = listOf(
            navArgument(ARG_CENTRO_ID) { type = NavType.StringType }
        )
    }
    
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
    /** Pantalla de gestión de cursos con selector de centro integrado */
    object GestionCursos : AppScreens("gestion_cursos")
    
    /**
     * Formulario para añadir o editar un curso
     * @param centroId Identificador único del centro
     * @param cursoId Identificador del curso (opcional, para edición)
     */
    object AddCurso : AppScreens("add_curso/{centroId}") {
        fun createRoute(centroId: String) = "add_curso/$centroId"
    }
    
    /**
     * Pantalla de calendario y eventos académicos
     */
    object Calendario : AppScreens("calendario")
    
    /**
     * Pantalla de detalle y edición de evento
     * @param eventoId Identificador único del evento
     */
    object DetalleEvento : AppScreens("detalle_evento/{eventoId}") {
        fun createRoute(eventoId: String) = "detalle_evento/$eventoId"
    }
    
    /**
     * Pantalla de detalle de clase
     * @param claseId Identificador único de la clase
     */
    object DetalleClase : AppScreens("detalle_clase/{claseId}") {
        fun createRoute(claseId: String) = "detalle_clase/$claseId"
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
    
    /** Pantalla de prueba de envío de emails */
    object PruebaEmail : AppScreens("prueba_email")
    
    /** Pantalla de configuración de la aplicación */
    object Config : AppScreens("config")
    
    /** 
     * Pantalla de perfil del usuario 
     * @param isAdminApp Indica si el usuario es administrador de la aplicación
     */
    object Perfil : AppScreens("perfil/{isAdminApp}") {
        fun createRoute(isAdminApp: Boolean) = "perfil/$isAdminApp"
        
        val arguments = listOf(
            navArgument("isAdminApp") { 
                type = NavType.BoolType
                defaultValue = false
            }
        )
    }
    
    /**
     * Acceso directo a la pantalla de perfil sin parámetros
     */
    object PerfilScreen : AppScreens("perfil_screen")
    
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
     * Chat con otro usuario (profesor, familiar, etc.)
     */
    object Chat : AppScreens("chat/{conversacionId}/{participanteId}?alumnoId={alumnoId}") {
        fun createRoute(participanteId: String, conversacionId: String, alumnoId: String? = null): String {
            return if (alumnoId != null) {
                "chat/$conversacionId/$participanteId?alumnoId=$alumnoId"
            } else {
                "chat/$conversacionId/$participanteId"
            }
        }
        
        val arguments = listOf(
            navArgument("conversacionId") { type = NavType.StringType },
            navArgument("participanteId") { type = NavType.StringType },
            navArgument("alumnoId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
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
     * @param dni Identificador único del alumno
     */
    object DetalleAlumno : AppScreens("detalle_alumno/{dni}") {
        fun createRoute(dni: String) = "detalle_alumno/$dni"

        val arguments = listOf(
            navArgument("dni") { type = NavType.StringType }
        )
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
     * Pantalla para editar o crear un familiar
     * @param familiarId Identificador único del familiar (opcional, para edición)
     */
    object EditFamiliar : AppScreens("edit_familiar/{familiarId}") {
        fun createRoute(familiarId: String = "") = if (familiarId.isEmpty()) "edit_familiar" else "edit_familiar/$familiarId"
        
        val arguments = listOf(
            navArgument("familiarId") { type = NavType.StringType; nullable = true; defaultValue = null }
        )
    }

    /**
     * Sección: Pantallas de comunicaciones y reportes
     */
    /** Pantalla de comunicados y circulares */
    object ComunicadosCirculares : AppScreens("comunicaciones/comunicados")
    
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
    
    /** 
     * Gestión unificada de cursos y clases
     * 
     * @deprecated Esta ruta está obsoleta. Usar la ruta "gestor_academico/CURSOS" o "gestor_academico/CLASES" 
     * con los parámetros adecuados en su lugar.
     */
    @Deprecated("Usar gestor_academico en su lugar", ReplaceWith("Navegación directa a 'gestor_academico/CURSOS' o 'gestor_academico/CLASES'"))
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
    
    /** Pantalla de contactos para iniciar un chat nuevo */
    object ChatContacts : AppScreens("chat_contacts/{chatRouteName}") {
        fun createRoute(chatRouteName: String) = "chat_contacts/$chatRouteName"
        
        val arguments = listOf(
            navArgument("chatRouteName") { type = NavType.StringType }
        )
    }
    
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
     * Histórico de registros diarios para profesores
     * Permite ver el historial completo de registros de los alumnos de sus clases
     */
    object HistoricoRegistroDiario : AppScreens("historico_registro_diario") {
        fun createRoute() = "historico_registro_diario"
    }
    
    /**
     * Detalle de un registro específico
     * @param registroId Identificador del registro
     */
    object DetalleRegistro : AppScreens("detalle_registro/{registroId}") {
        fun createRoute(registroId: String) = "detalle_registro/$registroId"
        
        val arguments = listOf(
            navArgument("registroId") { type = NavType.StringType }
        )
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
    
    /** Conversaciones para la familia */
    object ConversacionesFamilia : AppScreens("conversaciones_familia")
    
    /** Notificaciones para familiares */
    object NotificacionesFamiliar : AppScreens("notificaciones_familiar")
    
    /** Comunicados para familiares */
    object ComunicadosFamilia : AppScreens("comunicados_familia")

    /**
     * Sección: Actividades preescolares (2-3 años)
     */
    /** Visualización de actividades preescolares para familiares */
    object ActividadesPreescolar : AppScreens("actividades_preescolar")
    
    /** Gestión de actividades preescolares para profesores */
    object ActividadesPreescolarProfesor : AppScreens("actividades_preescolar_profesor")
    
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

    /**
     * Pantalla para vincular profesores a clases
     * @param centroId Identificador del centro (opcional)
     * @param claseId Identificador de la clase (opcional)
     */
    object VincularProfesorClase : AppScreens("vincular_profesor_clase?centroId={centroId}&claseId={claseId}") {
        fun createRoute(centroId: String? = null, claseId: String? = null): String {
            val params = mutableListOf<String>()
            centroId?.let { params.add("centroId=$it") }
            claseId?.let { params.add("claseId=$it") }
            
            return if (params.isEmpty()) {
                "vincular_profesor_clase"
            } else {
                "vincular_profesor_clase?" + params.joinToString("&")
            }
        }
        
        val arguments = listOf(
            navArgument("centroId") { type = NavType.StringType; nullable = true; defaultValue = null },
            navArgument("claseId") { type = NavType.StringType; nullable = true; defaultValue = null }
        )
    }
    
    /**
     * Pantalla para vincular alumnos a clases
     * @param centroId Identificador del centro (opcional)
     * @param claseId Identificador de la clase (opcional)
     */
    object VincularAlumnoClase : AppScreens("vincular_alumno_clase?centroId={centroId}&claseId={claseId}") {
        fun createRoute(centroId: String? = null, claseId: String? = null): String {
            val params = mutableListOf<String>()
            centroId?.let { params.add("centroId=$it") }
            claseId?.let { params.add("claseId=$it") }
            
            return if (params.isEmpty()) {
                "vincular_alumno_clase"
            } else {
                "vincular_alumno_clase?" + params.joinToString("&")
            }
        }
        
        val arguments = listOf(
            navArgument("centroId") { type = NavType.StringType; nullable = true; defaultValue = null },
            navArgument("claseId") { type = NavType.StringType; nullable = true; defaultValue = null }
        )
    }
    
    /** Pantalla de configuración de seguridad */
    object Seguridad : AppScreens("seguridad")
    
    /** Pantalla de configuración de email de soporte */
    object EmailConfigSoporte : AppScreens("email_config_soporte")
    
    /** Pantalla de historial de solicitudes de vinculación */
    object HistorialSolicitudes : AppScreens("historial_solicitudes")
    
    /** Pantalla de gestión de usuarios */
    object GestionUsuarios : AppScreens("gestion_usuarios/{isAdminApp}") {
        fun createRoute(isAdminApp: Boolean) = "gestion_usuarios/$isAdminApp"
    }

    // Pantallas dummy para funcionalidades en desarrollo
    object DummyGestionCursos : AppScreens("dummy_gestion_cursos")
    object DummyGestionClases : AppScreens("dummy_gestion_clases")
    object DummyGestionUsuarios : AppScreens("dummy_gestion_usuarios")
    object DummyEstadisticas : AppScreens("dummy_estadisticas")
    object DummyConfiguracion : AppScreens("dummy_configuracion")

    object EditCurso : AppScreens("edit_curso/{cursoId}") {
        fun createRoute(cursoId: String) = "edit_curso/$cursoId"
    }

    object DetalleCurso : AppScreens("detalle_curso/{cursoId}") {
        fun createRoute(cursoId: String) = "detalle_curso/$cursoId"
    }

    /** Pantalla de edición del perfil de usuario */
    object EditProfile : AppScreens("edit_profile")
    
    /** Registro diario del profesor para los alumnos */
    object RegistroDiarioProfesor : AppScreens("registro_diario_profesor/{alumnosIds}?fecha={fecha}") {
        fun createRoute(alumnoId: String) = "registro_diario_profesor/$alumnoId"
        
        fun createRouteWithParams(alumnosIds: String, fecha: String? = null): String {
            return if (fecha != null) {
                "registro_diario_profesor/$alumnosIds?fecha=$fecha"
            } else {
                "registro_diario_profesor/$alumnosIds"
            }
        }
        
        val arguments = listOf(
            navArgument("alumnosIds") { type = NavType.StringType },
            navArgument("fecha") { type = NavType.StringType; nullable = true }
        )
    }
    
    /**
     * Pantalla de listado previo para registro diario
     */
    object ListadoPreRegistroDiario : AppScreens("listado_pre_registro_diario")

    /** Gestión de alumnos asignados al profesor */
    object MisAlumnosProfesor : AppScreens("mis_alumnos_profesor")

    /** Pantalla de vinculación de alumnos con familiares */
    object VinculacionFamiliar : AppScreens("vinculacion_familiar")
    
    /**
     * Detalles de un comunicado
     * @param comunicadoId Identificador único del comunicado
     */
    object DetalleComunicado : AppScreens("detalle_comunicado/{comunicadoId}") {
        fun createRoute(comunicadoId: String) = "detalle_comunicado/$comunicadoId"
    }
    
    /* Esta definición se ha eliminado porque ya existe una definición de DetalleRegistro */
    
    /** Pantalla para crear un nuevo comunicado */
    object CrearComunicado : AppScreens("crear_comunicado")

    /** Pantalla de bandeja de entrada unificada */
    object UnifiedInbox : AppScreens("unified_inbox")

    /**
     * Detalle de mensaje unificado
     * @param messageId Identificador único del mensaje
     */
    object MessageDetail : AppScreens("message_detail/{messageId}") {
        fun createRoute(messageId: String) = "message_detail/$messageId"
    }

    /**
     * Pantalla para crear un nuevo mensaje unificado
     * @param receiverId Identificador del destinatario (opcional)
     * @param messageType Tipo de mensaje a crear (opcional)
     * @param receiverName Nombre del destinatario (opcional)
     */
    object NewMessage : AppScreens("new_message?receiverId={receiverId}&messageType={messageType}&receiverName={receiverName}") {
        fun createRoute(receiverId: String? = null, messageType: String? = null, receiverName: String? = null): String {
            var route = "new_message"
            val params = mutableListOf<String>()
            
            receiverId?.let { params.add("receiverId=$it") }
            messageType?.let { params.add("messageType=$it") }
            receiverName?.let { params.add("receiverName=${Uri.encode(it)}") }
            
            if (params.isNotEmpty()) {
                route += "?" + params.joinToString("&")
            }
            
            return route
        }
        
        val arguments = listOf(
            navArgument("receiverId") { 
                type = NavType.StringType 
                nullable = true
                defaultValue = null
            },
            navArgument("messageType") { 
                type = NavType.StringType 
                nullable = true
                defaultValue = null
            },
            navArgument("receiverName") { 
                type = NavType.StringType 
                nullable = true
                defaultValue = null
            }
        )
    }

    // Añadir en la sección de rutas de profesor
    object InformeAsistencia : AppScreens("informe_asistencia")

    
    object DetallePreRegistroDiario : AppScreens("detalle_preregistro_diario/{registroId}") {
        val arguments = listOf(
            navArgument("registroId") { type = NavType.StringType }
        )
        
        fun createRoute(registroId: String) = "detalle_preregistro_diario/$registroId"
    }
    
    /** Pantalla para cambiar el tema de la aplicación */
    object CambiarTema : AppScreens("cambiar_tema")
} 