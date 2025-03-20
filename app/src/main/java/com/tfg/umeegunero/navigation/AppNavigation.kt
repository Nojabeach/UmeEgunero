package com.tfg.umeegunero.navigation

import android.content.Context
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.UserType
import com.tfg.umeegunero.feature.admin.screen.AdminDashboardScreen
import com.tfg.umeegunero.feature.admin.screen.AddCentroScreen
import com.tfg.umeegunero.feature.admin.screen.AlumnoListScreen
import com.tfg.umeegunero.feature.admin.screen.CalendarioScreen
import com.tfg.umeegunero.feature.admin.screen.ClasesScreen
import com.tfg.umeegunero.feature.admin.screen.CursosScreen
import com.tfg.umeegunero.feature.admin.screen.DetalleCentroScreen
import com.tfg.umeegunero.feature.admin.screen.EditCentroScreen
import com.tfg.umeegunero.feature.admin.screen.EstadisticasScreen
import com.tfg.umeegunero.feature.admin.screen.FamiliarListScreen
import com.tfg.umeegunero.feature.admin.screen.NotificacionesScreen
import com.tfg.umeegunero.feature.admin.screen.ProfesorListScreen
import com.tfg.umeegunero.feature.admin.screen.UserDetailScreen
import com.tfg.umeegunero.feature.auth.screen.LoginScreen
import com.tfg.umeegunero.feature.auth.screen.RegistroScreen
import com.tfg.umeegunero.feature.centro.screen.CentroDashboardScreen
import com.tfg.umeegunero.feature.centro.screen.academico.AddCursoScreen
import com.tfg.umeegunero.feature.centro.screen.academico.AddClaseScreen
import com.tfg.umeegunero.feature.common.config.screen.ConfiguracionScreen
import com.tfg.umeegunero.feature.common.config.screen.PerfilConfiguracion
import com.tfg.umeegunero.feature.common.config.viewmodel.ConfiguracionViewModel
import com.tfg.umeegunero.feature.common.perfil.screen.PerfilScreen
import com.tfg.umeegunero.feature.common.screen.DummyScreen
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeScreen
import com.tfg.umeegunero.feature.familiar.screen.FamiliarDashboardScreen
import com.tfg.umeegunero.feature.familiar.screen.HiltFamiliarDashboardScreen
import com.tfg.umeegunero.feature.profesor.screen.ProfesorDashboardScreen
import com.tfg.umeegunero.feature.profesor.viewmodel.ProfesorDashboardViewModel
import com.tfg.umeegunero.feature.familiar.viewmodel.FamiliarDashboardViewModel
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeUserType
import com.tfg.umeegunero.feature.auth.screen.RecuperarPasswordScreen
import com.tfg.umeegunero.feature.common.welcome.screen.SoporteTecnicoScreen
import com.tfg.umeegunero.feature.admin.screen.EmailConfigScreen

