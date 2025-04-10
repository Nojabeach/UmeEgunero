package com.tfg.umeegunero.feature.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.tfg.umeegunero.MainActivity
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.auth.screen.LoginScreen
import com.tfg.umeegunero.feature.auth.viewmodel.LoginViewModel
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeScreen
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeUserType
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test de navegación completa para probar el flujo desde la pantalla de bienvenida,
 * pasando por las pantallas de login hasta los dashboards de cada perfil de usuario.
 * 
 * Este test verifica:
 * 1. Navegación desde Welcome a Login de cada perfil
 * 2. Proceso completo de login (con simulación)
 * 3. Redirección al dashboard correspondiente tras el login exitoso
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest
class DashboardNavigationTest {
    
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    private lateinit var navController: TestNavHostController
    
    // Mock del ViewModel de login para simular autenticación
    private val mockLoginViewModel: LoginViewModel = mockk(relaxed = true)
    
    @Before
    fun setup() {
        hiltRule.inject()
        
        // Configurar mock para Login exitoso
        every { mockLoginViewModel.loginState } returns androidx.compose.runtime.mutableStateOf(
            com.tfg.umeegunero.feature.auth.viewmodel.LoginState(
                isLoading = false,
                isSuccess = false,
                errorMessage = null
            )
        )
        
        // Configuración del NavController de prueba
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            
            UmeEguneroTheme {
                NavHost(
                    navController = navController,
                    startDestination = AppScreens.Welcome.route
                ) {
                    composable(AppScreens.Welcome.route) {
                        WelcomeScreen(
                            onNavigateToLogin = { userType ->
                                when (userType) {
                                    WelcomeUserType.ADMIN -> navController.navigate("admin_login")
                                    WelcomeUserType.CENTRO -> navController.navigate(AppScreens.Login.createRoute("CENTRO"))
                                    WelcomeUserType.PROFESOR -> navController.navigate(AppScreens.Login.createRoute("PROFESOR"))
                                    WelcomeUserType.FAMILIAR -> navController.navigate(AppScreens.Login.createRoute("FAMILIAR"))
                                }
                            },
                            onCloseApp = { /* No hacer nada en los tests */ },
                            onNavigateToTechnicalSupport = {
                                navController.navigate(AppScreens.SoporteTecnico.route)
                            },
                            onNavigateToFAQ = {
                                navController.navigate(AppScreens.FAQ.route)
                            }
                        )
                    }
                    
                    // Pantalla de login Admin (especial)
                    composable("admin_login") {
                        LoginSimuladaScreen(
                            userType = TipoUsuario.ADMIN_APP,
                            viewModel = mockLoginViewModel,
                            onLoginSuccess = {
                                navController.navigate(AppScreens.AdminDashboard.route) {
                                    popUpTo(AppScreens.Welcome.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    // Pantalla de login estándar
                    composable(
                        route = AppScreens.Login.route,
                        arguments = listOf(
                            androidx.navigation.NavArgument.Builder()
                                .setType(androidx.navigation.NavType.StringType)
                                .setName("userType")
                                .build()
                        )
                    ) { backStackEntry ->
                        val userTypeStr = backStackEntry.arguments?.getString("userType") ?: "FAMILIAR"
                        val userType = when(userTypeStr) {
                            "ADMIN" -> TipoUsuario.ADMIN_APP
                            "CENTRO" -> TipoUsuario.ADMIN_CENTRO
                            "PROFESOR" -> TipoUsuario.PROFESOR
                            else -> TipoUsuario.FAMILIAR
                        }
                        
                        LoginSimuladaScreen(
                            userType = userType,
                            viewModel = mockLoginViewModel,
                            onLoginSuccess = {
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
                    
                    // Dashboards simulados
                    composable(AppScreens.AdminDashboard.route) {
                        DashboardSimulado(titulo = "Dashboard Administrador")
                    }
                    
                    composable(AppScreens.CentroDashboard.route) {
                        DashboardSimulado(titulo = "Dashboard Centro")
                    }
                    
                    composable(AppScreens.ProfesorDashboard.route) {
                        DashboardSimulado(titulo = "Dashboard Profesor")
                    }
                    
                    composable(AppScreens.FamiliarDashboard.route) {
                        DashboardSimulado(titulo = "Dashboard Familiar")
                    }
                    
                    // Rutas de soporte
                    composable(AppScreens.SoporteTecnico.route) {
                        androidx.compose.material3.Text("Soporte Técnico")
                    }
                    
                    composable(AppScreens.FAQ.route) {
                        androidx.compose.material3.Text("Preguntas Frecuentes")
                    }
                }
            }
        }
    }
    
    /**
     * Test flujo completo: Welcome → Login Admin → Dashboard Admin
     */
    @Test
    fun flujoCompleto_Admin_HastaDashboard() {
        // 1. Verificamos que estamos en la pantalla de bienvenida
        composeTestRule.onNodeWithText("UmeEgunero", useUnmergedTree = true)
            .assertExists("No se encontró el logo de la aplicación")
        
        // 2. Navegamos al login de admin
        composeTestRule.onNodeWithText("Acceso Administrador", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        
        // 3. Simulamos un login exitoso al seleccionar "Acceder"
        // Para este momento deberíamos estar en la pantalla de login
        composeTestRule.onNodeWithText("Email", useUnmergedTree = true).performTextInput("admin@eguneroko.com")
        composeTestRule.onNodeWithText("Contraseña", useUnmergedTree = true).performTextInput("password")
        
        // Configuramos el mock para simular login exitoso antes de hacer clic
        every { mockLoginViewModel.loginState } returns androidx.compose.runtime.mutableStateOf(
            com.tfg.umeegunero.feature.auth.viewmodel.LoginState(
                isLoading = false,
                isSuccess = true,
                errorMessage = null
            )
        )
        
        // Hacemos clic en acceder
        composeTestRule.onNodeWithText("Acceder", useUnmergedTree = true).performClick()
        
        // 4. Verificamos que se ha navegado al dashboard correcto
        composeTestRule.onNodeWithText("Dashboard Administrador", useUnmergedTree = true)
            .assertExists("No se navegó correctamente al dashboard de administrador")
    }
    
    /**
     * Test flujo completo: Welcome → Login Centro → Dashboard Centro
     */
    @Test
    fun flujoCompleto_Centro_HastaDashboard() {
        // Navegamos al login de centro
        composeTestRule.onNodeWithText("Acceso Centro", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        
        // Simulamos login
        composeTestRule.onNodeWithText("Email", useUnmergedTree = true).performTextInput("bmerana@eguneroko.com")
        composeTestRule.onNodeWithText("Contraseña", useUnmergedTree = true).performTextInput("password")
        
        // Configuramos el mock para simular login exitoso
        every { mockLoginViewModel.loginState } returns androidx.compose.runtime.mutableStateOf(
            com.tfg.umeegunero.feature.auth.viewmodel.LoginState(
                isLoading = false,
                isSuccess = true,
                errorMessage = null
            )
        )
        
        // Accedemos
        composeTestRule.onNodeWithText("Acceder", useUnmergedTree = true).performClick()
        
        // Verificamos navegación al dashboard
        composeTestRule.onNodeWithText("Dashboard Centro", useUnmergedTree = true)
            .assertExists("No se navegó correctamente al dashboard de centro")
    }
    
    /**
     * Test flujo completo: Welcome → Login Profesor → Dashboard Profesor
     */
    @Test
    fun flujoCompleto_Profesor_HastaDashboard() {
        // Navegamos al login de profesor
        composeTestRule.onNodeWithText("Acceso Profesor", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        
        // Simulamos login
        composeTestRule.onNodeWithText("Email", useUnmergedTree = true).performTextInput("profesor@eguneroko.com")
        composeTestRule.onNodeWithText("Contraseña", useUnmergedTree = true).performTextInput("password")
        
        // Configuramos el mock para simular login exitoso
        every { mockLoginViewModel.loginState } returns androidx.compose.runtime.mutableStateOf(
            com.tfg.umeegunero.feature.auth.viewmodel.LoginState(
                isLoading = false,
                isSuccess = true,
                errorMessage = null
            )
        )
        
        // Accedemos
        composeTestRule.onNodeWithText("Acceder", useUnmergedTree = true).performClick()
        
        // Verificamos navegación al dashboard
        composeTestRule.onNodeWithText("Dashboard Profesor", useUnmergedTree = true)
            .assertExists("No se navegó correctamente al dashboard de profesor")
    }
    
    /**
     * Test flujo completo: Welcome → Login Familiar → Dashboard Familiar
     */
    @Test
    fun flujoCompleto_Familiar_HastaDashboard() {
        // Navegamos al login de familiar
        composeTestRule.onNodeWithText("Acceso Familiar", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        
        // Simulamos login
        composeTestRule.onNodeWithText("Email", useUnmergedTree = true).performTextInput("familiar@eguneroko.com")
        composeTestRule.onNodeWithText("Contraseña", useUnmergedTree = true).performTextInput("password")
        
        // Configuramos el mock para simular login exitoso
        every { mockLoginViewModel.loginState } returns androidx.compose.runtime.mutableStateOf(
            com.tfg.umeegunero.feature.auth.viewmodel.LoginState(
                isLoading = false,
                isSuccess = true,
                errorMessage = null
            )
        )
        
        // Accedemos
        composeTestRule.onNodeWithText("Acceder", useUnmergedTree = true).performClick()
        
        // Verificamos navegación al dashboard
        composeTestRule.onNodeWithText("Dashboard Familiar", useUnmergedTree = true)
            .assertExists("No se navegó correctamente al dashboard de familiar")
    }
}

/**
 * Pantalla de login simulada para pruebas
 */
@androidx.compose.runtime.Composable
fun LoginSimuladaScreen(
    userType: TipoUsuario,
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val loginState = viewModel.loginState.value
    
    // Si el login es exitoso, navegar
    androidx.compose.runtime.LaunchedEffect(loginState.isSuccess) {
        if (loginState.isSuccess) {
            onLoginSuccess()
        }
    }
    
    // UI simulada del login
    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { 
                    androidx.compose.material3.Text("Login ${userType.name}") 
                },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = {}) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            // Campos de entrada
            androidx.compose.material3.OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { androidx.compose.material3.Text("Email") },
                modifier = androidx.compose.ui.Modifier.fillMaxWidth()
            )
            
            androidx.compose.material3.OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { androidx.compose.material3.Text("Contraseña") },
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
            )
            
            // Botón de acceso
            androidx.compose.material3.Button(
                onClick = { 
                    viewModel.login("test@example.com", "password")
                },
                modifier = androidx.compose.ui.Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Text("Acceder")
            }
        }
    }
}

/**
 * Dashboard simulado para pruebas
 */
@androidx.compose.runtime.Composable
fun DashboardSimulado(titulo: String) {
    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { androidx.compose.material3.Text(titulo) }
            )
        }
    ) { paddingValues ->
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Text(
                    text = titulo,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                )
                
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                
                androidx.compose.material3.Text(
                    text = "Login exitoso",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
} 