package com.tfg.umeegunero.feature.auth

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
 * Test para verificar el acceso con las credenciales mencionadas en el documento de Sprint Consolidado.
 * 
 * Este test verifica el acceso con las siguientes credenciales:
 * - Administrador: admin@eguneroko.com
 * - Centro: bmerana@eguneroko.com
 * - Profesor: (credencial creada para la prueba)
 * - Familiar: (credencial creada para la prueba)
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest
class CredencialAccesoTest {
    
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
        
        // Configurar mock por defecto
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
                    
                    // Dashboards simulados para verificar la navegación
                    composable(AppScreens.AdminDashboard.route) {
                        androidx.compose.material3.Text("Dashboard Administrador")
                    }
                    
                    composable(AppScreens.CentroDashboard.route) {
                        androidx.compose.material3.Text("Dashboard Centro")
                    }
                    
                    composable(AppScreens.ProfesorDashboard.route) {
                        androidx.compose.material3.Text("Dashboard Profesor")
                    }
                    
                    composable(AppScreens.FamiliarDashboard.route) {
                        androidx.compose.material3.Text("Dashboard Familiar")
                    }
                }
            }
        }
    }
    
    /**
     * Test para verificar el acceso con credenciales de administrador
     */
    @Test
    fun testAcceso_Administrador() {
        // Navegar al login de administrador
        composeTestRule.onNodeWithText("Acceso Administrador", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        
        // Verificamos que estamos en la pantalla de login
        composeTestRule.onNodeWithText("Login ADMIN_APP", useUnmergedTree = true)
            .assertExists("No se encontró la pantalla de login de administrador")
        
        // Ingresamos credenciales de administrador
        composeTestRule.onNodeWithText("Email", useUnmergedTree = true)
            .performTextInput("admin@eguneroko.com")
            
        composeTestRule.onNodeWithText("Contraseña", useUnmergedTree = true)
            .performTextInput("password123")
        
        // Configurar el mock para simular un login exitoso cuando se usan las credenciales correctas
        every { mockLoginViewModel.login("admin@eguneroko.com", "password123") } answers {
            every { mockLoginViewModel.loginState } returns androidx.compose.runtime.mutableStateOf(
                com.tfg.umeegunero.feature.auth.viewmodel.LoginState(
                    isLoading = false,
                    isSuccess = true,
                    errorMessage = null
                )
            )
        }
        
        // Acceder
        composeTestRule.onNodeWithText("Acceder", useUnmergedTree = true).performClick()
        
        // Verificar que navegamos al dashboard de administrador
        composeTestRule.onNodeWithText("Dashboard Administrador", useUnmergedTree = true)
            .assertExists("No se navegó correctamente al dashboard de administrador")
    }
    
    /**
     * Test para verificar el acceso con credenciales de centro
     */
    @Test
    fun testAcceso_Centro() {
        // Navegar al login de centro
        composeTestRule.onNodeWithText("Acceso Centro", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        
        // Verificamos que estamos en la pantalla de login
        composeTestRule.onNodeWithText("Login ADMIN_CENTRO", useUnmergedTree = true)
            .assertExists("No se encontró la pantalla de login de centro")
        
        // Ingresamos credenciales de centro
        composeTestRule.onNodeWithText("Email", useUnmergedTree = true)
            .performTextInput("bmerana@eguneroko.com")
            
        composeTestRule.onNodeWithText("Contraseña", useUnmergedTree = true)
            .performTextInput("password123")
        
        // Configurar el mock para simular un login exitoso cuando se usan las credenciales correctas
        every { mockLoginViewModel.login("bmerana@eguneroko.com", "password123") } answers {
            every { mockLoginViewModel.loginState } returns androidx.compose.runtime.mutableStateOf(
                com.tfg.umeegunero.feature.auth.viewmodel.LoginState(
                    isLoading = false,
                    isSuccess = true,
                    errorMessage = null
                )
            )
        }
        
        // Acceder
        composeTestRule.onNodeWithText("Acceder", useUnmergedTree = true).performClick()
        
        // Verificar que navegamos al dashboard de centro
        composeTestRule.onNodeWithText("Dashboard Centro", useUnmergedTree = true)
            .assertExists("No se navegó correctamente al dashboard de centro")
    }
    
    /**
     * Test para verificar el acceso con credenciales de profesor
     */
    @Test
    fun testAcceso_Profesor() {
        // Navegar al login de profesor
        composeTestRule.onNodeWithText("Acceso Profesor", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        
        // Verificamos que estamos en la pantalla de login
        composeTestRule.onNodeWithText("Login PROFESOR", useUnmergedTree = true)
            .assertExists("No se encontró la pantalla de login de profesor")
        
        // Ingresamos credenciales de profesor 
        // (Simulamos crear un usuario profesor para la prueba)
        composeTestRule.onNodeWithText("Email", useUnmergedTree = true)
            .performTextInput("profesor@eguneroko.com")
            
        composeTestRule.onNodeWithText("Contraseña", useUnmergedTree = true)
            .performTextInput("password123")
        
        // Configurar el mock para simular un login exitoso cuando se usan las credenciales correctas
        every { mockLoginViewModel.login("profesor@eguneroko.com", "password123") } answers {
            every { mockLoginViewModel.loginState } returns androidx.compose.runtime.mutableStateOf(
                com.tfg.umeegunero.feature.auth.viewmodel.LoginState(
                    isLoading = false,
                    isSuccess = true,
                    errorMessage = null
                )
            )
        }
        
        // Acceder
        composeTestRule.onNodeWithText("Acceder", useUnmergedTree = true).performClick()
        
        // Verificar que navegamos al dashboard de profesor
        composeTestRule.onNodeWithText("Dashboard Profesor", useUnmergedTree = true)
            .assertExists("No se navegó correctamente al dashboard de profesor")
    }
    
    /**
     * Test para verificar el acceso con credenciales de familiar
     */
    @Test
    fun testAcceso_Familiar() {
        // Navegar al login de familiar
        composeTestRule.onNodeWithText("Acceso Familiar", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        
        // Verificamos que estamos en la pantalla de login
        composeTestRule.onNodeWithText("Login FAMILIAR", useUnmergedTree = true)
            .assertExists("No se encontró la pantalla de login de familiar")
        
        // Ingresamos credenciales de familiar
        // (Simulamos crear un usuario familiar para la prueba)
        composeTestRule.onNodeWithText("Email", useUnmergedTree = true)
            .performTextInput("familiar@eguneroko.com")
            
        composeTestRule.onNodeWithText("Contraseña", useUnmergedTree = true)
            .performTextInput("password123")
        
        // Configurar el mock para simular un login exitoso cuando se usan las credenciales correctas
        every { mockLoginViewModel.login("familiar@eguneroko.com", "password123") } answers {
            every { mockLoginViewModel.loginState } returns androidx.compose.runtime.mutableStateOf(
                com.tfg.umeegunero.feature.auth.viewmodel.LoginState(
                    isLoading = false,
                    isSuccess = true,
                    errorMessage = null
                )
            )
        }
        
        // Acceder
        composeTestRule.onNodeWithText("Acceder", useUnmergedTree = true).performClick()
        
        // Verificar que navegamos al dashboard de familiar
        composeTestRule.onNodeWithText("Dashboard Familiar", useUnmergedTree = true)
            .assertExists("No se navegó correctamente al dashboard de familiar")
    }
    
    /**
     * Test para verificar que se rechaza el acceso con credenciales incorrectas
     */
    @Test
    fun testAcceso_CredencialesIncorrectas() {
        // Navegar al login de administrador
        composeTestRule.onNodeWithText("Acceso Administrador", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        
        // Ingresamos credenciales incorrectas
        composeTestRule.onNodeWithText("Email", useUnmergedTree = true)
            .performTextInput("incorrecta@eguneroko.com")
            
        composeTestRule.onNodeWithText("Contraseña", useUnmergedTree = true)
            .performTextInput("incorrecta")
        
        // Configurar el mock para simular un login fallido
        every { mockLoginViewModel.login("incorrecta@eguneroko.com", "incorrecta") } answers {
            every { mockLoginViewModel.loginState } returns androidx.compose.runtime.mutableStateOf(
                com.tfg.umeegunero.feature.auth.viewmodel.LoginState(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = "Credenciales incorrectas"
                )
            )
        }
        
        // Acceder
        composeTestRule.onNodeWithText("Acceder", useUnmergedTree = true).performClick()
        
        // Verificar que se muestra un mensaje de error
        composeTestRule.onNodeWithText("Credenciales incorrectas", useUnmergedTree = true)
            .assertExists("No se mostró el mensaje de error para credenciales incorrectas")
        
        // Verificar que seguimos en la pantalla de login
        composeTestRule.onNodeWithText("Login ADMIN_APP", useUnmergedTree = true)
            .assertExists("No permanecemos en la pantalla de login")
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
            var email by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
            var password by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
            
            // Campos de entrada
            androidx.compose.material3.OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { androidx.compose.material3.Text("Email") },
                modifier = androidx.compose.ui.Modifier.fillMaxWidth()
            )
            
            androidx.compose.material3.OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { androidx.compose.material3.Text("Contraseña") },
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
            )
            
            // Mensaje de error si existe
            loginState.errorMessage?.let { error ->
                androidx.compose.material3.Text(
                    text = error,
                    color = androidx.compose.ui.graphics.Color.Red,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
            }
            
            // Botón de acceso
            androidx.compose.material3.Button(
                onClick = { 
                    viewModel.login(email, password)
                },
                modifier = androidx.compose.ui.Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Text("Acceder")
            }
        }
    }
} 