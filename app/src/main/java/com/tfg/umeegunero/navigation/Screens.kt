package com.tfg.umeegunero.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Objeto sealed que representa las pantallas disponibles en la aplicación.
 * Implementa un sistema de navegación tipo-seguro con definición explícita de argumentos.
 */
sealed class Screens(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    // Pantallas de autenticación
    object Login : Screens("login")
    object Registro : Screens("registro")
    object RecuperarPassword : Screens("recuperar_password")
    
    // Pantallas de administrador
    object AdminDashboard : Screens("admin_dashboard")
    object AddCentro : Screens("add_centro")
    object EditCentro : Screens(
        route = "edit_centro/{centroId}",
        arguments = listOf(
            navArgument("centroId") { type = NavType.StringType }
        )
    ) {
        fun createRoute(centroId: String) = "edit_centro/$centroId"
    }
    
    // Pantallas de centro
    object CentroDashboard : Screens("centro_dashboard")
    object AddProfesor : Screens("add_profesor")
    object AddFamiliar : Screens("add_familiar")
    
    // Pantallas de alumno
    object DetalleAlumno : Screens(
        route = "detalle_alumno/{alumnoId}",
        arguments = listOf(
            navArgument("alumnoId") { type = NavType.StringType }
        )
    ) {
        fun createRoute(alumnoId: String) = "detalle_alumno/$alumnoId"
    }
    
    object AddAlumno : Screens("add_alumno")
    
    object EditAlumno : Screens(
        route = "edit_alumno/{alumnoId}",
        arguments = listOf(
            navArgument("alumnoId") { type = NavType.StringType }
        )
    ) {
        fun createRoute(alumnoId: String) = "edit_alumno/$alumnoId"
    }
    
    // Pantallas de usuario
    object UserDetail : Screens(
        route = "user_detail/{userId}",
        arguments = listOf(
            navArgument("userId") { type = NavType.StringType }
        )
    ) {
        fun createRoute(userId: String) = "user_detail/$userId"
    }
    
    object AddUser : Screens("add_user")
    
    // Pantallas de profesor
    object ProfesorDashboard : Screens("profesor_dashboard")
    
    object RegistroDiario : Screens(
        route = "registro_diario/{alumnoId}/{claseId}/{profesorId}/{alumnoNombre}/{claseNombre}",
        arguments = listOf(
            navArgument("alumnoId") { type = NavType.StringType },
            navArgument("claseId") { type = NavType.StringType },
            navArgument("profesorId") { type = NavType.StringType },
            navArgument("alumnoNombre") { type = NavType.StringType },
            navArgument("claseNombre") { type = NavType.StringType }
        )
    ) {
        fun createRoute(
            alumnoId: String,
            claseId: String,
            profesorId: String,
            alumnoNombre: String,
            claseNombre: String
        ): String {
            return "registro_diario/$alumnoId/$claseId/$profesorId/$alumnoNombre/$claseNombre"
        }
    }
    
    // Pantallas de familiar
    object FamiliarDashboard : Screens("familiar_dashboard")
    
    object ConsultaRegistroDiario : Screens(
        route = "consulta_registro_diario/{alumnoId}/{alumnoNombre}",
        arguments = listOf(
            navArgument("alumnoId") { type = NavType.StringType },
            navArgument("alumnoNombre") { type = NavType.StringType }
        )
    ) {
        fun createRoute(alumnoId: String, alumnoNombre: String): String {
            return "consulta_registro_diario/$alumnoId/$alumnoNombre"
        }
    }
    
    // Pantallas académicas
    object CursosScreen : Screens("cursos_screen")
    
    object AddCurso : Screens(
        route = "add_curso/{centroId}",
        arguments = listOf(
            navArgument("centroId") { type = NavType.StringType }
        )
    ) {
        fun createRoute(centroId: String) = "add_curso/$centroId"
    }
    
    object EditCurso : Screens(
        route = "edit_curso/{cursoId}",
        arguments = listOf(
            navArgument("cursoId") { type = NavType.StringType }
        )
    ) {
        fun createRoute(cursoId: String) = "edit_curso/$cursoId"
    }
    
    // Pantallas de aulas
    object AulasScreen : Screens("aulas_screen")
    
    object AddAula : Screens(
        route = "add_aula/{centroId}",
        arguments = listOf(
            navArgument("centroId") { type = NavType.StringType }
        )
    ) {
        fun createRoute(centroId: String) = "add_aula/$centroId"
    }
    
    object EditAula : Screens(
        route = "edit_aula/{aulaId}",
        arguments = listOf(
            navArgument("aulaId") { type = NavType.StringType }
        )
    ) {
        fun createRoute(aulaId: String) = "edit_aula/$aulaId"
    }
    
    // Pantallas de clase
    object ClasesScreen : Screens("clases_screen")
    
    object AddClase : Screens(
        route = "add_clase/{cursoId}/{centroId}",
        arguments = listOf(
            navArgument("cursoId") { type = NavType.StringType },
            navArgument("centroId") { type = NavType.StringType }
        )
    ) {
        fun createRoute(cursoId: String, centroId: String) = "add_clase/$cursoId/$centroId"
    }
    
    object EditClase : Screens(
        route = "edit_clase/{claseId}",
        arguments = listOf(
            navArgument("claseId") { type = NavType.StringType }
        )
    ) {
        fun createRoute(claseId: String) = "edit_clase/$claseId"
    }
    
    // Pantallas de chat y mensajería
    object Conversaciones : Screens(
        route = "conversaciones?esFamiliar={esFamiliar}",
        arguments = listOf(
            navArgument("esFamiliar") { 
                type = NavType.BoolType
                defaultValue = false 
            }
        )
    ) {
        fun createRoute(esFamiliar: Boolean): String {
            return "conversaciones?esFamiliar=$esFamiliar"
        }
    }
    
    // Chat para profesor y familiar
    object ChatProfesor : Screens(
        route = "chat_familiar/{familiarId}",
        arguments = listOf(
            navArgument("familiarId") { type = NavType.StringType }
        )
    ) {
        fun createRoute(familiarId: String) = "chat_familiar/$familiarId"
    }
    
    object ChatFamiliar : Screens(
        route = "chat_profesor/{profesorId}",
        arguments = listOf(
            navArgument("profesorId") { type = NavType.StringType }
        )
    ) {
        fun createRoute(profesorId: String) = "chat_profesor/$profesorId"
    }
    
    // Otras pantallas
    object Home : Screens("home")
    object Settings : Screens("settings")
    object Cursos : Screens("cursos")
    object Clases : Screens("clases")
} 