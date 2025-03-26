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
import com.tfg.umeegunero.feature.admin.screen.DetalleCentroScreen
import com.tfg.umeegunero.feature.admin.screen.EditCentroScreen
import com.tfg.umeegunero.feature.common.academico.screen.ListFamiliarScreen
import com.tfg.umeegunero.feature.admin.screen.UserDetailScreen
import com.tfg.umeegunero.feature.auth.screen.LoginScreen
import com.tfg.umeegunero.feature.auth.screen.RegistroScreen
import com.tfg.umeegunero.feature.centro.screen.CentroDashboardScreen
import com.tfg.umeegunero.feature.common.config.screen.ConfiguracionScreen
import com.tfg.umeegunero.feature.common.config.screen.PerfilConfiguracion
import com.tfg.umeegunero.feature.common.config.viewmodel.ConfiguracionViewModel
import com.tfg.umeegunero.feature.common.perfil.screen.PerfilScreen
import com.tfg.umeegunero.feature.common.screen.DummyScreen
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeScreen
import com.tfg.umeegunero.feature.familiar.screen.FamiliarDashboardScreen
import com.tfg.umeegunero.feature.familiar.screen.HiltFamiliarDashboardScreen
import com.tfg.umeegunero.feature.profesor.screen.ProfesorDashboardScreen
import com.tfg.umeegunero.feature.profesor.screen.StudentDetailScreen
import com.tfg.umeegunero.feature.profesor.screen.ChatScreen
import com.tfg.umeegunero.feature.profesor.viewmodel.ProfesorDashboardViewModel
import com.tfg.umeegunero.feature.familiar.viewmodel.FamiliarDashboardViewModel
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeUserType
import com.tfg.umeegunero.feature.common.support.screen.TechnicalSupportScreen
import com.tfg.umeegunero.feature.admin.screen.EmailConfigScreen
import com.tfg.umeegunero.feature.common.academico.screen.GestionCursosScreen
import com.tfg.umeegunero.feature.common.academico.screen.GestionClasesScreen
import com.tfg.umeegunero.feature.common.academico.screen.CalendarioScreen
import com.tfg.umeegunero.feature.common.stats.screen.EstadisticasScreen
import com.tfg.umeegunero.feature.admin.screen.AdminNotificacionesScreen
import com.tfg.umeegunero.feature.common.academico.screen.EditClaseScreen
import com.tfg.umeegunero.feature.common.academico.screen.ListAlumnoScreen
import com.tfg.umeegunero.feature.common.academico.screen.ListProfesorScreen
import com.tfg.umeegunero.feature.common.academico.screen.AddCursosScreen
import com.tfg.umeegunero.feature.common.academico.screen.AddClasesScreen
import com.tfg.umeegunero.feature.common.academico.screen.HiltListAlumnoScreen
import com.tfg.umeegunero.feature.common.academico.screen.HiltListProfesorScreen
import com.tfg.umeegunero.feature.common.academico.viewmodel.AddCursosViewModel
import com.tfg.umeegunero.feature.common.academico.viewmodel.AddClasesViewModel
import com.tfg.umeegunero.feature.common.academico.screen.EditCursoScreen
import com.tfg.umeegunero.feature.common.academico.viewmodel.EditCursoViewModel

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
                onNavigateToTechnicalSupport = {
                    navController.navigate(AppScreens.SoporteTecnico.route)
                }
            )
        }

        // Pantalla de soporte técnico
        composable(route = AppScreens.SoporteTecnico.route) {
            TechnicalSupportScreen(
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

        // Pantallas de gestión académica
        composable(
            route = AppScreens.GestionCursos.route,
            arguments = listOf(
                navArgument("centroId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val centroId = backStackEntry.arguments?.getString("centroId") ?: ""
            GestionCursosScreen(
                navController = navController,
                centroId = centroId
            )
        }

        composable(
            route = AppScreens.AddCurso.route,
            arguments = listOf(
                navArgument("centroId") { type = NavType.StringType },
                navArgument("cursoId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val centroId = backStackEntry.arguments?.getString("centroId") ?: ""
            val cursoId = backStackEntry.arguments?.getString("cursoId")
            if (cursoId != null) {
                EditCursoScreen(navController = navController)
            } else {
                val viewModel: AddCursosViewModel = hiltViewModel()
                AddCursosScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onCursoAdded = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = AppScreens.GestionClases.route,
            arguments = listOf(
                navArgument("cursoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val cursoId = backStackEntry.arguments?.getString("cursoId") ?: ""
            GestionClasesScreen(
                navController = navController,
                cursoId = cursoId
            )
        }

        composable(
            route = AppScreens.AddClase.route,
            arguments = listOf(
                navArgument("cursoId") { type = NavType.StringType },
                navArgument("claseId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val cursoId = backStackEntry.arguments?.getString("cursoId") ?: ""
            val claseId = backStackEntry.arguments?.getString("claseId")
            val viewModel: AddClasesViewModel = hiltViewModel()
            claseId?.let { viewModel.setClaseId(it) }
            AddClasesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onClaseAdded = { navController.popBackStack() }
            )
        }

        // Pantallas de gestión de usuarios
        composable(AppScreens.AlumnoList.route) {
            HiltListAlumnoScreen(
                onNavigateToAddAlumno = {
                    navController.navigate(AppScreens.AddUser.createRoute(false, TipoUsuario.ALUMNO.toString()))
                },
                onNavigateToEditAlumno = { dni ->
                    navController.navigate(AppScreens.EditUser.createRoute(dni))
                }
            )
        }

        composable(AppScreens.ProfesorList.route) {
            HiltListProfesorScreen(
                onNavigateToAddProfesor = {
                    navController.navigate(AppScreens.AddUser.createRoute(false, TipoUsuario.PROFESOR.toString()))
                },
                onNavigateToEditProfesor = { dni ->
                    navController.navigate(AppScreens.EditUser.createRoute(dni))
                }
            )
        }

        composable(
            route = AppScreens.EditUser.route,
            arguments = listOf(
                navArgument("dni") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val dni = backStackEntry.arguments?.getString("dni") ?: ""
            UserDetailScreen(
                navController = navController,
                dni = dni
            )
        }

        composable(AppScreens.FamiliarList.route) {
            ListFamiliarScreen(
                navController = navController
            )
        }

        // Pantallas de detalle y chat
        composable(
            route = AppScreens.UserDetail.route,
            arguments = listOf(
                navArgument("dni") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val dni = backStackEntry.arguments?.getString("dni") ?: ""
            UserDetailScreen(
                navController = navController,
                dni = dni
            )
        }

        composable(
            route = AppScreens.StudentDetail.route,
            arguments = listOf(
                navArgument("alumnoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val alumnoId = backStackEntry.arguments?.getString("alumnoId") ?: ""
            StudentDetailScreen(
                navController = navController,
                alumnoId = alumnoId
            )
        }

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
            ChatScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Otras pantallas
        composable(route = AppScreens.Calendario.route) {
            CalendarioScreen(
                navController = navController
            )
        }

        composable(route = AppScreens.Estadisticas.route) {
            EstadisticasScreen(
                navController = navController
            )
        }

        composable(route = AppScreens.Notificaciones.route) {
            AdminNotificacionesScreen(
                navController = navController
            )
        }

        composable(route = AppScreens.Config.route) {
            ConfiguracionScreen(
                perfil = PerfilConfiguracion.SISTEMA,
                onNavigateBack = { navController.popBackStack() },
                onMenuClick = { navController.popBackStack() }
            )
        }

        composable(route = AppScreens.Perfil.route) {
            PerfilScreen(
                title = "Mi Perfil",
                onNavigateBack = { navController.popBackStack() },
                onMenuClick = {}
            )
        }

        composable(
            route = AppScreens.Dummy.route,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: ""
            DummyScreen(
                title = title,
                onNavigateBack = { navController.popBackStack() },
                onMenuClick = {}
            )
        }

        // Dashboard para Administrador de App
        composable(route = AppScreens.AdminDashboard.route) {
            AdminDashboardScreen(navController = navController)
        }

        // Dashboard para Administrador de Centro
        composable(route = AppScreens.CentroDashboard.route) {
            CentroDashboardScreen(navController = navController)
        }

        // Dashboard para Profesor
        composable(route = AppScreens.ProfesorDashboard.route) {
            val viewModel: ProfesorDashboardViewModel = hiltViewModel()
            ProfesorDashboardScreen(
                navController = navController,
                onLogout = {
                    navController.navigate(AppScreens.Welcome.route) {
                        popUpTo(AppScreens.ProfesorDashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // Dashboard para Familiar
        composable(route = AppScreens.FamiliarDashboard.route) {
            HiltFamiliarDashboardScreen(navController = navController)
        }
    }
}