/**
 * Navegación principal de la aplicación
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    onCloseApp: () -> Unit = {} // Para cerrar la app
) {
    NavHost(
        navController = navController,
        startDestination = AppScreens.Welcome.route
    ) {
        // Pantalla de bienvenida
        composable(route = AppScreens.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = { userType ->
                    val userTypeRoute = when (userType) {
                        WelcomeUserType.ADMIN -> "ADMIN"
                        WelcomeUserType.CENTRO -> "CENTRO"
                        WelcomeUserType.PROFESOR -> "PROFESOR"
                        WelcomeUserType.FAMILIAR -> "FAMILIAR"
                    }
                    navController.navigate(
                        AppScreens.Login.createRoute(userTypeRoute)
                    )
                },
                onNavigateToRegister = {
                    navController.navigate(AppScreens.Registro.route)
                },
                onCloseApp = onCloseApp,
                onNavigateToSupport = {
                    navController.navigate(AppScreens.SoporteTecnico.route)
                }
            )
        }

        // Pantalla de soporte técnico
        composable(route = AppScreens.SoporteTecnico.route) {
            SoporteTecnicoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pantalla de configuración de email para el administrador
        composable(route = AppScreens.EmailConfig.route) {
            EmailConfigScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pantalla de inicio de sesión (recibe tipo de usuario)
        composable(
            route = AppScreens.Login.route,
            arguments = listOf(
                navArgument("userType") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val userTypeStr = backStackEntry.arguments?.getString("userType") ?: "FAMILIAR"
            val userType = remember(userTypeStr) {
                when(userTypeStr) {
                    "ADMIN" -> UserType.ADMIN_APP
                    "CENTRO" -> UserType.ADMIN_CENTRO
                    "PROFESOR" -> UserType.PROFESOR
                    else -> UserType.FAMILIAR
                }
            }

            LoginScreen(
                userType = userType,
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = {
                    val route = when(userType) {
                        UserType.ADMIN_APP -> AppScreens.AdminDashboard.route
                        UserType.ADMIN_CENTRO -> AppScreens.CentroDashboard.route
                        UserType.PROFESOR -> AppScreens.ProfesorDashboard.route
                        UserType.FAMILIAR -> AppScreens.FamiliarDashboard.route
                    }
                    navController.navigate(route) {
                        popUpTo(AppScreens.Welcome.route) { inclusive = true }
                    }
                },
                onForgotPassword = {
                    navController.navigate(AppScreens.RecuperarPassword.route)
                }
            )
        }

        // Pantalla de registro
        composable(route = AppScreens.Registro.route) {
            RegistroScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onRegistroCompletado = {
                    navController.navigate(AppScreens.Login.createRoute("FAMILIAR")) {
                        popUpTo(AppScreens.Welcome.route)
                    }
                }
            )
        }

        // Pantalla de configuración (compartida por todos los perfiles)
        composable(route = AppScreens.Config.route) {
            // Determinamos el perfil basado en la ruta anterior
            val perfil = when {
                navController.previousBackStackEntry?.destination?.route == AppScreens.AdminDashboard.route -> 
                    PerfilConfiguracion.ADMIN
                navController.previousBackStackEntry?.destination?.route == AppScreens.CentroDashboard.route -> 
                    PerfilConfiguracion.CENTRO
                navController.previousBackStackEntry?.destination?.route == AppScreens.ProfesorDashboard.route -> 
                    PerfilConfiguracion.PROFESOR
                navController.previousBackStackEntry?.destination?.route == AppScreens.FamiliarDashboard.route -> 
                    PerfilConfiguracion.FAMILIAR
                else -> PerfilConfiguracion.SISTEMA
            }
            
            // Registramos la ruta anterior para poder volver a ella
            val previousRoute = when (perfil) {
                PerfilConfiguracion.ADMIN -> AppScreens.AdminDashboard.route
                PerfilConfiguracion.CENTRO -> AppScreens.CentroDashboard.route
                PerfilConfiguracion.PROFESOR -> AppScreens.ProfesorDashboard.route
                PerfilConfiguracion.FAMILIAR -> AppScreens.FamiliarDashboard.route
                else -> AppScreens.Welcome.route
            }
            
            ConfiguracionScreen(
                perfil = perfil,
                onNavigateBack = { 
                    // Navegamos de regreso a la pantalla de inicio correspondiente
                    navController.navigate(previousRoute) {
                        popUpTo(AppScreens.Config.route) { inclusive = true }
                    }
                },
                onMenuClick = {
                    // Volvemos a la pantalla anterior, que ya tiene su drawer configurado
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de añadir centro (admin)
        composable(route = AppScreens.AddCentro.route) {
            AddCentroScreen(
                viewModel = hiltViewModel(),
                navController = navController
            )
        }

        // Pantalla de editar centro (admin)
        composable(
            route = AppScreens.EditCentro.route,
            arguments = listOf(
                navArgument("centroId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val centroId = backStackEntry.arguments?.getString("centroId") ?: ""

            EditCentroScreen(
                navController = navController,
                centroId = centroId,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla de detalle de centro (admin)
        composable(
            route = AppScreens.DetalleCentro.route,
            arguments = listOf(
                navArgument("centroId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val centroId = backStackEntry.arguments?.getString("centroId") ?: ""
            
            DetalleCentroScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onMenuClick = {
                    // Volvemos a la pantalla anterior, que ya tiene su drawer configurado
                    navController.popBackStack()
                },
                onEditCentro = { centroId ->
                    navController.navigate(AppScreens.EditCentro.createRoute(centroId))
                }
            )
        }

        // Pantalla de dashboard de administrador
        composable(route = AppScreens.AdminDashboard.route) {
            AdminDashboardScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        // Pantalla de dashboard de centro
        composable(route = AppScreens.CentroDashboard.route) {
            CentroDashboardScreen(
                navController = navController
            )
        }

        // Pantalla de dashboard de profesor
        composable(route = AppScreens.ProfesorDashboard.route) {
            val viewModel: ProfesorDashboardViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            ProfesorDashboardScreen(
                navController = navController,
                isLoading = uiState.isLoading,
                error = uiState.error,
                selectedTab = uiState.selectedTab,
                onTabSelected = viewModel::setSelectedTab,
                alumnosPendientes = uiState.alumnosPendientes,
                alumnos = uiState.alumnos,
                mensajesNoLeidos = emptyList(),
                totalMensajesNoLeidos = uiState.totalMensajesNoLeidos,
                onNavigateToDetalleAlumno = { alumnoId -> 
                    navController.navigate(AppScreens.StudentDetail.createRoute(alumnoId)) 
                },
                onNavigateToChat = { familiarId, alumnoId ->
                    navController.navigate(AppScreens.Chat.createRoute(familiarId, alumnoId))
                },
                onCrearRegistroActividad = { alumnoId ->
                    // Acción para crear registro
                },
                onErrorDismissed = viewModel::clearError,
                onLogout = {
                    viewModel.logout()
                    navController.navigate(AppScreens.Welcome.route) {
                        popUpTo(AppScreens.ProfesorDashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla de dashboard de familiar
        composable(route = AppScreens.FamiliarDashboard.route) {
            HiltFamiliarDashboardScreen(
                navController = navController
            )
        }

        // Pantalla de añadir usuario
        composable(
            route = AppScreens.AddUser.route,
            arguments = listOf(
                navArgument("isAdminApp") {
                    type = NavType.BoolType
                    defaultValue = true
                }
            )
        ) { backStackEntry ->
            val isAdminApp = backStackEntry.arguments?.getBoolean("isAdminApp") ?: true
            // Comentado temporalmente hasta implementar AddUserScreen y componentes relacionados
            /*
            val viewModel: AddUserViewModel = hiltViewModel()
            val viewModelUiState = viewModel.uiState.collectAsState().value

            // Creamos una lista de centros disponibles (esto debería venir del ViewModel en una implementación real)
            val centrosDisponibles = remember { emptyList<Centro>() }

            // Buscamos el centro seleccionado por su ID (si existe)
            val centroSeleccionado = remember(viewModelUiState.centroId, centrosDisponibles) {
                if (viewModelUiState.centroId.isNotEmpty()) {
                    centrosDisponibles.find { it.id == viewModelUiState.centroId }
                } else {
                    null
                }
            }

            // Adaptamos el estado del ViewModel al formato que espera AddUserScreen
            val adaptedUiState = AddUserUiState(
                dni = viewModelUiState.dni,
                dniError = viewModelUiState.dniError,
                email = viewModelUiState.email,
                emailError = viewModelUiState.emailError,
                password = viewModelUiState.password,
                passwordError = viewModelUiState.passwordError,
                confirmPassword = viewModelUiState.confirmPassword,
                confirmPasswordError = viewModelUiState.confirmPasswordError,
                nombre = viewModelUiState.nombre,
                nombreError = viewModelUiState.nombreError,
                apellidos = viewModelUiState.apellidos,
                apellidosError = viewModelUiState.apellidosError,
                telefono = viewModelUiState.telefono,
                telefonoError = viewModelUiState.telefonoError,
                tipoUsuario = viewModelUiState.tipoUsuario,
                centroSeleccionado = centroSeleccionado,
                centrosDisponibles = centrosDisponibles,
                isLoading = viewModelUiState.isLoading,
                error = viewModelUiState.error,
                success = viewModelUiState.success,
                isAdminApp = isAdminApp
            )

            AddUserScreen(
                uiState = adaptedUiState,
                onUpdateDni = viewModel::updateDni,
                onUpdateEmail = viewModel::updateEmail,
                onUpdatePassword = viewModel::updatePassword,
                onUpdateConfirmPassword = viewModel::updateConfirmPassword,
                onUpdateNombre = viewModel::updateNombre,
                onUpdateApellidos = viewModel::updateApellidos,
                onUpdateTelefono = viewModel::updateTelefono,
                onUpdateTipoUsuario = viewModel::updateTipoUsuario,
                onUpdateCentroSeleccionado = { centro ->
                    centro?.let { viewModel.updateCentroSeleccionado(it.id) }
                },
                onSaveUser = viewModel::saveUser,
                onClearError = viewModel::clearError,
                onNavigateBack = { navController.popBackStack() }
            )
            */
            
            // Mientras tanto, mostramos una pantalla dummy para añadir usuario
            DummyScreen(
                title = "Añadir Usuario",
                onNavigateBack = { navController.popBackStack() },
                onMenuClick = {}
            )
        }

        // Pantallas de gestión académica
        // Añadir curso
        composable(
            route = AppScreens.AddCurso.route,
            arguments = listOf(
                navArgument("centroId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val centroId = backStackEntry.arguments?.getString("centroId") ?: ""

            AddCursoScreen(
                viewModel = hiltViewModel(),
                cursoId = "",
                centroId = centroId,
                onNavigateBack = { navController.popBackStack() },
                onCursoAdded = { navController.popBackStack() }
            )
        }

        // Editar curso
        composable(
            route = AppScreens.EditCurso.route,
            arguments = listOf(
                navArgument("centroId") {
                    type = NavType.StringType
                },
                navArgument("cursoId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val centroId = backStackEntry.arguments?.getString("centroId") ?: ""
            val cursoId = backStackEntry.arguments?.getString("cursoId") ?: ""

            AddCursoScreen(
                viewModel = hiltViewModel(),
                cursoId = cursoId,
                centroId = centroId,
                onNavigateBack = { navController.popBackStack() },
                onCursoAdded = { navController.popBackStack() }
            )
        }

        // Añadir clase
        composable(
            route = AppScreens.AddClase.route,
            arguments = listOf(
                navArgument("centroId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val centroId = backStackEntry.arguments?.getString("centroId") ?: ""

            AddClaseScreen(
                viewModel = hiltViewModel(),
                claseId = "",
                centroId = centroId,
                onNavigateBack = { navController.popBackStack() },
                onClaseAdded = { navController.popBackStack() }
            )
        }

        // Editar clase
        composable(
            route = AppScreens.EditClase.route,
            arguments = listOf(
                navArgument("centroId") {
                    type = NavType.StringType
                },
                navArgument("claseId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val centroId = backStackEntry.arguments?.getString("centroId") ?: ""
            val claseId = backStackEntry.arguments?.getString("claseId") ?: ""

            AddClaseScreen(
                viewModel = hiltViewModel(),
                claseId = claseId,
                centroId = centroId,
                onNavigateBack = { navController.popBackStack() },
                onClaseAdded = { navController.popBackStack() }
            )
        }

        // Pantalla para funcionalidades en desarrollo
        composable(
            route = AppScreens.Dummy.route,
            arguments = listOf(
                navArgument("title") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: "Funcionalidad en Desarrollo"
            
            DummyScreen(
                title = title,
                onNavigateBack = { navController.popBackStack() },
                onMenuClick = {}
            )
        }

        // Pantalla de perfil
        composable(
            route = AppScreens.Perfil.route
        ) {
            PerfilScreen(
                title = "Mi Perfil",
                onNavigateBack = { navController.popBackStack() },
                onMenuClick = {}
            )
        }

        // Pantalla de calendario
        composable(route = AppScreens.Calendario.route) {
            CalendarioScreen(
                navController = navController
            )
        }

        // Pantalla de gestión de cursos (admin)
        composable(
            route = AppScreens.Cursos.route
        ) {
            CursosScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        // Pantalla de gestión de clases (admin)
        composable(
            route = AppScreens.Clases.route
        ) {
            ClasesScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla de gestión de profesores (admin)
        composable(
            route = AppScreens.ProfesorList.route
        ) {
            ProfesorListScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla de gestión de alumnos (admin)
        composable(
            route = AppScreens.AlumnoList.route
        ) {
            AlumnoListScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        // Pantalla de gestión de familiares (admin)
        composable(
            route = AppScreens.FamiliarList.route
        ) {
            FamiliarListScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        // Pantalla de detalle de usuario
        composable(
            route = AppScreens.UserDetail.route,
            arguments = listOf(
                navArgument("dni") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val dni = backStackEntry.arguments?.getString("dni") ?: ""
            
            UserDetailScreen(
                navController = navController,
                dni = dni
            )
        }

        // Estadísticas
        composable(route = AppScreens.Estadisticas.route) {
            EstadisticasScreen(navController = navController)
        }
        
        // Notificaciones
        composable(route = AppScreens.Notificaciones.route) {
            NotificacionesScreen(navController = navController)
        }

        // Pantalla de chat
        composable(
            route = AppScreens.Chat.route,
            arguments = listOf(
                navArgument("familiarId") { type = NavType.StringType },
                navArgument("alumnoId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val familiarId = entry.arguments?.getString("familiarId") ?: ""
            val alumnoId = entry.arguments?.getString("alumnoId")
            
            com.tfg.umeegunero.feature.profesor.screen.ChatScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Pantalla de recuperación de contraseña
        composable(route = AppScreens.RecuperarPassword.route) {
            RecuperarPasswordScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    context: Context = LocalContext.current,
    startDestination: String = AppScreens.Login.route
) {
    // ... existing code ...
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ... existing code ...
        
        // Estadísticas
        composable(route = AppScreens.Estadisticas.route) {
            EstadisticasScreen(navController = navController)
        }
        
        // Notificaciones
        composable(route = AppScreens.Notificaciones.route) {
            NotificacionesScreen(navController = navController)
        }
        
        // ... existing code ...
    }
}