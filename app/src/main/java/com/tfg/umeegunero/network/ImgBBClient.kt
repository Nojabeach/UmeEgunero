package com.tfg.umeegunero.network

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cliente para la API de ImgBB que permite subir imágenes como alternativa a Firebase Storage
 * cuando se acerca al límite gratuito.
 */
@Singleton
class ImgBBClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // API Key de ImgBB
    private val API_KEY = "e64eb6fb0a5d6e7e001723871feb10a0"
    private val API_URL = "https://api.imgbb.com/1/upload"
    
    // Cliente HTTP para realizar las solicitudes
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * Sube una imagen a ImgBB
     * @param uri URI de la imagen a subir
     * @return URL de la imagen subida
     */
    suspend fun subirImagen(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            // Convertir Uri a archivo temporal
            val tempFile = createTempFileFromUri(uri)
            
            // Crear multipart request
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image", 
                    tempFile.name,
                    tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                )
                .addFormDataPart("key", API_KEY)
                .build()
            
            // Crear solicitud
            val request = Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .build()
            
            // Ejecutar solicitud
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Error al subir imagen: ${response.code}")
                }
                
                // Parsear respuesta JSON
                val responseBody = response.body?.string() ?: throw Exception("Respuesta vacía")
                val jsonResponse = JSONObject(responseBody)
                
                // Eliminar archivo temporal
                tempFile.delete()
                
                if (jsonResponse.getBoolean("success")) {
                    val data = jsonResponse.getJSONObject("data")
                    return@withContext data.getString("url")
                } else {
                    throw Exception("Error en la respuesta de ImgBB")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al subir imagen a ImgBB")
            throw e
        }
    }
    
    /**
     * Crea un archivo temporal a partir de un Uri
     */
    private suspend fun createTempFileFromUri(uri: Uri): File = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("No se pudo abrir el archivo")
        
        // Crear archivo temporal
        val tempFile = File.createTempFile("imgbb_upload_", ".jpg", context.cacheDir)
        
        // Copiar contenido
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.use {
                it.copyTo(outputStream)
            }
        }
        
        return@withContext tempFile
    }
} 