package com.tfg.umeegunero.navigation

import android.net.Uri
import android.util.Log
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
import java.time.LocalDate

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
                }
            )
        }
        
        // Pantalla de registro de usuarios (familiar)
        composable(route = AppScreens.Registro.route) {
            RegistroScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegistroCompletado = { navController.navigate(AppScreens.Login.createRoute("FAMILIAR")) }
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
            AdminDashboardScreen(
                navController = navController,
                viewModel = hiltViewModel()
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
        
        // Pantalla para añadir un nuevo centro
        composable(route = AppScreens.AddCentro.route) {
            com.tfg.umeegunero.feature.admin.screen.AddCentroScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onCentroAdded = { navController.navigate(AppScreens.GestionCentros.route) { popUpTo(AppScreens.GestionCentros.route) } }
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
                centroId = centroId,
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onCentroUpdated = { navController.popBackStack() }
            )
        }
        
        // Pantalla para ver detalles de un centro
        composable(
            route = AppScreens.DetalleCentro.route,
            arguments = listOf(
                navArgument("centroId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val centroId = backStackEntry.arguments?.getString("centroId") ?: ""
            com.tfg.umeegunero.feature.admin.screen.DetalleCentroScreen(
                centroId = centroId,
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { navController.navigate(AppScreens.EditCentro.createRoute(it)) },
                onDeleteSuccess = { navController.popBackStack() }
            )
        }
        
        // Pantalla de configuración de email
        composable(route = AppScreens.EmailConfig.route) {
            com.tfg.umeegunero.feature.admin.screen.EmailConfigScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Pantalla de perfil de usuario
        composable(route = AppScreens.Perfil.route) {
            com.tfg.umeegunero.feature.common.perfil.screen.PerfilScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla de reportes de uso
        composable(route = AppScreens.ReporteUso.route) {
            com.tfg.umeegunero.feature.admin.screen.ReporteUsoScreen(
                navController = navController
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
        
        // Pantalla de detalle de clase
        composable(
            route = AppScreens.DetalleClase.route,
            arguments = listOf(
                navArgument("claseId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val claseId = backStackEntry.arguments?.getString("claseId") ?: ""
            
            DetalleClaseScreen(
                claseId = claseId,
                navController = navController,
                viewModel = hiltViewModel()
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

        // Pantalla para editar usuario
        composable(
            route = AppScreens.EditUser.route,
            arguments = listOf(
                navArgument("dni") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val dni = backStackEntry.arguments?.getString("dni") ?: ""
            val viewModel: com.tfg.umeegunero.feature.common.users.viewmodel.AddUserViewModel = hiltViewModel()
            
            com.tfg.umeegunero.feature.common.users.screen.AddUserScreen(
                uiState = com.tfg.umeegunero.feature.common.users.screen.AddUserUiState(
                    dni = viewModel.uiState.collectAsState().value.dni,
                    dniError = viewModel.uiState.collectAsState().value.dniError,
                    email = viewModel.uiState.collectAsState().value.email,
                    emailError = viewModel.uiState.collectAsState().value.emailError,
                    password = viewModel.uiState.collectAsState().value.password,
                    passwordError = viewModel.uiState.collectAsState().value.passwordError,
                    confirmPassword = viewModel.uiState.collectAsState().value.confirmPassword,
                    confirmPasswordError = viewModel.uiState.collectAsState().value.confirmPasswordError,
                    nombre = viewModel.uiState.collectAsState().value.nombre,
                    nombreError = viewModel.uiState.collectAsState().value.nombreError,
                    apellidos = viewModel.uiState.collectAsState().value.apellidos,
                    apellidosError = viewModel.uiState.collectAsState().value.apellidosError,
                    telefono = viewModel.uiState.collectAsState().value.telefono,
                    telefonoError = viewModel.uiState.collectAsState().value.telefonoError,
                    tipoUsuario = viewModel.uiState.collectAsState().value.tipoUsuario,
                    isLoading = viewModel.uiState.collectAsState().value.isLoading,
                    error = viewModel.uiState.collectAsState().value.error,
                    success = viewModel.uiState.collectAsState().value.success
                ),
                onUpdateDni = viewModel::updateDni,
                onUpdateEmail = viewModel::updateEmail,
                onUpdatePassword = viewModel::updatePassword,
                onUpdateConfirmPassword = viewModel::updateConfirmPassword,
                onUpdateNombre = viewModel::updateNombre,
                onUpdateApellidos = viewModel::updateApellidos,
                onUpdateTelefono = viewModel::updateTelefono,
                onUpdateTipoUsuario = viewModel::updateTipoUsuario,
                onUpdateCentroSeleccionado = { centro -> 
                    centro?.id?.let { viewModel.updateCentroSeleccionado(it) }
                },
                onSaveUser = viewModel::saveUser,
                onClearError = viewModel::clearError,
                onNavigateBack = { navController.popBackStack() }
            )
            
            // Cargar los datos del usuario para edición
            LaunchedEffect(dni) {
                if (dni.isNotBlank()) {
                    viewModel.loadUserForEdit(dni)
                }
            }
        }
    }
} 