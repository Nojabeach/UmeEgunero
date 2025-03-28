package com.tfg.umeegunero.feature.familiar.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.NivelConsumo
import com.tfg.umeegunero.feature.familiar.viewmodel.DetalleRegistroViewModel
import com.tfg.umeegunero.ui.theme.FamiliarColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Clases de datos para el preview
enum class NivelConsumo {
    NADA, POCO, BIEN, TODO
}

data class ComidaModel(
    val consumoPrimero: String? = null,
    val descripcionPrimero: String? = null,
    val consumoSegundo: String? = null,
    val descripcionSegundo: String? = null,
    val consumoPostre: String? = null,
    val descripcionPostre: String? = null,
    val observaciones: String? = null
)

data class SiestaModel(
    val duracion: Int,
    val observaciones: String,
    val inicio: Timestamp? = null,
    val fin: Timestamp? = null
)

data class CacaControlModel(
    val tipo1: Boolean? = null,
    val tipo2: Boolean? = null,
    val tipo3: Boolean? = null,
    val hora: String? = null,
    val cantidad: String? = null,
    val tipo: String? = null,
    val descripcion: String? = null
)

data class ActividadesModel(
    val titulo: String,
    val descripcion: String,
    val participacion: String,
    val observaciones: String
)

data class RegistroModel(
    val id: String,
    val alumnoId: String,
    val alumnoNombre: String,
    val fecha: com.google.firebase.Timestamp,
    val profesorId: String? = null,
    val profesorNombre: String? = null,
    val comida: ComidaModel? = null,
    val siesta: SiestaModel? = null,
    val cacaControl: CacaControlModel? = null,
    val actividades: ActividadesModel? = null,
    val observaciones: String? = null
)

