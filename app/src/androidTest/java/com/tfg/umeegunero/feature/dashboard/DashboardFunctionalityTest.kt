package com.tfg.umeegunero.feature.dashboard

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.tfg.umeegunero.MainActivity
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.admin.screen.AdminDashboardScreen
import com.tfg.umeegunero.feature.admin.viewmodel.AdminDashboardViewModel
import com.tfg.umeegunero.feature.centro.screen.CentroDashboardScreen
import com.tfg.umeegunero.feature.centro.viewmodel.CentroDashboardViewModel
import com.tfg.umeegunero.feature.familiar.screen.FamiliarDashboardScreen
import com.tfg.umeegunero.feature.familiar.viewmodel.FamiliarDashboardViewModel
import com.tfg.umeegunero.feature.profesor.screen.ProfesorDashboardScreen
import com.tfg.umeegunero.feature.profesor.viewmodel.ProfesorDashboardViewModel
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
 * Test de funcionalidades de los dashboards según el tipo de usuario.
 * 
 * Este test verifica las funcionalidades específicas disponibles en cada
 * dashboard de usuario, basado en los requerimientos del documento de Sprint Consolidado.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest
class DashboardFunctionalityTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    // ViewModels simulados para cada tipo de dashboard
    private val mockAdminDashboardViewModel = mockk<AdminDashboardViewModel>(relaxed = true)
    private val mockCentroDashboardViewModel = mockk<CentroDashboardViewModel>(relaxed = true)
    private val mockProfesorDashboardViewModel = mockk<ProfesorDashboardViewModel>(relaxed = true)
    private val mockFamiliarDashboardViewModel = mockk<FamiliarDashboardViewModel>(relaxed = true)
    
    @Before
    fun setup() {
        hiltRule.inject()
        
        // Inicializar mocks con valores predeterminados si es necesario
        mockViewModelData()
    }
    
    /**
     * Configuración de datos simulados para los ViewModels
     */
    private fun mockViewModelData() {
        // Mock para el ViewModel de Administrador
        every { mockAdminDashboardViewModel.currentUser } returns androidx.compose.runtime.mutableStateOf(
            com.tfg.umeegunero.data.model.Usuario(
                id = "admin123",
                nombre = "Admin",
                apellidos = "App",
                email = "admin@eguneroko.com",
                tipoUsuario = TipoUsuario.ADMIN_APP
            )
        )
        
        // Mock para el ViewModel de Centro
        every { mockCentroDashboardViewModel.currentUser } returns androidx.compose.runtime.mutableStateOf(
            com.tfg.umeegunero.data.model.Usuario(
                id = "centro123",
                nombre = "Begoña",
                apellidos = "Merana",
                email = "bmerana@eguneroko.com",
                tipoUsuario = TipoUsuario.ADMIN_CENTRO
            )
        )
        
        // Mock para el ViewModel de Profesor
        every { mockProfesorDashboardViewModel.currentUser } returns androidx.compose.runtime.mutableStateOf(
            com.tfg.umeegunero.data.model.Usuario(
                id = "profesor123",
                nombre = "Profesor",
                apellidos = "Test",
                email = "profesor@eguneroko.com",
                tipoUsuario = TipoUsuario.PROFESOR
            )
        )
        
        // Mock para el ViewModel de Familiar
        every { mockFamiliarDashboardViewModel.currentUser } returns androidx.compose.runtime.mutableStateOf(
            com.tfg.umeegunero.data.model.Usuario(
                id = "familiar123",
                nombre = "Familiar",
                apellidos = "Test",
                email = "familiar@eguneroko.com",
                tipoUsuario = TipoUsuario.FAMILIAR
            )
        )
    }
    
    /**
     * Test para verificar las funcionalidades del dashboard de administrador
     */
    @Test
    fun testAdminDashboard_MuestraFuncionalidadesCorrectamente() {
        // Mostrar el dashboard de administrador
        composeTestRule.setContent {
            UmeEguneroTheme {
                AdminDashboardScreen(
                    navController = androidx.navigation.compose.rememberNavController(),
                    viewModel = mockAdminDashboardViewModel
                )
            }
        }
        
        // Verificar elementos específicos del dashboard de administrador según los requerimientos
        composeTestRule.onNodeWithText("Dashboard", useUnmergedTree = true).assertExists()
        
        // Verificar secciones principales
        composeTestRule.onNodeWithText("Gestión de Centros", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
            
        composeTestRule.onNodeWithText("Usuarios", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
            
        composeTestRule.onNodeWithText("Configuración", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
    }
    
    /**
     * Test para verificar las funcionalidades del dashboard de centro
     */
    @Test
    fun testCentroDashboard_MuestraFuncionalidadesCorrectamente() {
        // Mostrar el dashboard de centro
        composeTestRule.setContent {
            UmeEguneroTheme {
                CentroDashboardScreen(
                    navController = androidx.navigation.compose.rememberNavController(),
                    viewModel = mockCentroDashboardViewModel
                )
            }
        }
        
        // Verificar elementos específicos del dashboard de centro según los requerimientos
        composeTestRule.onNodeWithText("Begoña Merana", useUnmergedTree = true).assertExists()
        
        // Verificar secciones principales
        composeTestRule.onNodeWithText("Estadísticas", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
            
        composeTestRule.onNodeWithText("Profesores", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
            
        composeTestRule.onNodeWithText("Alumnos", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
            
        composeTestRule.onNodeWithText("Notificaciones", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
    }
    
    /**
     * Test para verificar las funcionalidades del dashboard de profesor
     */
    @Test
    fun testProfesorDashboard_MuestraFuncionalidadesCorrectamente() {
        // Mostrar el dashboard de profesor
        composeTestRule.setContent {
            UmeEguneroTheme {
                ProfesorDashboardScreen(
                    navController = androidx.navigation.compose.rememberNavController(),
                    viewModel = mockProfesorDashboardViewModel
                )
            }
        }
        
        // Verificar elementos específicos del dashboard de profesor según los requerimientos
        composeTestRule.onNodeWithText("Profesor Test", useUnmergedTree = true).assertExists()
        
        // Verificar secciones principales relacionadas con los requerimientos
        composeTestRule.onNodeWithText("Mis Clases", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
            
        composeTestRule.onNodeWithText("Actividades", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
            
        composeTestRule.onNodeWithText("Evaluación", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
            
        composeTestRule.onNodeWithText("Comunicación", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
    }
    
    /**
     * Test para verificar las funcionalidades del dashboard de familiar
     */
    @Test
    fun testFamiliarDashboard_MuestraFuncionalidadesCorrectamente() {
        // Mostrar el dashboard de familiar
        composeTestRule.setContent {
            UmeEguneroTheme {
                FamiliarDashboardScreen(
                    navController = androidx.navigation.compose.rememberNavController(),
                    viewModel = mockFamiliarDashboardViewModel
                )
            }
        }
        
        // Verificar elementos específicos del dashboard de familiar según los requerimientos
        composeTestRule.onNodeWithText("Familiar Test", useUnmergedTree = true).assertExists()
        
        // Verificar secciones principales
        composeTestRule.onNodeWithText("Mis Hijos", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
            
        composeTestRule.onNodeWithText("Actividades", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
            
        composeTestRule.onNodeWithText("Calendario", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
            
        composeTestRule.onNodeWithText("Comunicación", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
    }
    
    /**
     * Test para verificar que el administrador tiene acceso a todas las funcionalidades
     */
    @Test
    fun testAdmin_TieneAccesoATodaLasFuncionalidades() {
        // Mostrar el dashboard de administrador
        composeTestRule.setContent {
            UmeEguneroTheme {
                AdminDashboardScreen(
                    navController = androidx.navigation.compose.rememberNavController(),
                    viewModel = mockAdminDashboardViewModel
                )
            }
        }
        
        // Verificar que el administrador tiene acceso a funcionalidades de todos los perfiles
        composeTestRule.onNodeWithText("Gestión de Centros", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
            
        composeTestRule.onNodeWithText("Gestión de Usuarios", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
            
        composeTestRule.onNodeWithText("Configuración Global", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
            
        // Deberíamos encontrar también acceso a funcionalidades de otros perfiles
        composeTestRule.onNodeWithText("Acceso a Funcionalidades de Centro", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
            
        composeTestRule.onNodeWithText("Acceso a Funcionalidades de Profesor", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
            
        composeTestRule.onNodeWithText("Acceso a Funcionalidades de Familiar", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
    }
} 