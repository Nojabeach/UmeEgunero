package com.tfg.umeegunero.feature.common.academico.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.common.academico.viewmodel.GestorAcademicoViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.tfg.umeegunero.ui.theme.AcademicoColor
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.automirrored.filled.MenuBook
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.ui.components.DefaultTopAppBar
import com.tfg.umeegunero.navigation.AppScreens

enum class ModoVisualizacion { CURSOS, CLASES }

/**
 * Pantalla unificada para la gestión de Cursos y Clases.
 *
 * Muestra una lista de cursos o clases pertenecientes a un centro específico,
 * permitiendo la selección, adición, edición y eliminación de elementos.
 * El comportamiento y la apariencia se adaptan según el [modo] especificado.
 *
 * @param modo Indica si se deben mostrar CURSOS o CLASES.
 * @param centroId ID del centro cuyos cursos/clases se mostrarán. Requerido si el perfil es ADMIN_CENTRO.
 * @param cursoId ID del curso cuyas clases se mostrarán (solo aplica si modo es CLASES).
 * @param selectorCentroBloqueado Si es true, el selector de centro estará deshabilitado (útil si se navega desde un centro específico).
 * @param selectorCursoBloqueado Si es true, el selector de curso estará deshabilitado (útil si se navega desde un curso específico).
 * @param perfilUsuario Tipo de perfil del usuario actual (ADMIN_APP, ADMIN_CENTRO) para determinar permisos y visibilidad.
 * @param viewModel ViewModel que gestiona el estado y la lógica de esta pantalla.
 * @param onNavigate Lambda para gestionar la navegación a otras pantallas (ej. añadir/editar curso/clase, volver atrás).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestorAcademicoScreen(
    modo: ModoVisualizacion,
    centroId: String?,
    cursoId: String?,
    selectorCentroBloqueado: Boolean,
    selectorCursoBloqueado: Boolean,
    perfilUsuario: TipoUsuario,
    viewModel: GestorAcademicoViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val centros = uiState.centros
    val cursos = uiState.cursos
    val clases = uiState.clases
    val selectedCentro = uiState.selectedCentro
    val selectedCurso = uiState.selectedCurso
    val isLoadingCursos = uiState.isLoadingCursos
    val isLoadingClases = uiState.isLoadingClases
    val error = uiState.error
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<String?>(null) }
    var deleteType by remember { mutableStateOf("") } // "curso" o "clase"

    // Mostrar error en Snackbar
    LaunchedEffect(error) {
        error?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
        }
    }

    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = if (modo == ModoVisualizacion.CURSOS) "Gestión de Cursos" else "Gestión de Clases",
                showBackButton = true,
                onBackClick = { onNavigate("back") },
                containerColor = AcademicoColor,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (perfilUsuario == TipoUsuario.ADMIN_APP || perfilUsuario == TipoUsuario.ADMIN_CENTRO) {
                val fabEnabled = if (modo == ModoVisualizacion.CURSOS) {
                    selectedCentro != null
                } else {
                    selectedCurso != null
                }
                
                // Solo mostrar el FAB si está habilitado
                if (fabEnabled) {
                    FloatingActionButton(
                        onClick = {
                            if (modo == ModoVisualizacion.CURSOS) {
                                onNavigate("add_curso?centroId=${selectedCentro?.id ?: centroId}")
                            } else {
                                onNavigate("add_clase?centroId=${selectedCentro?.id ?: centroId}&cursoId=${selectedCurso?.id ?: cursoId}")
                            }
                        },
                        containerColor = AcademicoColor
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = if (modo == ModoVisualizacion.CURSOS) "Añadir curso" else "Añadir clase",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).padding(paddingValues)) {
            // Selector de centro
            if (perfilUsuario == TipoUsuario.ADMIN_APP) {
                ExposedDropdownMenuBox(
                    expanded = uiState.centroMenuExpanded,
                    onExpandedChange = { viewModel.onCentroMenuExpandedChanged(it) }
                ) {
                    OutlinedTextField(
                        value = selectedCentro?.nombre ?: "Selecciona un centro",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Centro") },
                        enabled = !selectorCentroBloqueado,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.centroMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = uiState.centroMenuExpanded,
                        onDismissRequest = { viewModel.onCentroMenuExpandedChanged(false) }
                    ) {
                        centros.forEach { centro ->
                            DropdownMenuItem(
                                text = { Text(centro.nombre) },
                                onClick = { viewModel.onCentroSelected(centro) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            // Selector de curso si corresponde
            if (modo == ModoVisualizacion.CLASES) {
                ExposedDropdownMenuBox(
                    expanded = uiState.cursoMenuExpanded,
                    onExpandedChange = { viewModel.onCursoMenuExpandedChanged(it) }
                ) {
                    OutlinedTextField(
                        value = selectedCurso?.nombre ?: "Selecciona un curso",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Curso") },
                        enabled = !selectorCursoBloqueado,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.cursoMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = uiState.cursoMenuExpanded,
                        onDismissRequest = { viewModel.onCursoMenuExpandedChanged(false) }
                    ) {
                        cursos.forEach { curso ->
                            DropdownMenuItem(
                                text = { Text(curso.nombre) },
                                onClick = { viewModel.onCursoSelected(curso) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            // Lista de cursos o clases con animación y menú contextual
            if (isLoadingCursos && modo == ModoVisualizacion.CURSOS || isLoadingClases && modo == ModoVisualizacion.CLASES) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (modo == ModoVisualizacion.CURSOS && cursos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(64.dp), tint = AcademicoColor.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text("No hay cursos para este centro.", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { onNavigate("back") }) { Text("Volver") }
                    }
                }
            } else if (modo == ModoVisualizacion.CLASES && clases.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Class, contentDescription = null, modifier = Modifier.size(64.dp), tint = AcademicoColor.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text("No hay clases para este curso.", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { onNavigate("back") }) { Text("Volver") }
                    }
                }
            } else {
                AnimatedVisibility(
                    visible = (modo == ModoVisualizacion.CURSOS && cursos.isNotEmpty()) || (modo == ModoVisualizacion.CLASES && clases.isNotEmpty()),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (modo == ModoVisualizacion.CURSOS) {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(cursos) { curso ->
                                var expanded by remember { mutableStateOf(false) }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(4.dp),
                                    onClick = { onNavigate("gestor_academico/CLASES?centroId=${selectedCentro?.id ?: centroId}&cursoId=${curso.id}&selectorCentroBloqueado=$selectorCentroBloqueado&selectorCursoBloqueado=true&perfilUsuario=${perfilUsuario.name}") }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = curso.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(text = "ID: ${curso.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(onClick = { expanded = true }) {
                                            Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                                        }
                                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                            DropdownMenuItem(
                                                text = { Text("Editar") },
                                                onClick = { onNavigate("edit_curso/${curso.id}") },
                                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Eliminar") },
                                                onClick = {
                                                    itemToDelete = curso.id
                                                    deleteType = "curso"
                                                    showDeleteDialog = true
                                                    expanded = false
                                                },
                                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(clases) { clase ->
                                var expanded by remember { mutableStateOf(false) }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(4.dp),
                                    onClick = { onNavigate("detalle_clase/${clase.id}") }
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = clase.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                Text(text = "ID: ${clase.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(text = "Aula: ${clase.aula}", style = MaterialTheme.typography.bodySmall)
                                            }
                                            IconButton(onClick = { expanded = true }) {
                                                Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                                            }
                                        }
                                        
                                        // Agregar botones para vincular alumnos y profesor
                                        if (perfilUsuario == TipoUsuario.ADMIN_CENTRO || perfilUsuario == TipoUsuario.ADMIN_APP) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                OutlinedButton(
                                                    onClick = { 
                                                        // Usar la ruta de AddUser directamente con los parámetros adecuados
                                                        onNavigate(AppScreens.AddUser.createRoute(
                                                            isAdminApp = false,
                                                            tipoUsuario = "ALUMNO",
                                                            centroId = selectedCentro?.id,
                                                            centroBloqueado = true
                                                        ))
                                                    },
                                                    modifier = Modifier.padding(end = 8.dp)
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Agregar Alumnos")
                                                }
                                                
                                                OutlinedButton(
                                                    onClick = { 
                                                        onNavigate("vincular_profesor_clase/${clase.id}?cursoId=${selectedCurso?.id ?: cursoId}")
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Vincular Profesor")
                                                }
                                            }
                                        }
                                        
                                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                            DropdownMenuItem(
                                                text = { Text("Editar") },
                                                onClick = { onNavigate("edit_clase/${clase.id}") },
                                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Eliminar") },
                                                onClick = {
                                                    itemToDelete = clase.id
                                                    deleteType = "clase"
                                                    showDeleteDialog = true
                                                    expanded = false
                                                },
                                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // Diálogo de confirmación de eliminación
        if (showDeleteDialog && itemToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmar eliminación") },
                text = { Text("¿Estás seguro de que quieres eliminar este ${if (deleteType == "curso") "curso" else "clase"}? Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(onClick = {
                        if (deleteType == "curso") {
                            viewModel.eliminarCurso(itemToDelete!!)
                        } else {
                            viewModel.eliminarClase(itemToDelete!!)
                        }
                        showDeleteDialog = false
                        itemToDelete = null
                    }) { Text("Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        itemToDelete = null
                    }) { Text("Cancelar") }
                }
            )
        }
    }
}

// --- Preview añadida --- 
@Preview(showBackground = true, name = "Gestor Cursos")
@Composable
fun GestorAcademicoScreenCursosPreview() {
    UmeEguneroTheme {
        GestorAcademicoScreen(
            modo = ModoVisualizacion.CURSOS,
            centroId = "centro_test_id",
            cursoId = null,
            selectorCentroBloqueado = false,
            selectorCursoBloqueado = false,
            perfilUsuario = TipoUsuario.ADMIN_APP,
            // viewModel = viewModel() // No usar hiltViewModel en Preview
            onNavigate = {}
        )
    }
}

@Preview(showBackground = true, name = "Gestor Clases")
@Composable
fun GestorAcademicoScreenClasesPreview() {
    UmeEguneroTheme {
        GestorAcademicoScreen(
            modo = ModoVisualizacion.CLASES,
            centroId = "centro_test_id",
            cursoId = "curso_test_id",
            selectorCentroBloqueado = true, // Ejemplo: centro bloqueado
            selectorCursoBloqueado = false,
            perfilUsuario = TipoUsuario.ADMIN_CENTRO,
            // viewModel = viewModel() // No usar hiltViewModel en Preview
            onNavigate = {}
        )
    }
}
// --- Fin Preview añadida --- 