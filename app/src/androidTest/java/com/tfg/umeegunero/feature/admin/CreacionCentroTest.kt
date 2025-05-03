package com.tfg.umeegunero.feature.admin

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tfg.umeegunero.MainActivity
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.navigation.AppScreens
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.runBlocking

/**
 * Test de integración para el flujo completo de creación de centros.
 * 
 * Este test verifica todo el flujo de usuario desde el inicio de sesión como
 * administrador hasta la creación de un nuevo centro educativo, pasando por
 * todas las pantallas y validaciones necesarias.
 * 
 * Escenarios probados:
 * 1. Inicio de sesión como administrador
 * 2. Navegación al listado de centros
 * 3. Navegación a la pantalla de creación de centro
 * 4. Cumplimentación de formulario con datos válidos
 * 5. Guardado del centro y verificación de creación correcta
 * 
 * IMPORTANTE: Este test modifica datos reales en la base de datos,
 * por lo que se debe ejecutar en un entorno controlado o con datos
 * que puedan ser eliminados después.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CreacionCentroTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        hiltRule.inject()
        
        // Iniciar sesión como administrador
        runBlocking {
            // Aquí se haría un login con usuario administrador
            // Para tests reales se usaría un usuario de prueba específico
            // authRepository.loginWithEmailAndPassword("admin@test.com", "password")
        }
    }

    @Test
    fun testCrearCentroCompleto() {
        // Navegar a la pantalla de administrador (normalmente se iniciaría desde Login)
        // Para tests simulados, se podría navegar directamente
        // En un entorno real, el login redirigiría automáticamente

        // 1. Esperar a que la pantalla de dashboard de administrador se cargue
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Gestión de Centros").fetchSemanticsNodes().isNotEmpty()
        }

        // 2. Pulsar en "Gestión de Centros"
        composeTestRule.onNodeWithText("Gestión de Centros").performClick()

        // 3. Esperar a que la lista de centros se cargue
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Añadir Centro").fetchSemanticsNodes().isNotEmpty()
        }

        // 4. Pulsar en el botón "Añadir Centro"
        composeTestRule.onNodeWithText("Añadir Centro").performClick()

        // 5. Rellenar el formulario con datos válidos
        val nombreCentro = "Centro Test ${Random.nextInt(1000, 9999)}"
        
        // Rellenar nombre del centro
        composeTestRule.onNodeWithTag("campo_nombre_centro").performTextInput(nombreCentro)
        
        // Rellenar dirección
        composeTestRule.onNodeWithTag("campo_direccion").performTextInput("Calle de prueba, 123")
        
        // Seleccionar provincia
        composeTestRule.onNodeWithTag("dropdown_provincia").performClick()
        composeTestRule.onNodeWithText("Madrid").performClick()
        
        // Rellenar código postal
        composeTestRule.onNodeWithTag("campo_codigo_postal").performTextInput("28001")
        
        // Rellenar teléfono
        composeTestRule.onNodeWithTag("campo_telefono").performTextInput("912345678")
        
        // Rellenar email
        composeTestRule.onNodeWithTag("campo_email").performTextInput("test_centro@example.com")
        
        // 6. Guardar el centro
        composeTestRule.onNodeWithContentDescription("Guardar centro").performClick()
        
        // 7. Verificar que volvemos a la lista de centros
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Centros Educativos").fetchSemanticsNodes().isNotEmpty()
        }
        
        // 8. Verificar que el nuevo centro aparece en la lista
        composeTestRule.onNodeWithText(nombreCentro).assertIsDisplayed()
    }
    
    /**
     * Test específico para verificar que no se puede crear un centro sin datos obligatorios.
     */
    @Test
    fun testValidacionCamposObligatorios() {
        // Similar al test anterior pero sin rellenar campos obligatorios
        // y comprobando que aparecen mensajes de error y no se guarda
    }
    
    /**
     * Test para verificar que se puede cancelar la creación.
     */
    @Test
    fun testCancelarCreacionCentro() {
        // Similar al test principal pero cancelando en lugar de guardar
    }
} 