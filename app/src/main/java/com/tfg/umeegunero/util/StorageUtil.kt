package com.tfg.umeegunero.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlinx.coroutines.suspendCancellableCoroutine
import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Clase de utilidad para operaciones relacionadas con Firebase Storage
 */
object StorageUtil {

    /**
     * Sube una imagen desde los recursos al Firebase Storage
     *
     * @param context Contexto Android para acceder a los recursos
     * @param resourceId ID del recurso en R.drawable
     * @param storagePath Ruta en Firebase Storage donde se guardará la imagen
     * @param fileName Nombre del archivo en Firebase Storage
     * @return URL de descarga de la imagen o null si falla la operación
     */
    suspend fun uploadImageFromResource(
        context: Context,
        resourceId: Int,
        storagePath: String,
        fileName: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            Timber.d("Iniciando subida de imagen desde recurso: $resourceId a $storagePath/$fileName")
            
            // Cargar la imagen desde los recursos
            val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
            if (bitmap == null) {
                Timber.e("No se pudo decodificar el recurso $resourceId")
                return@withContext null
            }
            
            // Convertir el bitmap a bytes
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val imageData = baos.toByteArray()
            Timber.d("Imagen convertida a ${imageData.size} bytes")
            
            // Subir los bytes a Firebase Storage
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("$storagePath/$fileName")
            
            // Subir la imagen
            Timber.d("Subiendo imagen a Firebase Storage...")
            imageRef.putBytes(imageData).awaitTaskCompletionWithResult()
            
            // Obtener la URL de descarga
            val downloadUrl = imageRef.downloadUrl.awaitTaskResult()
            Timber.d("✅ Imagen subida exitosamente: $downloadUrl")
            return@withContext downloadUrl.toString()
        } catch (e: IOException) {
            Timber.e(e, "❌ Error IO al subir imagen: ${e.message}")
            return@withContext null
        } catch (e: Exception) {
            Timber.e(e, "❌ Error general al subir imagen: ${e.message}")
            return@withContext null
        }
    }
    
    /**
     * Verifica si un archivo existe en Firebase Storage
     *
     * @param storagePath Ruta completa del archivo en Firebase Storage
     * @return true si el archivo existe, false si no
     */
    suspend fun fileExists(storagePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Timber.d("Verificando si el archivo existe: $storagePath")
            val storageRef = FirebaseStorage.getInstance().reference.child(storagePath)
            // Intentar obtener metadatos del archivo
            storageRef.metadata.awaitTaskCompletion()
            Timber.d("✅ Archivo existe en storage: $storagePath")
            return@withContext true
        } catch (e: Exception) {
            Timber.d("❌ Archivo no existe en storage: $storagePath - ${e.message}")
            return@withContext false
        }
    }
    
    /**
     * Obtiene la URL de descarga de un archivo en Firebase Storage
     *
     * @param storagePath Ruta completa del archivo en Firebase Storage
     * @return URL de descarga o null si no se puede obtener
     */
    suspend fun getDownloadUrl(storagePath: String): String? = withContext(Dispatchers.IO) {
        try {
            Timber.d("Obteniendo URL de descarga para: $storagePath")
            val storageRef = FirebaseStorage.getInstance().reference.child(storagePath)
            val downloadUrl = storageRef.downloadUrl.awaitTaskResult()
            Timber.d("✅ URL de descarga obtenida: $downloadUrl")
            return@withContext downloadUrl.toString()
        } catch (e: Exception) {
            Timber.e(e, "❌ Error al obtener URL de descarga para: $storagePath")
            return@withContext null
        }
    }
    
    /**
     * Extensión que convierte una Task<Void> de Firebase en una suspending function
     */
    private suspend fun <T> Task<T>.awaitTaskCompletion() = suspendCancellableCoroutine<Unit> { continuation ->
        addOnSuccessListener {
            if (!continuation.isCompleted) continuation.resume(Unit)
        }
        addOnFailureListener { exception ->
            if (!continuation.isCompleted) continuation.resumeWithException(exception)
        }
    }
    
    /**
     * Extensión que convierte una Task<T> de Firebase en una suspending function
     */
    private suspend fun <T> Task<T>.awaitTaskResult(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (!continuation.isCompleted) continuation.resume(result)
        }
        addOnFailureListener { exception ->
            if (!continuation.isCompleted) continuation.resumeWithException(exception)
        }
    }
    
    /**
     * Extensión que convierte una Task<Any> de Firebase en una suspending function que devuelve el resultado
     */
    private suspend fun <T> Task<T>.awaitTaskCompletionWithResult(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (!continuation.isCompleted) continuation.resume(result)
        }
        addOnFailureListener { exception ->
            if (!continuation.isCompleted) continuation.resumeWithException(exception)
        }
    }
} 