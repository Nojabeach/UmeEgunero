package com.tfg.umeegunero.feature.common.files.composable

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.MimeTypes
import timber.log.Timber
import java.io.File

/**
 * Estado para la UI del visor de documentos
 */
data class DocumentoUiState(
    val url: String = "",
    val nombre: String? = null,
    val tipoMime: String? = null,
    val isLoading: Boolean = false,
    val isDescargando: Boolean = false,
    val error: String? = null,
    val archivoLocal: File? = null,
    val infoAdicional: Map<String, String> = emptyMap()
)

/**
 * Componente para visualizar diferentes tipos de archivos
 */
@Composable
fun VisorArchivo(
    uiState: DocumentoUiState,
    onDescargar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Información básica del archivo
        InfoArchivo(
            nombre = uiState.nombre,
            onDescargar = onDescargar,
            descargando = uiState.isDescargando
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Visualizador según tipo de archivo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.LightGray.copy(alpha = 0.2f))
        ) {
            when {
                uiState.url.isEmpty() -> {
                    MensajeError(mensaje = "No se ha especificado una URL válida")
                }
                uiState.isDescargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    val tipoMime = uiState.tipoMime
                    val archivoLocal = uiState.archivoLocal
                    
                    when {
                        // Imagen
                        tipoMime?.startsWith("image/") == true || 
                        uiState.url.endsWith(".jpg", ignoreCase = true) ||
                        uiState.url.endsWith(".jpeg", ignoreCase = true) ||
                        uiState.url.endsWith(".png", ignoreCase = true) ||
                        uiState.url.endsWith(".gif", ignoreCase = true) ||
                        uiState.url.endsWith(".webp", ignoreCase = true) -> {
                            VisualizadorImagen(url = uiState.url)
                        }
                        
                        // PDF
                        tipoMime == "application/pdf" || uiState.url.endsWith(".pdf", ignoreCase = true) -> {
                            if (archivoLocal != null) {
                                VisualizadorPdf(archivo = archivoLocal)
                            } else {
                                BotonDescarga(onDescargar = onDescargar)
                            }
                        }
                        
                        // Video
                        tipoMime?.startsWith("video/") == true ||
                        uiState.url.endsWith(".mp4", ignoreCase = true) ||
                        uiState.url.endsWith(".avi", ignoreCase = true) ||
                        uiState.url.endsWith(".mov", ignoreCase = true) ||
                        uiState.url.endsWith(".mkv", ignoreCase = true) -> {
                            VisualizadorVideo(url = uiState.url)
                        }
                        
                        // Audio
                        tipoMime?.startsWith("audio/") == true ||
                        uiState.url.endsWith(".mp3", ignoreCase = true) ||
                        uiState.url.endsWith(".wav", ignoreCase = true) ||
                        uiState.url.endsWith(".ogg", ignoreCase = true) -> {
                            VisualizadorAudio(url = uiState.url)
                        }
                        
                        // Documentos de texto/word
                        tipoMime == "application/msword" ||
                        tipoMime == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
                        uiState.url.endsWith(".doc", ignoreCase = true) ||
                        uiState.url.endsWith(".docx", ignoreCase = true) ||
                        uiState.url.endsWith(".txt", ignoreCase = true) -> {
                            if (archivoLocal != null) {
                                VisualizadorDocumento(archivo = archivoLocal)
                            } else {
                                BotonDescarga(onDescargar = onDescargar)
                            }
                        }
                        
                        // Por defecto, mostramos ícono de archivo y ofrecemos descargar
                        else -> {
                            VisualizadorGenerico(
                                nombre = uiState.nombre ?: "Archivo",
                                onDescargar = onDescargar
                            )
                        }
                    }
                }
            }
        }
        
        // Información adicional del archivo si está disponible
        if (uiState.infoAdicional.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            InfoAdicionalArchivo(infoAdicional = uiState.infoAdicional)
        }
    }
}

@Composable
private fun InfoArchivo(
    nombre: String?,
    onDescargar: () -> Unit,
    descargando: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = nombre ?: "Archivo",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(
                onClick = onDescargar,
                enabled = !descargando
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Descargar archivo"
                )
            }
        }
    }
}

@Composable
private fun VisualizadorImagen(url: String) {
    AsyncImage(
        model = url,
        contentDescription = "Imagen",
        modifier = Modifier.fillMaxSize(),
        onError = {
            Timber.e("Error al cargar imagen: $url")
        }
    )
}

@Composable
private fun VisualizadorPdf(archivo: File) {
    // En una aplicación real, se usaría una biblioteca como AndroidPdfViewer
    // Aquí simulamos un visor simple
    Text(
        text = "Visor PDF - ${archivo.name}",
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun VisualizadorVideo(url: String) {
    val context = LocalContext.current
    
    // Creación del ExoPlayer (en una aplicación real, esto debería estar en un ViewModel)
    val exoPlayer = ExoPlayer.Builder(context).build().apply {
        setMediaItem(MediaItem.fromUri(url))
        prepare()
        playWhenReady = true
        repeatMode = Player.REPEAT_MODE_ONE
    }
    
    // Visualizador de video usando ExoPlayer
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        StyledPlayerView(context).apply {
            player = exoPlayer
            useController = true
            this.layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }
}

@Composable
private fun VisualizadorAudio(url: String) {
    val context = LocalContext.current
    
    // Creación del ExoPlayer para audio
    val exoPlayer = ExoPlayer.Builder(context).build().apply {
        setMediaItem(MediaItem.fromUri(url))
        prepare()
        playWhenReady = true
    }
    
    // Visualizador de audio
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.FilePresent,
                contentDescription = "Archivo de audio",
                modifier = Modifier.size(100.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Controles de audio
            StyledPlayerView(context).apply {
                player = exoPlayer
                useController = true
                controllerShowTimeoutMs = 0 // Siempre visible
                // No usar showBuffering que es privado
            }
        }
    }
}

@Composable
private fun VisualizadorDocumento(archivo: File) {
    // En una aplicación real, se usaría una biblioteca específica para documentos
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.FilePresent,
            contentDescription = "Documento",
            modifier = Modifier.size(100.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Documento: ${archivo.name}",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun VisualizadorGenerico(
    nombre: String,
    onDescargar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.FilePresent,
            contentDescription = "Archivo",
            modifier = Modifier.size(100.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Tipo de archivo no soportado para previsualización",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = nombre,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedButton(onClick = onDescargar) {
            Text(text = "Descargar para ver")
        }
    }
}

@Composable
private fun BotonDescarga(onDescargar: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        OutlinedButton(onClick = onDescargar) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Descargar"
            )
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Text(text = "Descargar para visualizar")
        }
    }
}

@Composable
private fun MensajeError(mensaje: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InfoAdicionalArchivo(infoAdicional: Map<String, String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "Información del archivo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        infoAdicional.forEach { (clave, valor) ->
            Text(
                text = "$clave: $valor",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 