package com.tfg.umeegunero.feature.common

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tfg.umeegunero.feature.common.login.screen.LoginScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de UI para la pantalla de login.
 * 
 * Estas pruebas verifican la correcta visualización y comportamiento
 * de los elementos de la interfaz de usuario en la pantalla de login,
 * comprobando la validación de campos y los eventos de interacción.
 * 
 * @author Maitane - TFG UmeEgunero
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_initialState_isDisplayedCorrectly() {
        // Given: La pantalla de login en estado inicial
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {},
                onNavigateToForgotPassword = {},
                onNavigateToHome = {},
                tipoUsuario = "DEFAULT"
            )
        }

        // Then: Verificar que los elementos iniciales se muestran correctamente
        composeTestRule.onNodeWithText("Iniciar sesión").assertIsDisplayed()
        composeTestRule.onNodeWithText("Correo electrónico").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()
        
        // El botón de login debería estar deshabilitado inicialmente
        composeTestRule.onNodeWithText("Entrar").assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun loginScreen_validInput_enablesLoginButton() {
        // Given: La pantalla de login
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {},
                onNavigateToForgotPassword = {},
                onNavigateToHome = {},
                tipoUsuario = "DEFAULT"
            )
        }

        // When: Introducimos un email y contraseña válidos
        composeTestRule.onNodeWithTag("email_field").performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("password123")

        // Then: El botón de login debería estar habilitado
        composeTestRule.onNodeWithText("Entrar").assertIsEnabled()
    }

    @Test
    fun loginScreen_invalidEmail_showsError() {
        // Given: La pantalla de login
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {},
                onNavigateToForgotPassword = {},
                onNavigateToHome = {},
                tipoUsuario = "DEFAULT"
            )
        }

        // When: Introducimos un email inválido y cambiamos el foco
        composeTestRule.onNodeWithTag("email_field").performTextInput("invalid-email")
        // Hacer clic en otro campo para perder el foco
        composeTestRule.onNodeWithTag("password_field").performClick()

        // Then: Debería mostrarse un mensaje de error
        composeTestRule.onNodeWithText("Email no válido").assertIsDisplayed()
    }
    
    @Test
    fun loginScreen_emptyPassword_keepLoginButtonDisabled() {
        // Given: La pantalla de login
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {},
                onNavigateToForgotPassword = {},
                onNavigateToHome = {},
                tipoUsuario = "DEFAULT"
            )
        }

        // When: Sólo introducimos un email válido pero no contraseña
        composeTestRule.onNodeWithTag("email_field").performTextInput("valid@example.com")

        // Then: El botón de login debería seguir deshabilitado
        composeTestRule.onNodeWithText("Entrar").assertIsNotEnabled()
    }
    
    @Test
    fun loginScreen_forgotPasswordLink_isClickable() {
        // Given: Un flag para verificar si el callback fue invocado
        var forgotPasswordClicked = false
        
        // When: Configuramos la pantalla con un callback
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {},
                onNavigateToForgotPassword = { forgotPasswordClicked = true },
                onNavigateToHome = {},
                tipoUsuario = "DEFAULT"
            )
        }

        // Then: Al hacer clic en "¿Olvidaste tu contraseña?", se invoca el callback
        composeTestRule.onNodeWithText("¿Olvidaste tu contraseña?").performClick()
        assert(forgotPasswordClicked)
    }
    
    @Test
    fun loginScreen_registerLink_isClickable() {
        // Given: Un flag para verificar si el callback fue invocado
        var registerClicked = false
        
        // When: Configuramos la pantalla con un callback
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = { registerClicked = true },
                onNavigateToForgotPassword = {},
                onNavigateToHome = {},
                tipoUsuario = "DEFAULT"
            )
        }

        // Then: Al hacer clic en "Regístrate aquí", se invoca el callback
        composeTestRule.onNodeWithText("Regístrate aquí").performClick()
        assert(registerClicked)
    }
} 