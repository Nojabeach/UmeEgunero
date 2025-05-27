package com.tfg.umeegunero.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Utilidad para exportar informes como documentos PDF
 */
class PdfExporter(private val context: Context) {

    /**
     * Crea un archivo PDF a partir de texto plano
     * 
     * @param contenido Texto a convertir a PDF
     * @param fileName Nombre del archivo PDF a generar
     * @param title Título opcional para el documento
     * @return true si la operación fue exitosa, false en caso contrario
     */
    suspend fun createPdfFromText(contenido: String, fileName: String, title: String = ""): Boolean = withContext(Dispatchers.IO) {
        try {
            // Crear documento PDF
            val document = PdfDocument()
            
            // Dividir el contenido en líneas
            val lines = contenido.split("\n")
            
            // Configurar página
            val pageWidth = 595 // A4 width in points (72 dpi)
            val pageHeight = 842 // A4 height in points
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            
            // Crear primera página
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            
            // Configurar estilos de texto
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 18f
                isFakeBoldText = true
            }
            
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
            }
            
            val headingPaint = Paint().apply {
                color = Color.BLACK
                textSize = 14f
                isFakeBoldText = true
            }
            
            // Márgenes
            val marginLeft = 50f
            val marginTop = 50f
            val lineHeight = 18f // Altura de línea para texto normal
            
            // Dibujar título si se proporciona
            var yPosition = marginTop
            
            if (title.isNotEmpty()) {
                canvas.drawText(title, marginLeft, yPosition, titlePaint)
                yPosition += 30f // Espacio después del título
            }
            
            // Procesar líneas
            var currentPage = 1
            var lineCount = 0
            
            for (line in lines) {
                // Determinar el estilo de texto basado en el contenido
                val paintToUse = when {
                    line.startsWith("=") -> headingPaint
                    line.startsWith("#") || line.trim().endsWith(":") -> headingPaint
                    else -> textPaint
                }
                
                // Comprobar si necesitamos una nueva página
                if (yPosition > pageHeight - marginTop) {
                    // Finalizar página actual
                    document.finishPage(page)
                    
                    // Crear nueva página
                    currentPage++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = marginTop
                }
                
                // Dibujar línea de texto
                canvas.drawText(line, marginLeft, yPosition, paintToUse)
                yPosition += lineHeight
                lineCount++
            }
            
            // Finalizar última página
            document.finishPage(page)
            
            // Guardar documento
            var success = false
            var outputStream: OutputStream? = null
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Usar MediaStore para Android 10 y superior
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    
                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        outputStream = resolver.openOutputStream(uri)
                        document.writeTo(outputStream)
                        success = true
                        Timber.d("PDF guardado en MediaStore: $uri")
                    }
                } else {
                    // Método tradicional para Android 9 y anteriores
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    if (!downloadsDir.exists()) {
                        downloadsDir.mkdirs()
                    }
                    
                    val file = File(downloadsDir, fileName)
                    outputStream = FileOutputStream(file)
                    document.writeTo(outputStream)
                    success = true
                    Timber.d("PDF guardado en la carpeta de descargas: ${file.absolutePath}")
                }
            } finally {
                outputStream?.close()
                document.close()
            }
            
            success
        } catch (e: IOException) {
            Timber.e(e, "Error al crear el archivo PDF")
            false
        } catch (e: Exception) {
            Timber.e(e, "Error general al crear PDF")
            false
        }
    }
} 