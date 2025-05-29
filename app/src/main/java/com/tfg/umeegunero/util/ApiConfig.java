package com.tfg.umeegunero.util;

/**
 * Configuración de API para servicios externos.
 * IMPORTANTE: Este archivo contiene versiones de ejemplo de las claves.
 * Debe ser reemplazado localmente con las claves reales y no debe subirse a repositorios públicos.
 * 
 * Instrucciones para desarrolladores:
 * 1. Copia este archivo y renómbralo a ApiConfig.java
 * 2. Reemplaza las claves de ejemplo con tus claves reales
 * 3. Asegúrate de que el archivo esté en .gitignore
 */
public class ApiConfig {
    // SendGrid
    public static final String SENDGRID_API_KEY = getSendGridApiKey();
    
    // URL del script de Google Apps Script que procesa y envía emails
    public static final String EMAIL_SCRIPT_URL = getEmailScriptUrl();
    
    // Firebase (mantener este valor vacío, usar google-services.json para Firebase)
    public static final String FIREBASE_API_KEY = "";
    
    // Otras APIs
    public static final String OTHER_API_KEY = "";
    
    /**
     * Intenta obtener la clave de API de SendGrid desde la configuración local.
     * Si no existe, devuelve un valor de placeholder.
     */
    private static String getSendGridApiKey() {
        try {
            // Intentar cargar la clase ApiConfigLocal (si existe)
            Class<?> localConfigClass = Class.forName("com.tfg.umeegunero.util.ApiConfigLocal");
            // Obtener el campo SENDGRID_API_KEY
            return (String) localConfigClass.getField("SENDGRID_API_KEY").get(null);
        } catch (Exception e) {
            // Si no existe, devolver un placeholder
            return "YOUR_SENDGRID_API_KEY";
        }
    }
    
    /**
     * Intenta obtener la URL del script de email desde la configuración local.
     * Si no existe, devuelve un valor de placeholder.
     */
    private static String getEmailScriptUrl() {
        try {
            // Intentar cargar la clase ApiConfigLocal (si existe)
            Class<?> localConfigClass = Class.forName("com.tfg.umeegunero.util.ApiConfigLocal");
            // Obtener el campo EMAIL_SCRIPT_URL
            return (String) localConfigClass.getField("EMAIL_SCRIPT_URL").get(null);
        } catch (Exception e) {
            // Si no existe, devolver un placeholder
            return "https://script.google.com/macros/s/YOUR-SCRIPT-ID-HERE/exec";
        }
    }
} 