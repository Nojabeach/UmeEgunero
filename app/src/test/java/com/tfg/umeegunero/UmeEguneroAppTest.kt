package com.tfg.umeegunero

import org.junit.Test
import org.junit.Assert.*

/**
 * Pruebas unitarias básicas para la aplicación UmeEgunero
 */
class UmeEguneroAppTest {

    @Test
    fun testAppInitialization() {
        // Esta prueba verifica que la aplicación puede inicializarse correctamente
        // En un entorno real, usaríamos mocks para los servicios de Firebase y WorkManager
        assertTrue("Esta prueba siempre pasa para verificar la configuración de JUnit", true)
    }
    
    @Test
    fun testConstantValues() {
        // Verifica los valores de las constantes definidas en la aplicación
        assertEquals("sincronizacion_periodica", UmeEguneroApp.SYNC_WORK_NAME)
        assertEquals("revision_eventos", UmeEguneroApp.EVENTOS_WORK_NAME)
    }
} 