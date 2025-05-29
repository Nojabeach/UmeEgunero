package com.tfg.umeegunero.util

import com.tfg.umeegunero.BuildConfig

/**
 * Clase que proporciona acceso seguro a las configuraciones locales.
 * Las claves API y credenciales sensibles se leen desde BuildConfig,
 * que se genera a partir del archivo local.properties.
 * 
 * IMPORTANTE: Nunca hardcodees claves API directamente en el código.
 * Siempre usa este sistema para acceder a valores sensibles.
 */
object LocalConfig {
    
    // ImgBB
    val IMGBB_API_KEY: String
        get() = BuildConfig.IMGBB_API_KEY.ifEmpty { 
            throw IllegalStateException("IMGBB_API_KEY no configurada en local.properties")
        }
    
    // SendGrid
    val SENDGRID_API_KEY: String
        get() = BuildConfig.SENDGRID_API_KEY.ifEmpty {
            throw IllegalStateException("SENDGRID_API_KEY no configurada en local.properties")
        }
    
    val SENDGRID_FROM_EMAIL: String
        get() = BuildConfig.SENDGRID_FROM_EMAIL
    
    val SENDGRID_FROM_NAME: String
        get() = BuildConfig.SENDGRID_FROM_NAME
    
    // Google Maps
    val GOOGLE_MAPS_API_KEY: String
        get() = BuildConfig.GOOGLE_MAPS_API_KEY.ifEmpty {
            throw IllegalStateException("GOOGLE_MAPS_API_KEY no configurada en local.properties")
        }
    
    // Firebase
    val FIREBASE_API_KEY: String
        get() = BuildConfig.FIREBASE_API_KEY.ifEmpty {
            throw IllegalStateException("FIREBASE_API_KEY no configurada en local.properties")
        }
    
    val FIREBASE_APPLICATION_ID: String
        get() = BuildConfig.FIREBASE_APPLICATION_ID.ifEmpty {
            throw IllegalStateException("FIREBASE_APPLICATION_ID no configurada en local.properties")
        }
    
    val FIREBASE_PROJECT_ID: String
        get() = BuildConfig.FIREBASE_PROJECT_ID
    
    // DNI del administrador principal
    val ADMIN_PRINCIPAL_DNI: String
        get() = BuildConfig.ADMIN_PRINCIPAL_DNI
    
    // Remote Config
    val REMOTE_CONFIG_PASSWORD_KEY: String
        get() = BuildConfig.REMOTE_CONFIG_PASSWORD_KEY
    
    /**
     * Verifica que todas las claves necesarias estén configuradas.
     * Útil para verificar la configuración al inicio de la aplicación.
     * 
     * @return true si todas las claves están configuradas, false en caso contrario
     */
    fun verificarConfiguracion(): Boolean {
        return try {
            // Intentar acceder a todas las claves críticas
            IMGBB_API_KEY
            SENDGRID_API_KEY
            GOOGLE_MAPS_API_KEY
            FIREBASE_API_KEY
            FIREBASE_APPLICATION_ID
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Obtiene un resumen del estado de configuración para debugging.
     * No revela las claves reales, solo indica si están configuradas.
     */
    fun obtenerEstadoConfiguracion(): Map<String, Boolean> {
        return mapOf(
            "IMGBB_API_KEY" to BuildConfig.IMGBB_API_KEY.isNotEmpty(),
            "SENDGRID_API_KEY" to BuildConfig.SENDGRID_API_KEY.isNotEmpty(),
            "GOOGLE_MAPS_API_KEY" to BuildConfig.GOOGLE_MAPS_API_KEY.isNotEmpty(),
            "FIREBASE_API_KEY" to BuildConfig.FIREBASE_API_KEY.isNotEmpty(),
            "FIREBASE_APPLICATION_ID" to BuildConfig.FIREBASE_APPLICATION_ID.isNotEmpty()
        )
    }
} 