package com.tfg.umeegunero.util

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.repository.StorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de avatares predeterminados para usuarios de UmeEgunero.
 *
 * Esta clase se encarga de gestionar los avatares predeterminados para los diferentes tipos de usuarios,
 * incluyendo la verificación de su existencia en Firebase Storage y la subida desde recursos locales cuando
 * sea necesario.
 *
 * @author Maitane Ibañez Irazabal
 */
@Singleton
class DefaultAvatarsManager @Inject constructor(
    private val context: Context,
    private val storageRepository: StorageRepository
) {
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    // URLs para los avatares predeterminados
    private val defaultAvatarUrls = mapOf(
        TipoUsuario.ADMIN_CENTRO to "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/%40centro.png?alt=media&token=ac002e24-dbd1-41a5-8c26-4959c714c649",
        TipoUsuario.FAMILIAR to "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/%40familiar.png?alt=media&token=0d69c88f-4eb1-4e94-a20a-624d91c38379",
        TipoUsuario.PROFESOR to "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/%40profesor.png?alt=media&token=89b1bae9-dddc-476f-b6dc-184ec0b55eaf",
        TipoUsuario.ALUMNO to "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/%40alumno.png?alt=media&token=bb66f9aa-8c9c-4f1a-b262-c0fa8c285a0d",
        TipoUsuario.ADMIN_APP to "avatares/@adminavatar.png"
    )

    /**
     * Obtiene el nombre del recurso de imagen para un tipo de usuario
     */
    private fun getResourceNameForType(tipoUsuario: TipoUsuario): String {
        return when (tipoUsuario) {
            TipoUsuario.ADMIN_APP -> "AdminAvatar.png"
            TipoUsuario.ADMIN_CENTRO -> "centro.png"
            TipoUsuario.PROFESOR -> "profesor.png"
            TipoUsuario.FAMILIAR -> "familiar.png"
            TipoUsuario.ALUMNO -> "alumno.png"
            else -> "default.png"
        }
    }

    /**
     * Obtiene la URL del avatar predeterminado para un tipo de usuario específico.
     * Si el avatar no existe en Firebase Storage, lo sube desde los recursos locales.
     * 
     * @param tipoUsuario Tipo de usuario para el que se necesita el avatar
     * @return URL del avatar predeterminado o cadena vacía si ocurre un error
     */
    suspend fun obtenerAvatarPredeterminado(tipoUsuario: TipoUsuario): String = withContext(Dispatchers.IO) {
        try {
            // Primero intentamos recuperar la URL directamente desde el mapa de URLs predeterminadas
            val directUrl = defaultAvatarUrls[tipoUsuario]
            if (directUrl != null) {
                Timber.d("URL predeterminada disponible para $tipoUsuario: $directUrl")
                return@withContext directUrl
            }

            // Si no hay URL predeterminada, verificar si existe en Storage
            val resourceName = getResourceNameForType(tipoUsuario)
            val avatarFileName = "@" + resourceName.replaceFirst("@", "") // Asegura el prefijo @
            val avatarPath = "avatares/${avatarFileName.lowercase()}"
            val storageRef = storage.reference.child(avatarPath)
            
            try {
                // Intentar obtener URL si ya existe
                val url = storageRef.downloadUrl.await().toString()
                Timber.d("Avatar encontrado en Storage para $tipoUsuario: $url")
                return@withContext url
            } catch (e: Exception) {
                Timber.d("Avatar no encontrado en Storage para $tipoUsuario, intentando subir desde recursos")
                
                try {
                    // Avatar no encontrado en Storage, intentamos cargarlo desde recursos
                    val tempFile = crearArchivoTemporalDesdeRecursos(resourceName)
                    if (tempFile != null) {
                        val uri = Uri.fromFile(tempFile)
                        var resultUrl = ""
                        
                        // Asegurarse de que el nombre del archivo incluya el prefijo @
                        val nombreArchivoFinal = "@" + resourceName.lowercase().replace("@", "")
                        
                        storageRepository.subirArchivo(uri, "avatares", nombreArchivoFinal).collect { result ->
                            if (result is com.tfg.umeegunero.util.Result.Success) {
                                resultUrl = result.data as String
                                Timber.d("Avatar subido exitosamente a Storage: $resultUrl")
                            }
                        }
                        if (resultUrl.isNotEmpty()) {
                            return@withContext resultUrl
                        }
                    }
                } catch (uploadError: Exception) {
                    Timber.e(uploadError, "Error al subir avatar desde recursos: ${uploadError.message}")
                }
            }
            
            // Si todo lo anterior falla, devolver cadena vacía
            Timber.w("No se pudo obtener avatar para $tipoUsuario")
            return@withContext ""
        } catch (e: Exception) {
            Timber.e(e, "Error general al obtener avatar predeterminado: ${e.message}")
            return@withContext ""
        }
    }

    /**
     * Crea un archivo temporal a partir de un recurso en resources/images
     */
    private fun crearArchivoTemporalDesdeRecursos(resourceName: String): File? {
        try {
            // Archivo temporal que vamos a crear
            val tempFile = File(context.cacheDir, resourceName)
            
            // 1. Intentar cargar desde la ruta absoluta en resources/images
            val resourcesDir = "/Users/maitane/AndroidStudioProjects/UmeEgunero/app/src/main/resources/images"
            val resourceFile = File("$resourcesDir/$resourceName")
            
            if (resourceFile.exists()) {
                Timber.d("🔍 Encontrado archivo en ruta absoluta: ${resourceFile.absolutePath}")
                resourceFile.copyTo(tempFile, overwrite = true)
                return tempFile
            }
            
            // 2. Intentar desde assets/resources/images
            try {
                context.assets.open("resources/images/$resourceName").use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Timber.d("🔍 Archivo cargado desde assets/resources/images/$resourceName")
                return tempFile
            } catch (e: IOException) {
                // No se encontró en resources/images, continuar con otras rutas
                Timber.d("🔍 No se encontró en assets/resources/images/$resourceName: ${e.message}")
            }
            
            // 3. Intentar desde assets/images
                try {
                    context.assets.open("images/$resourceName").use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Timber.d("🔍 Archivo cargado desde assets/images/$resourceName")
                return tempFile
            } catch (e2: IOException) {
                // No se encontró en images, verificar si existe un fallback genérico
                Timber.d("🔍 No se encontró en assets/images/$resourceName: ${e2.message}")
                
                // 4. Intentar con la versión default o AdminAvatar.png como último recurso
                try {
                    context.assets.open("images/default.png").use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    Timber.d("🔍 Usando imagen default.png como fallback")
                    return tempFile
                } catch (e3: IOException) {
                    // Como último recurso, intentar con AdminAvatar.png
                    try {
                        context.assets.open("images/AdminAvatar.png").use { input ->
                            FileOutputStream(tempFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        Timber.d("🔍 Usando imagen AdminAvatar.png como fallback final")
                        return tempFile
                    } catch (e4: IOException) {
                        Timber.e(e4, "❌ Error: No se encontró ninguna imagen fallback")
                    return null
                    }
                }
            }
        } catch (e: IOException) {
            Timber.e(e, "❌ Error general al crear archivo temporal desde recursos: ${e.message}")
            return null
        }
    }

    /**
     * Verifica la existencia y sube todos los avatares predeterminados a Firebase Storage
     * @return Mapa con los tipos de usuario y las URLs correspondientes
     */
    suspend fun verificarYSubirTodosLosAvatares(): Map<TipoUsuario, String> = withContext(Dispatchers.IO) {
        val resultado = mutableMapOf<TipoUsuario, String>()
        
        TipoUsuario.values().forEach { tipo ->
            if (tipo != TipoUsuario.DESCONOCIDO) {
                val url = obtenerAvatarPredeterminado(tipo)
                if (url.isNotEmpty()) {
                    resultado[tipo] = url
                }
            }
        }
        
        return@withContext resultado
    }
} 