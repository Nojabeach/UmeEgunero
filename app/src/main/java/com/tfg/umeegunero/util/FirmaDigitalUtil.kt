package com.tfg.umeegunero.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.Base64
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.UUID

/**
 * Utilidad para manejar firmas digitales en la aplicación
 *
 * Esta clase proporciona funciones para:
 * - Convertir datos de firma manuscrita a formato digital
 * - Generar representaciones Base64 de firmas
 * - Almacenar y recuperar firmas de Firebase Storage
 * - Verificar la autenticidad de firmas
 */
object FirmaDigitalUtil {

    private const val STORAGE_PATH = "firmas_digitales"
    private val storage = FirebaseStorage.getInstance()
    private val firmasRef = storage.reference.child(STORAGE_PATH)

    /**
     * Representa un punto de la firma
     */
    data class PuntoFirma(val x: Float, val y: Float, val presion: Float = 1.0f)

    /**
     * Convierte una lista de puntos de firma en una imagen Bitmap
     *
     * @param puntos Lista de puntos que conforman la firma
     * @param ancho Ancho de la imagen resultante
     * @param alto Alto de la imagen resultante
     * @return Bitmap de la firma
     */
    fun crearBitmapDeFirma(puntos: List<PuntoFirma>, ancho: Int = 500, alto: Int = 200): Bitmap {
        val bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Fondo blanco
        canvas.drawColor(Color.WHITE)
        
        // Configurar trazo
        val paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 5f
            isAntiAlias = true
        }
        
        // Dibujar la firma
        if (puntos.isNotEmpty()) {
            val path = Path()
            path.moveTo(puntos[0].x, puntos[0].y)
            
            for (i in 1 until puntos.size) {
                // Ajustar el grosor según la presión
                paint.strokeWidth = 2f + (puntos[i].presion * 3f)
                path.lineTo(puntos[i].x, puntos[i].y)
            }
            
            canvas.drawPath(path, paint)
        }
        
        return bitmap
    }

    /**
     * Convierte un Bitmap de firma a una cadena Base64
     *
     * @param bitmap Bitmap de la firma
     * @return Representación Base64 de la firma
     */
    fun bitmapABase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    /**
     * Convierte una cadena Base64 a un Bitmap de firma
     *
     * @param base64 Representación Base64 de la firma
     * @return Bitmap de la firma
     */
    fun base64ABitmap(base64: String): Bitmap {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    /**
     * Guarda la firma en Firebase Storage
     *
     * @param bitmap Bitmap de la firma
     * @param usuarioId ID del usuario que firma
     * @param documentoId ID del documento firmado
     * @return URL de la firma almacenada o null si ocurrió un error
     */
    suspend fun guardarFirmaEnStorage(
        bitmap: Bitmap,
        usuarioId: String,
        documentoId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val nombreArchivo = "firma_${usuarioId}_${documentoId}_${UUID.randomUUID()}.png"
            val referencia = firmasRef.child(nombreArchivo)
            
            // Convertir bitmap a bytes
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val datos = stream.toByteArray()
            
            // Subir a Firebase Storage
            val task = referencia.putBytes(datos).await()
            
            // Obtener la URL de descarga
            return@withContext referencia.downloadUrl.await().toString()
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar firma en Storage")
            return@withContext null
        }
    }

    /**
     * Genera un hash de la firma para verificar su autenticidad
     *
     * @param base64 Representación Base64 de la firma
     * @param usuarioId ID del usuario que firma
     * @param documentoId ID del documento firmado
     * @param timestamp Marca de tiempo de la firma
     * @return Hash SHA-256 de la firma
     */
    fun generarHashFirma(
        base64: String,
        usuarioId: String,
        documentoId: String,
        timestamp: Long
    ): String {
        val datosAHashear = "$base64|$usuarioId|$documentoId|$timestamp"
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(datosAHashear.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifica la autenticidad de una firma
     *
     * @param firmaHash Hash almacenado de la firma
     * @param base64 Representación Base64 de la firma
     * @param usuarioId ID del usuario que firma
     * @param documentoId ID del documento firmado
     * @param timestamp Marca de tiempo de la firma
     * @return true si la firma es auténtica, false en caso contrario
     */
    fun verificarFirma(
        firmaHash: String,
        base64: String,
        usuarioId: String,
        documentoId: String,
        timestamp: Long
    ): Boolean {
        val hashCalculado = generarHashFirma(base64, usuarioId, documentoId, timestamp)
        return hashCalculado == firmaHash
    }

    /**
     * Guarda temporalmente la firma en el almacenamiento interno
     *
     * @param context Contexto de la aplicación
     * @param bitmap Bitmap de la firma
     * @return Archivo de la firma o null si ocurrió un error
     */
    fun guardarFirmaTemporal(context: Context, bitmap: Bitmap): File? {
        try {
            val nombreArchivo = "firma_temp_${UUID.randomUUID()}.png"
            val archivo = File(context.cacheDir, nombreArchivo)
            
            FileOutputStream(archivo).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            
            return archivo
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar firma temporal: ${e.message}")
            return null
        }
    }
    
    /**
     * Borra una firma del almacenamiento
     *
     * @param firmaUrl URL de la firma a borrar
     * @return true si se borró correctamente, false en caso contrario
     */
    suspend fun borrarFirma(firmaUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val referencia = storage.getReferenceFromUrl(firmaUrl)
            referencia.delete().await()
            return@withContext true
        } catch (e: Exception) {
            Timber.e(e, "Error al borrar firma: ${e.message}")
            return@withContext false
        }
    }
} 