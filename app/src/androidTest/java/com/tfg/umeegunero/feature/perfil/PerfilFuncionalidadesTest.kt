package com.tfg.umeegunero.feature.perfil

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.tfg.umeegunero.MainActivity
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.admin.screen.AdminDashboardScreen
import com.tfg.umeegunero.feature.centro.screen.CentroDashboardScreen
import com.tfg.umeegunero.feature.profesor.screen.ProfesorDashboardScreen
import com.tfg.umeegunero.feature.familiar.screen.FamiliarDashboardScreen
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test para verificar que cada perfil tiene las funcionalidades específicas
 * mencionadas en el documento Sprint_Consolidado.md
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class PerfilFuncionalidadesTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    /**
     * Test de funcionalidades específicas del perfil Administrador
     * según el documento Sprint_Consolidado.md
     */
    @Test
    fun testAdminFuncionalidades() {
        // Configurar ViewModel simulado
        val viewModel = mockk<com.tfg.umeegunero.feature.admin.viewmodel.AdminDashboardViewModel>(relaxed = true)
        
        // Configurar el usuario actual
        every { viewModel.currentUser } returns androidx.compose.runtime.mutableStateOf(
            com.tfg.umeegunero.data.model.Usuario(
                id = "admin123",
                nombre = "Admin",
                apellidos = "App",
                email = "admin@eguneroko.com",
                tipoUsuario = TipoUsuario.ADMIN_APP
            )
        )
        
        // Mostrar el dashboard de administrador
        composeTestRule.setContent {
            UmeEguneroTheme {
                AdminDashboardScreen(
                    navController = androidx.navigation.compose.rememberNavController(),
                    viewModel = viewModel
                )
            }
        }
        
        // Verificar funcionalidades descritas en el documento
        verificarElementoDashboard("Dashboard completo")
        verificarElementoDashboard("Gestión de centros educativos")
        verificarElementoDashboard("Gestión de usuarios")
        verificarElementoDashboard("Configuración global")
        verificarElementoDashboard("Administradores")
        verificarElementoDashboard("Profesores")
        verificarElementoDashboard("Familias")
    }
    
    /**
     * Test de funcionalidades específicas del perfil Administrador de Centro
     * según el documento Sprint_Consolidado.md
     */
    @Test
    fun testCentroFuncionalidades() {
        // Configurar ViewModel simulado
        val viewModel = mockk<com.tfg.umeegunero.feature.centro.viewmodel.CentroDashboardViewModel>(relaxed = true)
        
        // Configurar el usuario actual
        every { viewModel.currentUser } returns androidx.compose.runtime.mutableStateOf(
            com.tfg.umeegunero.data.model.Usuario(
                id = "centro123",
                nombre = "Begoña",
                apellidos = "Merana",
                email = "bmerana@eguneroko.com",
                tipoUsuario = TipoUsuario.ADMIN_CENTRO
            )
        )
        
        // Mostrar el dashboard de centro
        composeTestRule.setContent {
            UmeEguneroTheme {
                CentroDashboardScreen(
                    navController = androidx.navigation.compose.rememberNavController(),
                    viewModel = viewModel
                )
            }
        }
        
        // Verificar funcionalidades descritas en el documento
        verificarElementoDashboard("Estadísticas")
        verificarElementoDashboard("Gestión de profesores")
        verificarElementoDashboard("Gestión de alumnos")
        verificarElementoDashboard("Vinculación de familiares")
        verificarElementoDashboard("Notificaciones")
        verificarElementoDashboard("Configuración del centro")
    }
    
    /**
     * Test de funcionalidades específicas del perfil Profesor
     * según el documento Sprint_Consolidado.md
     */
    @Test
    fun testProfesorFuncionalidades() {
        // Configurar ViewModel simulado
        val viewModel = mockk<com.tfg.umeegunero.feature.profesor.viewmodel.ProfesorDashboardViewModel>(relaxed = true)
        
        // Configurar el usuario actual
        every { viewModel.currentUser } returns androidx.compose.runtime.mutableStateOf(
            com.tfg.umeegunero.data.model.Usuario(
                id = "profesor123",
                nombre = "Profesor",
                apellidos = "Test",
                email = "profesor@eguneroko.com",
                tipoUsuario = TipoUsuario.PROFESOR
            )
        )
        
        // Configurar clases del profesor
        every { viewModel.clases } returns androidx.compose.runtime.mutableStateOf(
            listOf(
                com.tfg.umeegunero.data.model.Clase(
                    id = "clase1",
                    nombre = "1º Infantil A",
                    cursoId = "curso1"
                )
            )
        )
        
        // Mostrar el dashboard de profesor
        composeTestRule.setContent {
            UmeEguneroTheme {
                ProfesorDashboardScreen(
                    navController = androidx.navigation.compose.rememberNavController(),
                    viewModel = viewModel
                )
            }
        }
        
        // Verificar funcionalidades descritas en el documento
        verificarElementoDashboard("Gestión de alumnos")
        verificarElementoDashboard("Clases")
        verificarElementoDashboard("Actividades diarias")
        verificarElementoDashboard("Evaluación")
        verificarElementoDashboard("Rúbricas")
        verificarElementoDashboard("Reuniones")
        verificarElementoDashboard("Comunicación con familias")
        verificarElementoDashboard("Calendario académico")
    }
    
    /**
     * Test de funcionalidades específicas del perfil Familiar
     * según el documento Sprint_Consolidado.md
     */
    @Test
    fun testFamiliarFuncionalidades() {
        // Configurar ViewModel simulado
        val viewModel = mockk<com.tfg.umeegunero.feature.familiar.viewmodel.FamiliarDashboardViewModel>(relaxed = true)
        
        // Configurar el usuario actual
        every { viewModel.currentUser } returns androidx.compose.runtime.mutableStateOf(
            com.tfg.umeegunero.data.model.Usuario(
                id = "familiar123",
                nombre = "Familiar",
                apellidos = "Test",
                email = "familiar@eguneroko.com",
                tipoUsuario = TipoUsuario.FAMILIAR
            )
        )
        
        // Configurar hijos del familiar
        every { viewModel.hijos } returns androidx.compose.runtime.mutableStateOf(
            listOf(
                com.tfg.umeegunero.data.model.Alumno(
                    id = "alumno1",
                    nombre = "Juan",
                    apellidos = "García",
                    fechaNacimiento = java.util.Date(),
                    claseId = "clase1"
                )
            )
        )
        
        // Configurar selección de hijo actual
        every { viewModel.hijoSeleccionado } returns androidx.compose.runtime.mutableStateOf(
            com.tfg.umeegunero.data.model.Alumno(
                id = "alumno1",
                nombre = "Juan",
                apellidos = "García",
                fechaNacimiento = java.util.Date(),
                claseId = "clase1"
            )
        )
        
        // Mostrar el dashboard de familiar
        composeTestRule.setContent {
            UmeEguneroTheme {
                FamiliarDashboardScreen(
                    navController = androidx.navigation.compose.rememberNavController(),
                    viewModel = viewModel
                )
            }
        }
        
        // Verificar funcionalidades descritas en el documento
        verificarElementoDashboard("Actividades")
        verificarElementoDashboard("Selección de hijos")
        verificarElementoDashboard("Estadísticas diarias")
        verificarElementoDashboard("Comunicación con profesores")
        verificarElementoDashboard("Calendario familiar")
        verificarElementoDashboard("Notificaciones")
        verificarElementoDashboard("Historial de actividades")
    }
    
    /**
     * Método auxiliar para verificar la presencia de elementos en cualquier dashboard
     */
    private fun verificarElementoDashboard(texto: String) {
        composeTestRule.onAllNodesWithText(texto, substring = true, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .let { nodes ->
                assert(nodes.isNotEmpty()) {
                    "No se encontró el elemento '$texto' en el dashboard"
                }
            }
    }
} 