data class DetalleRegistroUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val registro: RegistroModel? = null,
    val profesorNombre: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRegistroScreen(
    viewModel: DetalleRegistroViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String, String?) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Extraer el registro para poder usarlo de forma segura
    val registro = uiState.registro

    // Mostrar error si existe
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle de actividad",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Botón para chatear con el profesor
                    registro?.profesorId?.let { profesorId ->
                        IconButton(
                            onClick = { onNavigateToChat(profesorId, registro.alumnoId) }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Message,
                                contentDescription = "Contactar al profesor",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FamiliarColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FamiliarColor)
                }
            } else if (registro == null) {
                // Mostrar mensaje de error si no hay registro
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No se encontró el registro de actividad",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Volver al listado")
                        }
                    }
                }
            } else {
                // Mostrar detalles del registro
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // Información básica
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
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
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = formatDateExtended(registro.fecha),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column {
                                    Text(
                                        text = "Profesor: ${uiState.profesorNombre ?: "Desconocido"}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }

                    // Sección de alimentación
                    if (registro.comida != null) {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                // Encabezado de la sección
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restaurant,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    Text(
                                        text = "Alimentación",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                // Detalles de la comida
                                Column {
                                    if (registro.comida?.consumoPrimero != null) {
                                        Text(
                                            text = "Primer plato",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = registro.comida?.consumoPrimero ?: "No registrado",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        
                                        if (registro.comida?.descripcionPrimero?.isNotBlank() == true) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = registro.comida?.descripcionPrimero ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                    
                                    if (registro.comida?.consumoSegundo != null) {
                                        Text(
                                            text = "Segundo plato",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = registro.comida?.consumoSegundo ?: "No registrado",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        
                                        if (registro.comida?.descripcionSegundo?.isNotBlank() == true) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = registro.comida?.descripcionSegundo ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                    
                                    if (registro.comida?.consumoPostre != null) {
                                        Text(
                                            text = "Postre",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = registro.comida?.consumoPostre ?: "No registrado",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        
                                        if (registro.comida?.descripcionPostre?.isNotBlank() == true) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = registro.comida?.descripcionPostre ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Sección de Siesta
                    registro.siesta?.let { siesta ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Descanso",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Hora inicio
                                siesta.inicio?.let { inicio ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "Inicio: ${formatTime(inicio)}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // Hora fin
                                siesta.fin?.let { fin ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "Fin: ${formatTime(fin)}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // Duración calculada
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Duración: ${formatDuracion(siesta.duracion)}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }

                    // Sección de Necesidades Fisiológicas
                    if (registro.cacaControl?.tipo1 != null ||
                        registro.cacaControl?.tipo2 != null ||
                        registro.cacaControl?.tipo3 != null ||
                        registro.cacaControl?.hora != null ||
                        registro.cacaControl?.cantidad != null ||
                        registro.cacaControl?.tipo != null ||
                        registro.cacaControl?.descripcion != null) {

                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Higiene",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                if (registro.cacaControl?.tipo1 == true) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "Tipo 1",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                if (registro.cacaControl?.tipo2 == true) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "Tipo 2",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                if (registro.cacaControl?.tipo3 == true) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "Tipo 3",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                if (registro.cacaControl?.hora != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "Hora: ${registro.cacaControl?.hora}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                if (registro.cacaControl?.cantidad != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "Cantidad: ${registro.cacaControl?.cantidad}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                if (registro.cacaControl?.tipo != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "Tipo: ${registro.cacaControl?.tipo}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                if (registro.cacaControl?.descripcion != null) {
                                    Text(
                                        text = "Descripción: ${registro.cacaControl?.descripcion ?: "Sin descripción"}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }

                    // Sección de Observaciones Generales
                    if (registro.observaciones?.isNotBlank() == true) {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Observaciones Generales",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = registro.observaciones ?: "No hay observaciones adicionales registradas",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRegistroTopBar(
    onNavigateBack: () -> Unit,
    fecha: String,
    nombreAlumno: String
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = nombreAlumno,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = fecha,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver atrás",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = FamiliarColor,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}

@Composable
fun DetalleRegistroContent(
    fecha: String,
    nombreAlumno: String,
    comidaData: ComidaModel? = null,
    observacionesTexto: String? = null,
    cacaControlData: CacaControlModel? = null,
    descanso: String,
    actividades: List<String>,
    isLoading: Boolean,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FamiliarColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Información básica
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp)
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
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = fecha,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Alumno: $nombreAlumno",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // Sección de alimentación
                if (comidaData != null) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Alimentación",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (comidaData.consumoPrimero != null) {
                                Text(
                                    text = "Primer plato",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = comidaData.consumoPrimero,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                
                                if (comidaData.descripcionPrimero != null && comidaData.descripcionPrimero.isNotBlank()) {
                                    Text(
                                        text = comidaData.descripcionPrimero,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            
                            if (comidaData.consumoSegundo != null) {
                                Text(
                                    text = "Segundo plato",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = comidaData.consumoSegundo,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                
                                if (comidaData.descripcionSegundo != null && comidaData.descripcionSegundo.isNotBlank()) {
                                    Text(
                                        text = comidaData.descripcionSegundo,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            
                            if (comidaData.consumoPostre != null) {
                                Text(
                                    text = "Postre",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = comidaData.consumoPostre,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                
                                if (comidaData.descripcionPostre != null && comidaData.descripcionPostre.isNotBlank()) {
                                    Text(
                                        text = comidaData.descripcionPostre,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Sección de Necesidades Fisiológicas
                if (cacaControlData != null) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Higiene",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (cacaControlData.tipo1 == true) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Tipo 1",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (cacaControlData.tipo2 == true) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Tipo 2",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            if (cacaControlData.tipo3 == true) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Tipo 3",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (cacaControlData.hora != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Hora: ${cacaControlData.hora}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (cacaControlData.cantidad != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Cantidad: ${cacaControlData.cantidad}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (cacaControlData.tipo != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Tipo: ${cacaControlData.tipo}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (cacaControlData.descripcion != null) {
                                Text(
                                    text = cacaControlData.descripcion,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Sección de Observaciones Generales
                if (observacionesTexto != null && observacionesTexto.isNotBlank()) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Observaciones Generales",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = observacionesTexto,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

// Funciones utilitarias
fun formatDateExtended(timestamp: Timestamp): String {
    val dateFormat = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    return dateFormat.format(timestamp.toDate()).replaceFirstChar { it.uppercase() }
}

fun formatTime(timestamp: Timestamp): String {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return timeFormat.format(timestamp.toDate())
}

fun formatDuracion(minutos: Int): String {
    val horas = minutos / 60
    val minutosRestantes = minutos % 60

    return if (horas > 0) {
        "$horas h $minutosRestantes min"
    } else {
        "$minutosRestantes min"
    }
}

fun nivelConsumoText(nivel: NivelConsumo): String {
    return when (nivel) {
        NivelConsumo.NADA -> "Nada"
        NivelConsumo.POCO -> "Poco"
        NivelConsumo.BIEN -> "Bien"
        NivelConsumo.TODO -> "Todo"
    }
}

fun nivelConsumoColor(nivel: NivelConsumo): Color {
    return when (nivel) {
        NivelConsumo.NADA -> Color.Red
        NivelConsumo.POCO -> Color(0xFFFF9800) // Orange
        NivelConsumo.BIEN -> Color(0xFF4CAF50) // Green
        NivelConsumo.TODO -> Color(0xFF2196F3) // Blue
    }
}

@Preview(showBackground = true)
@Composable
fun DetalleRegistroScreenPreview() {
    UmeEguneroTheme {
        DetalleRegistroScreenPreviewContent()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DetalleRegistroScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        DetalleRegistroScreenPreviewContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRegistroScreenPreviewContent() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    
    // Mock de datos para el preview
    val alumnoNombre = "Martín García López"
    val comidaMock = ComidaModel(
        consumoPrimero = "Lentejas con verduras",
        descripcionPrimero = "Delicioso plato de lentejas con verduras",
        consumoSegundo = "Filete de pollo con ensalada",
        descripcionSegundo = "Delicioso filete de pollo con ensalada",
        consumoPostre = "Fruta",
        descripcionPostre = "Deliciosa fruta fresca"
    )
    
    val siestaMock = SiestaModel(
        duracion = 45,
        observaciones = "Ha dormido tranquilamente.",
        inicio = Timestamp(Date()),
        fin = Timestamp(Date())
    )
    
    val cacaMock = CacaControlModel(
        tipo1 = true,
        tipo2 = true,
        tipo3 = true,
        hora = "12:00",
        cantidad = "1000 ml",
        tipo = "Normal",
        descripcion = "Sin problemas."
    )
    
    val actividadesMock = ActividadesModel(
        titulo = "Clase de pintura",
        descripcion = "Hoy hemos realizado dibujos con acuarelas sobre el otoño.",
        participacion = "Ha participado activamente y ha mostrado creatividad.",
        observaciones = "Se ha relacionado bien con sus compañeros."
    )
    
    val registroMock = RegistroModel(
        id = "registro1",
        alumnoId = "alumno1",
        alumnoNombre = alumnoNombre,
        fecha = Timestamp(Date()),
        profesorId = "maestro1", 
        profesorNombre = "Ana Martínez",
        comida = comidaMock,
        siesta = siestaMock,
        cacaControl = cacaMock,
        actividades = actividadesMock,
        observaciones = "Hoy ha sido un día muy bueno para Martín. Ha estado participativo y alegre."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Registro de $alumnoNombre",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = formatDateExtended(registroMock.fecha),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FamiliarColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Registro del día",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "Registrado por: ${registroMock.profesorNombre}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 32.dp, top = 4.dp, bottom = 16.dp)
            )
            
            // Comida
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
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
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Comida",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Menú del día
                    Text(
                        text = "Menú del día:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = comidaMock.consumoPrimero?.let { "$it (${comidaMock.descripcionPrimero})" } ?: "No registrado",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Consumo
                    Text(
                        text = "Consumo:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Primer plato
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Primer plato:",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.width(120.dp)
                        )
                        
                        Text(
                            text = comidaMock.consumoPrimero?.let { nivelConsumoText(when (it) {
                                "Lentejas con verduras" -> NivelConsumo.BIEN
                                "Filete de pollo con ensalada" -> NivelConsumo.BIEN
                                else -> NivelConsumo.POCO
                            }) } ?: "No registrado",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = comidaMock.consumoPrimero?.let { nivelConsumoColor(when (it) {
                                "Lentejas con verduras" -> NivelConsumo.BIEN
                                "Filete de pollo con ensalada" -> NivelConsumo.BIEN
                                else -> NivelConsumo.POCO
                            }) } ?: Color.Black
                        )
                    }
                    
                    // Segundo plato
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Segundo plato:",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.width(120.dp)
                        )
                        
                        Text(
                            text = comidaMock.consumoSegundo?.let { "$it (${comidaMock.descripcionSegundo})" } ?: "No registrado",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = comidaMock.consumoSegundo?.let { nivelConsumoColor(when (it) {
                                "Lentejas con verduras" -> NivelConsumo.BIEN
                                "Filete de pollo con ensalada" -> NivelConsumo.BIEN
                                else -> NivelConsumo.POCO
                            }) } ?: Color.Black
                        )
                    }
                    
                    // Postre
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Postre:",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.width(120.dp)
                        )
                        
                        Text(
                            text = comidaMock.consumoPostre?.let { "$it (${comidaMock.descripcionPostre})" } ?: "No registrado",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = comidaMock.consumoPostre?.let { nivelConsumoColor(when (it) {
                                "Fruta" -> NivelConsumo.BIEN
                                else -> NivelConsumo.POCO
                            }) } ?: Color.Black
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Observaciones de la comida
                    if (!comidaMock.observaciones.isNullOrBlank()) {
                        Text(
                            text = "Observaciones:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = comidaMock.observaciones,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            // Siesta
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
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
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Siesta",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Duración
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Duración:",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.width(120.dp)
                        )
                        
                        Text(
                            text = formatDuracion(siestaMock.duracion),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Observaciones de la siesta
                    if (!siestaMock.observaciones.isNullOrBlank()) {
                        Text(
                            text = "Observaciones:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = siestaMock.observaciones,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Actividades
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
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
                            imageVector = Icons.Default.LocalActivity,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(28.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = actividadesMock.titulo,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Descripción
                    Text(
                        text = "Descripción:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = actividadesMock.descripcion,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Participación
                    Text(
                        text = "Participación:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = actividadesMock.participacion,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    // Observaciones de las actividades
                    if (!actividadesMock.observaciones.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Observaciones:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = actividadesMock.observaciones,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            // Observaciones Generales
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Observaciones Generales",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = registroMock.observaciones ?: "No hay observaciones adicionales registradas",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}