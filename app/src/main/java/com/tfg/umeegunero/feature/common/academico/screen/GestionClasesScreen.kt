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
import com.tfg.umeegunero.data.model.Curso
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
    centroId: String = "",
    cursoId: String? = null,
    selectorCursoBloqueado: Boolean = false,
    viewModel: GestionClasesViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val cursos = uiState.cursos
    val selectedCurso = uiState.selectedCurso
    val clases = uiState.clases
    val isLoading = uiState.isLoading
    val isLoadingCursos = uiState.isLoadingCursos
    val error = uiState.error

    Column(modifier = Modifier.fillMaxSize()) {
        // Cabecera con selector de curso
        if (isLoadingCursos) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gestión de Clases",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                if (cursos.isNotEmpty()) {
                    if (selectorCursoBloqueado) {
                        // Selector bloqueado
                        Text(
                            text = selectedCurso?.nombre ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    } else {
                        // Selector editable
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { expanded = true }) {
                                Text(selectedCurso?.nombre ?: "Selecciona un curso")
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                cursos.forEach { curso ->
                                    DropdownMenuItem(
                                        text = { Text(curso.nombre) },
                                        onClick = {
                                            viewModel.onCursoSelected(curso)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        // Subcabecera con info del curso y profesor
        if (selectedCurso != null) {
            val profesorId = clases.firstOrNull()?.profesorTitularId ?: ""
            val numAlumnos = clases.firstOrNull()?.alumnosIds?.size ?: 0
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(text = "Curso: ${selectedCurso.nombre}", style = MaterialTheme.typography.bodyLarge)
                if (profesorId.isNotBlank()) {
                    Text(text = "Profesor ID: $profesorId", style = MaterialTheme.typography.bodyMedium)
                }
                Text(text = "Alumnos: $numAlumnos", style = MaterialTheme.typography.bodySmall)
            }
        }
        // Listado de clases
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (clases.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay clases para este curso")
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(clases) { clase ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = clase.nombre, style = MaterialTheme.typography.titleMedium)
                            Text(text = "Profesor ID: ${clase.profesorTitularId}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Alumnos: ${clase.alumnosIds.size}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        // Error
        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
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
    Card(
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

@Composable
fun CursoSelector(
    cursos: List<Curso>,
    selectedCurso: Curso?,
    onCursoSelected: (String) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = selectedCurso?.nombre ?: "Seleccionar curso",
        onValueChange = {},
        readOnly = true,
        enabled = enabled,
        label = { Text("Curso") },
        trailingIcon = {
            if (enabled) {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
    DropdownMenu(
        expanded = expanded && enabled,
        onDismissRequest = { expanded = false }
    ) {
        cursos.forEach { curso ->
            DropdownMenuItem(
                text = { Text(curso.nombre) },
                onClick = {
                    onCursoSelected(curso.id)
                    expanded = false
                }
            )
        }
    }
}

// @Preview(showBackground = true)
// @Composable
// fun GestionClasesScreenPreview() {
//     val clasesPreview = listOf(
//         Clase(
//             id = "1",
//             nombre = "1ºA",
//             cursoId = "curso1",
//             aula = "Aula 101",
//             profesorTitularId = "profesor1",
//             centroId = "centro1",
//             activo = true,
//             capacidadMaxima = 25,
//             horario = "Mañana",
//             profesoresAuxiliaresIds = emptyList(),
//             alumnosIds = listOf("al1", "al2")
//         ),
//         Clase(
//             id = "2",
//             nombre = "1ºB",
//             cursoId = "curso1",
//             aula = "Aula 102",
//             profesorTitularId = "profesor2",
//             centroId = "centro1",
//             activo = true,
//             capacidadMaxima = 25,
//             horario = "Tarde",
//             profesoresAuxiliaresIds = emptyList(),
//             alumnosIds = listOf("al3")
//         )
//     )
//     val cursosPreview = listOf(
//         Curso(id = "curso1", nombre = "Primero ESO", centroId = "centro1", activo = true),
//         Curso(id = "curso2", nombre = "Segundo ESO", centroId = "centro1", activo = true)
//     )
//     MaterialTheme {
//         Column {
//             Text("Preview Gestión de Clases")
//         }
//     }
// } 