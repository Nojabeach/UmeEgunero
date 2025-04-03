package com.tfg.umeegunero.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.tfg.umeegunero.data.model.InfoArchivo
import com.tfg.umeegunero.data.model.Resultado
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para la gestión de archivos
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
     * @return Flow con el resultado de la operación, incluyendo la URL del archivo subido
     */
    fun subirArchivo(uri: Uri, ruta: String, nombreArchivo: String? = null): Flow<Resultado<String>> = flow {
        emit(Resultado.Cargando())
        
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
            
            emit(Resultado.Exito(downloadUrl))
        } catch (e: Exception) {
            Timber.e(e, "Error al subir archivo")
            emit(Resultado.Error("Error al subir archivo: ${e.message}"))
        }
    }.catch { e ->
        Timber.e(e, "Exception en subirArchivo")
        emit(Resultado.Error("Error al subir archivo: ${e.message}"))
    }.flowOn(Dispatchers.IO)
    
    /**
     * Sube un archivo a Firebase Storage
     * @param inputStream Stream del archivo a subir
     * @param ruta Ruta en Firebase Storage donde se almacenará el archivo
     * @param nombreArchivo Nombre que tendrá el archivo en el servidor
     * @param tipoMime Tipo MIME del archivo
     * @return Flow con el resultado de la operación, incluyendo la URL del archivo subido
     */
    fun subirArchivo(
        inputStream: InputStream,
        ruta: String,
        nombreArchivo: String,
        tipoMime: String
    ): Flow<Resultado<String>> = flow {
        emit(Resultado.Cargando())
        
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
            
            emit(Resultado.Exito(downloadUrl))
        } catch (e: Exception) {
            Timber.e(e, "Error al subir archivo desde stream")
            emit(Resultado.Error("Error al subir archivo: ${e.message}"))
        } finally {
            try {
                inputStream.close()
            } catch (e: Exception) {
                Timber.e(e, "Error al cerrar input stream")
            }
        }
    }.catch { e ->
        Timber.e(e, "Exception en subirArchivo(InputStream)")
        emit(Resultado.Error("Error al subir archivo: ${e.message}"))
    }.flowOn(Dispatchers.IO)
    
    /**
     * Elimina un archivo de Firebase Storage
     * @param url URL completa del archivo a eliminar
     * @return Flow con el resultado de la operación
     */
    fun eliminarArchivo(url: String): Flow<Resultado<Boolean>> = flow {
        emit(Resultado.Cargando())
        
        try {
            // Convertir URL a gsReference si es necesario
            val fileRef = obtenerReferenciaDesdeUrl(url)
            
            // Eliminar archivo
            fileRef.delete().await()
            
            emit(Resultado.Exito(true))
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar archivo")
            emit(Resultado.Error("Error al eliminar archivo: ${e.message}"))
        }
    }.catch { e ->
        Timber.e(e, "Exception en eliminarArchivo")
        emit(Resultado.Error("Error al eliminar archivo: ${e.message}"))
    }.flowOn(Dispatchers.IO)
    
    /**
     * Descarga un archivo de Firebase Storage al almacenamiento local
     * @param url URL completa del archivo a descargar
     * @param nombreArchivo Nombre que tendrá el archivo descargado localmente
     * @return Flow con el resultado de la operación, incluyendo el File descargado
     */
    fun descargarArchivo(url: String, nombreArchivo: String): Flow<Resultado<File>> = flow {
        emit(Resultado.Cargando())
        
        try {
            // Convertir URL a gsReference si es necesario
            val fileRef = obtenerReferenciaDesdeUrl(url)
            
            // Preparar archivo local
            val localFile = File(context.cacheDir, nombreArchivo)
            
            // Descargar archivo
            fileRef.getFile(localFile).await()
            
            emit(Resultado.Exito(localFile))
        } catch (e: Exception) {
            Timber.e(e, "Error al descargar archivo")
            emit(Resultado.Error("Error al descargar archivo: ${e.message}"))
        }
    }.catch { e ->
        Timber.e(e, "Exception en descargarArchivo")
        emit(Resultado.Error("Error al descargar archivo: ${e.message}"))
    }.flowOn(Dispatchers.IO)
    
    /**
     * Obtiene información sobre un archivo en Firebase Storage
     * @param url URL completa del archivo
     * @return Flow con el resultado de la operación, incluyendo la información del archivo
     */
    fun obtenerInfoArchivo(url: String): Flow<Resultado<InfoArchivo>> = flow {
        emit(Resultado.Cargando())
        
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
            
            emit(Resultado.Exito(infoArchivo))
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener información del archivo")
            emit(Resultado.Error("Error al obtener información del archivo: ${e.message}"))
        }
    }.catch { e ->
        Timber.e(e, "Exception en obtenerInfoArchivo")
        emit(Resultado.Error("Error al obtener información del archivo: ${e.message}"))
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
} 