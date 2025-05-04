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
import com.tfg.umeegunero.feature.profesor.screen.TareasScreen
import com.tfg.umeegunero.feature.profesor.screen.DetalleTareaScreen
import com.tfg.umeegunero.feature.profesor.screen.EvaluacionScreen
import com.tfg.umeegunero.feature.common.academico.screen.CalendarioScreen
import com.tfg.umeegunero.feature.common.academico.screen.DetalleEventoScreen
import com.tfg.umeegunero.feature.common.academico.screen.DetalleClaseScreen
import com.tfg.umeegunero.feature.common.academico.viewmodel.CalendarioViewModel
import com.tfg.umeegunero.feature.admin.screen.AdminDashboardScreen
import com.tfg.umeegunero.feature.common.academico.screen.detallediaevento.DetalleDiaEventoScreen
import com.tfg.umeegunero.feature.common.comunicacion.screen.BandejaEntradaScreen
import com.tfg.umeegunero.feature.common.comunicacion.screen.ComponerMensajeScreen
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

/**
 * Navegación principal de la aplicación
 * 
 * @param navController Controlador de navegación
 * @param startDestination Ruta inicial
 * @param onCloseApp Función para cerrar la app
 */
@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppScreens.Welcome.route,
    onCloseApp: () -> Unit = {}
) {
    // Interceptor para registrar navegación en Logcat
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            val route = backStackEntry.destination.route ?: "desconocida"
            val screenName = route.split("?")[0].split("/")[0]
            // Usar un tag específico y nivel ERROR para asegurar visibilidad
            timber.log.Timber.tag("NAVEGACION_UME").e("📱 PANTALLA ACTUAL: $screenName (ruta: $route)")
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
                    navController.navigate(AppScreens.Config.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToEmailConfig = { navController.navigate(AppScreens.EmailConfig.route) },
                onNavigateToNotificaciones = { navController.navigate(AppScreens.Notificaciones.route) },
                onNavigateToComunicados = { navController.navigate(AppScreens.ComunicadosCirculares.route) },
                onNavigateToBandejaEntrada = { navController.navigate(AppScreens.BandejaEntrada.route) },
                onNavigateToComponerMensaje = { navController.navigate(AppScreens.ComponerMensaje.route) },
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
                    navController.navigate(AppScreens.Perfil.route) {
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
        
        // Pantalla para añadir usuario
        composable(
            route = AppScreens.AddUser.route,
            arguments = AppScreens.AddUser.arguments
        ) { backStackEntry ->
            val isAdminApp = backStackEntry.arguments?.getBoolean(AppScreens.AddUser.ARG_IS_ADMIN_APP) ?: false
            val tipoUsuarioStr = backStackEntry.arguments?.getString(AppScreens.AddUser.ARG_TIPO_USUARIO)
            val centroId = backStackEntry.arguments?.getString(AppScreens.AddUser.ARG_CENTRO_ID)
            val centroBloqueado = backStackEntry.arguments?.getBoolean(AppScreens.AddUser.ARG_CENTRO_BLOQUEADO) ?: false
            val dniUsuario = backStackEntry.arguments?.getString(AppScreens.AddUser.ARG_DNI_USUARIO)
            
            AddUserScreen(
                navController = navController,
                isAdminApp = isAdminApp,
                tipoPreseleccionado = tipoUsuarioStr,
                centroIdInicial = centroId,
                centroBloqueadoInicial = false,
                dniUsuario = dniUsuario
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
        
        // Pantalla de perfil
        composable(route = AppScreens.Perfil.route) {
            PerfilScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Pantalla de estadísticas
        composable(route = AppScreens.Estadisticas.route) {
            com.tfg.umeegunero.feature.admin.screen.EstadisticasScreen(
                navController = navController,
                viewModel = hiltViewModel()
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
            com.tfg.umeegunero.feature.familiar.screen.ComunicadosFamiliaScreen(
                viewModel = hiltViewModel(),
                onNavigateUp = { navController.popBackStack() },
                navController = navController
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
            EvaluacionScreen(
                navController = navController,
                alumnos = emptyList()
            )
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
        
        // Pantalla de bandeja de entrada
        composable(route = AppScreens.BandejaEntrada.route) {
            BandejaEntradaScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla de comunicados y circulares
        composable(route = AppScreens.ComunicadosCirculares.route) {
            com.tfg.umeegunero.feature.admin.screen.ComunicadosScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla para componer mensaje
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
            
            ComponerMensajeScreen(
                navController = navController,
                destinatarioId = destinatarioId,
                viewModel = hiltViewModel()
            )
        }

        // PANTALLAS DE TAREAS
        
        // Pantalla de tareas
        composable(route = AppScreens.Tareas.route) {
            TareasScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla de detalle de tarea
        composable(
            route = AppScreens.DetalleTarea.route,
            arguments = listOf(
                navArgument("tareaId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tareaId = backStackEntry.arguments?.getString("tareaId") ?: ""
            
            DetalleTareaScreen(
                tareaId = tareaId,
                navController = navController,
                viewModel = hiltViewModel()
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
                onNavigateToAddUser = { isAdminApp ->
                    navController.navigate(AppScreens.AddUser.createRoute(
                        isAdminApp = isAdminApp
                    )) {
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(AppScreens.Perfil.route) {
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
        
        composable(
            route = AppScreens.ProfesorList.route,
            arguments = AppScreens.ProfesorList.arguments
        ) { backStackEntry ->
            val centroId = backStackEntry.arguments?.getString(AppScreens.ProfesorList.ARG_CENTRO_ID)
            requireNotNull(centroId) { "El ID del centro es obligatorio para la lista de profesores." }
            
            com.tfg.umeegunero.feature.common.users.screen.ListProfesoresScreen(
                navController = navController,
                centroId = centroId,
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
            arguments = AppScreens.EditUser.arguments
        ) { backStackEntry ->
            val dni = backStackEntry.arguments?.getString("dni") ?: ""
            val tipoUsuarioStr = backStackEntry.arguments?.getString("tipoUsuario")
            
            AddUserScreen(
                navController = navController,
                viewModel = hiltViewModel(),
                isAdminApp = false,
                tipoPreseleccionado = tipoUsuarioStr,
                centroIdInicial = null,
                centroBloqueadoInicial = false,
                dniUsuario = dni
            )
        }

        // Pantalla de detalle de usuario
        composable(
            route = AppScreens.UserDetail.route,
            arguments = listOf(
                navArgument("dni") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val dni = backStackEntry.arguments?.getString("dni") ?: ""
            com.tfg.umeegunero.feature.common.users.screen.UserDetailScreen(
                navController = navController,
                userId = dni,
                viewModel = hiltViewModel()
            )
        }

        // Pantalla de detalle de estudiante
        composable(
            route = AppScreens.StudentDetail.route,
            arguments = listOf(
                navArgument("alumnoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val alumnoId = backStackEntry.arguments?.getString("alumnoId") ?: ""
            com.tfg.umeegunero.feature.profesor.screen.DetalleAlumnoProfesorScreen(
                navController = navController,
                alumnoId = alumnoId,
                viewModel = hiltViewModel()
            )
        }

        // Pantalla para vincular profesor a una clase
        composable(
            route = AppScreens.VincularProfesorClase.route,
            arguments = listOf(
                navArgument("centroId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val centroId = backStackEntry.arguments?.getString("centroId") ?: ""
            
            // Navegar a la pantalla de vincular profesor a clase
            com.tfg.umeegunero.feature.centro.screen.VincularProfesorClaseScreen(
                centroId = centroId,
                onBack = { navController.popBackStack() }
            )
        }

        // También mantenemos la ruta antigua para compatibilidad con navegaciones desde otras partes
        composable(
            route = "vincular_profesor_clase/{claseId}?cursoId={cursoId}",
            arguments = listOf(
                navArgument("claseId") { type = NavType.StringType },
                navArgument("cursoId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val claseId = backStackEntry.arguments?.getString("claseId") ?: ""
            val cursoId = backStackEntry.arguments?.getString("cursoId")
            
            // Navegar a la pantalla de vincular profesor a clase
            com.tfg.umeegunero.feature.centro.screen.VincularProfesorClaseScreen(
                claseId = claseId,
                onBack = { navController.popBackStack() }
            )
        }

        // Pantalla para vincular alumnos a familiares
        composable(route = AppScreens.VincularAlumnoFamiliar.route) {
            com.tfg.umeegunero.feature.centro.screen.VincularAlumnoFamiliarScreen(
                navController = navController
            )
        }

        // Pantalla de historial de solicitudes de vinculación
        composable(route = AppScreens.HistorialSolicitudes.route) {
            com.tfg.umeegunero.feature.centro.screen.HistorialSolicitudesScreen(
                navController = navController
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
            val centroId = backStackEntry.arguments?.getString("centroId")
            val cursoId = backStackEntry.arguments?.getString("cursoId")
            com.tfg.umeegunero.feature.common.academico.screen.AddClaseScreen(
                navController = navController
                // El ViewModel puede recibir centroId y cursoId si es necesario
            )
        }

        // Pantalla para editar una clase
        composable(
            route = "edit_clase/{claseId}",
            arguments = listOf(
                navArgument("claseId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val claseId = backStackEntry.arguments?.getString("claseId") ?: ""
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

        // PANTALLAS DE REGISTRO DIARIO

        // Pantalla de listado previo al registro diario (selección de alumnos)
        composable(route = AppScreens.ListadoPreRegistroDiario.route) {
            com.tfg.umeegunero.feature.profesor.registros.screen.ListadoPreRegistroDiarioScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla de registro diario del profesor (nueva versión con soporte para múltiples alumnos)
        composable(
            route = AppScreens.RegistroDiarioProfesor.route,
            arguments = AppScreens.RegistroDiarioProfesor.arguments
        ) { backStackEntry ->
            val alumnosIds = backStackEntry.arguments?.getString("alumnosIds") ?: ""
            val fecha = backStackEntry.arguments?.getString("fecha")
            
            com.tfg.umeegunero.feature.profesor.registros.screen.HiltRegistroDiarioScreen(
                alumnosIds = alumnosIds,
                fecha = fecha,
                navController = navController
            )
        }

        // Pantalla de chat entre usuarios
        composable(
            route = AppScreens.Chat.route,
            arguments = listOf(
                navArgument("familiarId") { type = NavType.StringType },
                navArgument("alumnoId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val familiarId = backStackEntry.arguments?.getString("familiarId") ?: ""
            com.tfg.umeegunero.feature.profesor.screen.ChatProfesorScreen(
                navController = navController,
                familiarId = familiarId
            )
        }

        // Reemplazo de la pantalla redundante:
        composable(route = AppScreens.VinculacionFamiliar.route) {
            // Redirigir a la implementación real sin complicaciones
            navController.navigate(AppScreens.VincularAlumnoFamiliar.route)
        }

        // Mis alumnos para el profesor
        composable(route = AppScreens.MisAlumnosProfesor.route) {
            com.tfg.umeegunero.feature.profesor.screen.MisAlumnosProfesorScreen(
                navController = navController
            )
        }

        // Pantalla de Calendario para profesor
        composable(route = AppScreens.ProfesorCalendario.route) {
            com.tfg.umeegunero.feature.profesor.screen.CalendarioProfesorScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
    }
} 