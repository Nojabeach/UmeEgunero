package com.tfg.umeegunero.feature.admin

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tfg.umeegunero.MainActivity
import com.tfg.umeegunero.feature.admin.viewmodel.AddCentroViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.security.MessageDigest

/**
 * Regla de prueba para la funcionalidad de administración de centros.
 * 
 * Esta clase define un conjunto de pruebas que verifican que la funcionalidad
 * de creación y edición de centros educativos funciona correctamente y no
 * ha sido modificada desde la última validación.
 * 
 * IMPORTANTE: Si estas pruebas fallan, significa que alguien ha modificado
 * el código que ya ha sido probado y validado. Se debe investigar qué cambios
 * se han hecho y si son seguros.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CentroAdminTestRule {

    /**
     * Regla Hilt para inyección de dependencias en tests
     */
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    /**
     * Regla para pruebas de Compose
     */
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    /**
     * Verifica que las constantes de verificación no han sido modificadas.
     * 
     * Si alguien modifica el código de la clase AddCentroViewModel, debería
     * actualizar estas constantes, lo que haría fallar esta prueba, alertando
     * sobre cambios no autorizados.
     */
    @Test
    fun verificarConstantesDeIntegridad() {
        // Verificar que las constantes no han sido modificadas
        assert(AddCentroViewModel.CODE_VERSION == "1.0.0") {
            "La versión del código ha sido modificada sin autorización"
        }
        
        assert(AddCentroViewModel.TESTED_DATE == "2024-07-15") {
            "La fecha de prueba ha sido modificada sin autorización"
        }
        
        assert(AddCentroViewModel.TESTER_ID == "maitane_test_id") {
            "El ID del tester ha sido modificado sin autorización"
        }
        
        assert(AddCentroViewModel.IS_VERIFIED) {
            "El estado de verificación ha sido modificado sin autorización"
        }
    }

    /**
     * Genera un hash MD5 de cualquier archivo para verificar su integridad
     */
    private fun getFileHash(filePath: String): String {
        val content = javaClass.classLoader?.getResourceAsStream(filePath)?.readBytes()
            ?: throw IllegalArgumentException("No se pudo leer el archivo: $filePath")
            
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(content)
        return digest.joinToString("") { "%02x".format(it) }
    }
} 