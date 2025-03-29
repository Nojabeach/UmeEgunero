package com.tfg.umeegunero.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
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
import com.tfg.umeegunero.feature.admin.screen.ListCentrosScreen
import com.tfg.umeegunero.feature.admin.screen.DetalleCentroScreen
import com.tfg.umeegunero.feature.admin.screen.EditCentroScreen
import com.tfg.umeegunero.feature.admin.screen.AddCentroScreen
import com.tfg.umeegunero.feature.common.users.screen.ListProfesoresScreen
import com.tfg.umeegunero.feature.common.users.screen.ListAlumnosScreen
import com.tfg.umeegunero.feature.common.users.screen.ListFamiliaresScreen
import com.tfg.umeegunero.feature.common.users.screen.ListAdministradoresScreen
import com.tfg.umeegunero.feature.common.users.screen.UserDetailScreen
import com.tfg.umeegunero.feature.admin.screen.ComunicadosScreen
import com.tfg.umeegunero.feature.admin.screen.ReporteUsoScreen
import com.tfg.umeegunero.feature.admin.screen.ReporteRendimientoScreen
import com.tfg.umeegunero.feature.centro.screen.AddAlumnoScreen
import com.tfg.umeegunero.feature.centro.screen.VinculacionFamiliarScreen
import com.tfg.umeegunero.feature.centro.screen.GestionCursosYClasesScreen
import com.tfg.umeegunero.feature.centro.screen.GestionNotificacionesCentroScreen
import com.tfg.umeegunero.feature.profesor.screen.AsistenciaScreen
import com.tfg.umeegunero.feature.profesor.screen.TareasScreen
import com.tfg.umeegunero.feature.profesor.screen.CalendarioScreen
import com.tfg.umeegunero.feature.familiar.screen.FamiliaDashboardScreen
import com.tfg.umeegunero.feature.familiar.screen.DetalleAlumnoFamiliaScreen
import com.tfg.umeegunero.feature.familiar.screen.CalendarioFamiliaScreen
import com.tfg.umeegunero.feature.familiar.screen.TareasFamiliaScreen
import com.tfg.umeegunero.feature.familiar.screen.ChatFamiliaScreen
import com.tfg.umeegunero.feature.familiar.screen.NotificacionesFamiliaScreen
import com.tfg.umeegunero.feature.profesor.registros.screen.HiltRegistroDiarioScreen
import com.tfg.umeegunero.feature.familiar.registros.screen.HiltConsultaRegistroDiarioScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import com.tfg.umeegunero.feature.centro.screen.GestionProfesoresScreen
import com.tfg.umeegunero.feature.common.users.screen.AddUserScreen
import com.tfg.umeegunero.feature.common.users.viewmodel.AddUserViewModel
import com.tfg.umeegunero.data.model.TipoUsuario

/**
 * Componente principal de navegación de la aplicación UmeEgunero.
 * 
 * Este componente implementa toda la estructura de navegación de la aplicación utilizando
 * Navigation Compose. Se encarga de definir:
 * 
 * - El grafo de navegación completo con todas las rutas posibles
 * - La configuración del cajón de navegación lateral (Navigation Drawer)
 * - Las transiciones entre pantallas
 * - La lógica de paso de parámetros entre destinos
 * 
 * La navegación está organizada por roles de usuario (administrador, centro, profesor, familiar)
 * y cada rol tiene su propio flujo de pantallas específicas con permisos adecuados.
 * 
 * @param navController Controlador de navegación que gestiona el gráfico de navegación.
 *                      Por defecto se crea uno nuevo con [rememberNavController].
 * @param onCloseApp Callback que se ejecuta cuando el usuario solicita cerrar la aplicación.
 *                   Por defecto es una función vacía.
 *
 * @see AppScreens Para la definición de todas las rutas disponibles
 * @see NavigationDrawerContent Para el contenido del cajón de navegación
 */
