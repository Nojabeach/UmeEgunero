package com.tfg.umeegunero.util;

import com.tfg.umeegunero.BuildConfig;
import timber.log.Timber;

/**
 * Configuración de API para servicios externos.
 * IMPORTANTE: Este archivo sirve como capa de abstracción para acceder a claves API.
 * Las claves reales se obtienen desde BuildConfig (que las lee de local.properties).
 * 
 * Las claves API nunca deben estar hardcodeadas en el código.
 */
public class ApiConfig {
    // SendGrid
    public static final String SENDGRID_API_KEY = getSendGridApiKey();
    
    // URL del script de Google Apps Script que procesa y envía emails
    public static final String EMAIL_SCRIPT_URL = getEmailScriptUrl();
    
    // Firebase (se obtiene de BuildConfig)
    public static final String FIREBASE_API_KEY = getFirebaseApiKey();
    
    // Otras APIs
    public static final String OTHER_API_KEY = "";
    
    /**
     * Obtiene la clave de API de SendGrid.
     * Primero intenta usar BuildConfig, luego ApiConfigLocal como fallback.
     */
    private static String getSendGridApiKey() {
        // Prioridad 1: BuildConfig (recomendado)
        if (BuildConfig.SENDGRID_API_KEY != null && !BuildConfig.SENDGRID_API_KEY.isEmpty()) {
            return BuildConfig.SENDGRID_API_KEY;
        }
        
        // Prioridad 2: ApiConfigLocal (fallback para compatibilidad)
        try {
            Class<?> localConfigClass = Class.forName("com.tfg.umeegunero.util.ApiConfigLocal");
            return (String) localConfigClass.getField("SENDGRID_API_KEY").get(null);
        } catch (Exception e) {
            // Si no existe o hay error, devolver valor vacío
            return "";
        }
    }
    
    /**
     * Obtiene la URL del script de email.
     * Primero intenta usar BuildConfig, luego ApiConfigLocal como fallback.
     */
    private static String getEmailScriptUrl() {
        // Prioridad 1: BuildConfig (recomendado)
        if (BuildConfig.EMAIL_SCRIPT_URL != null && !BuildConfig.EMAIL_SCRIPT_URL.isEmpty()) {
            return BuildConfig.EMAIL_SCRIPT_URL;
        }
        
        // Prioridad 2: ApiConfigLocal (fallback para compatibilidad)
        try {
            Class<?> localConfigClass = Class.forName("com.tfg.umeegunero.util.ApiConfigLocal");
            String url = (String) localConfigClass.getField("EMAIL_SCRIPT_URL").get(null);
            if (url != null && !url.isEmpty()) {
                return url;
            }
        } catch (Exception e) {
            // Ignorar errores
        }
        
        // Valor por defecto (placeholder que no funcionará)
            return "https://script.google.com/macros/s/YOUR-SCRIPT-ID-HERE/exec";
        }
    
    /**
     * Obtiene la clave de API de Firebase.
     * Utiliza BuildConfig que lee desde local.properties.
     */
    private static String getFirebaseApiKey() {
        if (BuildConfig.FIREBASE_API_KEY != null && !BuildConfig.FIREBASE_API_KEY.isEmpty()) {
            return BuildConfig.FIREBASE_API_KEY;
        }
        return "";
    }
} 