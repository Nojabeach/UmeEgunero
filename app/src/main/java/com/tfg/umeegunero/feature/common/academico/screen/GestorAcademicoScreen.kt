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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

enum class ModoVisualizacion { CURSOS, CLASES }

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
    val isLoading = uiState.isLoading
    val error = uiState.error
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Mostrar error en Snackbar
    LaunchedEffect(error) {
        error?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (modo == ModoVisualizacion.CURSOS) {
                    onNavigate("add_curso?centroId=${selectedCentro?.id ?: centroId}")
                } else {
                    onNavigate("add_clase?centroId=${selectedCentro?.id ?: centroId}&cursoId=${selectedCurso?.id ?: cursoId}")
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = null)
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
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (modo == ModoVisualizacion.CURSOS && cursos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay cursos para este centro.", style = MaterialTheme.typography.bodyLarge)
                }
            } else if (modo == ModoVisualizacion.CLASES && clases.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay clases para este curso.", style = MaterialTheme.typography.bodyLarge)
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
                                        .padding(vertical = 4.dp),
                                    onClick = { onNavigate("gestor_academico/CLASES?centroId=${selectedCentro?.id ?: centroId}&cursoId=${curso.id}&selectorCentroBloqueado=$selectorCentroBloqueado&selectorCursoBloqueado=true&perfilUsuario=${perfilUsuario.name}") }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = curso.nombre, style = MaterialTheme.typography.titleMedium)
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
                                                onClick = { /* viewModel.eliminarCurso(curso.id) */ },
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
                                        .padding(vertical = 4.dp),
                                    onClick = { /* Acción ver/editar clase */ }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = clase.nombre, style = MaterialTheme.typography.titleMedium)
                                        }
                                        IconButton(onClick = { expanded = true }) {
                                            Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                                        }
                                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                            DropdownMenuItem(
                                                text = { Text("Editar") },
                                                onClick = { onNavigate("edit_clase/${clase.id}") },
                                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Eliminar") },
                                                onClick = { /* viewModel.eliminarClase(clase.id) */ },
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
    }
} 