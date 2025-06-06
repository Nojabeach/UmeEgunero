package com.tfg.umeegunero.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import timber.log.Timber

/**
 * Constantes globales utilizadas en toda la aplicación.
 * 
 * Esta clase define valores constantes centralizados para evitar
 * la duplicación y facilitar el mantenimiento. Incluye configuraciones
 * para servicios de correo electrónico, endpoints de API, y constantes
 * de presentación.
 * 
 * Nota: Por seguridad, los valores sensibles como tokens y claves
 * se almacenan en el AndroidManifest.xml como meta-data o se generan
 * dinámicamente a través de SecureTokenManager.
 */
object Constants {
    /**
     * Dirección de correo electrónico para soporte técnico.
     * Utilizada como destinatario en los mensajes de soporte.
     */
    const val EMAIL_SOPORTE = "umeegunero@gmail.com"
    
    /**
     * Dirección de correo electrónico predeterminada para notificaciones.
     * Utilizada como dirección de destino en casos donde no se proporciona una específica.
     */
    const val EMAIL_DESTINATARIO = "umeegunero@gmail.com"
    
    /**
     * URL del endpoint de Google Apps Script que procesa y envía emails.
     * Este script gestiona las plantillas de correo y el envío a través
     * de Gmail API.
     * 
     * Nota: Esta URL se obtiene de local.properties a través de LocalConfig.
     */
    val EMAIL_SCRIPT_URL: String
        get() = try {
            // Obtener la URL desde LocalConfig (que usa BuildConfig)
            LocalConfig.EMAIL_SCRIPT_URL
        } catch (e: Exception) {
            // Si hay algún error, usar URL por defecto (que no funcionará pero evita crashes)
            Timber.e(e, "Error al acceder a EMAIL_SCRIPT_URL desde LocalConfig")
            "https://script.google.com/macros/s/YOUR-SCRIPT-ID-HERE/exec"
        }
    
    /**
     * Identificador único de la aplicación.
     * Utilizado para autenticación y seguridad.
     */
    const val APP_ID = "com.tfg.umeegunero"
    
    /**
     * Lista de temas predefinidos disponibles en el formulario de soporte técnico.
     * Ofrece categorías comunes para facilitar la clasificación de las consultas.
     */
    val TEMAS_SOPORTE = listOf(
        "Problema técnico",
        "Pregunta sobre funcionalidad",
        "Sugerencia de mejora",
        "Reporte de error",
        "Otro"
    )
    
    /**
     * Clase para gestionar constantes de seguridad.
     * 
     * Contiene métodos para acceder de forma segura a valores sensibles
     * almacenados en el manifest como meta-data.
     */
    object Security {
        /**
         * Obtiene una clave del AndroidManifest.xml de forma segura.
         * Permite almacenar claves sensibles en el manifest en lugar de en código.
         *
         * @param context Contexto de la aplicación
         * @param key Nombre de la clave en meta-data
         * @param defaultValue Valor por defecto si la clave no existe
         * @return Valor asociado a la clave
         */
        fun getMetadataString(context: Context, key: String, defaultValue: String = ""): String {
            return try {
                val appInfo = context.packageManager.getApplicationInfo(
                    context.packageName, 
                    PackageManager.GET_META_DATA
                )
                val bundle: Bundle = appInfo.metaData ?: Bundle()
                bundle.getString(key) ?: defaultValue
            } catch (e: Exception) {
                Timber.e(e, "Error al leer metadata: $key")
                defaultValue
            }
        }
        
        /**
         * Genera una máscara para ocultar información sensible en logs.
         * Útil para depuración sin exponer datos sensibles.
         * 
         * @param input String a enmascarar
         * @param visibleChars Número de caracteres visibles al inicio y final
         * @return String enmascarado (ej: "abcdef" -> "ab***f")
         */
        fun mask(input: String, visibleChars: Int = 2): String {
            if (input.length <= visibleChars * 2) return "*".repeat(input.length)
            val start = input.take(visibleChars)
            val end = input.takeLast(visibleChars)
            return "$start${"*".repeat(input.length - visibleChars * 2)}$end"
        }
    }
} 