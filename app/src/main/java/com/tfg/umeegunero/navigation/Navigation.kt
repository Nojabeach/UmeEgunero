package com.tfg.umeegunero.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.auth.screen.LoginScreen
import com.tfg.umeegunero.feature.auth.screen.RegistroScreen
import com.tfg.umeegunero.feature.common.config.screen.NotificacionesScreen
import com.tfg.umeegunero.feature.common.files.screen.DocumentoScreen
import com.tfg.umeegunero.feature.common.support.screen.FAQScreen
import com.tfg.umeegunero.feature.common.support.screen.TechnicalSupportScreen
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeScreen
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeUserType
import com.tfg.umeegunero.feature.common.academico.screen.CalendarioScreen
import com.tfg.umeegunero.feature.common.academico.screen.DetalleEventoScreen
import com.tfg.umeegunero.feature.common.academico.screen.DetalleClaseScreen
import com.tfg.umeegunero.feature.common.academico.viewmodel.CalendarioViewModel
import com.tfg.umeegunero.feature.admin.screen.AdminDashboardScreen
import com.tfg.umeegunero.feature.common.academico.screen.detallediaevento.DetalleDiaEventoScreen
import com.tfg.umeegunero.feature.familiar.screen.ComunicadosFamiliaScreen
import com.tfg.umeegunero.feature.common.legal.screen.TerminosCondicionesScreen
import java.time.LocalDate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tfg.umeegunero.feature.common.users.viewmodel.AddUserUiState
import com.tfg.umeegunero.feature.admin.viewmodel.AdminDashboardViewModel
import com.tfg.umeegunero.feature.common.config.screen.ConfiguracionScreen
import com.tfg.umeegunero.feature.common.config.screen.PerfilConfiguracion
import com.tfg.umeegunero.feature.common.users.screen.GestionUsuariosScreen
import com.tfg.umeegunero.feature.common.perfil.screen.EditProfileScreen
import com.tfg.umeegunero.feature.common.perfil.screen.PerfilScreen
import com.tfg.umeegunero.feature.common.users.screen.AddUserScreen
import com.tfg.umeegunero.feature.common.users.screen.ListAlumnosScreen
import com.tfg.umeegunero.feature.admin.viewmodel.SeguridadViewModel
import com.tfg.umeegunero.feature.admin.screen.SeguridadScreen
import com.tfg.umeegunero.feature.centro.screen.CentroDashboardScreen
import com.tfg.umeegunero.feature.familiar.screen.FamiliaDashboardScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.feature.profesor.screen.DetalleAlumnoProfesorScreen
import timber.log.Timber
import kotlinx.coroutines.flow.collectLatest
import com.tfg.umeegunero.feature.common.comunicacion.screen.UnifiedInboxScreen
import com.tfg.umeegunero.feature.common.comunicacion.viewmodel.UnifiedInboxViewModel
import com.google.firebase.auth.FirebaseAuth
import com.tfg.umeegunero.feature.common.comunicacion.screen.MessageDetailScreen
import com.tfg.umeegunero.feature.common.comunicacion.screen.ComunicadoDetailScreen
import com.tfg.umeegunero.feature.centro.screen.VincularProfesorClaseScreen
import com.tfg.umeegunero.feature.centro.screen.VincularAlumnoClaseScreen
import android.widget.Toast
import com.tfg.umeegunero.feature.common.comunicacion.viewmodel.MessageDetailViewModel

