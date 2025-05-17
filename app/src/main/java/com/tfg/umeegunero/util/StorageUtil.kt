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
import com.google.android.gms.tasks.Tasks

/**
 * Clase de utilidad para operaciones relacionadas con Firebase Storage
 */
object StorageUtil {

    private const val STORAGE_BUCKET = "umeegunero.firebasestorage.app"
    private val storage = FirebaseStorage.getInstance()

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
            Timber.d("Subiendo imagen desde recurso con ID: $resourceId a storage/$storagePath/$fileName")
            
            // Comprobamos que el recurso existe
            if (resourceId == 0) {
                Timber.e("El recurso no existe (resourceId = 0)")
                return@withContext null
            }

            // Referencia al archivo en Firebase Storage
            val storageRef = storage.reference
            val fileRef = storageRef.child("$storagePath/$fileName")
            
            Timber.d("Referencia a Firebase Storage: gs://$STORAGE_BUCKET/$storagePath/$fileName")

            // Cargar el recurso a un bitmap
            val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
            if (bitmap == null) {
                Timber.e("No se pudo cargar el bitmap desde el recurso: $resourceId")
                return@withContext null
            }
            
            Timber.d("Bitmap cargado correctamente: ${bitmap.width}x${bitmap.height}")

            // Convertir el bitmap a bytes
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val data = baos.toByteArray()

            // Subir el archivo
            Timber.d("Iniciando subida a Firebase Storage...")
            try {
                val uploadTask = fileRef.putBytes(data)
                val taskSnapshot = Tasks.await(uploadTask)
                val downloadUrl = Tasks.await(taskSnapshot.storage.downloadUrl)
                
                Timber.d("Imagen subida con éxito. URL: $downloadUrl")
                return@withContext downloadUrl.toString()
            } catch (e: Exception) {
                Timber.e(e, "Error al subir archivo a Firebase Storage: ${e.message}")
                return@withContext null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error en uploadImageFromResource: ${e.message}")
            return@withContext null
        }
    }

    /**
     * Verifica si un archivo existe en Firebase Storage
     *
     * @param path Ruta del archivo en Storage
     * @return true si el archivo existe, false en caso contrario
     */
    suspend fun fileExists(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Timber.d("Comprobando si existe el archivo en Storage: $path")
            val storageRef = storage.reference.child(path)
            
            try {
                val metadata = Tasks.await(storageRef.metadata)
                Timber.d("Archivo encontrado: $path, tamaño: ${metadata.sizeBytes} bytes")
                return@withContext true
            } catch (e: Exception) {
                Timber.d("Archivo no encontrado en Storage: $path")
                return@withContext false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al comprobar si existe el archivo: ${e.message}")
            return@withContext false
        }
    }

    /**
     * Obtiene la URL de descarga de un archivo en Firebase Storage
     *
     * @param path Ruta del archivo en Storage
     * @return URL de descarga o null si no se encuentra
     */
    suspend fun getDownloadUrl(path: String): String? = withContext(Dispatchers.IO) {
        try {
            Timber.d("Obteniendo URL de descarga para: $path")
            val storageRef = storage.reference.child(path)
            
            try {
                val uri = Tasks.await(storageRef.downloadUrl)
                Timber.d("URL de descarga obtenida: $uri")
                return@withContext uri.toString()
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener URL de descarga: ${e.message}")
                return@withContext null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error en getDownloadUrl: ${e.message}")
            return@withContext null
        }
    }
} 