package com.tfg.umeegunero.feature.common.academico.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.feature.common.academico.viewmodel.GestionCursosViewModel
import com.tfg.umeegunero.feature.common.academico.viewmodel.OrdenCursos
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.components.LoadingIndicator
import com.tfg.umeegunero.ui.theme.AcademicoColor

/**
 * Pantalla para gestionar los cursos académicos de un centro educativo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionCursosScreen(
    navController: NavController,
    viewModel: GestionCursosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var expandedCentroMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        if (uiState.isAdminApp) {
            viewModel.cargarCentros()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Cursos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AcademicoColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (uiState.centroSeleccionado != null) {
                ExtendedFloatingActionButton(
                    onClick = { 
                        navController.navigate(AppScreens.AddCurso.createRoute(uiState.centroSeleccionado?.id ?: ""))
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Añadir") },
                    text = { Text("Añadir Curso") },
                    containerColor = AcademicoColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Selector de centro siempre visible
            Text(
                text = "Selecciona un centro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            ExposedDropdownMenuBox(
                expanded = expandedCentroMenu,
                onExpandedChange = { expandedCentroMenu = !expandedCentroMenu }
            ) {
                OutlinedTextField(
                    value = uiState.centroSeleccionado?.nombre ?: "Seleccionar centro",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Centro Educativo") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = AcademicoColor
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCentroMenu)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedBorderColor = AcademicoColor,
                        unfocusedBorderColor = AcademicoColor.copy(alpha = 0.5f)
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandedCentroMenu,
                    onDismissRequest = { expandedCentroMenu = false }
                ) {
                    uiState.centros.forEach { centro ->
                        DropdownMenuItem(
                            text = { 
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = centro.nombre,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    if (!centro.activo) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.errorContainer,
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Text(
                                                text = "Inactivo",
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            onClick = { 
                                viewModel.seleccionarCentro(centro.id)
                                expandedCentroMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = if (centro.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            if (uiState.centros.isEmpty()) {
                Text(
                    text = "No hay centros disponibles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contenido principal
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            } else if (uiState.cursos.isEmpty()) {
                if (uiState.centroSeleccionado != null) {
                    EmptyCursosMessage(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = if (uiState.isAdminApp) 
                                    "Selecciona un centro para ver sus cursos" 
                                else 
                                    "No tienes acceso a ningún centro",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    // Barra de búsqueda y filtros
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar cursos...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filtros y ordenación
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Switch para mostrar solo activos
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Switch(
                                checked = uiState.mostrarSoloActivos,
                                onCheckedChange = { viewModel.toggleMostrarSoloActivos() }
                            )
                            Text(
                                text = "Solo activos",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        // Menú de ordenación
                        var showOrdenMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showOrdenMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Ordenar"
                                )
                            }
                            DropdownMenu(
                                expanded = showOrdenMenu,
                                onDismissRequest = { showOrdenMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Nombre (A-Z)") },
                                    onClick = {
                                        viewModel.updateOrden(OrdenCursos.NOMBRE_ASC)
                                        showOrdenMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Nombre (Z-A)") },
                                    onClick = {
                                        viewModel.updateOrden(OrdenCursos.NOMBRE_DESC)
                                        showOrdenMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Edad (Menor a Mayor)") },
                                    onClick = {
                                        viewModel.updateOrden(OrdenCursos.EDAD_ASC)
                                        showOrdenMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Edad (Mayor a Menor)") },
                                    onClick = {
                                        viewModel.updateOrden(OrdenCursos.EDAD_DESC)
                                        showOrdenMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Año Académico (Antiguo a Nuevo)") },
                                    onClick = {
                                        viewModel.updateOrden(OrdenCursos.AÑO_ACADEMICO_ASC)
                                        showOrdenMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Año Académico (Nuevo a Antiguo)") },
                                    onClick = {
                                        viewModel.updateOrden(OrdenCursos.AÑO_ACADEMICO_DESC)
                                        showOrdenMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contador de cursos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cursos disponibles",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "${uiState.cursosFiltrados.size} de ${uiState.cursos.size} cursos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    // Lista de cursos filtrados
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(uiState.cursosFiltrados) { _, curso ->
                            CursoItem(
                                curso = curso,
                                onEditClick = {
                                    navController.navigate(AppScreens.EditCurso.createRoute(curso.id))
                                },
                                onItemClick = {
                                    navController.navigate(AppScreens.DetalleCurso.createRoute(curso.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(message = error)
            viewModel.clearError()
        }
    }
}

@Composable
private fun EmptyCursosMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            color = AcademicoColor.copy(alpha = 0.5f),
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.surface
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No hay cursos disponibles",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Pulsa el botón + para añadir un nuevo curso",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CursoItem(
    curso: Curso,
    onEditClick: () -> Unit,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onItemClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Encabezado
            Surface(
                color = AcademicoColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = curso.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Contenido
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Descripción
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = curso.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Información adicional
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Año académico
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = curso.anioAcademico,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Rango de edad
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${curso.edadMinima}-${curso.edadMaxima} años",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Estado del curso
                if (!curso.activo) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "Inactivo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Botón de edición
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar curso",
                        tint = AcademicoColor
                    )
                }
            }
        }
    }
} 