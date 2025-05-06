package com.tfg.umeegunero.feature.common.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Perfil
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.common.login.viewmodel.LoginViewModel
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import kotlinx.coroutines.flow.first
import org.mockito.kotlin.never
import org.mockito.kotlin.anyString

/**
 * Pruebas unitarias para el ViewModel de login.
 * 
 * Estas pruebas validan el correcto funcionamiento de la lógica de autenticación,
 * actualización de estado, y manejo de errores en el proceso de inicio de sesión.
 * 
 * @author Maitane - TFG UmeEgunero
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest {

    // SUT (System Under Test)
    private lateinit var loginViewModel: LoginViewModel
    
    // Mocks
    private val authRepository: AuthRepository = mock()
    private val usuarioRepository: UsuarioRepository = mock()
    private val savedStateHandle = SavedStateHandle()
    
    // Test dispatcher para corrutinas
    private val testDispatcher = UnconfinedTestDispatcher()
    
    @Before
    fun setup() {
        // Configurar savedStateHandle con posibles valores iniciales
        savedStateHandle["tipoUsuario"] = "DEFAULT"
        
        // Inicializar ViewModel con los mocks
        loginViewModel = LoginViewModel(
            authRepository = authRepository,
            usuarioRepository = usuarioRepository,
            savedStateHandle = savedStateHandle
        )
    }
    
    @Test
    fun `updateEmail actualiza el estado correctamente`() = runTest {
        // When: Actualizamos el email
        loginViewModel.updateEmail("test@example.com")
        
        // Then: El estado debería reflejar el nuevo email
        val state = loginViewModel.uiState.first()
        assertEquals("test@example.com", state.email)
        assertNull(state.emailError)
    }
    
    @Test
    fun `updateEmail con valor inválido muestra error`() = runTest {
        // When: Actualizamos el email con un valor inválido
        loginViewModel.updateEmail("invalid-email")
        
        // Then: Debería haber un mensaje de error
        val state = loginViewModel.uiState.first()
        assertEquals("invalid-email", state.email)
        assertNotNull(state.emailError)
        assertTrue(state.emailError!!.contains("válido"))
    }
    
    @Test
    fun `updatePassword actualiza el estado correctamente`() = runTest {
        // When: Actualizamos la contraseña
        loginViewModel.updatePassword("password123")
        
        // Then: El estado debería reflejar la nueva contraseña
        val state = loginViewModel.uiState.first()
        assertEquals("password123", state.password)
    }
    
    @Test
    fun `login con credenciales correctas inicia sesión exitosamente`() = runTest {
        // Given: Email y contraseña válidos
        val email = "user@example.com"
        val password = "password123"
        val userId = "user123"
        val usuario = Usuario(
            dni = "12345678A",
            email = email,
            nombre = "Test",
            apellidos = "User",
            perfiles = listOf(Perfil(tipo = TipoUsuario.PROFESOR))
        )
        
        // Configurar comportamiento de mocks
        whenever(authRepository.loginWithEmailAndPassword(email, password))
            .thenReturn(Result.Success(userId))
        whenever(usuarioRepository.getUsuarioByEmail(email))
            .thenReturn(Result.Success(usuario))
        
        // When: Ejecutamos login
        loginViewModel.updateEmail(email)
        loginViewModel.updatePassword(password)
        loginViewModel.login()
        
        // Then: Debería haberse llamado a los métodos del repositorio
        verify(authRepository).loginWithEmailAndPassword(email, password)
        verify(usuarioRepository).getUsuarioByEmail(email)
        
        // Y el estado debería reflejar el login exitoso
        val state = loginViewModel.uiState.first()
        assertTrue(state.isLoginSuccess)
        assertFalse(state.isLoading)
        assertNull(state.loginError)
    }
    
    @Test
    fun `login con credenciales incorrectas muestra error`() = runTest {
        // Given: Email y contraseña, pero auth falla
        val email = "user@example.com"
        val password = "wrongpassword"
        
        // Configurar comportamiento de mocks para simular error
        whenever(authRepository.loginWithEmailAndPassword(email, password))
            .thenReturn(Result.Error(Exception("Credenciales inválidas")))
        
        // When: Ejecutamos login
        loginViewModel.updateEmail(email)
        loginViewModel.updatePassword(password)
        loginViewModel.login()
        
        // Then: Debería haberse llamado al método de login
        verify(authRepository).loginWithEmailAndPassword(email, password)
        
        // Y el estado debería reflejar el error
        val state = loginViewModel.uiState.first()
        assertFalse(state.isLoginSuccess)
        assertFalse(state.isLoading)
        assertNotNull(state.loginError)
    }
    
    @Test
    fun `clearLoginError elimina el error de login`() = runTest {
        // Given: Un estado con error
        loginViewModel.updateEmail("test@example.com")
        loginViewModel.updatePassword("password")
        
        // Configurar mock para generar error
        whenever(authRepository.loginWithEmailAndPassword(any(), any()))
            .thenReturn(Result.Error(Exception("Error test")))
        
        // Ejecutar login para generar error
        loginViewModel.login()
        
        // Verificar que hay error
        var state = loginViewModel.uiState.first()
        assertNotNull(state.loginError)
        
        // When: Limpiamos el error
        loginViewModel.clearLoginError()
        
        // Then: El error debería haberse eliminado
        state = loginViewModel.uiState.first()
        assertNull(state.loginError)
    }
    
    @Test
    fun `login sin email o password no intenta autenticación`() = runTest {
        // Given: Email sin password
        loginViewModel.updateEmail("test@example.com")
        
        // When: Ejecutamos login
        loginViewModel.login()
        
        // Then: No debería haberse llamado al repositorio
        verify(authRepository, never()).loginWithEmailAndPassword(anyString(), anyString())
        
        // Y debería haber error de validación
        val state = loginViewModel.uiState.first()
        assertFalse(state.isLoginSuccess)
        assertFalse(state.isLoading)
        assertNotNull(state.passwordError)
    }
} 