@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    onCloseApp: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Configuración del cajón de navegación lateral (drawer)
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
        // Definición del gráfico de navegación principal
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
                ListProfesoresScreen(
                    navController = navController
                )
            }

            composable(route = AppScreens.AlumnoList.route) {
                ListAlumnosScreen(
                    navController = navController
                )
            }

            composable(route = AppScreens.FamiliarList.route) {
                ListFamiliaresScreen(
                    navController = navController
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
                ListCentrosScreen(
                    navController = navController
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
                    navController = navController
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
            ) { backStackEntry ->
                val centroId = backStackEntry.arguments?.getString("centroId") ?: ""
                DetalleCentroScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onMenuClick = { 
                        navController.navigate(AppScreens.AdminDashboard.route) {
                            popUpTo(AppScreens.AdminDashboard.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                    onEditCentro = { centroId ->
                        navController.navigate(AppScreens.EditCentro.createRoute(centroId))
                    }
                )
            }

            // Pantalla para editar centro
            composable(
                route = AppScreens.EditCentro.route,
                arguments = listOf(
                    navArgument("centroId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val centroId = backStackEntry.arguments?.getString("centroId") ?: ""
                EditCentroScreen(
                    navController = navController,
                    centroId = centroId
                )
            }

            // Ruta para añadir un nuevo centro
            composable(route = AppScreens.AddCentro.route) {
                AddCentroScreen(navController = navController)
            }

            composable(
                route = AppScreens.UserDetail.route,
                arguments = listOf(
                    navArgument("dni") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val dni = backStackEntry.arguments?.getString("dni") ?: ""
                UserDetailScreen(
                    navController = navController,
                    userId = dni
                )
            }

            // Ruta para añadir un nuevo usuario
            composable(
                route = "add_user/{isAdminApp}?tipo={tipoUsuario}",
                arguments = listOf(
                    navArgument("isAdminApp") { type = NavType.BoolType },
                    navArgument("tipoUsuario") { 
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val isAdminApp = backStackEntry.arguments?.getBoolean("isAdminApp") ?: true
                val tipoUsuarioStr = backStackEntry.arguments?.getString("tipoUsuario")
                
                val tipoUsuario = tipoUsuarioStr?.let { TipoUsuario.valueOf(it) } ?: TipoUsuario.FAMILIAR
                
                val viewModel: AddUserViewModel = hiltViewModel()
                // Configurar el tipo de usuario inicial si se proporciona
                LaunchedEffect(tipoUsuario) {
                    viewModel.updateTipoUsuario(tipoUsuario)
                }
                
                val state = viewModel.uiState.collectAsState().value
                
                // Adaptamos el estado del viewmodel al formato que espera la UI
                val screenUiState = com.tfg.umeegunero.feature.common.users.screen.AddUserUiState(
                    dni = state.dni,
                    dniError = state.dniError,
                    email = state.email,
                    emailError = state.emailError,
                    password = state.password,
                    passwordError = state.passwordError,
                    confirmPassword = state.confirmPassword,
                    confirmPasswordError = state.confirmPasswordError,
                    nombre = state.nombre,
                    nombreError = state.nombreError,
                    apellidos = state.apellidos,
                    apellidosError = state.apellidosError,
                    telefono = state.telefono,
                    telefonoError = state.telefonoError,
                    tipoUsuario = state.tipoUsuario,
                    isLoading = state.isLoading,
                    error = state.error,
                    success = state.success,
                    isAdminApp = isAdminApp
                )
                
                AddUserScreen(
                    uiState = screenUiState,
                    onUpdateDni = viewModel::updateDni,
                    onUpdateEmail = viewModel::updateEmail,
                    onUpdatePassword = viewModel::updatePassword,
                    onUpdateConfirmPassword = viewModel::updateConfirmPassword,
                    onUpdateNombre = viewModel::updateNombre,
                    onUpdateApellidos = viewModel::updateApellidos,
                    onUpdateTelefono = viewModel::updateTelefono,
                    onUpdateTipoUsuario = viewModel::updateTipoUsuario,
                    onUpdateCentroSeleccionado = { centro -> centro?.id?.let { viewModel.updateCentroSeleccionado(it) } },
                    onSaveUser = viewModel::saveUser,
                    onClearError = viewModel::clearError,
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

            composable(route = AppScreens.AdminList.route) {
                ListAdministradoresScreen(
                    navController = navController
                )
            }

            composable(route = AppScreens.Comunicados.route) {
                ComunicadosScreen(
                    navController = navController
                )
            }
            
            composable(route = AppScreens.ReporteUso.route) {
                ReporteUsoScreen(
                    navController = navController
                )
            }
            
            composable(route = AppScreens.ReporteRendimiento.route) {
                ReporteRendimientoScreen(
                    navController = navController
                )
            }
            
            // Nuevas rutas para gestión de alumnos y vinculación familiar
            composable(route = AppScreens.AddAlumno.route) {
                AddAlumnoScreen(
                    navController = navController
                )
            }
            
            composable(
                route = AppScreens.EditAlumno.route,
                arguments = listOf(
                    navArgument("alumnoId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val alumnoId = backStackEntry.arguments?.getString("alumnoId") ?: ""
                // Por ahora usamos la misma pantalla para editar que para añadir
                AddAlumnoScreen(
                    navController = navController,
                    viewModel = hiltViewModel()
                )
            }
            
            composable(route = AppScreens.VinculacionFamiliar.route) {
                VinculacionFamiliarScreen(
                    navController = navController
                )
            }
            
            composable(route = AppScreens.GestionCursosYClases.route) {
                GestionCursosYClasesScreen(
                    navController = navController
                )
            }
            
            composable(route = AppScreens.GestionNotificacionesCentro.route) {
                GestionNotificacionesCentroScreen(
                    navController = navController
                )
            }

            composable(
                route = AppScreens.AsistenciaClase.route + "/{claseId}",
                arguments = listOf(
                    navArgument("claseId") { type = NavType.StringType }
                )
            ) {
                AsistenciaScreen(navController = navController)
            }

            // Rutas para el profesor
            composable(route = AppScreens.TareasProfesor.route) {
                TareasScreen(navController = navController)
            }

            // Rutas del profesor
            composable(AppScreens.ProfesorAsistencia.route) {
                AsistenciaScreen(navController = navController)
            }
            
            composable(AppScreens.ProfesorTareas.route) {
                TareasScreen(navController = navController)
            }
            
            composable(AppScreens.ProfesorCalendario.route) {
                CalendarioScreen(navController = navController)
            }

            // Rutas del profesor
            composable(route = AppScreens.AsistenciaProfesor.route) {
                // AsistenciaProfesorScreen(navController = navController)
                // Pantalla temporal mientras se implementa la definitiva
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Pantalla de asistencia en desarrollo", fontSize = 20.sp)
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Volver")
                    }
                }
            }

            composable(route = AppScreens.ChatProfesor.route) {
                // ChatProfesorScreen(navController = navController)
                // Pantalla temporal mientras se implementa la definitiva
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Pantalla de chat en desarrollo", fontSize = 20.sp)
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Volver")
                    }
                }
            }

            composable(
                route = AppScreens.DetalleAlumnoProfesor.route,
                arguments = listOf(
                    navArgument("alumnoId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val alumnoId = backStackEntry.arguments?.getString("alumnoId") ?: ""
                // DetalleAlumnoProfesorScreen(
                //     navController = navController,
                //     alumnoId = alumnoId
                // )
                // Pantalla temporal mientras se implementa la definitiva
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Pantalla de detalle de alumno ($alumnoId) en desarrollo", fontSize = 20.sp)
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Volver")
                    }
                }
            }

            // Rutas de la familia
            composable(route = AppScreens.FamiliaDashboard.route) {
                FamiliaDashboardScreen(navController = navController)
            }

            composable(
                route = AppScreens.DetalleAlumnoFamilia.route,
                arguments = listOf(
                    navArgument("alumnoId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val alumnoId = backStackEntry.arguments?.getString("alumnoId") ?: ""
                DetalleAlumnoFamiliaScreen(
                    navController = navController,
                    alumnoId = alumnoId
                )
            }

            composable(route = AppScreens.CalendarioFamilia.route) {
                CalendarioFamiliaScreen(navController = navController)
            }

            composable(
                route = AppScreens.ChatFamilia.route + "/{profesorId}",
                arguments = listOf(
                    navArgument("profesorId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val profesorId = backStackEntry.arguments?.getString("profesorId") ?: ""
                ChatFamiliaScreen(
                    navController = navController,
                    profesorId = profesorId
                )
            }

            composable(
                route = AppScreens.TareasFamilia.route + "/{alumnoId}",
                arguments = listOf(
                    navArgument("alumnoId") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val alumnoId = backStackEntry.arguments?.getString("alumnoId") ?: ""
                TareasFamiliaScreen(
                    navController = navController,
                    alumnoId = alumnoId
                )
            }

            composable(route = AppScreens.NotificacionesFamilia.route) {
                NotificacionesFamiliaScreen(navController = navController)
            }

            // Añadimos las rutas para los registros diarios
            
            // Pantalla para que el profesor registre actividades diarias
            composable(
                route = AppScreens.RegistroDiario.route,
                arguments = listOf(
                    navArgument("alumnoId") { type = NavType.StringType },
                    navArgument("claseId") { type = NavType.StringType },
                    navArgument("profesorId") { type = NavType.StringType },
                    navArgument("alumnoNombre") { type = NavType.StringType },
                    navArgument("claseNombre") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val alumnoId = backStackEntry.arguments?.getString("alumnoId") ?: ""
                val claseId = backStackEntry.arguments?.getString("claseId") ?: ""
                val profesorId = backStackEntry.arguments?.getString("profesorId") ?: ""
                val alumnoNombre = backStackEntry.arguments?.getString("alumnoNombre") ?: ""
                val claseNombre = backStackEntry.arguments?.getString("claseNombre") ?: ""
                
                HiltRegistroDiarioScreen(
                    onNavigateBack = { navController.popBackStack() },
                    alumnoId = alumnoId,
                    alumnoNombre = alumnoNombre,
                    claseId = claseId,
                    claseNombre = claseNombre,
                    profesorId = profesorId
                )
            }
            
            // Pantalla para que la familia consulte los registros diarios
            composable(
                route = AppScreens.ConsultaRegistroDiario.route,
                arguments = listOf(
                    navArgument("alumnoId") { type = NavType.StringType },
                    navArgument("alumnoNombre") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val alumnoId = backStackEntry.arguments?.getString("alumnoId") ?: ""
                val alumnoNombre = backStackEntry.arguments?.getString("alumnoNombre") ?: ""
                
                HiltConsultaRegistroDiarioScreen(
                    onNavigateBack = { navController.popBackStack() },
                    alumnoId = alumnoId,
                    alumnoNombre = alumnoNombre
                )
            }

            // Dentro del NavHost, añadir la ruta para la pantalla de gestión de profesores
            composable(route = AppScreens.GestionProfesores.route) {
                GestionProfesoresScreen(
                    navController = navController
                )
            }
        }
    }
} 