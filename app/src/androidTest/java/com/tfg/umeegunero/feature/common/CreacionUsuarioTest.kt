package com.tfg.umeegunero.feature.common

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tfg.umeegunero.MainActivity
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.random.Random

/**
 * Test de integración para el flujo completo de creación de usuarios.
 * 
 * Este test verifica todo el flujo desde la navegación a la pantalla de creación
 * de usuario hasta el guardado exitoso, pasando por todas las validaciones
 * y asignación de perfiles.
 * 
 * Escenarios probados:
 * 1. Navegación a la pantalla de creación de usuario
 * 2. Validación de campos obligatorios
 * 3. Selección de tipo de usuario
 * 4. Creación exitosa de un nuevo usuario
 * 5. Visualización del usuario en la lista de usuarios
 * 
 * @author Maitane - TFG UmeEgunero
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CreacionUsuarioTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var authRepository: AuthRepository
    
    @Inject
    lateinit var usuarioRepository: UsuarioRepository

    @Before
    fun setup() {
        hiltRule.inject()
        
        // Iniciar sesión como administrador para poder crear usuarios
        // Esta parte se comentaría en tests reales para no modificar datos de producción
        /*
        runBlocking {
            authRepository.loginWithEmailAndPassword("admin@test.com", "password")
        }
        */
    }

    @Test
    fun testCrearUsuarioProfesor() {
        // 1. Navegar al panel de administración (normalmente tras login)
        // Para pruebas simuladas, se navega directamente
        
        // 2. Esperar a que la pantalla de dashboard se cargue
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Gestión de Usuarios").fetchSemanticsNodes().isNotEmpty()
        }

        // 3. Pulsar en "Gestión de Usuarios"
        composeTestRule.onNodeWithText("Gestión de Usuarios").performClick()

        // 4. Esperar a que la lista de usuarios se cargue
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Añadir Usuario").fetchSemanticsNodes().isNotEmpty()
        }

        // 5. Pulsar en "Añadir Usuario"
        composeTestRule.onNodeWithText("Añadir Usuario").performClick()

        // 6. Rellenar el formulario con datos válidos
        val dni = generarDNIAleatorio()
        val nombre = "Profesor Test"
        val apellidos = "Prueba Automática"
        val email = "profesor.test.${Random.nextInt(1000, 9999)}@example.com"
        
        // 6.1 Seleccionar tipo de usuario (Profesor)
        composeTestRule.onNodeWithTag("dropdown_tipo_usuario").performClick()
        composeTestRule.onNodeWithText("Profesor").performClick()
        
        // 6.2 Rellenar datos personales
        composeTestRule.onNodeWithTag("campo_dni").performTextInput(dni)
        composeTestRule.onNodeWithTag("campo_nombre").performTextInput(nombre)
        composeTestRule.onNodeWithTag("campo_apellidos").performTextInput(apellidos)
        composeTestRule.onNodeWithTag("campo_email").performTextInput(email)
        composeTestRule.onNodeWithTag("campo_telefono").performTextInput("612345678")
        
        // 6.3 Establecer contraseña
        composeTestRule.onNodeWithTag("campo_password").performTextInput("Password123")
        composeTestRule.onNodeWithTag("campo_confirm_password").performTextInput("Password123")
        
        // 6.4 Seleccionar centro educativo
        composeTestRule.onNodeWithText("Seleccionar centro").performClick()
        composeTestRule.onNodeWithTag("lista_centros").onChildren()[0].performClick()
        
        // 6.5 Seleccionar curso/clase (dependiendo del centro seleccionado)
        composeTestRule.onNodeWithText("Seleccionar curso").performClick()
        composeTestRule.onNodeWithTag("lista_cursos").onChildren()[0].performClick()
        
        composeTestRule.onNodeWithText("Seleccionar clase").performClick()
        composeTestRule.onNodeWithTag("lista_clases").onChildren()[0].performClick()
        
        // 7. Guardar el usuario
        composeTestRule.onNodeWithText("Guardar").performClick()
        
        // 8. Verificar redirección a la lista de usuarios tras guardado exitoso
        composeTestRule.waitUntil(8000) {
            composeTestRule.onAllNodesWithText("Usuarios").fetchSemanticsNodes().isNotEmpty()
        }
        
        // 9. Verificar que el nuevo usuario aparece en la lista
        composeTestRule.onNodeWithText(nombre).assertIsDisplayed()
    }
    
    @Test
    fun testCrearUsuarioConDatosIncompletos() {
        // Similar al test anterior, pero intentando guardar sin datos obligatorios
        // para verificar que aparecen los mensajes de error y no se puede guardar
        
        // Navegar a "Añadir Usuario"
        // ...
        
        // Pulsar "Guardar" sin rellenar ningún dato
        composeTestRule.onNodeWithText("Guardar").performClick()
        
        // Verificar mensajes de error
        composeTestRule.onNodeWithText("El DNI es obligatorio").assertIsDisplayed()
        composeTestRule.onNodeWithText("El nombre es obligatorio").assertIsDisplayed()
        // etc.
    }
    
    /**
     * Genera un DNI español aleatorio con letra válida
     */
    private fun generarDNIAleatorio(): String {
        val numero = Random.nextInt(10000000, 99999999)
        val letras = "TRWAGMYFPDXBNJZSQVHLCKE"
        val letra = letras[numero % 23]
        return "$numero$letra"
    }
} 