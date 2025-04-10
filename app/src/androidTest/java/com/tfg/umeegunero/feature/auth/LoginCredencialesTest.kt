package com.tfg.umeegunero.feature.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tfg.umeegunero.MainActivity
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.auth.viewmodel.LoginViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test para las credenciales de acceso documentadas en Sprint Consolidado
 */
@RunWith(AndroidJUnit4::class)
class LoginCredencialesTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    private lateinit var viewModel: LoginViewModel
    
    @Before
    fun setup() {
        viewModel = mockk(relaxed = true)
    }
    
    @Test
    fun testLoginAdministrador() {
        // Comprobar acceso de administrador
        val email = "admin@eguneroko.com"
        val password = "password"
        
        // Configurar respuesta del ViewModel
        mockLoginExitoso(email, password)
        
        // Mostrar pantalla de login
        mostrarPantallaLogin(TipoUsuario.ADMIN_APP)
        
        // Simular login
        realizarLogin(email, password)
        
        // Verificar que se llamó al ViewModel correctamente
        verificarLogin(email, password)
    }
    
    @Test
    fun testLoginCentro() {
        // Comprobar acceso de centro
        val email = "bmerana@eguneroko.com"
        val password = "password"
        
        // Configurar respuesta del ViewModel
        mockLoginExitoso(email, password)
        
        // Mostrar pantalla de login
        mostrarPantallaLogin(TipoUsuario.ADMIN_CENTRO)
        
        // Simular login
        realizarLogin(email, password)
        
        // Verificar que se llamó al ViewModel correctamente
        verificarLogin(email, password)
    }
    
    @Test
    fun testLoginProfesor() {
        // Comprobar acceso de profesor
        val email = "profesor@eguneroko.com"
        val password = "password"
        
        // Configurar respuesta del ViewModel
        mockLoginExitoso(email, password)
        
        // Mostrar pantalla de login
        mostrarPantallaLogin(TipoUsuario.PROFESOR)
        
        // Simular login
        realizarLogin(email, password)
        
        // Verificar que se llamó al ViewModel correctamente
        verificarLogin(email, password)
    }
    
    @Test
    fun testLoginFamiliar() {
        // Comprobar acceso de familiar
        val email = "familiar@eguneroko.com"
        val password = "password"
        
        // Configurar respuesta del ViewModel
        mockLoginExitoso(email, password)
        
        // Mostrar pantalla de login
        mostrarPantallaLogin(TipoUsuario.FAMILIAR)
        
        // Simular login
        realizarLogin(email, password)
        
        // Verificar que se llamó al ViewModel correctamente
        verificarLogin(email, password)
    }
    
    @Test
    fun testLoginFallido() {
        // Comprobar login fallido con credenciales incorrectas
        val email = "incorrecto@eguneroko.com"
        val password = "incorrecto"
        
        // Configurar respuesta del ViewModel para login fallido
        mockLoginFallido(email, password)
        
        // Mostrar pantalla de login
        mostrarPantallaLogin(TipoUsuario.ADMIN_APP)
        
        // Simular login
        realizarLogin(email, password)
        
        // Verificar mensaje de error
        composeTestRule.onNodeWithText("Credenciales incorrectas", useUnmergedTree = true)
            .assertExists("No se mostró el mensaje de error para credenciales incorrectas")
    }
    
    private fun mockLoginExitoso(email: String, password: String) {
        every { viewModel.login(email, password) } answers {
            every { viewModel.loginState } returns androidx.compose.runtime.mutableStateOf(
                com.tfg.umeegunero.feature.auth.viewmodel.LoginState(
                    isLoading = false,
                    isSuccess = true,
                    errorMessage = null
                )
            )
        }
    }
    
    private fun mockLoginFallido(email: String, password: String) {
        every { viewModel.login(email, password) } answers {
            every { viewModel.loginState } returns androidx.compose.runtime.mutableStateOf(
                com.tfg.umeegunero.feature.auth.viewmodel.LoginState(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = "Credenciales incorrectas"
                )
            )
        }
    }
    
    private fun mostrarPantallaLogin(tipoUsuario: TipoUsuario) {
        composeTestRule.setContent {
            com.tfg.umeegunero.feature.auth.screen.LoginScreen(
                userType = tipoUsuario,
                viewModel = viewModel,
                onNavigateBack = {},
                onLoginSuccess = {},
                onForgotPassword = {}
            )
        }
    }
    
    private fun realizarLogin(email: String, password: String) {
        // Ingresar credenciales
        composeTestRule.onNodeWithText("Correo electrónico", useUnmergedTree = true)
            .performTextInput(email)
        
        composeTestRule.onNodeWithText("Contraseña", useUnmergedTree = true)
            .performTextInput(password)
        
        // Hacer login
        composeTestRule.onNodeWithText("Iniciar sesión", useUnmergedTree = true)
            .performClick()
    }
    
    private fun verificarLogin(email: String, password: String) {
        // Verificar que se llamó al login con las credenciales correctas
        verify { viewModel.login(email, password) }
    }
} 