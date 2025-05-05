package com.tfg.umeegunero.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.tfg.umeegunero.data.model.InfoArchivo
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar el almacenamiento de archivos en la aplicación UmeEgunero.
 *
 * Esta clase proporciona métodos para subir, descargar, eliminar y gestionar
 * archivos multimedia y documentos asociados a diferentes entidades como
 * tareas, actividades, mensajes y perfiles de usuario.
 *
 * Características principales:
 * - Subida de archivos a Firebase Storage
 * - Generación de URLs de descarga
 * - Gestión de permisos de archivos
 * - Compresión y optimización de imágenes
 * - Manejo de diferentes tipos de archivos (imágenes, documentos, etc.)
 *
 * El repositorio se encarga de:
 * - Almacenar archivos de manera segura
 * - Generar nombres únicos para archivos
 * - Manejar límites de tamaño y tipos de archivos
 * - Proporcionar métodos de acceso a archivos
 *
 * @property storage Instancia de FirebaseStorage para operaciones de almacenamiento
 * @property firestore Instancia de FirebaseFirestore para referencias de documentos
 * @property authRepository Repositorio de autenticación para identificar al usuario actual
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
@Singleton
class StorageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: FirebaseStorage
) {
    private val storageRef: StorageReference = storage.reference
    
    /**
     * Sube un archivo a Firebase Storage
     * @param uri URI del archivo local a subir
     * @param ruta Ruta en Firebase Storage donde se almacenará el archivo
     * @param nombreArchivo Nombre que tendrá el archivo en el servidor (opcional)
     * @return Flow<Result<String>> con la URL pública del archivo subido
     */
    fun subirArchivo(uri: Uri, ruta: String, nombreArchivo: String? = null): Flow<Result<String>> = flow {
        emit(Result.Loading())
        
        try {
            // Obtener el tipo MIME del archivo
            val tipoMime = context.contentResolver.getType(uri) ?: ""
            
            // Generar un nombre si no se proporciona
            val nombre = nombreArchivo ?: generarNombreArchivo(uri)
            
            // Crear referencia al archivo en Storage
            val fileRef = storageRef.child("$ruta/$nombre")
            
            // Configurar metadatos
            val metadata = StorageMetadata.Builder()
                .setContentType(tipoMime)
                .build()
            
            // Subir archivo
            fileRef.putFile(uri, metadata).await()
            
            // Obtener URL de descarga
            val downloadUrl = fileRef.downloadUrl.await().toString()
            
            emit(Result.Success(downloadUrl))
        } catch (e: Exception) {
            Timber.e(e, "Error al subir archivo")
            emit(Result.Error(e))
        }
    }.catch { e ->
        Timber.e(e, "Exception en subirArchivo")
        emit(Result.Error(e))
    }.flowOn(Dispatchers.IO)
    
    /**
     * Sube un archivo a Firebase Storage
     * @param inputStream Stream del archivo a subir
     * @param ruta Ruta en Firebase Storage donde se almacenará el archivo
     * @param nombreArchivo Nombre que tendrá el archivo en el servidor
     * @param tipoMime Tipo MIME del archivo
     * @return Flow<Result<String>> con la URL pública del archivo subido
     */
    fun subirArchivo(
        inputStream: InputStream,
        ruta: String,
        nombreArchivo: String,
        tipoMime: String
    ): Flow<Result<String>> = flow {
        emit(Result.Loading())
        
        try {
            // Crear referencia al archivo en Storage
            val fileRef = storageRef.child("$ruta/$nombreArchivo")
            
            // Configurar metadatos
            val metadata = StorageMetadata.Builder()
                .setContentType(tipoMime)
                .build()
            
            // Subir archivo
            val bytes = inputStream.readBytes()
            fileRef.putBytes(bytes, metadata).await()
            
            // Obtener URL de descarga
            val downloadUrl = fileRef.downloadUrl.await().toString()
            
            emit(Result.Success(downloadUrl))
        } catch (e: Exception) {
            Timber.e(e, "Error al subir archivo desde stream")
            emit(Result.Error(e))
        } finally {
            try {
                inputStream.close()
            } catch (e: Exception) {
                Timber.e(e, "Error al cerrar input stream")
            }
        }
    }.catch { e ->
        Timber.e(e, "Exception en subirArchivo(InputStream)")
        emit(Result.Error(e))
    }.flowOn(Dispatchers.IO)
    
    /**
     * Elimina un archivo de Firebase Storage
     * @param url URL completa del archivo a eliminar
     * @return Flow<Result<Unit>> con el resultado de la operación
     */
    fun eliminarArchivo(url: String): Flow<Result<Unit>> = flow {
        emit(Result.Loading())
        
        try {
            // Convertir URL a gsReference si es necesario
            val fileRef = obtenerReferenciaDesdeUrl(url)
            
            // Eliminar archivo
            fileRef.delete().await()
            
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar archivo")
            emit(Result.Error(e))
        }
    }.catch { e ->
        Timber.e(e, "Exception en eliminarArchivo")
        emit(Result.Error(e))
    }.flowOn(Dispatchers.IO)
    
    /**
     * Descarga un archivo de Firebase Storage al almacenamiento local
     * @param url URL completa del archivo a descargar
     * @param nombreArchivo Nombre que tendrá el archivo descargado localmente
     * @return Flow<Result<File>> con el archivo local descargado
     */
    fun descargarArchivo(url: String, nombreArchivo: String): Flow<Result<File>> = flow {
        emit(Result.Loading())
        
        try {
            // Convertir URL a gsReference si es necesario
            val fileRef = obtenerReferenciaDesdeUrl(url)
            
            // Preparar archivo local
            val localFile = File(context.cacheDir, nombreArchivo)
            
            // Descargar archivo
            fileRef.getBytes(10L * 1024 * 1024).await()
            
            // Guardar en archivo local
            FileOutputStream(localFile).use { outputStream ->
                outputStream.write(fileRef.getBytes(10L * 1024 * 1024).await())
            }
            
            emit(Result.Success(localFile))
        } catch (e: Exception) {
            Timber.e(e, "Error al descargar archivo")
            emit(Result.Error(e))
        }
    }.catch { e ->
        Timber.e(e, "Exception en descargarArchivo")
        emit(Result.Error(e))
    }.flowOn(Dispatchers.IO)
    
    /**
     * Obtiene información sobre un archivo en Firebase Storage
     * @param url URL completa del archivo
     * @return Flow<Result<InfoArchivo>> con la información del archivo
     */
    fun obtenerInfoArchivo(url: String): Flow<Result<InfoArchivo>> = flow {
        emit(Result.Loading())
        
        try {
            // Convertir URL a gsReference si es necesario
            val fileRef = obtenerReferenciaDesdeUrl(url)
            
            // Obtener metadatos
            val metadata = fileRef.metadata.await()
            
            // Crear objeto InfoArchivo
            val infoArchivo = InfoArchivo(
                nombre = fileRef.name,
                tamaño = metadata.sizeBytes,
                tipo = metadata.contentType,
                fechaCreacion = metadata.creationTimeMillis,
                fechaModificacion = metadata.updatedTimeMillis,
                metadatos = metadata.customMetadataKeys.associateWith { key -> 
                    metadata.getCustomMetadata(key) ?: "" 
                }
            )
            
            emit(Result.Success(infoArchivo))
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener información del archivo")
            emit(Result.Error(e))
        }
    }.catch { e ->
        Timber.e(e, "Exception en obtenerInfoArchivo")
        emit(Result.Error(e))
    }.flowOn(Dispatchers.IO)
    
    /**
     * Crea un archivo temporal en el directorio de caché
     * @return Archivo temporal
     */
    fun crearArchivoTemporal(extension: String): File {
        val nombreArchivo = "temp_${UUID.randomUUID()}.$extension"
        return File(context.cacheDir, nombreArchivo)
    }
    
    /**
     * Obtiene URI de contenido para un archivo
     */
    fun obtenerUriContenido(archivo: File): Uri {
        return Uri.fromFile(archivo)
    }
    
    /**
     * Genera un nombre único para un archivo
     */
    private fun generarNombreArchivo(uri: Uri): String {
        val nombreOriginal = uri.lastPathSegment ?: "archivo"
        val extension = obtenerExtension(uri)
        val timestamp = System.currentTimeMillis()
        
        return if (extension.isNotEmpty()) {
            "${nombreOriginal.substringBeforeLast(".")}_$timestamp.$extension"
        } else {
            "${nombreOriginal}_$timestamp"
        }
    }
    
    /**
     * Obtiene la extensión de un archivo a partir de su URI
     */
    private fun obtenerExtension(uri: Uri): String {
        // Intenta obtener la extensión del MIME type
        val tipoMime = context.contentResolver.getType(uri)
        if (tipoMime != null) {
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(tipoMime)
            if (!extension.isNullOrBlank()) {
                return extension
            }
        }
        
        // Si no se puede obtener del MIME type, intenta obtenerla del path
        val path = uri.path
        if (path != null) {
            val punto = path.lastIndexOf('.')
            if (punto > 0 && punto < path.length - 1) {
                return path.substring(punto + 1)
            }
        }
        
        return ""
    }
    
    /**
     * Obtiene una referencia de Storage a partir de una URL
     */
    private fun obtenerReferenciaDesdeUrl(url: String): StorageReference {
        return try {
            // Si es una URL de descarga normal (https://firebasestorage.googleapis.com/...)
            storage.getReferenceFromUrl(url)
        } catch (e: Exception) {
            // Si es una ruta relativa, asumimos que es relativa a la raíz
            if (!url.startsWith("http") && !url.startsWith("gs://")) {
                storageRef.child(url)
            } else {
                // Si falló y no es una ruta relativa, propagamos el error
                throw e
            }
        }
    }
    
    /**
     * Obtiene la URL de descarga directa de un archivo
     * @param url URL o ruta del archivo
     * @return URL de descarga pública del archivo
     */
    suspend fun obtenerUrlDescarga(url: String): String {
        return try {
            val fileRef = obtenerReferenciaDesdeUrl(url)
            fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener URL de descarga")
            throw e
        }
    }
} 