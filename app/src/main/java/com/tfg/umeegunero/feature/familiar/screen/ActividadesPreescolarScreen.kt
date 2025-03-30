package com.tfg.umeegunero.feature.familiar.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.model.ActividadPreescolar
import com.tfg.umeegunero.data.model.CategoriaActividad
import com.tfg.umeegunero.data.model.EstadoActividad
import com.tfg.umeegunero.feature.familiar.viewmodel.ActividadesPreescolarViewModel
import com.tfg.umeegunero.feature.familiar.viewmodel.AlumnoPreescolarInfo
import com.tfg.umeegunero.feature.familiar.viewmodel.FiltroActividad
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import com.tfg.umeegunero.ui.theme.colorCategoriaActividad

// Pantalla temporal simplificada mientras se completan los modelos
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActividadesPreescolarScreen(
    navController: NavController,
    familiarId: String,
    viewModel: ActividadesPreescolarViewModel = hiltViewModel()
) {
    // Inicializar ViewModel
    LaunchedEffect(familiarId) {
        viewModel.inicializar(familiarId)
    }
    
    // Obtener estado
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Mostrar mensajes
    LaunchedEffect(uiState.error, uiState.mensaje) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
        
        uiState.mensaje?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearMensaje()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actividades de mi peque") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Selector de niño (solo visible si hay más de uno)
                    if (uiState.alumnos.size > 1) {
                        var expandido by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { expandido = true }) {
                                Icon(
                                    Icons.Default.ChildCare,
                                    contentDescription = "Seleccionar niño",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            
                            DropdownMenu(
                                expanded = expandido,
                                onDismissRequest = { expandido = false }
                            ) {
                                uiState.alumnos.forEach { alumno ->
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                "${alumno.nombre} ${alumno.apellidos} (${alumno.edad} años)",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            ) 
                                        },
                                        onClick = {
                                            // viewModel.cargarActividadesDelAlumno(alumno.id) // Temporalmente comentado
                                            expandido = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Información del niño seleccionado
                NinoInfoHeader(
                    uiState.alumnos.find { it.id == uiState.alumnoSeleccionadoId },
                    modifier = Modifier.padding(16.dp)
                )
                
                // Mensaje temporal mientras se implementa
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Construction,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "En desarrollo",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Estamos trabajando en implementar las actividades preescolares. ¡Próximamente disponible!",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Cabecera con información del niño seleccionado
 */
@Composable
fun NinoInfoHeader(
    nino: AlumnoPreescolarInfo?,
    modifier: Modifier = Modifier
) {
    if (nino == null) return
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChildCare,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "${nino.nombre} ${nino.apellidos}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${nino.edad} años · ${nino.aula}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Filtros para actividades
 */
@Composable
fun FiltroCategorias(
    filtroSeleccionado: FiltroActividad,
    onFiltroSelected: (FiltroActividad) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        FiltroActividad.values().forEach { filtro ->
            SegmentedButton(
                selected = filtro == filtroSeleccionado,
                onClick = { onFiltroSelected(filtro) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = filtro.ordinal,
                    count = FiltroActividad.values().size
                )
            ) {
                Text(
                    when (filtro) {
                        FiltroActividad.TODAS -> "Todas"
                        FiltroActividad.PENDIENTES -> "Pendientes"
                        FiltroActividad.REALIZADAS -> "Realizadas"
                        FiltroActividad.RECIENTES -> "Recientes"
                    },
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Filtros por categoría de actividad
 */
@Composable
fun CategoriasFiltro(
    categoriaSeleccionada: CategoriaActividad?,
    onCategoriaSelected: (CategoriaActividad?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Opción para mostrar todas las categorías
        item {
            CategoriaChip(
                categoriaLabel = "Todas",
                icon = Icons.Default.FilterList,
                isSelected = categoriaSeleccionada == null,
                onClick = { onCategoriaSelected(null) }
            )
        }
        
        // Opciones para cada categoría
        items(CategoriaActividad.values()) { categoria ->
            CategoriaChip(
                categoriaLabel = obtenerNombreCategoria(categoria),
                icon = obtenerIconoCategoria(categoria),
                isSelected = categoria == categoriaSeleccionada,
                onClick = { onCategoriaSelected(categoria) }
            )
        }
    }
}

@Composable
fun CategoriaChip(
    categoriaLabel: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    Surface(
        modifier = Modifier
            .height(40.dp)
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = categoriaLabel,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Tarjeta para mostrar una actividad
 */
@Composable
fun ActividadCard(
    actividad: ActividadPreescolar,
    onActividadClick: () -> Unit,
    onMarcarRevisada: () -> Unit
) {
    val fechaFormateada = remember(actividad.fechaProgramada) {
        actividad.fechaProgramada?.toDate()?.let { 
            SimpleDateFormat("dd MMM", Locale("es", "ES")).format(it)
        } ?: ""
    }
    
    val esRevisada = actividad.revisadaPorFamiliar
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onActividadClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (esRevisada) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Cabecera
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono de categoría
                Icon(
                    imageVector = obtenerIconoCategoria(actividad.categoria),
                    contentDescription = null,
                    tint = obtenerColorCategoria(actividad.categoria),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(obtenerColorCategoria(actividad.categoria).copy(alpha = 0.1f))
                        .padding(6.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Título y fecha
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = actividad.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = obtenerNombreCategoria(actividad.categoria) + 
                            if (fechaFormateada.isNotEmpty()) " · $fechaFormateada" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Estado
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (actividad.estado) {
                                EstadoActividad.PENDIENTE -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                EstadoActividad.REALIZADA -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                EstadoActividad.CANCELADA -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (actividad.estado) {
                            EstadoActividad.PENDIENTE -> "Pendiente"
                            EstadoActividad.REALIZADA -> "Realizada"
                            EstadoActividad.CANCELADA -> "Cancelada"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = when (actividad.estado) {
                            EstadoActividad.PENDIENTE -> MaterialTheme.colorScheme.tertiary
                            EstadoActividad.REALIZADA -> MaterialTheme.colorScheme.primary
                            EstadoActividad.CANCELADA -> MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Descripción
            Text(
                text = actividad.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pie de tarjeta con botón para marcar como revisado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Profesor: ${actividad.profesorNombre}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                if (!esRevisada) {
                    Button(
                        onClick = onMarcarRevisada,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Marcar como revisada",
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "Revisado",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "Visto",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Mensaje cuando no hay actividades
 */
@Composable
fun MensajeVacio(filtro: FiltroActividad) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ContentPasteOff,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = when (filtro) {
                    FiltroActividad.TODAS -> "No hay actividades registradas"
                    FiltroActividad.PENDIENTES -> "No hay actividades pendientes"
                    FiltroActividad.REALIZADAS -> "No hay actividades realizadas"
                    FiltroActividad.RECIENTES -> "No hay actividades recientes"
                },
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "El profesor añadirá nuevas actividades pronto",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Funciones auxiliares
 */
fun obtenerNombreCategoria(categoria: CategoriaActividad): String {
    return when (categoria) {
        CategoriaActividad.JUEGO -> "Juego"
        CategoriaActividad.MOTOR -> "Motricidad"
        CategoriaActividad.LENGUAJE -> "Lenguaje"
        CategoriaActividad.MUSICA -> "Música"
        CategoriaActividad.ARTE -> "Arte"
        CategoriaActividad.EXPLORACION -> "Exploración"
        CategoriaActividad.AUTONOMIA -> "Autonomía"
        CategoriaActividad.OTRA -> "Otra"
    }
}

@Composable
fun obtenerIconoCategoria(categoria: CategoriaActividad): ImageVector {
    return when (categoria) {
        CategoriaActividad.JUEGO -> Icons.Default.Toys
        CategoriaActividad.MOTOR -> Icons.Default.DirectionsRun
        CategoriaActividad.LENGUAJE -> Icons.Default.RecordVoiceOver
        CategoriaActividad.MUSICA -> Icons.Default.MusicNote
        CategoriaActividad.ARTE -> Icons.Default.Palette
        CategoriaActividad.EXPLORACION -> Icons.Default.Park
        CategoriaActividad.AUTONOMIA -> Icons.Default.AccessTime
        CategoriaActividad.OTRA -> Icons.Default.Category
    }
}

@Composable
fun obtenerColorCategoria(categoria: CategoriaActividad): Color {
    return colorCategoriaActividad(categoria)
} 