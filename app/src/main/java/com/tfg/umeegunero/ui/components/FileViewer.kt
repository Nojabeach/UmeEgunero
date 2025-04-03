package com.tfg.umeegunero.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.tfg.umeegunero.data.repository.StorageRepository
import timber.log.Timber
import java.io.File

/**
 * Componente para visualizar archivos
 *
 * @param url URL del archivo a visualizar
 * @param nombre Nombre del archivo
 * @param tipo Tipo MIME del archivo
 * @param archivo Archivo local (si está disponible)
 * @param onDescargar Callback cuando se solicita descargar un archivo
 * @param isDescargando Si está descargando muestra un indicador
 * @param modifier Modifier para personalizar el componente
 */
@Composable
fun VisorArchivo(
    url: String,
    nombre: String,
    tipo: String,
    archivo: File? = null,
    onDescargar: () -> Unit,
    isDescargando: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var errorAlAbrirArchivo by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = nombre,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            when {
                isDescargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                errorAlAbrirArchivo -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No se puede previsualizar este archivo",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                archivo != null -> {
                    when {
                        tipo.startsWith("image/") -> {
                            AsyncImage(
                                model = archivo,
                                contentDescription = "Imagen",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        tipo.startsWith("text/") || tipo == "application/json" -> {
                            // Implementación simple para archivos de texto
                            val texto = try {
                                archivo.readText()
                            } catch (e: Exception) {
                                "Error al leer el archivo: ${e.message}"
                            }
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = texto,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        else -> {
                            // Para otros tipos de archivos, mostrar un icono y opciones para abrir
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = getIconForFileType(tipo),
                                        contentDescription = "Icono de archivo",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Text(
                                        text = "Este tipo de archivo no se puede previsualizar directamente",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Button(
                                        onClick = {
                                            try {
                                                val uri = FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.provider",
                                                    archivo
                                                )
                                                
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(uri, tipo)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                
                                                startActivity(context, intent, null)
                                            } catch (e: ActivityNotFoundException) {
                                                errorAlAbrirArchivo = true
                                                Timber.e(e, "No hay aplicación para abrir este tipo de archivo")
                                            } catch (e: Exception) {
                                                errorAlAbrirArchivo = true
                                                Timber.e(e, "Error al abrir archivo")
                                            }
                                        }
                                    ) {
                                        Text("Abrir con otra aplicación")
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    // Si no está descargado
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Descargar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Descarga el archivo para previsualizarlo",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedButton(onClick = onDescargar) {
                                Text("Descargar archivo")
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (archivo != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        try {
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                archivo
                            )
                            
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = tipo
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            
                            val shareIntent = Intent.createChooser(intent, "Compartir archivo")
                            startActivity(context, shareIntent, null)
                        } catch (e: Exception) {
                            Timber.e(e, "Error al compartir archivo")
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartir"
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = {
                        try {
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                archivo
                            )
                            
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, tipo)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            
                            startActivity(context, intent, null)
                        } catch (e: ActivityNotFoundException) {
                            errorAlAbrirArchivo = true
                            Timber.e(e, "No hay aplicación para abrir este tipo de archivo")
                        } catch (e: Exception) {
                            errorAlAbrirArchivo = true
                            Timber.e(e, "Error al abrir archivo")
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = "Abrir con otra aplicación"
                    )
                }
            }
        }
    }
}

/**
 * Componente simplificado para mostrar una vista previa de archivo pequeña
 */
@Composable
fun VistaPreviaArchivoSimple(
    nombre: String,
    tipo: String,
    uriArchivo: Uri? = null,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(120.dp)
            .height(120.dp)
            .padding(4.dp)
            .fillMaxWidth(),
        onClick = onTap
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (tipo.startsWith("image/") && uriArchivo != null) {
                AsyncImage(
                    model = uriArchivo,
                    contentDescription = "Vista previa",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth()
                )
            } else {
                Icon(
                    imageVector = getIconForFileType(tipo),
                    contentDescription = "Icono de archivo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(60.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = nombre,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
} 