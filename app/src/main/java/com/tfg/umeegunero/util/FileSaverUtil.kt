package com.tfg.umeegunero.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton
import java.io.IOException

/**
 * Utilidad para guardar archivos de texto en el almacenamiento del dispositivo.
 *
 * Prioriza el uso de MediaStore para > Android Q para una mejor gestión del almacenamiento
 * y compatibilidad con Scoped Storage. Para versiones anteriores, usa el almacenamiento externo público.
 */
@Singleton
class FileSaverUtil @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Guarda un contenido de texto en un archivo en el directorio de Descargas.
     *
     * @param fileName Nombre del archivo (ej. "mi_informe.txt").
     * @param content Contenido de texto a guardar.
     * @return Result<String> con la ruta o mensaje de éxito, o Result.Error con la excepción.
     */
    fun saveTextToFile(fileName: String, content: String): Result<String> {
        var outputStream: OutputStream? = null
        var uri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Usar MediaStore para Android 10+
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri == null) {
                    Timber.e("Error al crear el archivo con MediaStore: URI nulo")
                    return Result.Error(IOException("No se pudo crear el archivo en Descargas."))
                }
                outputStream = resolver.openOutputStream(uri)

            } else {
                // Método antiguo para < Android 10 (requiere permiso WRITE_EXTERNAL_STORAGE)
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val file = java.io.File(downloadsDir, fileName)
                outputStream = java.io.FileOutputStream(file)
                // Para versiones antiguas, podríamos devolver la ruta absoluta si es útil
                // uri = Uri.fromFile(file) // Opcional
            }

            if (outputStream == null) {
                Timber.e("Error al abrir OutputStream")
                return Result.Error(IOException("No se pudo abrir el flujo de salida para guardar el archivo."))
            }

            outputStream.bufferedWriter().use {
                it.write(content)
            }
            Timber.i("Archivo '$fileName' guardado correctamente.")
            // Devolvemos el nombre como indicador de éxito, ya que la URI/ruta exacta puede variar
            return Result.Success(fileName)

        } catch (e: Exception) {
            Timber.e(e, "Error al guardar el archivo '$fileName'")
            // Intentar limpiar si se creó la URI pero falló la escritura
            if (uri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    context.contentResolver.delete(uri, null, null)
                } catch (deleteEx: Exception) {
                    Timber.w(deleteEx, "Error al intentar eliminar archivo parcial tras fallo de escritura.")
                }
            }
            return Result.Error(e)
        } finally {
            try {
                outputStream?.close()
            } catch (e: IOException) {
                Timber.e(e, "Error al cerrar outputStream")
            }
        }
    }
} 