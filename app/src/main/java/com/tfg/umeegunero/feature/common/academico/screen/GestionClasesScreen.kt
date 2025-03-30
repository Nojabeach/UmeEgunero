package com.tfg.umeegunero.feature.common.academico.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.feature.common.academico.viewmodel.GestionClasesViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.components.LoadingIndicator
import com.tfg.umeegunero.ui.theme.AcademicoColor
import com.tfg.umeegunero.ui.theme.AcademicoColorDark

/**
 * Pantalla para la gestión de clases
 * Permite ver la lista de clases, añadir nuevas y editar existentes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionClasesScreen(
    navController: NavController,
    cursoId: String,
    viewModel: GestionClasesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Efecto para mostrar Snackbar cuando hay un error
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            snackbarHostState.showSnackbar(
                message = uiState.error ?: "Error desconocido",
                duration = SnackbarDuration.Short
            )
            viewModel.limpiarError()
        }
    }
    
    // Efecto para mostrar Snackbar cuando hay éxito
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar(
                message = uiState.successMessage ?: "Operación completada con éxito",
                duration = SnackbarDuration.Short
            )
            viewModel.limpiarExito()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Gestión de Clases",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                },
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
            ExtendedFloatingActionButton(
                onClick = { 
                    // Navegar a la pantalla de añadir clase
                    navController.navigate(AppScreens.AddClase.createRoute(cursoId, "0"))
                },
                icon = { 
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir clase"
                    ) 
                },
                text = { Text("Añadir Clase") },
                containerColor = AcademicoColor,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mostrar el contenido principal
            if (uiState.clases.isEmpty() && !uiState.isLoading) {
                // No hay clases que mostrar
                EmptyClasesMessage(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    cursoId = cursoId
                )
            } else {
                // Mostrar la lista de clases
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(uiState.clases) { index, clase ->
                        val animatedVisibilityState = remember {
                            MutableTransitionState(false).apply { 
                                targetState = true 
                            }
                        }
                        
                        AnimatedVisibility(
                            visibleState = animatedVisibilityState,
                            enter = slideInVertically(
                                initialOffsetY = { it * (index + 1) / 8 },
                                animationSpec = tween(durationMillis = 300, delayMillis = index * 50)
                            ) + fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = index * 50)),
                            exit = fadeOut()
                        ) {
                            ClaseItem(
                                clase = clase,
                                onEditClick = {
                                    // Navegar a la pantalla de editar clase con el ID de la clase
                                    navController.navigate(AppScreens.EditClase.createRoute(clase.id))
                                },
                                onDeleteClick = {
                                    viewModel.mostrarDialogoEliminar(clase)
                                },
                                onItemClick = {
                                    // Navegar a la pantalla de detalle de la clase
                                    navController.navigate(AppScreens.Dummy.createRoute("Detalle de Clase: ${clase.nombre}"))
                                }
                            )
                        }
                    }
                    
                    // Espacio adicional al final para evitar que el FAB tape el último elemento
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
            
            // Mostrar el indicador de carga si está cargando
            if (uiState.isLoading) {
                LoadingIndicator(
                    isLoading = true,
                    message = "Cargando clases..."
                )
            }
            
            // Mostrar diálogo de confirmación de eliminación si está visible
            if (uiState.isDeleteDialogVisible) {
                DeleteConfirmationDialog(
                    clase = uiState.selectedClase!!,
                    onConfirm = { viewModel.eliminarClase(uiState.selectedClase!!.id) },
                    onDismiss = { viewModel.ocultarDialogoEliminar() }
                )
            }
        }
    }
}

@Composable
private fun EmptyClasesMessage(
    modifier: Modifier = Modifier,
    cursoId: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.School,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = AcademicoColor.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Gestión de Clases",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Curso ID: $cursoId",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Aquí podrá gestionar todas las clases del curso académico seleccionado",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Utilice el botón + para añadir una nueva clase o grupo",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    clase: Clase,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { 
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            ) 
        },
        title = { 
            Text(
                "Confirmar eliminación",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            ) 
        },
        text = { 
            Text(
                "¿Estás seguro de que quieres eliminar la clase ${clase.nombre}? Esta acción no se puede deshacer.",
                style = MaterialTheme.typography.bodyMedium
            ) 
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            FilledTonalButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClaseItem(
    clase: Clase,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
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
                    text = clase.nombre,
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
                            imageVector = Icons.Default.Room,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Aula: ${clase.aula}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Horario: ${clase.horario.ifBlank { "No especificado" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Row {
                    FilledTonalIconButton(
                        onClick = onEditClick,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar clase"
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    FilledTonalIconButton(
                        onClick = onDeleteClick,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar clase"
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GestionClasesScreenPreview() {
    val clasesPreview = listOf(
        Clase(
            id = "1",
            nombre = "1ºA",
            cursoId = "curso1",
            aula = "Aula 101",
            profesorTitularId = "profesor1",
            centroId = "centro1",
            activo = true,
            capacidadMaxima = 25,
            horario = "Mañana",
            profesoresAuxiliaresIds = emptyList(),
            alumnosIds = emptyList()
        ),
        Clase(
            id = "2",
            nombre = "1ºB",
            cursoId = "curso1",
            aula = "Aula 102",
            profesorTitularId = "profesor2",
            centroId = "centro1",
            activo = true,
            capacidadMaxima = 25,
            horario = "Tarde",
            profesoresAuxiliaresIds = emptyList(),
            alumnosIds = emptyList()
        )
    )
    
    MaterialTheme {
        GestionClasesScreen(
            navController = rememberNavController(),
            cursoId = ""
        )
    }
} 