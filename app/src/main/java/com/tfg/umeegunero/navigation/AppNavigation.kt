package com.tfg.umeegunero.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tfg.umeegunero.data.model.UserType

import com.tfg.umeegunero.feature.admin.screen.AdminDashboardScreen
import com.tfg.umeegunero.feature.admin.screen.HiltAddCentroScreen
import com.tfg.umeegunero.feature.auth.screen.LoginScreen
import com.tfg.umeegunero.feature.auth.screen.RegistroScreen
import com.tfg.umeegunero.feature.centro.screen.CentroDashboardScreen
import com.tfg.umeegunero.feature.profesor.screen.ProfesorDashboardScreen
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeScreen
import com.tfg.umeegunero.feature.common.users.screen.AddUserScreen
import androidx.compose.runtime.collectAsState

import com.tfg.umeegunero.feature.common.users.viewmodel.AddUserViewModel
import com.tfg.umeegunero.feature.common.users.screen.AddUserUiState
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.feature.centro.screen.academico.HiltAddCursoScreen
import com.tfg.umeegunero.feature.centro.screen.academico.HiltAddClaseScreen

/**
 * Sealed class para las rutas de navegación de la app
 */
sealed class AppScreens(val route: String) {
    // Pantallas de acceso/autenticación
    object Welcome : AppScreens("welcome")
    object Login : AppScreens("login/{userType}") {
        fun createRoute(userType: String) = "login/$userType"
    }
    object Register : AppScreens("register")

    // Pantallas principales según tipo de usuario
    object AdminDashboard : AppScreens("admin_dashboard")
    object CentroDashboard : AppScreens("centro_dashboard")
    object ProfesorDashboard : AppScreens("profesor_dashboard")
    object FamiliarDashboard : AppScreens("familiar_dashboard")

    // Pantallas de administración
    object AddCentro : AppScreens("add_centro")
    object EditCentro : AppScreens("edit_centro/{centroId}") {
        fun createRoute(centroId: String) = "edit_centro/$centroId"
    }
    object AddUser : AppScreens("add_user/{isAdminApp}") {
        fun createRoute(isAdminApp: Boolean) = "add_user/$isAdminApp"
    }

    // Pantallas de gestión académica
    object AddCurso : AppScreens("add_curso/{centroId}") {
        fun createRoute(centroId: String) = "add_curso/$centroId"
    }
    object EditCurso : AppScreens("edit_curso/{centroId}/{cursoId}") {
        fun createRoute(centroId: String, cursoId: String) = "edit_curso/$centroId/$cursoId"
    }
    object AddClase : AppScreens("add_clase/{centroId}") {
        fun createRoute(centroId: String) = "add_clase/$centroId"
    }
    object EditClase : AppScreens("edit_clase/{centroId}/{claseId}") {
        fun createRoute(centroId: String, claseId: String) = "edit_clase/$centroId/$claseId"
    }

    // Otras pantallas
    object StudentDetail : AppScreens("student_detail/{studentId}") {
        fun createRoute(studentId: String) = "student_detail/$studentId"
    }
    object ReportDetail : AppScreens("report_detail/{reportId}") {
        fun createRoute(reportId: String) = "report_detail/$reportId"
    }
    object Chat : AppScreens("chat/{recipientId}") {
        fun createRoute(recipientId: String) = "chat/$recipientId"
    }
}

