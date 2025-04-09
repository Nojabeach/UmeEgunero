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
                    val userTypeRoute: String = when (userType) {
                        WelcomeUserType.ADMIN -> "ADMIN"
                        WelcomeUserType.CENTRO -> "CENTRO"
                        WelcomeUserType.PROFESOR -> "PROFESOR"
                        WelcomeUserType.FAMILIAR -> "FAMILIAR"
                    }
                    navController.navigate(AppScreens.Login.createRoute(userTypeRoute))
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
                onLoginSuccess = { _ ->
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
                }
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
            com.tfg.umeegunero.feature.centro.screen.GestionCursosYClasesScreen(
                navController = navController,
                viewModel = hiltViewModel()
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
            com.tfg.umeegunero.feature.centro.screen.GestionProfesoresScreen(
                navController = navController
            )
        }
        
        // Pantalla de vinculación familiar
        composable(route = AppScreens.VinculacionFamiliar.route) {
            com.tfg.umeegunero.feature.centro.screen.VinculacionFamiliarScreen(
                navController = navController
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
            com.tfg.umeegunero.feature.centro.screen.GestionNotificacionesCentroScreen(
                navController = navController
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
        
        // Pantalla de perfil de usuario
        composable(route = AppScreens.Perfil.route) {
            com.tfg.umeegunero.feature.common.perfil.screen.PerfilScreen(
                navController = navController
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
            val viewModel = hiltViewModel<com.tfg.umeegunero.feature.common.mensajeria.ConversacionesViewModel>()
            com.tfg.umeegunero.feature.common.mensajeria.ConversacionesScreen(
                navController = navController,
                rutaChat = AppScreens.ChatFamilia.route.substringBefore("/{conversacionId}"),
                viewModel = viewModel
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
    }
} 