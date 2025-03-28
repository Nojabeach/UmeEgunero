package com.tfg.umeegunero.feature.admin.screen

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.admin.viewmodel.DetalleCentroViewModel
import androidx.compose.ui.tooling.preview.Preview
import android.content.res.Configuration
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.google.firebase.Timestamp
import java.util.Date
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.tfg.umeegunero.R
import android.net.Uri
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.material3.HorizontalDivider

/**
 * Pantalla de detalle de un centro educativo
 *
 * TODO: Mejoras pendientes:
 * - Implementar fuentes de datos reales para las estadísticas del centro
 * - Mejorar la visualización del mapa con datos más precisos
 * - Añadir indicadores de rendimiento académico
 * - Implementar gráficos de asistencia y rendimiento
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleCentroScreen(
    viewModel: DetalleCentroViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onMenuClick: () -> Unit,
    onEditCentro: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Mostrar error en Snackbar si existe
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(message = it)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopBar(
                onMenuClicked = onMenuClick,
                onEditClicked = { onEditCentro(uiState.centro?.id ?: "") },
                onBackClicked = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                // Mostrar indicador de carga
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            } else if (uiState.centro == null) {
                // Mostrar mensaje si no hay centro
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Centro no encontrado",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(onClick = onNavigateBack) {
                        Text("Volver")
                    }
                }
            } else {
                // Mostrar detalles del centro
                DetalleCentroContent(
                    centro = uiState.centro!!,
                    administradores = uiState.administradores,
                    profesores = uiState.profesores,
                    numAlumnos = uiState.numAlumnos,
                    numClases = uiState.numClases,
                    onEditCentro = onEditCentro
                )
            }
        }
    }
}

@Composable
fun DetalleCentroContent(
    centro: Centro,
    administradores: List<Usuario>,
    profesores: List<Usuario>,
    numAlumnos: Int,
    numClases: Int,
    onEditCentro: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Encabezado con nombre del centro
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 8.dp)
                )
                
                Text(
                    text = centro.nombre,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Tarjeta de mapa
        MapaCard(centro)
        
        // Tarjeta de información del centro
        InfoCard(
            title = "Información del Centro",
            icon = Icons.Default.Business,
            items = listOf(
                InfoItem(
                    icon = Icons.Default.LocationOn,
                    title = "Dirección",
                    content = "${centro.direccion.calle}, ${centro.direccion.numero}\n${centro.direccion.codigoPostal} ${centro.direccion.ciudad}, ${centro.direccion.provincia}"
                ),
                InfoItem(
                    icon = Icons.Default.Call,
                    title = "Teléfono",
                    content = centro.contacto.telefono
                ),
                InfoItem(
                    icon = Icons.Default.Email,
                    title = "Email",
                    content = centro.contacto.email
                )
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tarjeta de estadísticas
        EstadisticasCard(
            numAlumnos = numAlumnos,
            numClases = numClases,
            numProfesores = profesores.size
        )
        
        // Administradores del centro
        if (administradores.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Administradores del Centro",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    administradores.forEachIndexed { index, admin ->
                        UsuarioItem(usuario = admin)
                        
                        if (index < administradores.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }
        
        // Profesores del centro
        if (profesores.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Profesores del Centro",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    profesores.take(5).forEachIndexed { index, profesor ->
                        UsuarioItem(usuario = profesor)
                        
                        if (index < profesores.take(5).size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                    
                    if (profesores.size > 5) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "... y ${profesores.size - 5} más",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        
        // Botón de editar centro
        Button(
            onClick = { onEditCentro(centro.id) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Editar Centro")
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    icon: ImageVector,
    items: List<InfoItem>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            items.forEach { item ->
                InfoItemRow(item = item)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun InfoItemRow(
    item: InfoItem
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = item.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun EstadisticasCard(
    numAlumnos: Int,
    numClases: Int,
    numProfesores: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Estadísticas del Centro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                EstadisticaItem(
                    icon = Icons.Default.School,
                    valor = numClases.toString(),
                    descripcion = "Clases",
                    modifier = Modifier.weight(1f)
                )
                
                EstadisticaItem(
                    icon = Icons.Default.Group,
                    valor = numAlumnos.toString(),
                    descripcion = "Alumnos",
                    modifier = Modifier.weight(1f)
                )
                
                EstadisticaItem(
                    icon = Icons.Default.Person,
                    valor = numProfesores.toString(),
                    descripcion = "Profesores",
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Mensaje de "pendiente de desarrollo"
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Nota: Las estadísticas avanzadas están pendientes de desarrollo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun EstadisticaItem(
    icon: ImageVector,
    valor: String,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = valor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = descripcion,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun UsuarioItem(
    usuario: Usuario
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${usuario.nombre} ${usuario.apellidos}",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = usuario.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

data class InfoItem(
    val icon: ImageVector,
    val title: String,
    val content: String
)

@Composable
private fun MapaCard(centro: Centro) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.height(200.dp)) {
            // Mapa como fondo
            ImagenMapa(
                latitud = centro.latitud,
                longitud = centro.longitud,
                modifier = Modifier.fillMaxSize()
            )
            
            // Overlay con información de ubicación
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Ubicación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "${centro.direccion.calle}, ${centro.direccion.numero}, ${centro.direccion.ciudad}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            val direccion = "${centro.direccion.calle}, ${centro.direccion.numero}, ${centro.direccion.ciudad}"
                            val gmmIntentUri = Uri.parse("geo:${centro.latitud},${centro.longitud}?q=${Uri.encode(direccion)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            
                            if (mapIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(mapIntent)
                            } else {
                                Toast.makeText(
                                    context,
                                    "No se pudo abrir Google Maps",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Ver en Google Maps")
                    }
                }
            }
        }
    }
}

@Composable
private fun ImagenMapa(
    latitud: Double?,
    longitud: Double?,
    modifier: Modifier = Modifier
) {
    if (latitud != null && longitud != null) {
        // Construir URL para la imagen estática de Google Maps
        val url = "https://maps.googleapis.com/maps/api/staticmap?" +
                "center=$latitud,$longitud&" +
                "zoom=15&" +
                "size=600x300&" +
                "maptype=roadmap&" +
                "markers=color:red%7C$latitud,$longitud&" +
                "key=AIzaSyDUBTmwtJ9djJpnT7NaPoQUrzYl4YNqAXk"
        
        // Cargar la imagen usando Image con painterResource ya que no tenemos Glide
        Image(
            painter = painterResource(id = R.drawable.map_placeholder),
            contentDescription = "Mapa de ubicación",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        // Mostrar placeholder si no hay coordenadas
        Box(
            modifier = modifier
                .background(color = MaterialTheme.colorScheme.surfaceVariant, shape = RectangleShape)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOff,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No hay coordenadas disponibles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetalleCentroScreenPreview() {
    UmeEguneroTheme {
        // Simplemente mostrar un componente estático para el preview
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "IES Valle Inclán",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Información General", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Dirección: Calle Principal 123, 28001 Madrid")
                    Text("Teléfono: 912345678")
                    Text("Email: contacto@valleinclan.edu")
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DetalleCentroScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        // Simplemente mostrar un componente estático para el preview
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "IES Valle Inclán",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Información General", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Dirección: Calle Principal 123, 28001 Madrid")
                    Text("Teléfono: 912345678")
                    Text("Email: contacto@valleinclan.edu")
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    onMenuClicked: () -> Unit = {},
    onEditClicked: () -> Unit = {},
    onBackClicked: () -> Unit = {}
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver atrás"
                )
            }
        },
        title = {
            Text(
                text = "Detalles del Centro",
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {
            IconButton(onClick = onEditClicked) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar centro"
                )
            }
            IconButton(onClick = onMenuClicked) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menú"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
} 