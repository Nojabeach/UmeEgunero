package com.tfg.umeegunero.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestoreException
import timber.log.Timber

/**
 * Utilidad para manejar errores de índices en Firestore
 * 
 * Esta clase proporciona métodos para:
 * - Detectar errores relacionados con índices faltantes en Firestore
 * - Extraer URLs para crear índices
 * - Mostrar diálogos de error con opciones para crear índices
 * 
 * @since 1.0.0
 */
object FirestoreIndexErrorHandler {

    /**
     * Comprueba si el error es debido a un índice faltante en Firestore
     * 
     * @param error Excepción a comprobar
     * @return true si es un error de índice faltante, false en caso contrario
     */
    fun isIndexError(error: Exception): Boolean {
        return error.message?.contains("FAILED_PRECONDITION") == true && 
               error.message?.contains("requires an index") == true
    }
    
    /**
     * Extrae la URL para crear el índice desde el mensaje de error
     * 
     * @param error Excepción que contiene la URL del índice
     * @return URL para crear el índice o null si no se encuentra
     */
    fun extractIndexUrl(error: Exception): String? {
        val regex = "https://console\\.firebase\\.google\\.com[^\\s]+".toRegex()
        val matchResult = regex.find(error.message ?: "")
        return matchResult?.value
    }
    
    /**
     * Registra en el log el error de índice faltante
     * 
     * @param error Excepción original
     * @param tag Etiqueta para el log (opcional)
     */
    fun logIndexError(error: Exception, tag: String = "FirestoreIndex") {
        val indexUrl = extractIndexUrl(error)
        Timber.e(error, "Error de índice en Firestore. URL para crear índice: $indexUrl")
    }
    
    /**
     * Muestra un diálogo para informar al usuario sobre el error de índice y permitirle crear el índice
     * 
     * @param context Contexto de la aplicación
     * @param error Excepción original
     * @param onDismiss Acción a ejecutar cuando se cierra el diálogo
     */
    fun showIndexErrorDialog(context: Context, error: Exception, onDismiss: () -> Unit = {}) {
        val indexUrl = extractIndexUrl(error)
        
        if (indexUrl != null) {
            AlertDialog.Builder(context)
                .setTitle("Actualización necesaria")
                .setMessage("Se necesita crear un índice en la base de datos para que esta funcionalidad opere correctamente. ¿Desea abrir el enlace para crear el índice?")
                .setPositiveButton("Crear índice") { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(indexUrl))
                    context.startActivity(intent)
                }
                .setNegativeButton("Cancelar", null)
                .setOnDismissListener { onDismiss() }
                .show()
        } else {
            AlertDialog.Builder(context)
                .setTitle("Error de índice")
                .setMessage("Se ha producido un error con la base de datos. Por favor, contacte con soporte técnico.")
                .setPositiveButton("Aceptar", null)
                .setOnDismissListener { onDismiss() }
                .show()
        }
    }
} 