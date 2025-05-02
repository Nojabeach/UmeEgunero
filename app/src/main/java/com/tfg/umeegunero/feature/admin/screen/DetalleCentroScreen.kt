/**
 * Módulo de gestión detallada de centros educativos del sistema UmeEgunero.
 * 
 * Este módulo implementa la interfaz para visualizar y gestionar los
 * detalles específicos de un centro educativo.
 */
package com.tfg.umeegunero.feature.admin.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.feature.admin.viewmodel.AdminViewModel
import com.tfg.umeegunero.ui.components.LoadingIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.tooling.preview.Preview
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import androidx.navigation.compose.rememberNavController
import android.content.res.Configuration

/**
 * Clase que gestiona el estado de la pantalla de detalle de centro.
 * 
 * Esta clase encapsula toda la lógica de estado y operaciones
 * relacionadas con la visualización y gestión de un centro educativo.
 * 
 * ## Estados
 * - Centro actual
 * - Estado de carga
 * - Diálogo de eliminación
 * - Proceso de eliminación
 * 
 * ## Operaciones
 * - Carga de datos del centro
 * - Eliminación del centro
 * - Gestión de estados de UI
 * 
 * @property centro Centro educativo actual
 * @property isLoading Estado de carga de datos
 * @property showDeleteDialog Visibilidad del diálogo de eliminación
 * @property isDeleting Estado del proceso de eliminación
 * @property error Mensaje de error en caso de falla
 * 
 * @see Centro
 * @see AdminViewModel
 */
class DetalleCentroState {
    var centro by mutableStateOf<Centro?>(null)
    var isLoading by mutableStateOf(true)
    var showDeleteDialog by mutableStateOf(false)
    var isDeleting by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    
    /**
     * Carga los datos del centro educativo.
     * 
     * @param viewModel ViewModel que gestiona la lógica de administración
     * @param centroId Identificador del centro a cargar
     */
    fun cargarCentro(viewModel: AdminViewModel, centroId: String) {
        isLoading = true
        error = null
        viewModel.getCentro(centroId) { loadedCentro ->
            centro = loadedCentro
            isLoading = false
            
            if (loadedCentro == null) {
                error = "No se pudo cargar la información del centro. Intente nuevamente."
            }
        }
    }
    
    /**
     * Elimina el centro educativo.
     * 
     * @param viewModel ViewModel que gestiona la lógica de administración
     * @param centroId Identificador del centro a eliminar
     * @param onSuccess Callback ejecutado tras eliminación exitosa
     * @param onError Callback ejecutado si ocurre un error
     */
    fun eliminarCentro(
        viewModel: AdminViewModel, 
        centroId: String,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        isDeleting = true
        viewModel.eliminarCentro(centroId) { success ->
            isDeleting = false
            showDeleteDialog = false
            
            if (success) {
                onSuccess()
            } else {
                onError()
            }
        }
    }
}