/**
 * Navegación principal de la aplicación
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    onCloseApp: () -> Unit = {} // Agregamos el parámetro para cerrar la app
) {
    NavHost(
        navController = navController,
        startDestination = AppScreens.Welcome.route
    ) {
        // Pantalla de bienvenida
        composable(route = AppScreens.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = { userType ->
                    val userTypeStr = when(userType) {
                        UserType.ADMIN_APP -> "ADMIN"
                        UserType.ADMIN_CENTRO-> "CENTRO"
                        UserType.PROFESOR -> "PROFESOR"
                        UserType.FAMILIAR -> "FAMILIAR"
                    }
                    navController.navigate(AppScreens.Login.createRoute(userTypeStr))
                },
                onNavigateToRegister = {
                    navController.navigate(AppScreens.Register.route)
                },
                onCloseApp = onCloseApp // Pasamos la función de cierre
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
                    // TODO: Implementar recuperación de contraseña
                }
            )
        }

        // Pantalla de registro
        composable(route = AppScreens.Register.route) {
            RegistroScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onRegistroCompletado = {
                    // Navegamos a la pantalla de inicio de sesión para familiares
                    navController.navigate(AppScreens.Login.createRoute("FAMILIAR")) {
                        popUpTo(AppScreens.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla de añadir centro (admin)
        composable(route = AppScreens.AddCentro.route) {
            HiltAddCentroScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onCentroAdded = { navController.popBackStack() }
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

            HiltAddCentroScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onCentroAdded = { navController.popBackStack() },
                centroId = centroId
            )
        }

        // Pantalla de dashboard de administrador
        composable(route = AppScreens.AdminDashboard.route) {
            AdminDashboardScreen(
                onLogout = {
                    navController.navigate(AppScreens.Welcome.route) {
                        popUpTo(AppScreens.AdminDashboard.route) { inclusive = true }
                    }
                },
                onNavigateToAddCentro = {
                    navController.navigate(AppScreens.AddCentro.route)
                },
                onNavigateToEditCentro = { centroId ->
                    navController.navigate(AppScreens.EditCentro.createRoute(centroId))
                },
                onNavigateToAddUser = {
                    navController.navigate(AppScreens.AddUser.createRoute(true))
                }
            )
        }

        // Pantalla de dashboard de centro
        composable(route = AppScreens.CentroDashboard.route) {
            CentroDashboardScreen(
                onLogout = {
                    navController.navigate(AppScreens.Welcome.route) {
                        popUpTo(AppScreens.CentroDashboard.route) { inclusive = true }
                    }
                },
                onNavigateToAddCurso = {
                    // Aquí deberíamos obtener el ID del centro del usuario actual
                    // Por ahora, usamos un ID de ejemplo
                    val centroId = "centro_actual_id"
                    navController.navigate(AppScreens.AddCurso.createRoute(centroId))
                },
                onNavigateToEditCurso = { cursoId ->
                    // Aquí deberíamos obtener el ID del centro del usuario actual
                    val centroId = "centro_actual_id"
                    navController.navigate(AppScreens.EditCurso.createRoute(centroId, cursoId))
                },
                onNavigateToAddClase = {
                    // Aquí deberíamos obtener el ID del centro del usuario actual
                    val centroId = "centro_actual_id"
                    navController.navigate(AppScreens.AddClase.createRoute(centroId))
                },
                onNavigateToEditClase = { claseId ->
                    // Aquí deberíamos obtener el ID del centro del usuario actual
                    val centroId = "centro_actual_id"
                    navController.navigate(AppScreens.EditClase.createRoute(centroId, claseId))
                },
                onNavigateToAddUser = {
                    // Pasamos false para indicar que no es el admin de la app
                    navController.navigate(AppScreens.AddUser.createRoute(false))
                }
            )
        }

        // Pantalla de dashboard de profesor
        composable(route = AppScreens.ProfesorDashboard.route) {
            ProfesorDashboardScreen(
                onLogout = {
                    navController.navigate(AppScreens.Welcome.route) {
                        popUpTo(AppScreens.ProfesorDashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla de dashboard de familiar - Integración con FamiliarNavGraph
        composable(route = AppScreens.FamiliarDashboard.route) {
            FamiliarNavGraph(
                onLogout = {
                    navController.navigate(AppScreens.Welcome.route) {
                        popUpTo(AppScreens.FamiliarDashboard.route) { inclusive = true }
                    }
                }
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

            HiltAddCursoScreen(
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

            HiltAddCursoScreen(
                centroId = centroId,
                cursoId = cursoId,
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

            HiltAddClaseScreen(
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

            HiltAddClaseScreen(
                centroId = centroId,
                claseId = claseId,
                onNavigateBack = { navController.popBackStack() },
                onClaseAdded = { navController.popBackStack() }
            )
        }
    }
}