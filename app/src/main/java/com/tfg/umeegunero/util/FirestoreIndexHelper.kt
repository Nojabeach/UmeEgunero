package com.tfg.umeegunero.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestoreException
import timber.log.Timber

/**
 * Clase de utilidad para manejar errores relacionados con índices de Firestore
 */
object FirestoreIndexHelper {
    
    private const val REQUIRED_INDEX_ERROR = "FAILED_PRECONDITION: The query requires an index"
    private const val INDEX_URL_PREFIX = "https://console.firebase.google.com/"
    
    /**
     * Verifica si un error de Firestore está relacionado con índices faltantes
     * 
     * @param exception Excepción capturada desde Firestore
     * @return true si es un error de índice faltante
     */
    fun isIndexError(exception: Exception?): Boolean {
        if (exception !is FirebaseFirestoreException) return false
        return exception.message?.contains(REQUIRED_INDEX_ERROR) == true
    }
    
    /**
     * Extrae la URL del índice de Firestore desde el mensaje de error
     * 
     * @param exception Excepción de Firestore
     * @return URL para crear el índice o null si no se puede extraer
     */
    fun extractIndexUrl(exception: Exception?): String? {
        if (exception !is FirebaseFirestoreException) return null
        
        val message = exception.message ?: return null
        val urlStart = message.indexOf(INDEX_URL_PREFIX)
        if (urlStart < 0) return null
        
        // Extraer la URL hasta el final o hasta un espacio/nueva línea
        val urlEnd = message.indexOf('\n', urlStart).takeIf { it > 0 } 
            ?: message.indexOf(' ', urlStart).takeIf { it > 0 }
            ?: message.length
            
        return message.substring(urlStart, urlEnd)
    }
    
    /**
     * Abre la URL del índice en un navegador
     * 
     * @param context Contexto de la aplicación
     * @param indexUrl URL del índice
     */
    fun openIndexCreationUrl(context: Context, indexUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(indexUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Error al abrir URL de índice: $indexUrl")
        }
    }
    
    /**
     * Devuelve instrucciones para crear índices manualmente
     * 
     * @return Instrucciones detalladas
     */
    fun getIndexCreationInstructions(): String {
        return """
            Para optimizar las consultas en la aplicación, necesitas crear índices en Firestore:
            
            1. Accede a la consola de Firebase: https://console.firebase.google.com/
            2. Selecciona tu proyecto
            3. Ve a Firestore Database > Índices
            4. Crea los siguientes índices:
               
               • Para mensajes por destinatario:
                 Colección: unified_messages
                 Campos: receiverId (Ascendente), timestamp (Descendente)
               
               • Para mensajes por lista de destinatarios:
                 Colección: unified_messages
                 Campos: receiversIds (Array contains), timestamp (Descendente)
               
               • Para mensajes filtrados por tipo:
                 Colección: unified_messages
                 Campos: type (Ascendente), timestamp (Descendente)
               
               • Para mensajes de conversación:
                 Colección: unified_messages
                 Campos: conversationId (Ascendente), timestamp (Ascendente)
                 
            También puedes encontrar un archivo de configuración de índices en:
            app/src/main/assets/firestore/firestore.indexes.json
        """.trimIndent()
    }
} 