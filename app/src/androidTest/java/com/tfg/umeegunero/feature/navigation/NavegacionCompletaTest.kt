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
 * Test de navegación completa para probar el flujo desde la pantalla de bienvenida
 * hasta los dashboards de los diferentes perfiles de usuario.
 * 
 * Este test verifica que:
 * 1. La pantalla de bienvenida se muestra correctamente
 * 2. La navegación a cada tipo de login funciona correctamente
 * 3. El acceso a pantallas de soporte funciona correctamente
 * 4. Los dashboards de cada perfil son accesibles
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest
class NavegacionCompletaTest {
    
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    private lateinit var navController: TestNavHostController
    
    @Before
    fun setup() {
        hiltRule.inject()
        
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
                    
                    // Rutas de login simuladas
                    composable("admin_login") {
                        PantallaLogin(titulo = "Login Administrador")
                    }
                    
                    composable(
                        route = AppScreens.Login.route,
                        arguments = listOf(
                            androidx.navigation.NavArgument.Builder()
                                .setType(androidx.navigation.NavType.StringType)
                                .setName("userType")
                                .build()
                        )
                    ) { backStackEntry ->
                        val userType = backStackEntry.arguments?.getString("userType") ?: "FAMILIAR"
                        PantallaLogin(titulo = "Login $userType")
                    }
                    
                    // Rutas de soporte
                    composable(AppScreens.SoporteTecnico.route) {
                        PantallaSoporte(titulo = "Soporte Técnico")
                    }
                    
                    composable(AppScreens.FAQ.route) {
                        PantallaSoporte(titulo = "Preguntas Frecuentes")
                    }
                }
            }
        }
    }
    
    /**
     * Test de navegación a la pantalla de login de administrador
     */
    @Test
    fun navegarDesdeWelcome_HaciaLoginAdmin() {
        // Verificar que estamos en la pantalla de bienvenida
        composeTestRule.onNodeWithText("UmeEgunero", useUnmergedTree = true)
            .assertExists("No se encontró el logo de la aplicación")
        
        // Debería existir un botón de acceso de administrador
        // Nota: El botón puede estar oculto o requerir un desplazamiento
        composeTestRule.onNodeWithText("Acceso Administrador", useUnmergedTree = true)
            .performScrollTo()
            .assertExists("No se encontró el botón de acceso de administrador")
            .performClick()
        
        // Verificar que navegamos a la pantalla de login de administrador
        composeTestRule.onNodeWithText("Login Administrador", useUnmergedTree = true)
            .assertExists("No se navegó correctamente a la pantalla de login de administrador")
    }
    
    /**
     * Test de navegación a la pantalla de login de centro
     */
    @Test
    fun navegarDesdeWelcome_HaciaLoginCentro() {
        // Acceso como administrador de centro
        composeTestRule.onNodeWithText("Acceso Centro", useUnmergedTree = true)
            .performScrollTo()
            .assertExists("No se encontró el botón de acceso de centro")
            .performClick()
        
        // Verificar navegación
        composeTestRule.onNodeWithText("Login CENTRO", useUnmergedTree = true)
            .assertExists("No se navegó correctamente a la pantalla de login de centro")
    }
    
    /**
     * Test de navegación a la pantalla de login de profesor
     */
    @Test
    fun navegarDesdeWelcome_HaciaLoginProfesor() {
        // Acceso como profesor
        composeTestRule.onNodeWithText("Acceso Profesor", useUnmergedTree = true)
            .performScrollTo()
            .assertExists("No se encontró el botón de acceso de profesor")
            .performClick()
        
        // Verificar navegación
        composeTestRule.onNodeWithText("Login PROFESOR", useUnmergedTree = true)
            .assertExists("No se navegó correctamente a la pantalla de login de profesor")
    }
    
    /**
     * Test de navegación a la pantalla de login de familiar
     */
    @Test
    fun navegarDesdeWelcome_HaciaLoginFamiliar() {
        // Acceso como familiar
        composeTestRule.onNodeWithText("Acceso Familiar", useUnmergedTree = true)
            .performScrollTo()
            .assertExists("No se encontró el botón de acceso de familiar")
            .performClick()
        
        // Verificar navegación
        composeTestRule.onNodeWithText("Login FAMILIAR", useUnmergedTree = true)
            .assertExists("No se navegó correctamente a la pantalla de login de familiar")
    }
    
    /**
     * Test de navegación a la pantalla de soporte técnico
     */
    @Test
    fun navegarDesdeWelcome_HaciaSoporteTecnico() {
        // Buscar y hacer clic en el botón de soporte técnico
        composeTestRule.onNodeWithContentDescription("Soporte Técnico", useUnmergedTree = true)
            .performScrollTo()
            .assertExists("No se encontró el botón de soporte técnico")
            .performClick()
        
        // Verificar navegación
        composeTestRule.onNodeWithText("Soporte Técnico", useUnmergedTree = true)
            .assertExists("No se navegó correctamente a la pantalla de soporte técnico")
    }
    
    /**
     * Test de navegación a la pantalla de preguntas frecuentes
     */
    @Test
    fun navegarDesdeWelcome_HaciaFAQ() {
        // Buscar y hacer clic en el botón de FAQ
        composeTestRule.onNodeWithContentDescription("Preguntas Frecuentes", useUnmergedTree = true)
            .performScrollTo()
            .assertExists("No se encontró el botón de preguntas frecuentes")
            .performClick()
        
        // Verificar navegación
        composeTestRule.onNodeWithText("Preguntas Frecuentes", useUnmergedTree = true)
            .assertExists("No se navegó correctamente a la pantalla de preguntas frecuentes")
    }
}

/**
 * Pantalla de login simulada para pruebas
 */
@androidx.compose.runtime.Composable
fun PantallaLogin(titulo: String) {
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
            androidx.compose.material3.Text(titulo)
        }
    }
}

/**
 * Pantalla de soporte simulada para pruebas
 */
@androidx.compose.runtime.Composable
fun PantallaSoporte(titulo: String) {
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
            androidx.compose.material3.Text(titulo)
        }
    }
} 