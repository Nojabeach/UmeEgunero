package com.tfg.umeegunero

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tfg.umeegunero.data.model.UserType
import com.tfg.umeegunero.feature.auth.screen.LoginScreen
import com.tfg.umeegunero.feature.auth.viewmodel.LoginViewModel
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test instrumental para la pantalla de login.
 *
 * Estos tests se ejecutan en un dispositivo físico o emulador.
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun loginScreen_initialState_buttonsAreDisplayed() {
        // Given: Configuramos la pantalla de login
        composeTestRule.setContent {
            UmeEguneroTheme {
                LoginScreen(
                    viewModel = mockk(relaxed = true),
                    onNavigateToRegister = {},
                    onNavigateToSupport = {},
                    onNavigateToDashboard = {},
                    userType = UserType.FAMILIAR
                )
            }
        }
        
        // Then: Verificamos que los elementos principales están visibles
        composeTestRule.onNodeWithText("Iniciar sesión").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()
    }
    
    @Test
    fun loginScreen_emptyFields_loginButtonDisabled() {
        // Given: Configuramos la pantalla de login
        composeTestRule.setContent {
            UmeEguneroTheme {
                LoginScreen(
                    viewModel = mockk(relaxed = true),
                    onNavigateToRegister = {},
                    onNavigateToSupport = {},
                    onNavigateToDashboard = {},
                    userType = UserType.FAMILIAR
                )
            }
        }
        
        // Then: Verificamos que el botón de login está deshabilitado
        composeTestRule.onNodeWithText("Iniciar sesión").assertIsNotEnabled()
    }
    
    @Test
    fun loginScreen_validInputs_loginButtonEnabled() {
        // Given: Configuramos la pantalla de login
        composeTestRule.setContent {
            UmeEguneroTheme {
                LoginScreen(
                    viewModel = mockk(relaxed = true, defaultAnswer = { call ->
                        if (call.method.name == "getUiState") {
                            return@mockk com.tfg.umeegunero.feature.auth.viewmodel.LoginUiState(
                                email = "test@example.com",
                                password = "password123",
                                emailError = null,
                                passwordError = null,
                                isLoading = false,
                                error = null
                            )
                        }
                        call.originalCall.callOriginal()
                    }),
                    onNavigateToRegister = {},
                    onNavigateToSupport = {},
                    onNavigateToDashboard = {},
                    userType = UserType.FAMILIAR
                )
            }
        }
        
        // Then: El botón de login debería estar habilitado
        composeTestRule.onNodeWithText("Iniciar sesión").assertIsEnabled()
    }
    
    @Test
    fun loginScreen_showsErrorMessage() {
        // Given: Configuramos la pantalla de login con un error
        val errorMessage = "Credenciales inválidas"
        composeTestRule.setContent {
            UmeEguneroTheme {
                LoginScreen(
                    viewModel = mockk(relaxed = true, defaultAnswer = { call ->
                        if (call.method.name == "getUiState") {
                            return@mockk com.tfg.umeegunero.feature.auth.viewmodel.LoginUiState(
                                email = "test@example.com",
                                password = "password123",
                                emailError = null,
                                passwordError = null,
                                isLoading = false,
                                error = errorMessage
                            )
                        }
                        call.originalCall.callOriginal()
                    }),
                    onNavigateToRegister = {},
                    onNavigateToSupport = {},
                    onNavigateToDashboard = {},
                    userType = UserType.FAMILIAR
                )
            }
        }
        
        // Then: El mensaje de error debería mostrarse
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }
}