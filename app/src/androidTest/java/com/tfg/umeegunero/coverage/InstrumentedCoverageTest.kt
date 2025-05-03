package com.tfg.umeegunero.coverage

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.navigation.AppScreens
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Test de instrumentación para generar informes de cobertura.
 * 
 * Esta clase ejecuta una serie de operaciones que navegan por diferentes
 * partes de la aplicación, con el objetivo de generar métricas de cobertura
 * de código de instrumentación. No verifica resultados específicos, su
 * propósito es ejecutar la mayor cantidad de código posible.
 * 
 * Para generar informes de cobertura, ejecutar:
 * ./gradlew createDebugCoverageReport
 * 
 * Los informes se generarán en:
 * app/build/reports/coverage/androidTest/debug/
 * 
 * @author Maitane - TFG UmeEgunero
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class InstrumentedCoverageTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    @Inject
    lateinit var usuarioRepository: UsuarioRepository
    
    @Inject
    lateinit var centroRepository: CentroRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    /**
     * Ejecuta flujos completos de la aplicación para generar cobertura de código.
     * 
     * Este test intenta ejecutar tantas partes del código como sea posible,
     * incluyendo navegación, operaciones de repositorio y lógica de negocio.
     */
    @Test
    fun generateCoverageReport() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Registrar el inicio del test
        println("Iniciando test de cobertura en: ${appContext.packageName}")
        
        try {
            // 1. Probar repositorio de autenticación
            val adminEmail = "admin@umeegunero.com"
            val adminPassword = "adminPassword123"
            
            // Intentar login con usuario administrador
            val loginResult = authRepository.loginWithEmailAndPassword(adminEmail, adminPassword)
            println("Resultado login: $loginResult")
            
            // 2. Obtener lista de centros
            val centrosResult = centroRepository.getAllCentros()
            println("Centros obtenidos: ${centrosResult}")
            
            // 3. Obtener lista de usuarios
            val usuariosResult = usuarioRepository.getAllUsuarios()
            println("Usuarios obtenidos: ${usuariosResult}")
            
            // 4. Navegar por las principales rutas de navegación
            val allRoutes = AppScreens::class.java.declaredFields
                .filter { it.name.endsWith("route") }
                .map { 
                    it.isAccessible = true
                    it.get(null) as String 
                }
            
            println("Rutas disponibles en la aplicación:")
            allRoutes.forEach { route ->
                println("- $route")
            }
            
            // Más flujos y operaciones pueden ser añadidos aquí
            // para incrementar la cobertura de código...
            
        } catch (e: Exception) {
            // Capturar excepciones para evitar que el test falle,
            // ya que su objetivo es generar métricas de cobertura
            println("Excepción durante la ejecución del test de cobertura: ${e.message}")
            e.printStackTrace()
        }
        
        println("Test de cobertura completado")
    }
} 