package com.tfg.umeegunero.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Objeto sealed que representa las pantallas disponibles en la aplicación
 */
sealed class Screens(val route: String) {
    // Pantallas de autenticación
    object Login : Screens("login")
    object Registro : Screens("registro")
    object RecuperarPassword : Screens("recuperar_password")
    
    // Pantallas de administrador
    object AdminDashboard : Screens("admin_dashboard")
    object AddCentro : Screens("add_centro")
    object EditCentro : Screens("edit_centro/{centroId}") {
        fun createRoute(centroId: String) = "edit_centro/$centroId"
    }
    
    // Pantallas de centro
    object CentroDashboard : Screens("centro_dashboard")
    object AddProfesor : Screens("add_profesor")
    object AddFamiliar : Screens("add_familiar")
    
    // Pantallas de alumno
    object DetalleAlumno : Screens("detalle_alumno/{alumnoId}") {
        fun createRoute(alumnoId: String) = "detalle_alumno/$alumnoId"
    }
    object AddAlumno : Screens("add_alumno")
    object EditAlumno : Screens("edit_alumno/{alumnoId}") {
        fun createRoute(alumnoId: String) = "edit_alumno/$alumnoId"
    }
    
    // Pantallas de usuario
    object UserDetail : Screens("user_detail/{userId}") {
        fun createRoute(userId: String) = "user_detail/$userId"
    }
    object AddUser : Screens("add_user")
    
    // Pantallas de profesor
    object ProfesorDashboard : Screens("profesor_dashboard")
    object RegistroDiario : Screens("registro_diario/{alumnoId}/{claseId}/{profesorId}/{alumnoNombre}/{claseNombre}") {
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
    object ConsultaRegistroDiario : Screens("consulta_registro_diario/{alumnoId}/{alumnoNombre}") {
        fun createRoute(alumnoId: String, alumnoNombre: String): String {
            return "consulta_registro_diario/$alumnoId/$alumnoNombre"
        }
    }
    
    // Pantallas académicas
    object CursosScreen : Screens("cursos_screen")
    object AddCurso : Screens("add_curso/{centroId}") {
        fun createRoute(centroId: String) = "add_curso/$centroId"
    }
    object EditCurso : Screens("edit_curso/{cursoId}") {
        fun createRoute(cursoId: String) = "edit_curso/$cursoId"
    }
    
    // Pantallas de aulas
    object AulasScreen : Screens("aulas_screen")
    object AddAula : Screens("add_aula/{centroId}") {
        fun createRoute(centroId: String) = "add_aula/$centroId"
    }
    object EditAula : Screens("edit_aula/{aulaId}") {
        fun createRoute(aulaId: String) = "edit_aula/$aulaId"
    }
    
    // Pantallas de clase
    object ClasesScreen : Screens("clases_screen")
    object AddClase : Screens("add_clase/{cursoId}/{centroId}") {
        fun createRoute(cursoId: String, centroId: String) = "add_clase/$cursoId/$centroId"
    }
    object EditClase : Screens("edit_clase/{claseId}") {
        fun createRoute(claseId: String) = "edit_clase/$claseId"
    }
    
    // Pantallas de chat y mensajería
    object Conversaciones : Screens("conversaciones?esFamiliar={esFamiliar}") {
        fun createRoute(esFamiliar: Boolean): String {
            return "conversaciones?esFamiliar=$esFamiliar"
        }
    }
    
    // Otras pantallas
    object Home : Screens("home")
    object Settings : Screens("settings")
    object Cursos : Screens("cursos")
    object Clases : Screens("clases")
} 