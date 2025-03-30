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
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
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
                
                // Filtros para actividades
                FiltersSection(
                    filtroSeleccionado = uiState.filtroSeleccionado,
                    categoriaSeleccionada = uiState.categoriaSeleccionada,
                    onFiltroSelected = { viewModel.aplicarFiltro(it) },
                    onCategoriaSelected = { viewModel.aplicarFiltroCategoria(it) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Lista de actividades filtradas
                if (uiState.actividadesFiltradas.isEmpty()) {
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
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "No hay actividades",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "No se encontraron actividades con los filtros seleccionados",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.actividadesFiltradas) { actividad ->
                            ActividadPreescolarItem(
                                actividad = actividad,
                                onClick = { viewModel.seleccionarActividad(actividad.id) }
                            )
                        }
                    }
                }
            }
            
            // Detalle de actividad en un diálogo
            if (uiState.mostrarDetalleActividad && uiState.actividadSeleccionada != null) {
                DetalleActividadDialog(
                    actividad = uiState.actividadSeleccionada!!,
                    onDismiss = { viewModel.cerrarDetalleActividad() },
                    onMarcarRevisada = { comentario ->
                        viewModel.marcarComoRevisada(uiState.actividadSeleccionada!!.id, comentario)
                    }
                )
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
        CategoriaActividad.MOTOR -> Icons.AutoMirrored.Filled.DirectionsRun
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

/**
 * Sección de filtros para actividades
 */
@Composable
fun FiltersSection(
    filtroSeleccionado: FiltroActividad,
    categoriaSeleccionada: CategoriaActividad?,
    onFiltroSelected: (FiltroActividad) -> Unit,
    onCategoriaSelected: (CategoriaActividad?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Filtrar actividades",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        // Chips de filtros por estado
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(FiltroActividad.values()) { filtro ->
                FilterChip(
                    selected = filtroSeleccionado == filtro,
                    onClick = { onFiltroSelected(filtro) },
                    label = { 
                        Text(
                            when(filtro) {
                                FiltroActividad.TODAS -> "Todas"
                                FiltroActividad.PENDIENTES -> "Pendientes"
                                FiltroActividad.REALIZADAS -> "Realizadas"
                                FiltroActividad.RECIENTES -> "Recientes"
                            }
                        ) 
                    },
                    leadingIcon = if (filtroSeleccionado == filtro) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Chips de categorías
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Chip "Todas las categorías"
            item {
                FilterChip(
                    selected = categoriaSeleccionada == null,
                    onClick = { onCategoriaSelected(null) },
                    label = { Text("Todas") },
                    leadingIcon = if (categoriaSeleccionada == null) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                )
            }
            
            // Chips para cada categoría
            items(CategoriaActividad.values()) { categoria ->
                FilterChip(
                    selected = categoriaSeleccionada == categoria,
                    onClick = { onCategoriaSelected(categoria) },
                    label = { 
                        Text(
                            when(categoria) {
                                CategoriaActividad.JUEGO -> "Juego"
                                CategoriaActividad.MOTOR -> "Motricidad"
                                CategoriaActividad.LENGUAJE -> "Lenguaje"
                                CategoriaActividad.MUSICA -> "Música"
                                CategoriaActividad.ARTE -> "Arte"
                                CategoriaActividad.EXPLORACION -> "Exploración"
                                CategoriaActividad.AUTONOMIA -> "Autonomía"
                                CategoriaActividad.OTRA -> "Otras"
                            }
                        ) 
                    },
                    leadingIcon = if (categoriaSeleccionada == categoria) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colorCategoriaActividad(categoria).copy(alpha = 0.2f),
                        selectedLabelColor = colorCategoriaActividad(categoria)
                    )
                )
            }
        }
    }
}

/**
 * Item de actividad preescolar en la lista
 */
@Composable
fun ActividadPreescolarItem(
    actividad: ActividadPreescolar,
    onClick: () -> Unit
) {
    val colorCategoria = colorCategoriaActividad(actividad.categoria)
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de categoría
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colorCategoria.copy(alpha = 0.2f))
                    .border(2.dp, colorCategoria.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoriaIcon(actividad.categoria),
                    contentDescription = null,
                    tint = colorCategoria,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = actividad.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = actividad.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = dateFormat.format(actividad.fechaCreacion.toDate()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Profesor
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = actividad.profesorNombre,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Indicador de estado
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when (actividad.estado) {
                            EstadoActividad.PENDIENTE -> MaterialTheme.colorScheme.errorContainer
                            EstadoActividad.REALIZADA -> MaterialTheme.colorScheme.tertiaryContainer
                            EstadoActividad.CANCELADA -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (actividad.estado) {
                        EstadoActividad.PENDIENTE -> Icons.Default.HourglassEmpty
                        EstadoActividad.REALIZADA -> Icons.Default.Done
                        EstadoActividad.CANCELADA -> Icons.Default.Close
                    },
                    contentDescription = null,
                    tint = when (actividad.estado) {
                        EstadoActividad.PENDIENTE -> MaterialTheme.colorScheme.error
                        EstadoActividad.REALIZADA -> MaterialTheme.colorScheme.tertiary
                        EstadoActividad.CANCELADA -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Diálogo para mostrar detalles de una actividad
 */
@Composable
fun DetalleActividadDialog(
    actividad: ActividadPreescolar,
    onDismiss: () -> Unit,
    onMarcarRevisada: (String) -> Unit
) {
    var comentario by remember { mutableStateOf("") }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val colorCategoria = colorCategoriaActividad(actividad.categoria)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(actividad.titulo, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            ) {
                // Categoría
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colorCategoria.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoriaIcon(actividad.categoria),
                            contentDescription = null,
                            tint = colorCategoria,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = when(actividad.categoria) {
                            CategoriaActividad.JUEGO -> "Juego"
                            CategoriaActividad.MOTOR -> "Motricidad"
                            CategoriaActividad.LENGUAJE -> "Lenguaje"
                            CategoriaActividad.MUSICA -> "Música"
                            CategoriaActividad.ARTE -> "Arte"
                            CategoriaActividad.EXPLORACION -> "Exploración"
                            CategoriaActividad.AUTONOMIA -> "Autonomía"
                            CategoriaActividad.OTRA -> "Otra categoría"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = colorCategoria
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Descripción
                Text(
                    text = actividad.descripcion,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Información adicional
                Text(
                    text = "Fecha: ${dateFormat.format(actividad.fechaCreacion.toDate())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Profesor: ${actividad.profesorNombre}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Estado
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (actividad.estado) {
                                EstadoActividad.PENDIENTE -> MaterialTheme.colorScheme.errorContainer
                                EstadoActividad.REALIZADA -> MaterialTheme.colorScheme.tertiaryContainer
                                EstadoActividad.CANCELADA -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = when (actividad.estado) {
                            EstadoActividad.PENDIENTE -> Icons.Default.HourglassEmpty
                            EstadoActividad.REALIZADA -> Icons.Default.Done
                            EstadoActividad.CANCELADA -> Icons.Default.Close
                        },
                        contentDescription = null,
                        tint = when (actividad.estado) {
                            EstadoActividad.PENDIENTE -> MaterialTheme.colorScheme.error
                            EstadoActividad.REALIZADA -> MaterialTheme.colorScheme.tertiary
                            EstadoActividad.CANCELADA -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = when (actividad.estado) {
                            EstadoActividad.PENDIENTE -> "Pendiente"
                            EstadoActividad.REALIZADA -> "Realizada"
                            EstadoActividad.CANCELADA -> "Cancelada"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = when (actividad.estado) {
                            EstadoActividad.PENDIENTE -> MaterialTheme.colorScheme.error
                            EstadoActividad.REALIZADA -> MaterialTheme.colorScheme.tertiary
                            EstadoActividad.CANCELADA -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                // Comentarios del profesor
                if (actividad.comentariosProfesor.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Comentarios del profesor:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    Text(
                        text = actividad.comentariosProfesor,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                // Si ya ha sido revisada, mostrar comentarios del familiar
                if (actividad.revisadaPorFamiliar) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Sus comentarios:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    Text(
                        text = actividad.comentariosFamiliar.ifEmpty { "Sin comentarios" },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    if (actividad.fechaRevision != null) {
                        Text(
                            text = "Revisado el: ${dateFormat.format(actividad.fechaRevision.toDate())}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } 
                // Si no ha sido revisada, mostrar campo para comentarios
                else if (actividad.estado == EstadoActividad.REALIZADA) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = comentario,
                        onValueChange = { comentario = it },
                        label = { Text("Añadir comentarios (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }
        },
        confirmButton = {
            if (!actividad.revisadaPorFamiliar && actividad.estado == EstadoActividad.REALIZADA) {
                Button(
                    onClick = { 
                        onMarcarRevisada(comentario)
                        onDismiss()
                    }
                ) {
                    Text("Marcar como revisada")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Aceptar")
                }
            }
        },
        dismissButton = {
            if (!actividad.revisadaPorFamiliar && actividad.estado == EstadoActividad.REALIZADA) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}

/**
 * Devuelve un icono para cada categoría de actividad
 */
@Composable
fun getCategoriaIcon(categoria: CategoriaActividad): ImageVector {
    return when (categoria) {
        CategoriaActividad.JUEGO -> Icons.Default.Extension
        CategoriaActividad.MOTOR -> Icons.AutoMirrored.Filled.DirectionsRun
        CategoriaActividad.LENGUAJE -> Icons.Default.RecordVoiceOver
        CategoriaActividad.MUSICA -> Icons.Default.MusicNote
        CategoriaActividad.ARTE -> Icons.Default.Palette
        CategoriaActividad.EXPLORACION -> Icons.Default.Search
        CategoriaActividad.AUTONOMIA -> Icons.Default.EmojiPeople
        CategoriaActividad.OTRA -> Icons.Default.Lightbulb
    }
} 