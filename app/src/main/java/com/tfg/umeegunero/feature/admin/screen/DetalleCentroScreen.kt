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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.feature.admin.viewmodel.AdminViewModel
import com.tfg.umeegunero.ui.components.LoadingIndicator
import androidx.compose.material3.HorizontalDivider

/**
 * Pantalla de detalle de un centro educativo simplificada
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
    
    // Estados
    var centro: Centro? by mutableStateOf(null)
    var isLoading by mutableStateOf(true)
    var showDeleteDialog by mutableStateOf(false)
    var isDeleting by mutableStateOf(false)
    
    // Carga inicial del centro
    val loadCentro = {
        viewModel.getCentro(centroId) { loadedCentro ->
            centro = loadedCentro
            isLoading = false
        }
    }
    
    // Cargar el centro al iniciar
    if (isLoading) {
        loadCentro()
    }
    
    // Función para eliminar un centro
    val deleteCentro = {
        isDeleting = true
        viewModel.eliminarCentro(centroId) { success ->
            isDeleting = false
            showDeleteDialog = false
            
            if (success) {
                Toast.makeText(context, "Centro eliminado correctamente", Toast.LENGTH_SHORT).show()
                onDeleteSuccess()
            } else {
                Toast.makeText(context, "Error al eliminar el centro", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    // Función para abrir la ubicación en Google Maps
    val openInMaps = { lat: Double, lon: Double ->
        val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon(${centro?.nombre})")
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
                        text = centro?.nombre ?: "Detalle Centro",
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
                        enabled = !isLoading && centro != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Centro"
                        )
                    }
                    
                    // Botón de eliminar
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        enabled = !isLoading && centro != null
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (centro != null) {
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
                            text = centro?.nombre ?: "",
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
                                text = centro?.direccion ?: "",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Botón para ver en el mapa (si hay coordenadas)
                        if (centro?.latitud != 0.0 || centro?.longitud != 0.0) {
                            Button(
                                onClick = { 
                                    openInMaps(
                                        centro?.latitud ?: 0.0, 
                                        centro?.longitud ?: 0.0
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
                        if (!centro?.telefono.isNullOrEmpty()) {
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
                                    text = centro?.telefono ?: "",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(
                                    onClick = { callPhone(centro?.telefono ?: "") }
                                ) {
                                    Text("Llamar")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        // Email con acción
                        if (!centro?.email.isNullOrEmpty()) {
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
                                    text = centro?.email ?: "",
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(
                                    onClick = { sendEmail(centro?.email ?: "") }
                                ) {
                                    Text("Email")
                                }
                            }
                        }
                    }
                }
                
                // Coordenadas
                if (centro?.latitud != 0.0 || centro?.longitud != 0.0) {
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
                                        text = centro?.latitud.toString(),
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
                                        text = centro?.longitud.toString(),
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
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Eliminar Centro") },
                text = { Text("¿Está seguro de que desea eliminar este centro? Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = deleteCentro
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        // Indicador de carga durante la eliminación
        if (isDeleting) {
            LoadingIndicator(message = "Eliminando centro...")
        }
    }
} 