/**
 * Pantalla de detalle de centro educativo.
 * 
 * Esta pantalla proporciona una interfaz completa para la visualización
 * y gestión de los detalles de un centro educativo específico.
 * 
 * ## Características
 * - Información detallada del centro
 * - Acciones de gestión (editar, eliminar)
 * - Integración con mapas y comunicaciones
 * - Estados de carga y error
 * 
 * ## Funcionalidades
 * - Visualización de datos del centro
 * - Edición de información
 * - Eliminación del centro
 * - Acciones rápidas (llamada, email, ubicación)
 * 
 * @param centroId Identificador del centro a mostrar
 * @param viewModel ViewModel que gestiona la lógica de administración
 * @param onNavigateBack Callback para navegar hacia atrás
 * @param onNavigateToEdit Callback para navegar a edición
 * @param onDeleteSuccess Callback ejecutado tras eliminación exitosa
 * 
 * @see DetalleCentroState
 * @see AdminViewModel
 * @see Centro
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleCentroScreen(
    centroId: String,
    viewModel: AdminViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {},
    onDeleteSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Estado de la pantalla
    val state = remember { DetalleCentroState() }
    
    // Carga inicial del centro
    LaunchedEffect(centroId) {
        state.cargarCentro(viewModel, centroId)
    }
    
    // Función para abrir la ubicación en Google Maps
    val openInMaps = { lat: Double, lon: Double ->
        val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon(${state.centro?.nombre})")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")
        
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Si Google Maps no está instalado, abrir en el navegador
            val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lon")
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
            context.startActivity(browserIntent)
        }
    }
    
    // Función para llamar por teléfono
    val callPhone = { phone: String ->
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
        }
        context.startActivity(intent)
    }
    
    // Función para enviar email
    val sendEmail = { email: String ->
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
        }
        context.startActivity(intent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.centro?.nombre ?: "Detalle Centro",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // Botón de editar
                    IconButton(
                        onClick = { onNavigateToEdit(centroId) },
                        enabled = !state.isLoading && state.centro != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Centro"
                        )
                    }
                    
                    // Botón de eliminar
                    IconButton(
                        onClick = { state.showDeleteDialog = true },
                        enabled = !state.isLoading && state.centro != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar Centro"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            // Mostrar mensaje de error
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Text(
                        text = state.error!!,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    Button(
                        onClick = { state.cargarCentro(viewModel, centroId) }
                    ) {
                        Text(text = "Reintentar")
                    }
                }
            }
        } else if (state.centro != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarjeta de información general
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Información del Centro",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        
                        // Nombre
                        Text(
                            text = state.centro?.nombre ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        
                        // Dirección con icono
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = state.centro?.direccion ?: "",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Botón para ver en el mapa (si hay coordenadas)
                        if (state.centro?.latitud != 0.0 || state.centro?.longitud != 0.0) {
                            Button(
                                onClick = { 
                                    openInMaps(
                                        state.centro?.latitud ?: 0.0, 
                                        state.centro?.longitud ?: 0.0
                                    ) 
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Ver en mapa"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ver en mapa")
                            }
                        }
                    }
                }
                
                // Tarjeta de contacto
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Contacto",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        
                        // Teléfono con acción
                        if (!state.centro?.telefono.isNullOrEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = state.centro?.telefono ?: "",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(
                                    onClick = { callPhone(state.centro?.telefono ?: "") }
                                ) {
                                    Text("Llamar")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        // Email con acción
                        if (!state.centro?.email.isNullOrEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = state.centro?.email ?: "",
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(
                                    onClick = { sendEmail(state.centro?.email ?: "") }
                                ) {
                                    Text("Email")
                                }
                            }
                        }
                    }
                }
                
                // Coordenadas
                if (state.centro?.latitud != 0.0 || state.centro?.longitud != 0.0) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Coordenadas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        text = "Latitud:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = state.centro?.latitud.toString(),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(32.dp))
                                
                                Column {
                                    Text(
                                        text = "Longitud:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = state.centro?.longitud.toString(),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Error o centro no encontrado
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Centro no encontrado",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(onClick = onNavigateBack) {
                    Text("Volver")
                }
            }
        }
        
        // Diálogo de confirmación para eliminar
        if (state.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { state.showDeleteDialog = false },
                title = { Text("Eliminar Centro") },
                text = { Text("¿Está seguro de que desea eliminar este centro? Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = { 
                            state.eliminarCentro(
                                viewModel = viewModel,
                                centroId = centroId,
                                onSuccess = {
                                    Toast.makeText(context, "Centro eliminado correctamente", Toast.LENGTH_SHORT).show()
                                    onDeleteSuccess()
                                },
                                onError = {
                                    Toast.makeText(context, "Error al eliminar el centro", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { state.showDeleteDialog = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        // Indicador de carga durante la eliminación
        if (state.isDeleting) {
            LoadingIndicator(message = "Eliminando centro...")
        }
    }
}

/**
 * Vista previa de la pantalla de detalle de centro en modo claro.
 */
@Preview(showBackground = true)
@Composable
fun DetalleCentroScreenPreview() {
    UmeEguneroTheme {
        Surface {
            DetalleCentroScreen(
                centroId = "1",
                viewModel = hiltViewModel(),
                onNavigateBack = {},
                onNavigateToEdit = {},
                onDeleteSuccess = {}
            )
        }
    }
}

/**
 * Vista previa de la pantalla de detalle de centro en modo oscuro.
 */
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun DetalleCentroScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        Surface {
            DetalleCentroScreen(
                centroId = "1",
                viewModel = hiltViewModel(),
                onNavigateBack = {},
                onNavigateToEdit = {},
                onDeleteSuccess = {}
            )
        }
    }
}

/**
 * Vista previa del contenido de detalle de un centro
 */
@Preview(showBackground = true)
@Composable
fun DetalleCentroContentPreview() {
    UmeEguneroTheme {
        Surface {
            val centro = Centro(
                id = "1",
                nombre = "Colegio San José",
                direccion = "Calle Principal 123, 48001 Bilbao, Vizcaya",
                telefono = "944123123",
                email = "contacto@sanjose.edu.es",
                latitud = 43.2569629,
                longitud = -2.9234409
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = centro.nombre,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Información de Contacto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = centro.direccion,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
} 