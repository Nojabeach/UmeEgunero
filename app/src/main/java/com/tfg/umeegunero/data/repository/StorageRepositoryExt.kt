package com.tfg.umeegunero.data.repository

import android.net.Uri
import com.tfg.umeegunero.network.ImgBBClient
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.flow.first

/**
 * Función de extensión del StorageRepository para usar ImgBB como alternativa
 * cuando se acerca al límite gratuito de Firebase Storage.
 * 
 * Este enfoque permite gestionar automáticamente entre servicios de almacenamiento
 * de imágenes según las necesidades y restricciones de la aplicación.
 * 
 * @param uri URI de la imagen a subir
 * @return URL de la imagen subida
 */
suspend fun StorageRepository.subirImagenImgBB(uri: Uri): String {
    // Verificar si estamos cerca del límite gratuito de Firebase Storage
    if (estaEnLimiteStorage()) {
        return imgBBClient.subirImagen(uri)
    }
    
    // Si no, usar Firebase Storage normalmente
    val result = subirArchivo(uri, "imagenes").first()
    return when (result) {
        is Result.Success -> result.data
        else -> ""
    }
} 