/**
 * Sistema de navegación principal de la aplicación UmeEgunero.
 * 
 * Esta función Composable implementa el sistema de navegación completo de la aplicación,
 * gestionando todas las rutas y transiciones entre pantallas. Utiliza Navigation Compose
 * para proporcionar una navegación fluida y consistente entre los diferentes módulos
 * de la aplicación educativa.
 * 
 * ## Características principales:
 * - Navegación basada en rutas definidas en [AppScreens]
 * - Animaciones de transición personalizadas según el tipo de pantalla
 * - Integración con [NavigationViewModel] para navegación basada en eventos
 * - Soporte para argumentos de navegación tipados
 * - Gestión de backstack optimizada para diferentes flujos de usuario
 * 
 * ## Flujos de navegación soportados:
 * - **Autenticación**: Welcome → Login → Dashboard específico por rol
 * - **Registro**: Welcome → Registro → Login
 * - **Dashboards**: Específicos para cada tipo de usuario (Admin, Centro, Profesor, Familiar)
 * - **Gestión académica**: Centros → Cursos → Clases → Alumnos
 * - **Comunicación**: Mensajería unificada, comunicados, notificaciones
 * - **Configuración**: Perfil, preferencias, seguridad
 * - **Soporte**: FAQ, soporte técnico, términos y condiciones
 * 
 * ## Tipos de animaciones:
 * - **DASHBOARD**: Para pantallas principales y de bienvenida
 * - **DETAIL**: Para pantallas de detalle y formularios
 * 
 * La navegación está optimizada para diferentes tipos de usuario:
 * - **ADMIN_APP**: Acceso completo al sistema
 * - **ADMIN_CENTRO**: Gestión de centro educativo
 * - **PROFESOR**: Gestión de clases y alumnos
 * - **FAMILIAR**: Seguimiento de hijos y comunicación
 * 
 * @param navController Controlador de navegación de Jetpack Navigation
 * @param startDestination Ruta inicial de la aplicación (por defecto: Welcome)
 * @param onCloseApp Función callback para cerrar la aplicación
 * @param navigationViewModel ViewModel que gestiona comandos de navegación basados en eventos
 * 
 * @see AppScreens Para las definiciones de rutas
 * @see NavigationViewModel Para la gestión de navegación basada en eventos
 * @see NavAnimations Para las animaciones de transición
 * 
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppScreens.Welcome.route,
    onCloseApp: () -> Unit = {},
    navigationViewModel: NavigationViewModel = hiltViewModel()
) {
    // Observar comandos de navegación
    LaunchedEffect(navigationViewModel, navController) {
        navigationViewModel.navigationCommands.collectLatest { command ->
            when (command) {
                is NavigationCommand.NavigateTo -> {
                    try {
                        navController.navigate(command.route) {
                            launchSingleTop = true
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error al navegar a: ${command.route}")
                    }
                }
                is NavigationCommand.NavigateBack -> {
                    navController.popBackStack()
                }
                is NavigationCommand.NavigateToWithClearBackstack -> {
                    navController.navigate(command.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { 
            val animationType = if (targetState.destination.route?.contains("dashboard") == true || 
                targetState.destination.route == AppScreens.Welcome.route) {
                NavAnimations.AnimationType.DASHBOARD
            } else {
                NavAnimations.AnimationType.DETAIL
            }
            NavAnimations.setTransitionAnimations(targetState, animationType).first
        },
        exitTransition = { 
            val animationType = if (initialState.destination.route?.contains("dashboard") == true || 
                initialState.destination.route == AppScreens.Welcome.route) {
                NavAnimations.AnimationType.DASHBOARD
            } else {
                NavAnimations.AnimationType.DETAIL
            }
            NavAnimations.setTransitionAnimations(initialState, animationType).second
        },
        popEnterTransition = { 
            val animationType = if (targetState.destination.route?.contains("dashboard") == true || 
                targetState.destination.route == AppScreens.Welcome.route) {
                NavAnimations.AnimationType.DASHBOARD
            } else {
                NavAnimations.AnimationType.DETAIL
            }
            NavAnimations.setTransitionAnimations(targetState, animationType).first
        },
        popExitTransition = { 
            val animationType = if (initialState.destination.route?.contains("dashboard") == true || 
                initialState.destination.route == AppScreens.Welcome.route) {
                NavAnimations.AnimationType.DASHBOARD
            } else {
                NavAnimations.AnimationType.DETAIL
            }
            NavAnimations.setTransitionAnimations(initialState, animationType).second
        }
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
                    try {
                        navController.navigate(AppScreens.Login.createRoute(userTypeRoute))
                    } catch (e: Exception) {
                        Log.e("Navigation", "Error al navegar a login: ${e.message}", e)
                        navController.navigate(AppScreens.SoporteTecnico.route)
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(AppScreens.Registro.route)
                },
                onCloseApp = onCloseApp,
                onNavigateToTechnicalSupport = {
                    navController.navigate(AppScreens.SoporteTecnico.route)
                },
                onNavigateToFAQ = {
                    navController.navigate(AppScreens.FAQ.route)
                },
                onNavigateToTerminosCondiciones = {
                    navController.navigate(AppScreens.TerminosCondiciones.route)
                }
            )
        }
        
        // Pantalla de registro de usuarios (familiar)
        composable(route = AppScreens.Registro.route) {
            RegistroScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegistroCompletado = { navController.navigate(AppScreens.Login.createRoute("FAMILIAR")) },
                onNavigateToTerminosCondiciones = { navController.navigate(AppScreens.TerminosCondiciones.route) }
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
        
        // Pantalla de términos y condiciones
        composable(route = AppScreens.TerminosCondiciones.route) {
            TerminosCondicionesScreen(
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
                onNavigateBack = { 
                    navController.navigate(AppScreens.Welcome.route) {
                        popUpTo(AppScreens.Login.route) { inclusive = true }
                    }
                },
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
            NotificacionesScreen(
                navController = navController
            )
        }
        
        // Pantalla de visualización de documentos
        composable(
            route = AppScreens.VisualizadorDocumento.route,
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
                documentoNombre = nombre
            )
        }

        // Pantalla de administración
        composable(route = AppScreens.AdminDashboard.route) {
            val viewModel: AdminDashboardViewModel = hiltViewModel()
            AdminDashboardScreen(
                navController = navController,
                viewModel = viewModel,
                onNavigateToGestionUsuarios = { 
                    navController.navigate(AppScreens.GestionUsuarios.createRoute(true)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToGestionCentros = { navController.navigate(AppScreens.GestionCentros.route) },
                onNavigateToEstadisticas = { navController.navigate(AppScreens.Estadisticas.route) },
                onNavigateToSeguridad = { navController.navigate(AppScreens.Seguridad.route) },
                onNavigateToTema = { 
                    navController.navigate(AppScreens.CambiarTema.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToEmailConfig = { navController.navigate(AppScreens.EmailConfig.route) },
                onNavigateToComunicados = { navController.navigate(AppScreens.ComunicadosCirculares.route) },
                onNavigateToSoporteTecnico = { navController.navigate(AppScreens.SoporteTecnico.route) },
                onNavigateToFAQ = { navController.navigate(AppScreens.FAQ.route) },
                onNavigateToTerminos = { navController.navigate(AppScreens.TerminosCondiciones.route) },
                onNavigateToLogout = {
                    // Cerrar sesión y navegar a la pantalla de login
                    viewModel.logout()
                    navController.navigate(AppScreens.Login.createRoute("ADMIN")) {
                        popUpTo(AppScreens.AdminDashboard.route) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(AppScreens.Perfil.createRoute(true)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Pantalla de configuración
        composable(route = AppScreens.Config.route) {
            ConfiguracionScreen(
                perfil = PerfilConfiguracion.ADMIN,
                onNavigateBack = { navController.popBackStack() },
                onMenuClick = { /* Implementar si es necesario */ }
            )
        }

        // PANTALLAS DE ADMINISTRACIÓN
        
        // Pantalla de gestión de centros
        composable(route = AppScreens.GestionCentros.route) {
            com.tfg.umeegunero.feature.admin.screen.ListCentrosScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantallas de gestión para el centro educativo
        composable(route = AppScreens.CrearUsuarioRapido.route) {
            com.tfg.umeegunero.feature.centro.screen.CrearUsuarioRapidoScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla para crear/editar usuarios
        composable(
            route = AppScreens.AddUser.route,
            arguments = listOf(
                navArgument(AppScreens.AddUser.ARG_IS_ADMIN_APP) { 
                    type = NavType.BoolType 
                    defaultValue = false
                },
                navArgument(AppScreens.AddUser.ARG_TIPO_USUARIO) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(AppScreens.AddUser.ARG_CENTRO_ID) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(AppScreens.AddUser.ARG_CENTRO_BLOQUEADO) {
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument(AppScreens.AddUser.ARG_DNI_USUARIO) {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val isAdminAppParam = backStackEntry.arguments?.getBoolean(AppScreens.AddUser.ARG_IS_ADMIN_APP) ?: false
            val tipoUsuarioParam = backStackEntry.arguments?.getString(AppScreens.AddUser.ARG_TIPO_USUARIO)
            val centroIdParam = backStackEntry.arguments?.getString(AppScreens.AddUser.ARG_CENTRO_ID)
            val bloqueadoParam = backStackEntry.arguments?.getBoolean(AppScreens.AddUser.ARG_CENTRO_BLOQUEADO) ?: false
            val dniParam = backStackEntry.arguments?.getString(AppScreens.AddUser.ARG_DNI_USUARIO)
            
            AddUserScreen(
                navController = navController,
                centroIdParam = centroIdParam,
                bloqueadoParam = bloqueadoParam,
                tipoUsuarioParam = tipoUsuarioParam,
                dniParam = dniParam,
                isAdminAppParam = isAdminAppParam
            )
        }
        
        // Pantalla para añadir un nuevo centro
        composable(route = AppScreens.AddCentro.route) {
            com.tfg.umeegunero.feature.admin.screen.AddCentroScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla para editar un centro existente
        composable(
            route = AppScreens.EditCentro.route,
            arguments = listOf(
                navArgument("centroId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val centroId = backStackEntry.arguments?.getString("centroId") ?: ""
            com.tfg.umeegunero.feature.admin.screen.EditCentroScreen(
                navController = navController,
                centroId = centroId,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla de detalles de un centro educativo
        composable(
            route = AppScreens.DetalleCentro.route,
            arguments = listOf(
                navArgument("centroId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val centroId = backStackEntry.arguments?.getString("centroId") ?: ""
            com.tfg.umeegunero.feature.admin.screen.DetalleCentroScreen(
                centroId = centroId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(AppScreens.EditCentro.createRoute(id)) }
            )
        }
        
        // Pantalla de configuración de email
        composable(route = AppScreens.EmailConfig.route) {
            com.tfg.umeegunero.feature.admin.screen.EmailConfigScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Pantalla de configuración de email de soporte
        composable(route = AppScreens.EmailConfigSoporte.route) {
            com.tfg.umeegunero.feature.admin.screen.EmailConfigScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Pantalla de prueba de emails
        composable(route = AppScreens.PruebaEmail.route) {
            com.tfg.umeegunero.feature.admin.screen.test.EmailTestScreen(
                onClose = { navController.popBackStack() }
            )
        }
        
        // Perfil
        composable(
            route = AppScreens.Perfil.route,
            arguments = AppScreens.Perfil.arguments
        ) { backStackEntry ->
            val isAdminApp = backStackEntry.arguments?.getBoolean("isAdminApp") ?: false
            PerfilScreen(
                navController = navController,
                isAdminApp = isAdminApp
            )
        }
        
        // Perfil sin parámetros (acceso directo)
        composable(route = AppScreens.PerfilScreen.route) {
            PerfilScreen(
                navController = navController,
                isAdminApp = false
            )
        }
        
        // PANTALLAS DE ESTADÍSTICAS
        composable(route = AppScreens.Estadisticas.route) {
            com.tfg.umeegunero.feature.admin.screen.EstadisticasScreen(
                navController = navController
            )
        }

        // PANTALLAS DE CENTRO
        
        // Pantalla del dashboard del centro educativo
        composable(route = AppScreens.CentroDashboard.route) {
            com.tfg.umeegunero.feature.centro.screen.CentroDashboardScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // PANTALLAS DE FAMILIA
        
        // Pantalla del dashboard familiar
        composable(route = AppScreens.FamiliarDashboard.route) {
            com.tfg.umeegunero.feature.familiar.screen.FamiliaDashboardScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla de comunicados para familiares
        composable(route = AppScreens.ComunicadosFamilia.route) {
            ComunicadosFamiliaScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // PANTALLAS DE PROFESOR
        
        // Pantalla del dashboard de profesor
        composable(route = AppScreens.ProfesorDashboard.route) {
            // Muestra el dashboard del profesor directamente
            com.tfg.umeegunero.feature.profesor.screen.ProfesorDashboardScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        // Ruta para la evaluación académica
        composable(route = AppScreens.Evaluacion.route) {
            // La pantalla de evaluación ha sido eliminada
            // Redirigir a otra pantalla o mostrar un mensaje
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.Text("Pantalla de evaluación no disponible")
            }
        }
        
        // PANTALLAS DE CALENDARIO Y EVENTOS
        
        // Pantalla de calendario y eventos académicos
        composable(route = AppScreens.Calendario.route) {
            CalendarioScreen(
                navController = navController,
                viewModel = hiltViewModel<CalendarioViewModel>()
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
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla de detalle de día con eventos
        composable(
            route = AppScreens.DetalleDiaEvento.route,
            arguments = listOf(
                navArgument("fecha") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val fechaStr = backStackEntry.arguments?.getString("fecha") ?: LocalDate.now().toString()
            val fecha = try {
                LocalDate.parse(fechaStr)
            } catch (e: Exception) {
                LocalDate.now()
            }
            
            DetalleDiaEventoScreen(
                fecha = fecha,
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // PANTALLAS DE COMUNICACIÓN
        
        // Pantalla de bandeja de entrada - Redirigir a UnifiedInbox
        composable(route = AppScreens.BandejaEntrada.route) {
            UnifiedInboxScreen(
                onNavigateToMessage = { messageId ->
                    navController.navigate(AppScreens.MessageDetail.createRoute(messageId))
                },
                onNavigateToNewMessage = {
                    navController.navigate(AppScreens.NewMessage.createRoute())
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Pantalla de comunicados y circulares
        composable(route = AppScreens.ComunicadosCirculares.route) {
            com.tfg.umeegunero.feature.admin.screen.ComunicadosScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla para componer mensaje - Redirigir a NewMessage
        composable(
            route = AppScreens.ComponerMensaje.route,
            arguments = listOf(
                navArgument("destinatarioId") { 
                    type = NavType.StringType
                    nullable = true 
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val destinatarioId = backStackEntry.arguments?.getString("destinatarioId")
            
            com.tfg.umeegunero.feature.common.comunicacion.screen.NewMessageScreen(
                receiverId = destinatarioId,
                messageType = null,
                onBack = { navController.popBackStack() },
                onMessageSent = { navController.popBackStack() },
                viewModel = hiltViewModel()
            )
        }

        // PANTALLAS DE TAREAS
        
        // Pantalla de gestión de profesores
        composable(
            route = AppScreens.ProfesorList.route,
            arguments = AppScreens.ProfesorList.arguments
        ) { backStackEntry ->
            val centroId = backStackEntry.arguments?.getString(AppScreens.ProfesorList.ARG_CENTRO_ID) ?: ""
            
            com.tfg.umeegunero.feature.common.users.screen.ListProfesoresScreen(
                navController = navController,
                viewModel = hiltViewModel(),
                centroId = centroId
            )
        }
        
        // Pantalla de gestión de usuarios
        composable(
            route = AppScreens.GestionUsuarios.route,
            arguments = listOf(
                navArgument("isAdminApp") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val isAdminApp = backStackEntry.arguments?.getBoolean("isAdminApp") ?: false
            
            GestionUsuariosScreen(
                navController = navController,
                isAdminApp = isAdminApp,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserList = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToAddUser = { isAdmin ->
                    navController.navigate(AppScreens.AddUser.createRoute(
                        isAdminApp = isAdmin
                    )) {
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(AppScreens.Perfil.createRoute(isAdminApp)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Pantalla de seguridad
        composable(route = AppScreens.Seguridad.route) {
            val viewModel: SeguridadViewModel = hiltViewModel()
            SeguridadScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pantalla Dummy para desarrollo
        composable(
            route = AppScreens.Dummy.route,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: "Pantalla en desarrollo"
            
            com.tfg.umeegunero.feature.common.screen.DummyScreen(
                title = title,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pantallas de listado por tipo de usuario
        composable(route = AppScreens.AdminList.route) {
            com.tfg.umeegunero.feature.common.users.screen.ListAdministradoresScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        composable(route = AppScreens.AdminCentroList.route) {
            com.tfg.umeegunero.feature.common.users.screen.ListAdministradoresCentroScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // Lista de alumnos (ahora requiere centroId)
        composable(
            route = AppScreens.AlumnoList.route,
            arguments = AppScreens.AlumnoList.arguments
        ) { backStackEntry ->
            val centroId = backStackEntry.arguments?.getString(AppScreens.AlumnoList.ARG_CENTRO_ID)
            requireNotNull(centroId) { "El ID del centro es obligatorio para esta pantalla." }
            
            ListAlumnosScreen(
                navController = navController,
                centroId = centroId
            )
        }
        
        composable(route = AppScreens.FamiliarList.route) {
            com.tfg.umeegunero.feature.common.users.screen.ListFamiliaresScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        // Pantalla para editar usuario
        composable(
            route = AppScreens.EditUser.route,
            arguments = listOf(
                navArgument("dni") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val dni = backStackEntry.arguments?.getString("dni") ?: ""
            
            // Usar el componente AddUserScreen con los parámetros correctos
            com.tfg.umeegunero.feature.common.users.screen.AddUserScreen(
                navController = navController,
                dniParam = dni,
                isAdminAppParam = false, // Por defecto, no es administrador de aplicación
                bloqueadoParam = false // No bloquear centro por defecto
            )
        }

        // Pantalla de detalle de alumno (versión genérica)
        composable(
            route = AppScreens.DetalleAlumno.route,
            arguments = AppScreens.DetalleAlumno.arguments
        ) { backStackEntry ->
            val dni = backStackEntry.arguments?.getString("dni") ?: ""
            
            // Determinar el tipo de usuario actual
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val email = currentUser.email ?: ""
                // Usar pantalla completa para profesores y administradores
                if (email.contains("profesor") || email.contains("admin") || email.contains("centro")) {
                    // Si es profesor o administrador, mostrar la vista completa
                    com.tfg.umeegunero.feature.profesor.screen.DetalleAlumnoProfesorScreen(
                        navController = navController,
                        alumnoId = dni
                    )
                } else {
                    // Solo para familiares mostrar una vista más limitada
                    com.tfg.umeegunero.feature.common.users.screen.UserDetailScreen(
                        navController = navController,
                        userId = dni
                    )
                }
            } else {
                // Si no hay usuario logueado, mostrar vista básica
                com.tfg.umeegunero.feature.common.users.screen.UserDetailScreen(
                    navController = navController,
                    userId = dni
                )
            }
        }

        // Vinculación de profesores a clases
        composable(
            route = AppScreens.VincularProfesorClase.route,
            arguments = AppScreens.VincularProfesorClase.arguments
        ) { backStackEntry ->
            val centroId = backStackEntry.arguments?.getString("centroId")
            val claseId = backStackEntry.arguments?.getString("claseId")
            
            VincularProfesorClaseScreen(
                onBack = { navController.popBackStack() },
                centroId = centroId,
                claseId = claseId
            )
        }
        
        // Vinculación de alumnos a clases
        composable(
            route = AppScreens.VincularAlumnoClase.route,
            arguments = AppScreens.VincularAlumnoClase.arguments
        ) { backStackEntry ->
            val centroId = backStackEntry.arguments?.getString("centroId")
            val claseId = backStackEntry.arguments?.getString("claseId")
            
            VincularAlumnoClaseScreen(
                onBack = { navController.popBackStack() },
                centroId = centroId,
                claseId = claseId
            )
        }

        // Nueva navegación para gestor académico
        composable(
            route = "gestor_academico/{modo}?centroId={centroId}&cursoId={cursoId}&selectorCentroBloqueado={selectorCentroBloqueado}&selectorCursoBloqueado={selectorCursoBloqueado}&perfilUsuario={perfilUsuario}",
            arguments = listOf(
                navArgument("modo") { type = NavType.StringType },
                navArgument("centroId") { type = NavType.StringType; defaultValue = ""; nullable = true },
                navArgument("cursoId") { type = NavType.StringType; defaultValue = ""; nullable = true },
                navArgument("selectorCentroBloqueado") { type = NavType.BoolType; defaultValue = false },
                navArgument("selectorCursoBloqueado") { type = NavType.BoolType; defaultValue = false },
                navArgument("perfilUsuario") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val modo = backStackEntry.arguments?.getString("modo") ?: "CURSOS"
            val centroId = backStackEntry.arguments?.getString("centroId")
            val cursoId = backStackEntry.arguments?.getString("cursoId")
            val selectorCentroBloqueado = backStackEntry.arguments?.getBoolean("selectorCentroBloqueado") ?: false
            val selectorCursoBloqueado = backStackEntry.arguments?.getBoolean("selectorCursoBloqueado") ?: false
            val perfilUsuarioStr = backStackEntry.arguments?.getString("perfilUsuario") ?: "ADMIN_CENTRO"
            val perfilUsuario = when(perfilUsuarioStr) {
                "ADMIN_APP" -> com.tfg.umeegunero.data.model.TipoUsuario.ADMIN_APP
                else -> com.tfg.umeegunero.data.model.TipoUsuario.ADMIN_CENTRO
            }
            com.tfg.umeegunero.feature.common.academico.screen.GestorAcademicoScreen(
                modo = com.tfg.umeegunero.feature.common.academico.screen.ModoVisualizacion.valueOf(modo),
                centroId = centroId,
                cursoId = cursoId,
                selectorCentroBloqueado = selectorCentroBloqueado,
                selectorCursoBloqueado = selectorCursoBloqueado,
                perfilUsuario = perfilUsuario,
                onNavigate = { destino ->
                    if (destino == "back") {
                        navController.popBackStack()
                    } else {
                        navController.navigate(destino)
                    }
                }
            )
        }

        // Pantalla para añadir un curso
        composable(
            route = "add_curso?centroId={centroId}",
            arguments = listOf(
                navArgument("centroId") { type = NavType.StringType; defaultValue = ""; nullable = true }
            )
        ) { backStackEntry ->
            val centroId = backStackEntry.arguments?.getString("centroId")
            com.tfg.umeegunero.feature.common.academico.screen.HiltAddCursoScreen(
                centroId = centroId,
                onNavigateBack = { navController.popBackStack() },
                onCursoAdded = { navController.popBackStack() },
                navController = navController
            )
        }

        // Pantalla para editar un curso
        composable(
            route = "edit_curso/{cursoId}",
            arguments = listOf(
                navArgument("cursoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val cursoId = backStackEntry.arguments?.getString("cursoId") ?: ""
            com.tfg.umeegunero.feature.common.academico.screen.EditCursoScreen(
                navController = navController
                // El ViewModel se encarga de cargar el curso por ID
            )
        }

        // Pantalla para añadir una clase
        composable(
            route = "add_clase?centroId={centroId}&cursoId={cursoId}",
            arguments = listOf(
                navArgument("centroId") { type = NavType.StringType; defaultValue = ""; nullable = true },
                navArgument("cursoId") { type = NavType.StringType; defaultValue = ""; nullable = true }
            )
        ) { backStackEntry ->
            // Los parámetros se leen aquí pero se pasan directamente al ViewModel en AddClaseScreen
            // val centroId = backStackEntry.arguments?.getString("centroId")
            // val cursoId = backStackEntry.arguments?.getString("cursoId")
            com.tfg.umeegunero.feature.common.academico.screen.AddClaseScreen(
                navController = navController
            )
        }

        // Pantalla para editar una clase
        composable(
            route = "edit_clase/{claseId}",
            arguments = listOf(
                navArgument("claseId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // El claseId se lee aquí pero se usa directamente en el ViewModel
            // val claseId = backStackEntry.arguments?.getString("claseId") ?: ""
            com.tfg.umeegunero.feature.common.academico.screen.EditClaseScreen(
                navController = navController
                // El ViewModel se encarga de cargar la clase por ID
            )
        }

        // Pantalla de detalle de clase
        composable(
            route = AppScreens.DetalleClase.route,
            arguments = listOf(
                navArgument("claseId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val claseId = backStackEntry.arguments?.getString("claseId") ?: ""
            com.tfg.umeegunero.feature.common.academico.screen.DetalleClaseScreen(
                navController = navController,
                claseId = claseId,
                viewModel = hiltViewModel()
            )
        }

        // Pantalla de edición de perfil
        composable(route = AppScreens.EditProfile.route) {
            EditProfileScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Reemplazo de la pantalla redundante:
        composable(route = AppScreens.VinculacionFamiliar.route) {
            // Redirigir a la implementación real sin complicaciones
            navController.navigate(AppScreens.VincularAlumnoFamiliar.route)
        }

        // Pantalla de vinculación de alumnos y familiares
        composable(route = AppScreens.VincularAlumnoFamiliar.route) {
            com.tfg.umeegunero.feature.centro.screen.VincularAlumnoFamiliarScreen(
                navController = navController
            )
        }

        // Pantallas específicas de profesor
        composable(route = AppScreens.ListadoPreRegistroDiario.route) {
            com.tfg.umeegunero.feature.profesor.registros.screen.ListadoPreRegistroDiarioScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        // Pantalla de Registro Diario para el Profesor
        composable(
            route = AppScreens.RegistroDiarioProfesor.route,
            arguments = AppScreens.RegistroDiarioProfesor.arguments
        ) { backStackEntry ->
            val alumnosIds = backStackEntry.arguments?.getString("alumnosIds") ?: ""
            val fecha = backStackEntry.arguments?.getString("fecha")
            
            if (alumnosIds.isNotBlank()) {
                com.tfg.umeegunero.feature.profesor.registros.screen.HiltRegistroDiarioScreen(
                    alumnosIds = alumnosIds,
                    fecha = fecha,
                    navController = navController
                )
            } else {
                // Mostrar mensaje de error si no hay alumnos seleccionados
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: No se han seleccionado alumnos para el registro diario")
                }
            }
        }

        composable(route = AppScreens.HistoricoRegistroDiario.route) {
            com.tfg.umeegunero.feature.profesor.registros.screen.HistoricoRegistroDiarioScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(route = AppScreens.MisAlumnosProfesor.route) {
            com.tfg.umeegunero.feature.profesor.screen.MisAlumnosProfesorScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        // Pantalla de detalle de alumno para profesor
        composable(
            route = AppScreens.DetalleAlumnoProfesor.route,
            arguments = listOf(
                navArgument("alumnoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val alumnoId = backStackEntry.arguments?.getString("alumnoId") ?: ""
            Timber.d("Navegando a DetalleAlumnoProfesorScreen con alumnoId: $alumnoId")
            com.tfg.umeegunero.feature.profesor.screen.DetalleAlumnoProfesorScreen(
                navController = navController,
                alumnoId = alumnoId,
                viewModel = hiltViewModel()
            )
        }

        composable(route = AppScreens.UnifiedInbox.route) {
            UnifiedInboxScreen(
                onNavigateToMessage = { messageId ->
                    navController.navigate(AppScreens.MessageDetail.createRoute(messageId))
                },
                onNavigateToNewMessage = {
                    navController.navigate(AppScreens.NewMessage.createRoute())
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = AppScreens.ProfesorCalendario.route) {
            com.tfg.umeegunero.feature.profesor.screen.CalendarioProfesorScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        // Pantallas específicas de familiar
        composable(route = AppScreens.CalendarioFamilia.route) {
            com.tfg.umeegunero.feature.familiar.screen.CalendarioFamiliaScreen(
                navController = navController
            )
        }

        composable(route = AppScreens.NotificacionesFamiliar.route) {
            com.tfg.umeegunero.feature.familiar.screen.NotificacionesFamiliarScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Añadir ruta para pantalla de nuevo mensaje
        composable(
            route = AppScreens.NewMessage.route,
            arguments = AppScreens.NewMessage.arguments
        ) { backStackEntry ->
            val receiverId = backStackEntry.arguments?.getString("receiverId")
            val messageType = backStackEntry.arguments?.getString("messageType")
            
            com.tfg.umeegunero.feature.common.comunicacion.screen.NewMessageScreen(
                receiverId = receiverId,
                messageType = messageType,
                onBack = { navController.popBackStack() },
                onMessageSent = { navController.popBackStack() },
                viewModel = hiltViewModel()
            )
        }

        // Pantalla de detalle de mensaje unificado
        composable(
            route = AppScreens.MessageDetail.route,
            arguments = listOf(
                navArgument("messageId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val messageId = backStackEntry.arguments?.getString("messageId") ?: ""
            
            val onNavigateToConversation: (String) -> Unit = { combinedParams ->
                try {
                    // Extraer conversationId y participantId del string combinado
                    val params = combinedParams.split("/")
                    val conversationId = params.getOrNull(0) ?: ""
                    val participantId = params.getOrNull(1) ?: ""
                    
                    // Obtener el usuario actual para determinar el tipo
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val isProfesor = currentUser?.email?.contains("profesor") == true
                    
                    // Navegar con los parámetros correctos
                    val route = if (isProfesor) {
                        // Para profesores
                        AppScreens.ChatProfesor.createRoute(conversationId, participantId)
                    } else {
                        // Para familiares
                        AppScreens.ChatFamilia.createRoute(conversationId, participantId)
                    }
                    
                    Timber.d("Navegando a chat: $route")
                    navController.navigate(route)
                } catch (e: Exception) {
                    Timber.e(e, "Error al navegar a conversación: ${e.message}")
                    Toast.makeText(
                        navController.context,
                        "No se pudo abrir la conversación. Inténtelo más tarde.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            MessageDetailScreen(
                messageId = messageId,
                onBack = { navController.popBackStack() },
                onNavigateToConversation = onNavigateToConversation
            )
        }
        
        // Pantalla de chat (utilizando UnifiedMessage)
        composable(
            route = AppScreens.Chat.route,
            arguments = listOf(
                navArgument("conversacionId") { type = NavType.StringType },
                navArgument("participanteId") { type = NavType.StringType },
                navArgument("alumnoId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val conversacionId = backStackEntry.arguments?.getString("conversacionId") ?: ""
            val participanteId = backStackEntry.arguments?.getString("participanteId") ?: ""
            val alumnoId = backStackEntry.arguments?.getString("alumnoId")
            
            // Determinar si el usuario es profesor o familiar y redirigir a la pantalla apropiada
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                if (currentUser.email?.contains("profesor") == true) {
                    navController.navigate(AppScreens.ChatProfesor.createRoute(conversacionId, participanteId)) {
                        popUpTo(AppScreens.Chat.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(AppScreens.ChatFamilia.createRoute(conversacionId, participanteId)) {
                        popUpTo(AppScreens.Chat.route) { inclusive = true }
                    }
                }
            } else {
                // Si no hay usuario actual, simplemente volver atrás
                navController.popBackStack()
            }
        }
        
        // Pantalla de contactos para iniciar un chat nuevo
        composable(
            route = AppScreens.ChatContacts.route,
            arguments = AppScreens.ChatContacts.arguments
        ) { backStackEntry ->
            val chatRouteName = backStackEntry.arguments?.getString("chatRouteName") ?: ""
            
            com.tfg.umeegunero.feature.profesor.screen.ChatContactsScreen(
                navController = navController,
                chatRouteName = chatRouteName
            )
        }

        // Pantalla de detalle de comunicado
        composable(
            route = AppScreens.DetalleComunicado.route,
            arguments = listOf(
                navArgument("comunicadoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val comunicadoId = backStackEntry.arguments?.getString("comunicadoId") ?: ""
            ComunicadoDetailScreen(
                comunicadoId = comunicadoId,
                onBack = { navController.popBackStack() }
            )
        }

        // Pantalla de chat para profesores
        composable(
            route = AppScreens.ChatProfesor.route,
            arguments = listOf(
                navArgument("conversacionId") { type = NavType.StringType },
                navArgument("participanteId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val conversacionId = backStackEntry.arguments?.getString("conversacionId") ?: ""
            val participanteId = backStackEntry.arguments?.getString("participanteId") ?: ""
            
            com.tfg.umeegunero.feature.profesor.screen.ChatProfesorScreen(
                navController = navController,
                familiarId = participanteId,
                conversacionId = conversacionId
            )
        }
        
        // Pantalla de chat para familiares
        composable(
            route = AppScreens.ChatFamilia.route,
            arguments = listOf(
                navArgument("conversacionId") { type = NavType.StringType },
                navArgument("participanteId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val conversacionId = backStackEntry.arguments?.getString("conversacionId") ?: ""
            val participanteId = backStackEntry.arguments?.getString("participanteId") ?: ""
            
            com.tfg.umeegunero.feature.familiar.screen.ChatFamiliaScreen(
                navController = navController,
                profesorId = participanteId,
                conversacionId = conversacionId
            )
        }

        // Pantalla de detalle de usuario (UserDetail)
        composable(
            route = AppScreens.UserDetail.route,
            arguments = listOf(
                navArgument("dni") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val dni = backStackEntry.arguments?.getString("dni") ?: ""
            com.tfg.umeegunero.feature.common.users.screen.UserDetailScreen(
                navController = navController,
                userId = dni
            )
        }

        // Pantalla de historial de solicitudes
        composable(route = AppScreens.HistorialSolicitudes.route) {
            com.tfg.umeegunero.feature.centro.screen.HistorialSolicitudesScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla de configuración de tema
        composable(route = AppScreens.CambiarTema.route) {
            com.tfg.umeegunero.feature.common.theme.CambiarTemaScreen(
                navController = navController
            )
        }

        // Pantalla de consulta de registros diarios para familiares
        composable(
            route = AppScreens.ConsultaRegistroDiario.route,
            arguments = AppScreens.ConsultaRegistroDiario.arguments
        ) { backStackEntry ->
            val alumnoId = backStackEntry.arguments?.getString("alumnoId") ?: ""
            val alumnoNombre = backStackEntry.arguments?.getString("alumnoNombre") ?: "Alumno"
            val registroId = backStackEntry.arguments?.getString("registroId")
            
            Timber.d("Navegando a ConsultaRegistroDiario para alumno: $alumnoId, nombre: $alumnoNombre, registroId: $registroId")
            
            com.tfg.umeegunero.feature.familiar.registros.screen.HiltConsultaRegistroDiarioScreen(
                alumnoId = alumnoId,
                alumnoNombre = alumnoNombre,
                registroId = registroId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Ruta para la pantalla de detalle de registro de actividad para familiares
        composable(
            route = AppScreens.DetalleRegistro.route,
            arguments = listOf(
                navArgument("registroId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val registroId = backStackEntry.arguments?.getString("registroId") ?: ""
            com.tfg.umeegunero.feature.familiar.screen.DetalleRegistroScreen(
                registroId = registroId,
                navController = navController
            )
        }
    }
} 