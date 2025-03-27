package com.tfg.umeegunero.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import com.tfg.umeegunero.data.model.UserType
import com.tfg.umeegunero.feature.auth.screen.LoginScreen
import com.tfg.umeegunero.feature.auth.screen.RegistroScreen
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeScreen
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeUserType
import com.tfg.umeegunero.feature.common.support.screen.TechnicalSupportScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.feature.common.support.screen.FAQScreen
import com.tfg.umeegunero.feature.admin.screen.AdminDashboardScreen
import com.tfg.umeegunero.feature.centro.screen.CentroDashboardScreen
import com.tfg.umeegunero.feature.profesor.screen.ProfesorDashboardScreen
import com.tfg.umeegunero.feature.familiar.screen.FamiliarDashboardScreen
import com.tfg.umeegunero.feature.common.screen.DummyScreen
import com.tfg.umeegunero.feature.common.config.screen.ConfiguracionScreen
import com.tfg.umeegunero.feature.admin.screen.EstadisticasScreen
import com.tfg.umeegunero.feature.admin.screen.AdminNotificacionesScreen
import com.tfg.umeegunero.feature.common.perfil.screen.PerfilScreen
import com.tfg.umeegunero.feature.admin.screen.EmailConfigScreen

/**
 * Navegación principal de la aplicación
 * Esta implementación es una versión extendida de SimpleNavigation
 */
@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    onCloseApp: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerContent(
                    navController = navController,
                    onCloseDrawer = { 
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        }
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
                        navController.navigate(AppScreens.Login.createRoute(userTypeRoute))
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

            // Dashboards
            composable(route = AppScreens.AdminDashboard.route) {
                AdminDashboardScreen(
                    navController = navController,
                    viewModel = hiltViewModel()
                )
            }

            // Rutas anidadas del AdminDashboard
            composable(route = AppScreens.Cursos.route) {
                DummyScreen(
                    title = "Gestión de Cursos",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = AppScreens.Clases.route) {
                DummyScreen(
                    title = "Gestión de Clases",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = AppScreens.ProfesorList.route) {
                DummyScreen(
                    title = "Listado de Profesores",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = AppScreens.AlumnoList.route) {
                DummyScreen(
                    title = "Listado de Alumnos",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = AppScreens.FamiliarList.route) {
                DummyScreen(
                    title = "Listado de Familiares",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Otros dashboards
            composable(route = AppScreens.CentroDashboard.route) {
                CentroDashboardScreen(
                    navController = navController,
                    viewModel = hiltViewModel()
                )
            }

            composable(route = AppScreens.ProfesorDashboard.route) {
                ProfesorDashboardScreen(
                    navController = navController,
                    onLogout = {
                        navController.navigate(AppScreens.Welcome.route) {
                            popUpTo(AppScreens.ProfesorDashboard.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(route = AppScreens.FamiliarDashboard.route) {
                FamiliarDashboardScreen(
                    viewModel = hiltViewModel(),
                    onLogout = {
                        navController.navigate(AppScreens.Welcome.route) {
                            popUpTo(AppScreens.FamiliarDashboard.route) { inclusive = true }
                        }
                    }
                )
            }

            // Pantallas de gestión
            composable(route = AppScreens.GestionCentros.route) {
                DummyScreen(
                    title = "Gestión de Centros",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = AppScreens.GestionAcademica.route) {
                DummyScreen(
                    title = "Gestión Académica",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = AppScreens.Configuracion.route) {
                ConfiguracionScreen(
                    viewModel = hiltViewModel(),
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = AppScreens.Calendario.route) {
                DummyScreen(
                    title = "Calendario",
                    onNavigateBack = { navController.popBackStack() }
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

            composable(route = AppScreens.Perfil.route) {
                PerfilScreen(
                    title = "Mi Perfil",
                    onNavigateBack = { navController.popBackStack() },
                    onMenuClick = {}
                )
            }

            composable(route = AppScreens.EmailConfig.route) {
                EmailConfigScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Pantallas de detalle
            composable(
                route = AppScreens.DetalleCentro.route,
                arguments = listOf(
                    navArgument("centroId") { type = NavType.StringType }
                )
            ) {
                DummyScreen(
                    title = "Detalle del Centro",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = AppScreens.DetalleUsuario.route,
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType }
                )
            ) {
                DummyScreen(
                    title = "Detalle del Usuario",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = AppScreens.DetalleAlumno.route,
                arguments = listOf(
                    navArgument("alumnoId") { type = NavType.StringType }
                )
            ) {
                DummyScreen(
                    title = "Detalle del Alumno",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = AppScreens.DetalleFamiliar.route,
                arguments = listOf(
                    navArgument("familiarId") { type = NavType.StringType },
                    navArgument("alumnoId") { type = NavType.StringType }
                )
            ) {
                DummyScreen(
                    title = "Detalle del Familiar",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Pantalla dummy para rutas no implementadas
            composable(
                route = AppScreens.Dummy.route,
                arguments = listOf(
                    navArgument("title") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title") ?: "Funcionalidad en Desarrollo"
                DummyScreen(
                    title = title,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
} 