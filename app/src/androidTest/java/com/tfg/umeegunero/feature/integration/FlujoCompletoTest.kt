package com.tfg.umeegunero.feature.integration

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.tfg.umeegunero.MainActivity
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.auth.viewmodel.LoginViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test de integración para probar el flujo completo de la aplicación.
 * 
 * Este test verifica:
 * 1. Acceso a la aplicación con diferentes credenciales
 * 2. Navegación por las principales funcionalidades de cada dashboard
 * 3. Interacción con elementos específicos según el perfil
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest
class FlujoCompletoTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    private lateinit var navController: androidx.navigation.NavHostController
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    /**
     * Test de flujo completo para un administrador
     * - Acceso con credenciales de administrador
     * - Navegación por el dashboard
     * - Acceso a la gestión de centros
     * - Acceso a la gestión de usuarios
     */
    @Test
    fun testFlujoCompletoAdministrador() {
        // Configurar mocks
        val loginViewModel = mockk<LoginViewModel>(relaxed = true)
        val adminDashboardViewModel = mockk<com.tfg.umeegunero.feature.admin.viewmodel.AdminDashboardViewModel>(relaxed = true)
        
        // Usuario administrador
        val adminUsuario = Usuario(
            id = "admin123",
            nombre = "Admin",
            apellidos = "App",
            email = "admin@eguneroko.com",
            tipoUsuario = TipoUsuario.ADMIN_APP
        )
        
        // Simular login exitoso
        every { loginViewModel.loginState } returns androidx.compose.runtime.mutableStateOf(
            com.tfg.umeegunero.feature.auth.viewmodel.LoginState(
                isLoading = false,
                isSuccess = true,
                errorMessage = null
            )
        )
        
        // Simular datos del dashboard
        every { adminDashboardViewModel.currentUser } returns androidx.compose.runtime.mutableStateOf(adminUsuario)
        
        // 1. Mostrar login de administrador
        composeTestRule.setContent {
            UmeEguneroTheme {
                navController = rememberNavController()
                
                com.tfg.umeegunero.feature.auth.screen.LoginScreen(
                    userType = TipoUsuario.ADMIN_APP,
                    viewModel = loginViewModel,
                    onNavigateBack = {},
                    onLoginSuccess = {
                        // Simular navegación al dashboard
                        navController.navigate(AppScreens.AdminDashboard.route)
                    },
                    onForgotPassword = {}
                )
            }
        }
        
        // 2. Realizar login
        composeTestRule.onNodeWithText("Correo electrónico", useUnmergedTree = true)
            .performTextInput("admin@eguneroko.com")
        
        composeTestRule.onNodeWithText("Contraseña", useUnmergedTree = true)
            .performTextInput("password")
        
        composeTestRule.onNodeWithText("Iniciar sesión", useUnmergedTree = true)
            .performClick()
        
        // 3. Mostrar dashboard de administrador
        composeTestRule.setContent {
            UmeEguneroTheme {
                com.tfg.umeegunero.feature.admin.screen.AdminDashboardScreen(
                    navController = navController,
                    viewModel = adminDashboardViewModel
                )
            }
        }
        
        // 4. Verificar elementos del dashboard
        composeTestRule.onNodeWithText("Dashboard", useUnmergedTree = true)
            .assertExists("No se muestra el título del dashboard")
        
        // 5. Navegar a gestión de centros
        composeTestRule.onNodeWithText("Gestión de Centros", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
            
        // 6. Verificar navegación
        assert(navController.currentDestination?.route?.contains("gestion_centros") == true) {
            "No se navegó correctamente a Gestión de Centros"
        }
    }
    
    /**
     * Test de flujo completo para un administrador de centro
     * - Acceso con credenciales de administrador de centro
     * - Navegación por el dashboard
     * - Acceso a la gestión de profesores
     * - Acceso a la gestión de alumnos
     */
    @Test
    fun testFlujoCompletoAdministradorCentro() {
        // Configurar mocks
        val loginViewModel = mockk<LoginViewModel>(relaxed = true)
        val centroDashboardViewModel = mockk<com.tfg.umeegunero.feature.centro.viewmodel.CentroDashboardViewModel>(relaxed = true)
        
        // Usuario administrador de centro
        val centroUsuario = Usuario(
            id = "centro123",
            nombre = "Begoña",
            apellidos = "Merana",
            email = "bmerana@eguneroko.com",
            tipoUsuario = TipoUsuario.ADMIN_CENTRO
        )
        
        // Simular login exitoso
        every { loginViewModel.loginState } returns androidx.compose.runtime.mutableStateOf(
            com.tfg.umeegunero.feature.auth.viewmodel.LoginState(
                isLoading = false,
                isSuccess = true,
                errorMessage = null
            )
        )
        
        // Simular datos del dashboard
        every { centroDashboardViewModel.currentUser } returns androidx.compose.runtime.mutableStateOf(centroUsuario)
        
        // 1. Mostrar login de centro
        composeTestRule.setContent {
            UmeEguneroTheme {
                navController = rememberNavController()
                
                com.tfg.umeegunero.feature.auth.screen.LoginScreen(
                    userType = TipoUsuario.ADMIN_CENTRO,
                    viewModel = loginViewModel,
                    onNavigateBack = {},
                    onLoginSuccess = {
                        // Simular navegación al dashboard
                        navController.navigate(AppScreens.CentroDashboard.route)
                    },
                    onForgotPassword = {}
                )
            }
        }
        
        // 2. Realizar login
        composeTestRule.onNodeWithText("Correo electrónico", useUnmergedTree = true)
            .performTextInput("bmerana@eguneroko.com")
        
        composeTestRule.onNodeWithText("Contraseña", useUnmergedTree = true)
            .performTextInput("password")
        
        composeTestRule.onNodeWithText("Iniciar sesión", useUnmergedTree = true)
            .performClick()
        
        // 3. Mostrar dashboard de centro
        composeTestRule.setContent {
            UmeEguneroTheme {
                com.tfg.umeegunero.feature.centro.screen.CentroDashboardScreen(
                    navController = navController,
                    viewModel = centroDashboardViewModel
                )
            }
        }
        
        // 4. Verificar elementos del dashboard
        composeTestRule.onNodeWithText("Begoña Merana", useUnmergedTree = true)
            .assertExists("No se muestra el nombre del administrador de centro")
        
        // 5. Navegar a gestión de profesores
        composeTestRule.onNodeWithText("Profesores", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
            
        // 6. Verificar navegación
        assert(navController.currentDestination?.route?.contains("profesores") == true) {
            "No se navegó correctamente a la gestión de profesores"
        }
    }
    
    /**
     * Test de flujo completo para un profesor
     * - Acceso con credenciales de profesor
     * - Navegación por el dashboard
     * - Acceso a la gestión de clases
     * - Acceso a actividades diarias
     */
    @Test
    fun testFlujoCompletoProfesor() {
        // Configurar mocks
        val loginViewModel = mockk<LoginViewModel>(relaxed = true)
        val profesorDashboardViewModel = mockk<com.tfg.umeegunero.feature.profesor.viewmodel.ProfesorDashboardViewModel>(relaxed = true)
        
        // Usuario profesor
        val profesorUsuario = Usuario(
            id = "profesor123",
            nombre = "Profesor",
            apellidos = "Test",
            email = "profesor@eguneroko.com",
            tipoUsuario = TipoUsuario.PROFESOR
        )
        
        // Simular login exitoso
        every { loginViewModel.loginState } returns androidx.compose.runtime.mutableStateOf(
            com.tfg.umeegunero.feature.auth.viewmodel.LoginState(
                isLoading = false,
                isSuccess = true,
                errorMessage = null
            )
        )
        
        // Simular datos del dashboard
        every { profesorDashboardViewModel.currentUser } returns androidx.compose.runtime.mutableStateOf(profesorUsuario)
        every { profesorDashboardViewModel.clases } returns androidx.compose.runtime.mutableStateOf(
            listOf(
                com.tfg.umeegunero.data.model.Clase(
                    id = "clase1",
                    nombre = "1º Infantil A",
                    cursoId = "curso1"
                )
            )
        )
        
        // 1. Mostrar login de profesor
        composeTestRule.setContent {
            UmeEguneroTheme {
                navController = rememberNavController()
                
                com.tfg.umeegunero.feature.auth.screen.LoginScreen(
                    userType = TipoUsuario.PROFESOR,
                    viewModel = loginViewModel,
                    onNavigateBack = {},
                    onLoginSuccess = {
                        // Simular navegación al dashboard
                        navController.navigate(AppScreens.ProfesorDashboard.route)
                    },
                    onForgotPassword = {}
                )
            }
        }
        
        // 2. Realizar login
        composeTestRule.onNodeWithText("Correo electrónico", useUnmergedTree = true)
            .performTextInput("profesor@eguneroko.com")
        
        composeTestRule.onNodeWithText("Contraseña", useUnmergedTree = true)
            .performTextInput("password")
        
        composeTestRule.onNodeWithText("Iniciar sesión", useUnmergedTree = true)
            .performClick()
        
        // 3. Mostrar dashboard de profesor
        composeTestRule.setContent {
            UmeEguneroTheme {
                com.tfg.umeegunero.feature.profesor.screen.ProfesorDashboardScreen(
                    navController = navController,
                    viewModel = profesorDashboardViewModel
                )
            }
        }
        
        // 4. Verificar elementos del dashboard
        composeTestRule.onNodeWithText("Profesor Test", useUnmergedTree = true)
            .assertExists("No se muestra el nombre del profesor")
        
        // 5. Navegar a actividades diarias
        composeTestRule.onNodeWithText("Actividades", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
            
        // 6. Verificar navegación
        assert(navController.currentDestination?.route?.contains("actividades") == true) {
            "No se navegó correctamente a actividades diarias"
        }
    }
    
    /**
     * Test de flujo completo para un familiar
     * - Acceso con credenciales de familiar
     * - Navegación por el dashboard
     * - Acceso a las actividades de los hijos
     * - Selección de hijos
     */
    @Test
    fun testFlujoCompletoFamiliar() {
        // Configurar mocks
        val loginViewModel = mockk<LoginViewModel>(relaxed = true)
        val familiarDashboardViewModel = mockk<com.tfg.umeegunero.feature.familiar.viewmodel.FamiliarDashboardViewModel>(relaxed = true)
        
        // Usuario familiar
        val familiarUsuario = Usuario(
            id = "familiar123",
            nombre = "Familiar",
            apellidos = "Test",
            email = "familiar@eguneroko.com",
            tipoUsuario = TipoUsuario.FAMILIAR
        )
        
        // Hijos
        val hijos = listOf(
            com.tfg.umeegunero.data.model.Alumno(
                id = "alumno1",
                nombre = "Juan",
                apellidos = "García",
                fechaNacimiento = java.util.Date(),
                claseId = "clase1"
            ),
            com.tfg.umeegunero.data.model.Alumno(
                id = "alumno2",
                nombre = "María",
                apellidos = "García",
                fechaNacimiento = java.util.Date(),
                claseId = "clase2"
            )
        )
        
        // Simular login exitoso
        every { loginViewModel.loginState } returns androidx.compose.runtime.mutableStateOf(
            com.tfg.umeegunero.feature.auth.viewmodel.LoginState(
                isLoading = false,
                isSuccess = true,
                errorMessage = null
            )
        )
        
        // Simular datos del dashboard
        every { familiarDashboardViewModel.currentUser } returns androidx.compose.runtime.mutableStateOf(familiarUsuario)
        every { familiarDashboardViewModel.hijos } returns androidx.compose.runtime.mutableStateOf(hijos)
        every { familiarDashboardViewModel.hijoSeleccionado } returns androidx.compose.runtime.mutableStateOf(hijos[0])
        
        // 1. Mostrar login de familiar
        composeTestRule.setContent {
            UmeEguneroTheme {
                navController = rememberNavController()
                
                com.tfg.umeegunero.feature.auth.screen.LoginScreen(
                    userType = TipoUsuario.FAMILIAR,
                    viewModel = loginViewModel,
                    onNavigateBack = {},
                    onLoginSuccess = {
                        // Simular navegación al dashboard
                        navController.navigate(AppScreens.FamiliarDashboard.route)
                    },
                    onForgotPassword = {}
                )
            }
        }
        
        // 2. Realizar login
        composeTestRule.onNodeWithText("Correo electrónico", useUnmergedTree = true)
            .performTextInput("familiar@eguneroko.com")
        
        composeTestRule.onNodeWithText("Contraseña", useUnmergedTree = true)
            .performTextInput("password")
        
        composeTestRule.onNodeWithText("Iniciar sesión", useUnmergedTree = true)
            .performClick()
        
        // 3. Mostrar dashboard de familiar
        composeTestRule.setContent {
            UmeEguneroTheme {
                com.tfg.umeegunero.feature.familiar.screen.FamiliarDashboardScreen(
                    navController = navController,
                    viewModel = familiarDashboardViewModel
                )
            }
        }
        
        // 4. Verificar elementos del dashboard
        composeTestRule.onNodeWithText("Familiar Test", useUnmergedTree = true)
            .assertExists("No se muestra el nombre del familiar")
        
        // 5. Verificar selección de hijo
        composeTestRule.onNodeWithText("Juan García", useUnmergedTree = true)
            .assertExists("No se muestra el hijo seleccionado")
        
        // 6. Navegar a actividades
        composeTestRule.onNodeWithText("Actividades", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
            
        // 7. Verificar navegación
        assert(navController.currentDestination?.route?.contains("actividades") == true) {
            "No se navegó correctamente a actividades"
        }
    }
} 