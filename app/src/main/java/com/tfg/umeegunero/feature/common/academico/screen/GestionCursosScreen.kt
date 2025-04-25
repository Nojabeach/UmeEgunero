package com.tfg.umeegunero.feature.common.academico.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
    centroId: String? = null,
    viewModel: GestionCursosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var expandedCentroMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(centroId) {
        if (!centroId.isNullOrEmpty()) {
            viewModel.seleccionarCentro(centroId)
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
            if (uiState.isAdminApp) {
                // Selector de centro para admin
                ExposedDropdownMenuBox(
                    expanded = expandedCentroMenu,
                    onExpandedChange = { expandedCentroMenu = !expandedCentroMenu }
                ) {
                    OutlinedTextField(
                        value = uiState.centroSeleccionado?.nombre ?: "Seleccionar centro",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Centro Educativo") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCentroMenu)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCentroMenu,
                        onDismissRequest = { expandedCentroMenu = false }
                    ) {
                        uiState.centros.forEach { centro ->
                            DropdownMenuItem(
                                text = { Text(centro.nombre) },
                                onClick = { 
                                    viewModel.seleccionarCentro(centro.id)
                                    expandedCentroMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.isLoading) {
                LoadingIndicator()
            } else if (uiState.cursos.isEmpty() && uiState.centroSeleccionado != null) {
                EmptyCursosMessage(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else if (uiState.centroSeleccionado == null && !uiState.isAdminApp) {
                // Mensaje cuando no hay centro seleccionado
                Text(
                    text = "Seleccione un centro para gestionar sus cursos",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(uiState.cursos) { _, curso ->
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
        Icon(
            imageVector = Icons.AutoMirrored.Filled.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = AcademicoColor.copy(alpha = 0.5f)
        )
        
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
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onItemClick,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Encabezado coloreado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AcademicoColor.copy(alpha = 0.8f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = curso.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Contenido de la tarjeta
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = curso.descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                FilledTonalIconButton(
                    onClick = onEditClick,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar curso"
                    )
                }
            }
        }
    }
} 