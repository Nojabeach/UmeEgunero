package com.tfg.umeegunero.navigation

import android.net.Uri
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.auth.screen.LoginScreen
import com.tfg.umeegunero.feature.common.config.screen.NotificacionesScreen
import com.tfg.umeegunero.feature.common.files.screen.DocumentoScreen
import com.tfg.umeegunero.feature.common.screen.DummyScreen
import com.tfg.umeegunero.feature.common.support.screen.FAQScreen
import com.tfg.umeegunero.feature.common.support.screen.TechnicalSupportScreen
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeScreen
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeUserType
import com.tfg.umeegunero.feature.profesor.screen.TareasScreen
import com.tfg.umeegunero.feature.profesor.screen.DetalleTareaScreen
import com.tfg.umeegunero.feature.profesor.screen.EvaluacionScreen
import com.tfg.umeegunero.feature.common.academico.screen.CalendarioScreen
import com.tfg.umeegunero.feature.common.academico.screen.DetalleEventoScreen
import com.tfg.umeegunero.feature.common.academico.viewmodel.CalendarioViewModel
import com.tfg.umeegunero.feature.admin.screen.AdminDashboardScreen
import com.tfg.umeegunero.feature.common.academico.screen.detallediaevento.DetalleDiaEventoScreen
import com.tfg.umeegunero.feature.common.comunicacion.screen.BandejaEntradaScreen
import com.tfg.umeegunero.feature.common.comunicacion.screen.ComponerMensajeScreen
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.time.LocalDate as JavaLocalDate
import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Navegación principal de la aplicación
 */
