package com.tfg.umeegunero.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.tfg.umeegunero.data.model.Result
import com.tfg.umeegunero.data.model.Usuario
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.lang.Exception

/**
 * Test unitario para el repositorio de autenticación.
 * 
 * Este repositorio se encarga de gestionar la autenticación de usuarios
 * en Firebase Auth y la consulta/actualización de datos en Firestore.
 * 
 * @author Estudiante 2º DAM
 */
class AuthRepositoryTest {
    
    // Mocks
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var firebaseUser: FirebaseUser
    
    // Repositorio a probar
    private lateinit var authRepository: AuthRepository
    
    @Before
    fun setup() {
        // Inicializar mocks
        firebaseAuth = mockk(relaxed = true)
        usuarioRepository = mockk(relaxed = true)
        firebaseUser = mockk(relaxed = true)
        
        // Configurar comportamiento de los mocks
        every { firebaseUser.email } returns "test@example.com"
        
        // Inicializar repositorio
        authRepository = AuthRepositoryImpl(firebaseAuth, usuarioRepository)
    }
    
    @Test
    fun `getCurrentUser devuelve usuario cuando está autenticado y existe en Firestore`() = runBlocking {
        // Given: Usuario autenticado en Firebase y registrado en Firestore
        val usuario = Usuario(
            dni = "12345678A",
            nombre = "Test User",
            email = "test@example.com"
        )
        
        every { firebaseAuth.currentUser } returns firebaseUser
        coEvery { usuarioRepository.getUsuarioByEmail("test@example.com") } returns Result.Success(usuario)
        
        // When: Obtenemos el usuario actual
        val result = authRepository.getCurrentUser()
        
        // Then: Debería devolver el usuario
        assertNotNull(result)
        assertEquals("Test User", result?.nombre)
        assertEquals("test@example.com", result?.email)
        
        // Verify: Se llamó al método getUsuarioByEmail
        coVerify { usuarioRepository.getUsuarioByEmail("test@example.com") }
    }
    
    @Test
    fun `getCurrentUser devuelve null cuando no hay usuario autenticado`() = runBlocking {
        // Given: No hay usuario autenticado en Firebase
        every { firebaseAuth.currentUser } returns null
        
        // When: Obtenemos el usuario actual
        val result = authRepository.getCurrentUser()
        
        // Then: Debería devolver null
        assertNull(result)
        
        // Verify: No se llamó al método getUsuarioByEmail
        coVerify(exactly = 0) { usuarioRepository.getUsuarioByEmail(any()) }
    }
    
    @Test
    fun `getCurrentUser devuelve null cuando hay error en Firestore`() = runBlocking {
        // Given: Usuario autenticado en Firebase pero error en Firestore
        every { firebaseAuth.currentUser } returns firebaseUser
        coEvery { usuarioRepository.getUsuarioByEmail("test@example.com") } returns 
            Result.Error(Exception("Error en Firestore"))
        
        // When: Obtenemos el usuario actual
        val result = authRepository.getCurrentUser()
        
        // Then: Debería devolver null por el error
        assertNull(result)
        
        // Verify: Se llamó al método getUsuarioByEmail
        coVerify { usuarioRepository.getUsuarioByEmail("test@example.com") }
    }
    
    @Test
    fun `signOut llama al método de Firebase`() = runBlocking {
        // When: Cerramos sesión
        authRepository.signOut()
        
        // Then: Debería llamar al método signOut de Firebase Auth
        verify { firebaseAuth.signOut() }
    }
    
    @Test
    fun `sendPasswordResetEmail retorna éxito cuando se envía correctamente`() = runBlocking {
        // Given: Firebase enviará email correctamente
        val resetEmailTask = mockk<com.google.android.gms.tasks.Task<Void>> {
            every { await() } returns mockk()
        }
        every { firebaseAuth.sendPasswordResetEmail(any()) } returns resetEmailTask
        
        // When: Enviamos email de reset
        val result = authRepository.sendPasswordResetEmail("test@example.com")
        
        // Then: Debería devolver éxito
        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data)
        
        // Verify: Se llamó al método sendPasswordResetEmail
        verify { firebaseAuth.sendPasswordResetEmail("test@example.com") }
    }
} 