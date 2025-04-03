package com.tfg.umeegunero.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.auth.screen.LoginScreen
import com.tfg.umeegunero.feature.auth.screen.RegistroScreen
import com.tfg.umeegunero.feature.common.support.screen.FAQScreen
import com.tfg.umeegunero.feature.common.support.screen.TechnicalSupportScreen
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeScreen
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeUserType
import kotlinx.coroutines.launch
import androidx.navigation.compose.navigation
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.feature.common.mensajeria.ChatScreen
import com.tfg.umeegunero.feature.common.mensajeria.ConversacionesScreen
import com.tfg.umeegunero.feature.profesor.screen.TareasScreen
import com.tfg.umeegunero.feature.profesor.screen.DetalleTareaScreen
import com.tfg.umeegunero.feature.familiar.screen.ActividadesPreescolarScreen
import com.tfg.umeegunero.feature.profesor.screen.ActividadesPreescolarProfesorScreen
import com.tfg.umeegunero.feature.profesor.screen.EvaluacionScreen

/**
 * Navegación principal de la aplicación
 */
@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppScreens.Welcome.route,
    onCloseApp: () -> Unit = {}
) {
    // Estado para almacenar el ID y nombre del usuario actual
    var currentUserId by remember { mutableStateOf("") }
    var currentUserName by remember { mutableStateOf("") }
    
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
                    // Usar IDs ficticios para desarrollo
                    val userId = "user123"
                    val userName = "Usuario de Prueba"
                    
                    // Guardar el ID y nombre de usuario para pasarlo a los grafos de navegación
                    currentUserId = userId
                    currentUserName = userName
                    
                    val route = when(userType) {
                        TipoUsuario.ADMIN_APP -> AppScreens.AdminDashboard.route
                        TipoUsuario.ADMIN_CENTRO -> AppScreens.CentroDashboard.route
                        TipoUsuario.PROFESOR -> "profesor_graph"
                        TipoUsuario.FAMILIAR -> "familiar_graph"
                        else -> AppScreens.Welcome.route
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
        
        // Grafo de navegación para Familiar
        familiarNavGraph(
            navController = navController,
            userId = currentUserId,
            userName = currentUserName
        )
        
        // Grafo de navegación para Profesor
        profesorNavGraph(
            navController = navController,
            userId = currentUserId,
            userName = currentUserName
        )

        // Rutas del profesor
        /*composable(AppScreens.ProfesorAsistencia.route) {
            AsistenciaScreen(navController = navController)
        }
        
        composable(AppScreens.ProfesorTareas.route) {
            TareasScreen(navController = navController)
        }
        
        composable(AppScreens.ProfesorCalendario.route) {
            CalendarioScreen(navController = navController)
        }*/
        
        // Ruta para la evaluación académica
        composable("evaluacion") {
            EvaluacionScreen(
                navController = navController,
                alumnos = emptyList() // Aquí deberías pasar la lista real de alumnos
            )
        }
    }
}

// Pantalla temporal para CentroDashboard
@Composable
fun CentroDashboardScreen(
    navController: NavController,
    viewModel: Any = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Centro Dashboard (Temporal)", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.navigate(AppScreens.Welcome.route) {
                popUpTo(AppScreens.CentroDashboard.route) { inclusive = true }
            }
        }) {
            Text("Cerrar Sesión")
        }
    }
}

