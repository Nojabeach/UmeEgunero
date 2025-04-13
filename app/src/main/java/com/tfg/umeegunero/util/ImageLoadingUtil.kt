package com.tfg.umeegunero.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilidad para optimizar la carga de imágenes y recursos
 */
@Singleton
class ImageLoadingUtil @Inject constructor(
    private val context: Context
) {
    // Directorio de caché para imágenes
    private val imageCacheDir = File(context.cacheDir, "image_cache").apply {
        if (!exists()) mkdirs()
    }
    
    // Tamaño máximo de caché en bytes (50MB)
    private val MAX_CACHE_SIZE = 50 * 1024 * 1024L
    
    /**
     * Carga una imagen desde una URL con soporte para caché
     * 
     * @param url URL de la imagen
     * @param width Ancho deseado de la imagen (opcional)
     * @param height Alto deseado de la imagen (opcional)
     * @return Bitmap de la imagen o null si hay error
     */
    suspend fun loadImageFromUrl(
        url: String,
        width: Int? = null,
        height: Int? = null
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                // Verificar si la imagen está en caché
                val cacheFile = getCacheFile(url)
                if (cacheFile.exists()) {
                    Timber.d("Cargando imagen desde caché: $url")
                    return@withContext BitmapFactory.decodeFile(cacheFile.absolutePath)
                }
                
                // Si no está en caché, descargar
                Timber.d("Descargando imagen: $url")
                val imageLoader = ImageLoader.Builder(context)
                    .crossfade(true)
                    .build()
                
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .size(if (width != null && height != null) Size(width, height) else Size.ORIGINAL)
                    .crossfade(true)
                    .build()
                
                val result = imageLoader.execute(request)
                
                if (result.drawable is BitmapDrawable) {
                    val bitmap = (result.drawable as BitmapDrawable).bitmap
                    
                    // Guardar en caché
                    saveToCache(url, bitmap)
                    
                    // Limpiar caché si es necesario
                    cleanCacheIfNeeded()
                    
                    bitmap
                } else {
                    null
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar imagen: $url")
                null
            }
        }
    }
    
    /**
     * Obtiene el archivo de caché para una URL
     */
    private fun getCacheFile(url: String): File {
        val filename = generateCacheFilename(url)
        return File(imageCacheDir, filename)
    }
    
    /**
     * Guarda un bitmap en caché
     */
    private fun saveToCache(url: String, bitmap: Bitmap) {
        try {
            val cacheFile = getCacheFile(url)
            FileOutputStream(cacheFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar imagen en caché: $url")
        }
    }
    
    /**
     * Limpia la caché si excede el tamaño máximo
     */
    private fun cleanCacheIfNeeded() {
        try {
            var totalSize = 0L
            val files = imageCacheDir.listFiles() ?: return
            
            // Calcular tamaño total
            for (file in files) {
                totalSize += file.length()
            }
            
            // Si excede el máximo, eliminar archivos más antiguos
            if (totalSize > MAX_CACHE_SIZE) {
                val filesToDelete = (totalSize - MAX_CACHE_SIZE) / (totalSize / files.size)
                
                // Ordenar por fecha de modificación (más antiguos primero)
                val sortedFiles = files.sortedBy { it.lastModified() }
                
                // Eliminar archivos más antiguos
                for (i in 0 until filesToDelete.toInt()) {
                    if (i < sortedFiles.size) {
                        sortedFiles[i].delete()
                    }
                }
                
                Timber.d("Caché limpiada: eliminados $filesToDelete archivos")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al limpiar caché de imágenes")
        }
    }
    
    /**
     * Genera un nombre de archivo único para la caché
     */
    private fun generateCacheFilename(url: String): String {
        val md = MessageDigest.getInstance("MD5")
        val hash = md.digest(url.toByteArray())
        return hash.joinToString("") { "%02x".format(it) } + ".jpg"
    }
    
    /**
     * Limpia toda la caché de imágenes
     */
    fun clearImageCache() {
        try {
            val files = imageCacheDir.listFiles() ?: return
            for (file in files) {
                file.delete()
            }
            Timber.d("Caché de imágenes limpiada completamente")
        } catch (e: Exception) {
            Timber.e(e, "Error al limpiar caché de imágenes")
        }
    }
}

/**
 * Composable para cargar imágenes de forma eficiente
 * 
 * @param url URL de la imagen
 * @param contentDescription Descripción de la imagen para accesibilidad
 * @param modifier Modificador para personalizar la apariencia
 */
@Composable
fun OptimizedAsyncImage(
    url: String,
    contentDescription: String?,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var imageLoader by remember { mutableStateOf<ImageLoader?>(null) }
    
    LaunchedEffect(context, lifecycleOwner) {
        imageLoader = ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        onLoading = {
            // Mostrar placeholder mientras carga
        },
        onError = {
            Timber.e("Error al cargar imagen: $url")
        }
    )
} 