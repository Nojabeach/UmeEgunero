package com.tfg.umeegunero.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tfg.umeegunero.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de navegación para la aplicación.
 * 
 * Estas pruebas verifican que el sistema de navegación de la app funciona correctamente,
 * asegurando que las transiciones entre pantallas operan según lo esperado y que la
 * estructura de navegación se mantiene coherente.
 * 
 * @author Maitane - TFG UmeEgunero
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavegacionTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        hiltRule.inject()
        
        // Configurar el navController para pruebas
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            
            // Aquí iría el navhost con el navController de prueba
            // Por limitaciones del test instrumentado, puede que tengamos que
            // implementar una versión simplificada o componentes individuales
        }
    }
    
    /**
     * Prueba la navegación desde el login a la pantalla de registro.
     */
    @Test
    fun navegacionDesdeLoginHaciaRegistro() {
        // Simular un escenario en el que se navega desde Login a Registro
        
        // Verificar que estamos en la pantalla de login
        composeTestRule.onNodeWithText("Iniciar sesión").assertIsDisplayed()
        
        // Hacer clic en el enlace de registro
        composeTestRule.onNodeWithText("Regístrate aquí").performClick()
        
        // Verificar que navegamos a la pantalla de registro
        composeTestRule.onNodeWithText("Crear cuenta").assertIsDisplayed()
    }
    
    /**
     * Prueba la navegación desde el login a la pantalla de recuperación de contraseña.
     */
    @Test
    fun navegacionDesdeLoginHaciaRecuperacionContrasena() {
        // Simular un escenario en el que se navega desde Login a Recuperación
        
        // Verificar que estamos en la pantalla de login
        composeTestRule.onNodeWithText("Iniciar sesión").assertIsDisplayed()
        
        // Hacer clic en el enlace de recuperación
        composeTestRule.onNodeWithText("¿Olvidaste tu contraseña?").performClick()
        
        // Verificar que navegamos a la pantalla de recuperación
        composeTestRule.onNodeWithText("Recuperar contraseña").assertIsDisplayed()
    }
    
    /**
     * Prueba la navegación desde el dashboard de administrador a la gestión de centros.
     */
    @Test
    fun navegacionDesdeDashboardAdminHaciaGestionCentros() {
        // Para esta prueba, necesitamos iniciar sesión como administrador primero
        // Normalmente esto requeriría un login o navegar programáticamente
        
        // Asumimos que ya estamos en el dashboard (login simulado o navegación directa)
        
        // Verificar que estamos en el dashboard de admin
        composeTestRule.onNodeWithText("Panel de Administración").assertIsDisplayed()
        
        // Clic en la opción de gestión de centros
        composeTestRule.onNodeWithText("Gestión de Centros").performClick()
        
        // Verificar que navegamos a la pantalla de gestión de centros
        composeTestRule.onNodeWithText("Centros Educativos").assertIsDisplayed()
    }
    
    /**
     * Prueba la navegación atrás con el botón de la barra superior.
     */
    @Test
    fun navegacionAtrasConBotonBarra() {
        // Simular una navegación desde dashboard a una pantalla secundaria
        
        // Asumimos que estamos en el dashboard y navegamos a otra pantalla
        
        // Clic en un botón que navega a otra pantalla
        composeTestRule.onNodeWithText("Gestión de Usuarios").performClick()
        
        // Verificar que estamos en la pantalla de gestión de usuarios
        composeTestRule.onNodeWithText("Usuarios").assertIsDisplayed()
        
        // Clic en el botón de navegación atrás en la barra superior
        composeTestRule.onNodeWithContentDescription("Navegar hacia atrás").performClick()
        
        // Verificar que volvemos al dashboard
        composeTestRule.onNodeWithText("Panel de Administración").assertIsDisplayed()
    }
    
    /**
     * Prueba la navegación desde el menú lateral.
     */
    @Test
    fun navegacionDesdeMenuLateral() {
        // Abrir el menú lateral
        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        
        // Verificar que aparecen las opciones del menú
        composeTestRule.onNodeWithText("Mi Perfil").assertIsDisplayed()
        
        // Seleccionar una opción del menú
        composeTestRule.onNodeWithText("Configuración").performClick()
        
        // Verificar que navegamos a la pantalla correspondiente
        composeTestRule.onNodeWithText("Configuración de la aplicación").assertIsDisplayed()
    }
} 