// Grafo de navegación para Familiar
fun NavGraphBuilder.familiarNavGraph(
    navController: NavController,
    userId: String,
    userName: String
) {
    navigation(
        startDestination = AppScreens.FamiliarDashboard.route,
        route = "familiar_graph"
    ) {
        // Dashboard Familiar
        composable(route = AppScreens.FamiliarDashboard.route) {
            // Implementación temporal del dashboard familiar
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Familiar Dashboard (Temporal)", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    navController.navigate(AppScreens.TareasFamilia.route)
                }) {
                    Text("Ver Tareas")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    navController.navigate(AppScreens.ActividadesPreescolar.route)
                }) {
                    Text("Actividades Preescolares")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    navController.navigate(AppScreens.Welcome.route) {
                        popUpTo("familiar_graph") { inclusive = true }
                    }
                }) {
                    Text("Cerrar Sesión")
                }
            }
        }
        
        // Pantallas de tareas
        composable(route = AppScreens.TareasFamilia.route) {
            // Importar la pantalla real de tareas para familiares
            com.tfg.umeegunero.feature.familiar.screen.TareasFamiliaScreen(
                navController = navController,
                familiarId = userId
            )
        }
        
        // Actividades preescolares
        composable(route = AppScreens.ActividadesPreescolar.route) {
            // Importar ActividadesPreescolarScreen del feature/familiar/screen
            com.tfg.umeegunero.feature.familiar.screen.ActividadesPreescolarScreen(
                navController = navController,
                familiarId = userId
            )
        }
        
        // Detalle de tarea para alumno (vista de familia)
        composable(
            route = AppScreens.DetalleTareaAlumno.route,
            arguments = listOf(
                navArgument("tareaId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tareaId = backStackEntry.arguments?.getString("tareaId") ?: ""
            com.tfg.umeegunero.feature.familiar.screen.DetalleTareaAlumnoScreen(
                navController = navController,
                tareaId = tareaId
            )
        }
        
        // Pantalla de entrega de tarea
        composable(
            route = AppScreens.EntregaTarea.route,
            arguments = listOf(
                navArgument("tareaId") { type = NavType.StringType },
                navArgument("alumnoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tareaId = backStackEntry.arguments?.getString("tareaId") ?: ""
            val alumnoId = backStackEntry.arguments?.getString("alumnoId") ?: ""
            com.tfg.umeegunero.feature.familiar.screen.EntregaTareaScreen(
                navController = navController,
                tareaId = tareaId,
                alumnoId = alumnoId
            )
        }
        
        // Pantalla de chat para familiar
        composable(
            route = AppScreens.ChatFamilia.route,
            arguments = listOf(
                navArgument("conversacionId") { type = NavType.StringType },
                navArgument("participanteId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val conversacionId = backStackEntry.arguments?.getString("conversacionId") ?: ""
            val participanteId = backStackEntry.arguments?.getString("participanteId") ?: ""
            ChatScreen(
                conversacionId = conversacionId,
                participanteId = participanteId,
                esFamiliar = true,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Pantalla de conversaciones para familiares
        composable(route = AppScreens.ConversacionesFamilia.route) {
            ConversacionesScreen(
                navController = navController,
                rutaChat = AppScreens.ChatFamilia.route
            )
        }
    }
}

// Grafo de navegación para Profesor
fun NavGraphBuilder.profesorNavGraph(
    navController: NavController,
    userId: String,
    userName: String
) {
    navigation(
        startDestination = AppScreens.ProfesorDashboard.route,
        route = "profesor_graph"
    ) {
        // Dashboard Profesor
        composable(route = AppScreens.ProfesorDashboard.route) {
            // Implementación temporal del dashboard del profesor
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Profesor Dashboard (Temporal)", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    navController.navigate(AppScreens.TareasProfesor.route)
                }) {
                    Text("Gestionar Tareas")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    navController.navigate(AppScreens.ActividadesPreescolarProfesor.route)
                }) {
                    Text("Actividades Preescolares")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    navController.navigate(AppScreens.Welcome.route) {
                        popUpTo("profesor_graph") { inclusive = true }
                    }
                }) {
                    Text("Cerrar Sesión")
                }
            }
        }
        
        // Pantallas de tareas para profesor
        composable(route = AppScreens.TareasProfesor.route) {
            TareasScreen(
                navController = navController
            )
        }
        
        // Actividades preescolares para profesor
        composable(route = AppScreens.ActividadesPreescolarProfesor.route) {
            com.tfg.umeegunero.feature.profesor.screen.ActividadesPreescolarProfesorScreen(
                profesorId = userId,
                profesorNombre = userName,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // Detalle de tarea para profesor
        composable(
            route = AppScreens.DetalleTareaProfesor.route,
            arguments = listOf(
                navArgument("tareaId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tareaId = backStackEntry.arguments?.getString("tareaId") ?: ""
            DetalleTareaScreen(
                navController = navController,
                tareaId = tareaId
            )
        }
        
        // Pantalla de chat para profesor
        composable(
            route = AppScreens.ChatProfesor.route,
            arguments = listOf(
                navArgument("conversacionId") { type = NavType.StringType },
                navArgument("participanteId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val conversacionId = backStackEntry.arguments?.getString("conversacionId") ?: ""
            val participanteId = backStackEntry.arguments?.getString("participanteId") ?: ""
            ChatScreen(
                conversacionId = conversacionId,
                participanteId = participanteId,
                esFamiliar = false,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Pantalla de conversaciones para profesores
        composable(route = AppScreens.ConversacionesProfesor.route) {
            ConversacionesScreen(
                navController = navController,
                rutaChat = AppScreens.ChatProfesor.route
            )
        }
    }
}
