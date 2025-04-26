package com.tfg.umeegunero.feature.familiar.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyItemScope
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.ActividadPreescolar
import com.tfg.umeegunero.data.model.CategoriaActividad
import com.tfg.umeegunero.data.model.EstadoActividad
// Importar el ViewModel correcto y los filtros para Familiar
import com.tfg.umeegunero.feature.familiar.viewmodel.ActividadesPreescolarViewModel
import com.tfg.umeegunero.feature.familiar.viewmodel.FiltroActividad
import com.tfg.umeegunero.ui.theme.colorCategoriaActividad

/**
 * Pantalla para que los familiares visualicen actividades preescolares de sus hijos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActividadesPreescolarScreen(
    familiarId: String, // Cambiado de profesorId
    onBackClick: () -> Unit,
    viewModel: ActividadesPreescolarViewModel = hiltViewModel() // ViewModel para Familiar
) {
    val uiState by viewModel.uiState.collectAsState()

    // Inicializar ViewModel cuando se inicia la pantalla
    LaunchedEffect(familiarId) {
        viewModel.inicializar(familiarId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actividades Preescolares") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                // Eliminar acción de añadir
                actions = { /* No actions needed for familiar view */ },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        // Eliminar FloatingActionButton
        floatingActionButton = { /* No FAB needed for familiar view */ }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    // Indicador de carga
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    // Mensaje de error
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = uiState.error ?: "Error desconocido",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                // Actualizar la llamada a inicializar, quitar alumnoId
                                onClick = { viewModel.inicializar(familiarId) }
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Eliminar ClaseSelector

                        // Filtros adaptados para Familiar
                        FiltersSection(
                            filtroSeleccionado = uiState.filtroSeleccionado,
                            categoriaSeleccionada = uiState.categoriaSeleccionada,
                            onFiltroSelected = { viewModel.aplicarFiltro(it) },
                            onCategoriaSelected = { viewModel.aplicarFiltroCategoria(it) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Lista de actividades
                        if (uiState.actividadesFiltradas.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No hay actividades para mostrar",
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center
                                    )
                                    // Mensaje adaptado para familiar
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Consulta más tarde si hay nuevas actividades.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 16.dp)
                            ) {
                                items(uiState.actividadesFiltradas) { actividad ->
                                    // Usar ActividadItemFamiliar sin onClick de edición
                                    ActividadItemFamiliar(
                                        actividad = actividad
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    // Eliminar el diálogo de creación/edición
}

/**
 * Sección de filtros para actividades (Re-añadida)
 */
@OptIn(ExperimentalMaterial3Api::class)
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

        // Chips de filtros por estado (Usando Segmented Buttons como ejemplo anterior)
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
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

        Spacer(modifier = Modifier.height(8.dp))

        // Chips de categorías (Usando LazyRow como ejemplo anterior)
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
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else null
                )
            }

            // Chips para cada categoría
            items(CategoriaActividad.values()) { categoria ->
                // Omitir 'OTRA' si no se desea mostrar
                 if (categoria != CategoriaActividad.OTRA) {
                    FilterChip(
                        selected = categoriaSeleccionada == categoria,
                        onClick = { onCategoriaSelected(categoria) },
                        label = { Text(categoria.display_name) }, // Usar nombre legible
                        leadingIcon = if (categoriaSeleccionada == categoria) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorCategoriaActividad(categoria.name).copy(alpha = 0.2f),
                            selectedLabelColor = colorCategoriaActividad(categoria.name)
                        )
                    )
                }
            }
        }
    }
}

// Renombrar y adaptar el item para Familiar
@Composable
fun ActividadItemFamiliar(
    actividad: ActividadPreescolar
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // Eliminar onClick para edición
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de categoría (sin cambios)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorCategoriaActividad(actividad.categoria.name))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (actividad.categoria) {
                        CategoriaActividad.JUEGO -> Icons.Default.VideogameAsset
                        CategoriaActividad.MOTOR -> Icons.AutoMirrored.Filled.DirectionsRun
                        CategoriaActividad.LENGUAJE -> Icons.Default.RecordVoiceOver
                        CategoriaActividad.MUSICA -> Icons.Default.MusicNote
                        CategoriaActividad.ARTE -> Icons.Default.Palette
                        CategoriaActividad.EXPLORACION -> Icons.Default.Explore
                        CategoriaActividad.AUTONOMIA -> Icons.Default.PersonOutline
                        CategoriaActividad.OTRA -> Icons.Default.MoreHoriz
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Contenido de la actividad (sin cambios)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = actividad.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = actividad.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2, // O permitir más líneas si es necesario
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Estado (sin cambios)
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = when (actividad.estado) {
                                    EstadoActividad.PENDIENTE -> "Pendiente"
                                    EstadoActividad.REALIZADA -> "Realizada"
                                    EstadoActividad.CANCELADA -> "Cancelada" // Podría ocultarse si no es relevante
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = when (actividad.estado) {
                                EstadoActividad.PENDIENTE -> MaterialTheme.colorScheme.errorContainer
                                EstadoActividad.REALIZADA -> MaterialTheme.colorScheme.primaryContainer
                                EstadoActividad.CANCELADA -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    )

                    // Fecha (sin cambios)
                    Text(
                        text = actividad.fechaCreacion.let {
                            try {
                                java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(it.toDate())
                            } catch (e: Exception) {
                                "Fecha inválida"
                            }
                        } ?: "Sin fecha",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Eliminar IconButton de edición
        }
    }
}

// Helper para nombre legible de Categoría (podría ir en el modelo o utils)
val CategoriaActividad.display_name: String
    get() = this.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() } 