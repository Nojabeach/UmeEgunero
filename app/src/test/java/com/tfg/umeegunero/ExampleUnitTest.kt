package com.tfg.umeegunero

import android.content.SharedPreferences
import com.tfg.umeegunero.data.model.Result
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Perfil
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.feature.auth.viewmodel.LoginViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests unitarios para el LoginViewModel
 */
@ExperimentalCoroutinesApi
class LoginViewModelTest {
    // Mocks
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor
    
    // ViewModel a probar
    private lateinit var viewModel: LoginViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        
        // Setup mocks
        usuarioRepository = mockk(relaxed = true)
        sharedPreferences = mockk(relaxed = true)
        sharedPreferencesEditor = mockk(relaxed = true)
        
        // Configuración de sharedPreferences
        every { sharedPreferences.getString(any(), any()) } returns ""
        every { sharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } returns Unit
        
        // Inicializar ViewModel
        viewModel = LoginViewModel(usuarioRepository, sharedPreferences)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `cuando ingresa email válido, no hay error`() {
        // When: Ingresamos un email válido
        viewModel.updateEmail("usuario@ejemplo.com")
        
        // Then: No debe haber error de email
        assertNull(viewModel.uiState.value.emailError)
    }
    
    @Test
    fun `cuando ingresa email inválido, muestra error`() {
        // When: Ingresamos un email inválido
        viewModel.updateEmail("usuario-invalido")
        
        // Then: Debe mostrar error de email
        assertNotNull(viewModel.uiState.value.emailError)
    }
    
    @Test
    fun `cuando ingresa contraseña válida, no hay error`() {
        // When: Ingresamos una contraseña válida (mínimo 6 caracteres)
        viewModel.updatePassword("123456")
        
        // Then: No debe haber error de contraseña
        assertNull(viewModel.uiState.value.passwordError)
    }
    
    @Test
    fun `cuando ingresa contraseña muy corta, muestra error`() {
        // When: Ingresamos una contraseña muy corta
        viewModel.updatePassword("123")
        
        // Then: Debe mostrar error de contraseña
        assertNotNull(viewModel.uiState.value.passwordError)
    }
    
    @Test
    fun `login exitoso como administrador`() = runTest {
        // Given: Configuramos mocks para respuesta exitosa
        val email = "admin@ejemplo.com"
        val password = "123456"
        val dniUsuario = "12345678A"
        
        val usuario = Usuario(
            dni = dniUsuario,
            email = email,
            nombre = "Admin",
            apellidos = "Test",
            perfiles = listOf(Perfil(tipo = TipoUsuario.ADMIN_APP))
        )
        
        coEvery { usuarioRepository.iniciarSesion(email, password) } returns Result.Success(dniUsuario)
        coEvery { usuarioRepository.getUsuarioPorDni(dniUsuario) } returns Result.Success(usuario)
        
        // Preparar datos para prueba
        viewModel.updateEmail(email)
        viewModel.updatePassword(password)
        
        // When: Realizamos login
        viewModel.login(TipoUsuario.ADMIN_APP, true)
        
        // Then: Estado de éxito y tipo de usuario correcto
        assertTrue(viewModel.uiState.value.success)
        assertEquals(TipoUsuario.ADMIN_APP, viewModel.uiState.value.userType)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
        
        // Verificar que se guardaron las credenciales
        verify { sharedPreferencesEditor.putString(any(), email) }
    }
    
    @Test
    fun `login fallido por credenciales incorrectas`() = runTest {
        // Given: Configuramos mocks para respuesta fallida
        val email = "usuario@ejemplo.com"
        val password = "123456"
        
        coEvery { usuarioRepository.iniciarSesion(email, password) } returns 
            Result.Error(Exception("Credenciales inválidas"))
        
        // Preparar datos para prueba
        viewModel.updateEmail(email)
        viewModel.updatePassword(password)
        
        // When: Realizamos login con credenciales incorrectas
        viewModel.login(TipoUsuario.FAMILIAR)
        
        // Then: Estado de error y no éxito
        assertFalse(viewModel.uiState.value.success)
        assertNull(viewModel.uiState.value.userType)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.error)
    }
    
    @Test
    fun `login fallido por perfil incorrecto`() = runTest {
        // Given: Usuario existe pero no tiene el perfil correcto
        val email = "usuario@ejemplo.com"
        val password = "123456"
        val dniUsuario = "12345678A"
        
        val usuario = Usuario(
            dni = dniUsuario,
            email = email,
            nombre = "Usuario",
            apellidos = "Test",
            perfiles = listOf(Perfil(tipo = TipoUsuario.FAMILIAR)) // Solo tiene perfil familiar
        )
        
        coEvery { usuarioRepository.iniciarSesion(email, password) } returns Result.Success(dniUsuario)
        coEvery { usuarioRepository.getUsuarioPorDni(dniUsuario) } returns Result.Success(usuario)
        
        // Preparar datos para prueba
        viewModel.updateEmail(email)
        viewModel.updatePassword(password)
        
        // When: Intentamos login como profesor (perfil que no tiene)
        viewModel.login(TipoUsuario.PROFESOR)
        
        // Then: Estado de error y no éxito
        assertFalse(viewModel.uiState.value.success)
        assertNull(viewModel.uiState.value.userType)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.error)
        
        // Verificar que se cerró sesión
        verify { usuarioRepository.cerrarSesion() }
    }
}