@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppScreens.Welcome.route,
    onCloseApp: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Pantalla de bienvenida
        composable(route = AppScreens.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = { userType: WelcomeUserType ->
                    try {
                        val userTypeRoute: String = when (userType) {
                            WelcomeUserType.ADMIN -> "ADMIN"
                            WelcomeUserType.CENTRO -> "CENTRO"
                            WelcomeUserType.PROFESOR -> "PROFESOR"
                            WelcomeUserType.FAMILIAR -> "FAMILIAR"
                        }
                        
                        // Para el caso de administrador, usamos una ruta especial
                        if (userType == WelcomeUserType.ADMIN) {
                            Log.d("Navigation", "Navegando a admin_login")
                            navController.navigate("admin_login")
                        } else {
                            Log.d("Navigation", "Navegando a login/$userTypeRoute")
                            navController.navigate(AppScreens.Login.createRoute(userTypeRoute))
                        }
                    } catch (e: Exception) {
                        Log.e("Navigation", "Error al navegar a login: ${e.message}", e)
                        // Si hay un error, mostramos una pantalla de error
                        navController.navigate("login_error")
                    }
                },
                onCloseApp = onCloseApp,
                onNavigateToTechnicalSupport = {
                    navController.navigate(AppScreens.SoporteTecnico.route)
                },
                onNavigateToFAQ = {
                    navController.navigate(AppScreens.FAQ.route)
                }
            )
        }
        
        // Pantalla de error de login
        composable("login_error") {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Ha ocurrido un error al intentar acceder. Reinicia la aplicación.")
            }
        }
        
        // Pantalla de soporte técnico
        composable(route = AppScreens.SoporteTecnico.route) {
            TechnicalSupportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Pantalla de FAQ
        composable(route = AppScreens.FAQ.route) {
            FAQScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Pantalla de login específica para administradores
        composable("admin_login") {
            LoginScreen(
                userType = TipoUsuario.ADMIN_APP,
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate(AppScreens.AdminDashboard.route) {
                        popUpTo(AppScreens.Welcome.route) { inclusive = true }
                    }
                },
                onForgotPassword = { navController.navigate(AppScreens.SoporteTecnico.route) }
            )
        }
        
        // Pantalla de login
        composable(
            route = AppScreens.Login.route,
            arguments = listOf(
                navArgument("userType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userTypeStr = backStackEntry.arguments?.getString("userType") ?: "FAMILIAR"
            val userType = remember(userTypeStr) {
                when(userTypeStr) {
                    "ADMIN" -> TipoUsuario.ADMIN_APP
                    "CENTRO" -> TipoUsuario.ADMIN_CENTRO
                    "PROFESOR" -> TipoUsuario.PROFESOR
                    else -> TipoUsuario.FAMILIAR
                }
            }
            
            LoginScreen(
                userType = userType,
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = {
                    val route = when(userType) {
                        TipoUsuario.ADMIN_APP -> AppScreens.AdminDashboard.route
                        TipoUsuario.ADMIN_CENTRO -> AppScreens.CentroDashboard.route
                        TipoUsuario.PROFESOR -> AppScreens.ProfesorDashboard.route
                        TipoUsuario.FAMILIAR -> AppScreens.FamiliarDashboard.route
                        else -> AppScreens.Welcome.route
                    }
                    navController.navigate(route) {
                        popUpTo(AppScreens.Welcome.route) { inclusive = true }
                    }
                },
                onForgotPassword = { navController.navigate(AppScreens.SoporteTecnico.route) }
            )
        }
        
        // Pantalla de notificaciones
        composable(route = AppScreens.Notificaciones.route) {
            com.tfg.umeegunero.feature.common.config.screen.NotificacionesScreen(
                navController = navController
            )
        }
        
        // Pantalla de visualización de documentos
        composable(
            route = AppScreens.Documento.route,
            arguments = listOf(
                navArgument("url") { 
                    type = NavType.StringType 
                },
                navArgument("nombre") { 
                    type = NavType.StringType 
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val url = Uri.decode(backStackEntry.arguments?.getString("url") ?: "")
            val nombre = backStackEntry.arguments?.getString("nombre")?.let { Uri.decode(it) }
            
            DocumentoScreen(
                navController = navController,
                documentoUrl = url,
                documentoNombre = nombre,
                viewModel = hiltViewModel()
            )
        }

        // Pantalla de administración
        composable(route = AppScreens.AdminDashboard.route) {
            AdminDashboardScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        // Pantalla de gestión de centros
        composable(route = AppScreens.GestionCentros.route) {
            DummyScreen(
                title = "Gestión de Centros",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pantalla del dashboard del centro educativo
        composable(route = AppScreens.CentroDashboard.route) {
            com.tfg.umeegunero.feature.centro.screen.CentroDashboardScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla del dashboard familiar
        composable(route = AppScreens.FamiliarDashboard.route) {
            com.tfg.umeegunero.feature.familiar.screen.FamiliaDashboardScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla del dashboard de profesor
        composable(route = AppScreens.ProfesorDashboard.route) {
            com.tfg.umeegunero.feature.profesor.screen.ProfesorDashboardScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        // Ruta para la evaluación académica
        composable("evaluacion") {
            EvaluacionScreen(
                navController = navController,
                alumnos = emptyList()
            )
        }
        
        // Pantalla de calendario y eventos académicos
        composable(route = AppScreens.Calendario.route) {
            CalendarioScreen(
                navController = navController
            )
        }
        
        // Pantalla de detalle de evento
        composable(
            route = AppScreens.DetalleEvento.route,
            arguments = listOf(
                navArgument("eventoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventoId = backStackEntry.arguments?.getString("eventoId") ?: ""
            
            DetalleEventoScreen(
                eventoId = eventoId,
                navController = navController
            )
        }

        // Detalle de día con eventos
        composable(
            route = AppScreens.DetalleDiaEvento.route,
            arguments = listOf(
                navArgument(AppScreens.DetalleDiaEvento.Fecha) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val fechaString = backStackEntry.arguments?.getString(AppScreens.DetalleDiaEvento.Fecha) ?: LocalDate.now().toString()
            
            // Usar DateTimeFormatter para compatibilidad con API 24
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val fecha = try {
                LocalDate.parse(fechaString, formatter)
            } catch (e: Exception) {
                LocalDate.now()
            }
            
            // Convertir org.threeten.bp.LocalDate a java.time.LocalDate usando el toString y parse
            val fechaStr = fecha.toString()
            val javaTimeFecha = try {
                JavaLocalDate.parse(fechaStr)
            } catch (e: Exception) {
                JavaLocalDate.now()
            }
            
            DetalleDiaEventoScreen(
                navController = navController,
                fecha = javaTimeFecha
            )
        }
        
        // Pantalla de bandeja de entrada de mensajes
        composable(route = AppScreens.BandejaEntrada.route) {
            BandejaEntradaScreen(
                navController = navController
            )
        }
        
        // Pantalla para componer un nuevo mensaje
        composable(
            route = AppScreens.ComponerMensaje.route,
            arguments = listOf(
                navArgument(AppScreens.ComponerMensaje.MensajeId) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val mensajeId = backStackEntry.arguments?.getString(AppScreens.ComponerMensaje.MensajeId)
            
            ComponerMensajeScreen(
                navController = navController,
                mensajeIdRespuesta = mensajeId
            )
        }
        
        // Pantalla de gestión de cursos y clases
        composable(route = AppScreens.GestionCursosYClases.route) {
            DummyScreen(
                title = "Gestión de Cursos y Clases",
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Pantalla de gestión de clases por curso
        composable(
            route = AppScreens.GestionClases.route,
            arguments = listOf(
                navArgument("cursoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val cursoId = backStackEntry.arguments?.getString("cursoId") ?: ""
            
            // Usar la implementación existente en lugar de DummyScreen
            com.tfg.umeegunero.feature.common.academico.screen.GestionClasesScreen(
                navController = navController,
                cursoId = cursoId,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla de gestión de profesores
        composable(route = AppScreens.GestionProfesores.route) {
            DummyScreen(
                title = "Gestión de Profesores",
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Pantalla de vinculación familiar
        composable(route = AppScreens.VinculacionFamiliar.route) {
            DummyScreen(
                title = "Vinculación Familiar",
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Pantalla de añadir alumno
        composable(route = AppScreens.AddAlumno.route) {
            com.tfg.umeegunero.feature.centro.screen.AddAlumnoScreen(
                navController = navController
            )
        }
        
        // Pantalla de notificaciones del centro
        composable(route = AppScreens.GestionNotificacionesCentro.route) {
            DummyScreen(
                title = "Gestión de Notificaciones",
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Pantalla de calendario para familiares
        composable(route = AppScreens.CalendarioFamilia.route) {
            com.tfg.umeegunero.feature.familiar.screen.CalendarioFamiliaScreen(
                navController = navController
            )
        }
        
        // Pantalla de notificaciones para familiares
        composable(route = AppScreens.NotificacionesFamilia.route) {
            com.tfg.umeegunero.feature.common.config.screen.NotificacionesScreen(
                navController = navController
            )
        }
        
        // Pantalla de perfil
        composable(route = AppScreens.Perfil.route) {
            DummyScreen(
                title = "Mi Perfil",
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Pantalla de configuración
        composable(route = AppScreens.Configuracion.route) {
            val viewModel = hiltViewModel<com.tfg.umeegunero.feature.common.config.viewmodel.ConfiguracionViewModel>()
            com.tfg.umeegunero.feature.common.config.screen.ConfiguracionScreen(
                viewModel = viewModel,
                perfil = com.tfg.umeegunero.feature.common.config.screen.PerfilConfiguracion.SISTEMA,
                onNavigateBack = { navController.popBackStack() },
                onMenuClick = {}
            )
        }
        
        // Pantalla de consulta de registro diario
        composable(
            route = AppScreens.ConsultaRegistroDiario.route,
            arguments = listOf(
                navArgument("alumnoId") { type = NavType.StringType },
                navArgument("alumnoNombre") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val alumnoId = backStackEntry.arguments?.getString("alumnoId") ?: ""
            val alumnoNombre = backStackEntry.arguments?.getString("alumnoNombre") ?: ""
            
            com.tfg.umeegunero.feature.familiar.registros.screen.ConsultaRegistroDiarioScreen(
                viewModel = hiltViewModel(),
                alumnoId = alumnoId,
                alumnoNombre = alumnoNombre,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Pantalla de conversaciones para familia
        composable(route = AppScreens.ConversacionesFamilia.route) {
            DummyScreen(
                title = "Mensajes Familiares",
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Pantalla para añadir una clase a un curso
        composable(
            route = AppScreens.AddClase.route,
            arguments = listOf(
                navArgument("cursoId") { type = NavType.StringType },
                navArgument("centroId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val cursoId = backStackEntry.arguments?.getString("cursoId") ?: ""
            val centroId = backStackEntry.arguments?.getString("centroId") ?: ""
            
            com.tfg.umeegunero.feature.common.academico.screen.AddClaseScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla para editar una clase existente
        composable(
            route = AppScreens.EditClase.route,
            arguments = listOf(
                navArgument("claseId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val claseId = backStackEntry.arguments?.getString("claseId") ?: ""
            
            com.tfg.umeegunero.feature.common.academico.screen.EditClaseScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        // Pantalla de conversaciones para profesor
        composable(route = AppScreens.ConversacionesProfesor.route) {
            DummyScreen(
                title = "Conversaciones",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pantalla de registro de asistencia para profesor
        composable(route = AppScreens.AsistenciaProfesor.route) {
            DummyScreen(
                title = "Registro de Asistencia",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pantalla de gestión de tareas para profesor
        composable(route = AppScreens.TareasProfesor.route) {
            DummyScreen(
                title = "Gestión de Tareas",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pantalla de calendario para profesor
        composable(route = AppScreens.CalendarioProfesor.route) {
            DummyScreen(
                title = "Calendario del Profesor",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pantalla de estadísticas (Dummy)
        composable(route = AppScreens.Estadisticas.route) {
            DummyScreen(
                title = "Estadísticas",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pantalla de reporte de uso (Dummy)
        composable(route = AppScreens.ReporteUso.route) {
            DummyScreen(
                title = "Reporte de Uso",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pantalla de reporte de rendimiento (Dummy)
        composable(route = AppScreens.ReporteRendimiento.route) {
            DummyScreen(
                title = "Reporte de Rendimiento",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pantalla de configuración (Dummy)
        composable(route = AppScreens.Config.route) {
            DummyScreen(
                title = "Configuración",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pantalla de comunicados (Dummy)
        composable(route = AppScreens.Comunicados.route) {
            DummyScreen(
                title = "Comunicados",
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
} 