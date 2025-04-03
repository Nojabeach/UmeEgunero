package com.tfg.umeegunero.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.tfg.umeegunero.data.repository.StorageRepository
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Componente para seleccionar archivos y mostrarlos
 * 
 * @param archivosSeleccionados Lista de archivos seleccionados
 * @param onArchivosSeleccionados Callback cuando se seleccionan archivos
 * @param maxArchivos Número máximo de archivos permitidos (por defecto 5)
 * @param tiposPermitidos Tipos MIME permitidos (por defecto todos)
 * @param modifier Modifier para personalizar el componente
 * @param isLoading Si está cargando no permite seleccionar más archivos
 * @param titulo Título opcional a mostrar
 */
@Composable
fun SelectorArchivos(
    archivosSeleccionados: List<ArchivoLocal>,
    onArchivosSeleccionados: (List<ArchivoLocal>) -> Unit,
    maxArchivos: Int = 5,
    tiposPermitidos: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    titulo: String? = null
) {
    val context = LocalContext.current
    
    // Lanzador para seleccionar múltiples archivos
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val nuevosArchivos = uris.map { uri ->
                val nombreArchivo = getFileNameFromUri(context, uri) ?: "archivo_${System.currentTimeMillis()}"
                ArchivoLocal(
                    uri = uri,
                    nombre = nombreArchivo,
                    tipo = context.contentResolver.getType(uri) ?: "application/octet-stream"
                )
            }
            
            // Combinamos los archivos existentes con los nuevos, hasta el límite
            val archivosActualizados = (archivosSeleccionados + nuevosArchivos)
                .distinctBy { it.uri }
                .take(maxArchivos)
            
            onArchivosSeleccionados(archivosActualizados)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        if (titulo != null) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Archivos adjuntos (${archivosSeleccionados.size}/$maxArchivos)",
                style = MaterialTheme.typography.bodyMedium
            )
            
            OutlinedButton(
                onClick = {
                    val mimeTypes = if (tiposPermitidos.isEmpty()) {
                        "*/*"
                    } else {
                        tiposPermitidos.firstOrNull() ?: "*/*"
                    }
                    filePickerLauncher.launch(mimeTypes)
                },
                enabled = archivosSeleccionados.size < maxArchivos && !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Adjuntar archivo"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Añadir archivo")
            }
        }
        
        AnimatedVisibility(
            visible = archivosSeleccionados.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                items(archivosSeleccionados) { archivo ->
                    TarjetaArchivoLocal(
                        archivo = archivo,
                        onEliminar = {
                            onArchivosSeleccionados(archivosSeleccionados.filter { it != archivo })
                        },
                        isLoading = isLoading
                    )
                }
            }
        }
        
        if (archivosSeleccionados.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        val mimeTypes = if (tiposPermitidos.isEmpty()) {
                            "*/*"
                        } else {
                            tiposPermitidos.firstOrNull() ?: "*/*"
                        }
                        filePickerLauncher.launch(mimeTypes)
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir archivo",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pulsa para seleccionar archivos",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        if (isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cargando archivos...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Componente para mostrar un archivo local como una tarjeta
 */
@Composable
fun TarjetaArchivoLocal(
    archivo: ArchivoLocal,
    onEliminar: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(end = 8.dp)
            .width(150.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (archivo.tipo.startsWith("image/")) {
                    AsyncImage(
                        model = archivo.uri,
                        contentDescription = "Vista previa",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Icon(
                        imageVector = getIconForFileType(archivo.tipo),
                        contentDescription = "Icono de archivo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = archivo.nombre,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            IconButton(
                onClick = onEliminar,
                enabled = !isLoading,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar archivo",
                    tint = if (isLoading) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                          else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Componente para mostrar una lista de archivos remotos
 * 
 * @param archivos Lista de archivos remotos
 * @param onDescargar Callback cuando se solicita descargar un archivo
 * @param onEliminar Callback cuando se solicita eliminar un archivo (opcional)
 * @param isLoading Si está cargando muestra un indicador
 * @param titulo Título opcional a mostrar
 */
@Composable
fun ListaArchivosRemotos(
    archivos: List<ArchivoRemoto>,
    onDescargar: (ArchivoRemoto) -> Unit,
    onEliminar: ((ArchivoRemoto) -> Unit)? = null,
    isLoading: Boolean = false,
    titulo: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        if (titulo != null) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (archivos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay archivos disponibles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                items(archivos) { archivo ->
                    TarjetaArchivoRemoto(
                        archivo = archivo,
                        onDescargar = { onDescargar(archivo) },
                        onEliminar = if (onEliminar != null) {
                            { onEliminar(archivo) }
                        } else null
                    )
                }
            }
        }
    }
}

/**
 * Componente para mostrar un archivo remoto como una tarjeta
 */
@Composable
fun TarjetaArchivoRemoto(
    archivo: ArchivoRemoto,
    onDescargar: () -> Unit,
    onEliminar: (() -> Unit)? = null,
    isDescargando: Boolean = false,
    progresoDescarga: Float = 0f,
    modifier: Modifier = Modifier
) {
    var expandirOpciones by remember { mutableStateOf(false) }
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .padding(end = 8.dp)
            .width(150.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { expandirOpciones = !expandirOpciones }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (archivo.tipo?.startsWith("image/") == true && archivo.hayVistaPreviaCacheada) {
                    AsyncImage(
                        model = archivo.urlVistaPreviaCacheada,
                        contentDescription = "Vista previa",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Icon(
                        imageVector = getIconForFileType(archivo.tipo ?: "application/octet-stream"),
                        contentDescription = "Icono de archivo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = archivo.nombre,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Muestra la fecha en formato local
            if (archivo.fecha != null) {
                val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                Text(
                    text = formato.format(archivo.fecha),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = onDescargar,
                    enabled = !isDescargando,
                    modifier = Modifier.size(24.dp)
                ) {
                    if (isDescargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "Descargar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                if (onEliminar != null) {
                    IconButton(
                        onClick = { mostrarConfirmacionEliminar = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Indicador de progreso si está descargando
            if (isDescargando) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progresoDescarga },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    
    // Diálogo de confirmación para eliminar
    if (mostrarConfirmacionEliminar && onEliminar != null) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionEliminar = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar este archivo?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEliminar()
                        mostrarConfirmacionEliminar = false
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarConfirmacionEliminar = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Obtiene el icono adecuado según el tipo MIME
 */
@Composable
fun getIconForFileType(mimeType: String): ImageVector {
    return when {
        mimeType.startsWith("image/") -> Icons.Default.Image
        mimeType.startsWith("application/pdf") -> Icons.Default.PictureAsPdf
        mimeType.startsWith("text/") -> Icons.Default.Description
        else -> Icons.Default.AttachFile
    }
}

/**
 * Obtiene el nombre del archivo a partir de su URI
 */
fun getFileNameFromUri(context: android.content.Context, uri: Uri): String? {
    val contentResolver = context.contentResolver
    val cursor = contentResolver.query(uri, null, null, null, null)
    
    return cursor?.use {
        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        it.moveToFirst()
        it.getString(nameIndex)
    }
}

/**
 * Modelo de archivo local
 */
data class ArchivoLocal(
    val uri: Uri,
    val nombre: String,
    val tipo: String,
    val tamaño: Long? = null,
    val fecha: Date? = null
)

/**
 * Modelo de archivo remoto
 */
data class ArchivoRemoto(
    val id: String,
    val nombre: String,
    val url: String,
    val tipo: String? = null,
    val tamaño: Long? = null,
    val fecha: Date? = null,
    val urlVistaPreviaCacheada: String? = null,
    val hayVistaPreviaCacheada: Boolean = false,
    val metadatos: Map<String, String> = emptyMap()
) 