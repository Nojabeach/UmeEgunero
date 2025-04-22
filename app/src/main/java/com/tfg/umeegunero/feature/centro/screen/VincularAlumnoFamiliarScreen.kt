package com.tfg.umeegunero.feature.centro.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.SubtipoFamiliar
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.centro.viewmodel.VinculacionFamiliarViewModel
import com.tfg.umeegunero.ui.components.SearchBar
import com.tfg.umeegunero.ui.theme.Green500
import com.tfg.umeegunero.ui.theme.Red500
import kotlinx.coroutines.launch

/**
 * Pantalla para vincular alumnos a familiares
 * 
 * Esta pantalla permite al administrador del centro vincular alumnos con sus familiares,
 * facilitando la gestión de relaciones familiares en el sistema.
 *
 * @param onBackClick Función para volver a la pantalla anterior
 * @param onDashboardClick Función para ir al dashboard del centro
 * @param viewModel ViewModel para manejar la lógica de la pantalla
 * 
 * @author Maitane (Estudiante 2º DAM)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VincularAlumnoFamiliarScreen(
    onBackClick: () -> Unit,
    onDashboardClick: () -> Unit,
    viewModel: VinculacionFamiliarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    var showFamiliarDialog by remember { mutableStateOf(false) }
    var showTipoParentescoDialog by remember { mutableStateOf(false) }
    var selectedAlumno by remember { mutableStateOf<Alumno?>(null) }
    var selectedFamiliar by remember { mutableStateOf<Usuario?>(null) }
    var selectedParentesco by remember { mutableStateOf<SubtipoFamiliar?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDesvincularDialog by remember { mutableStateOf(false) }
    var searchAlumnoQuery by remember { mutableStateOf("") }
    var searchFamiliarQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        viewModel.cargarDatosIniciales()
    }
    
    // Mostrar mensajes de éxito
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            scope.launch {
                viewModel.clearMensaje()
            }
        }
    }
    
    // Mostrar errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                viewModel.clearError()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vincular Alumnos y Familiares") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onDashboardClick) {
                        Icon(Icons.Default.Home, contentDescription = "Ir al Dashboard")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Indicador de carga
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Mensaje de error o éxito
                    uiState.mensaje?.let { mensaje ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Green500.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Green500
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(mensaje)
                            }
                        }
                    }
                    
                    uiState.error?.let { error ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Red500.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Red500
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(error)
                            }
                        }
                    }
                    
                    // Selección de alumno
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Seleccionar Alumno",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Barra de búsqueda de alumnos
                            SearchBar(
                                query = searchAlumnoQuery,
                                onQueryChange = { 
                                    searchAlumnoQuery = it
                                    viewModel.updateFiltroAlumnos(it)
                                },
                                onSearch = { viewModel.updateFiltroAlumnos(it) },
                                placeholder = "Buscar alumno..."
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Lista de alumnos
                            if (uiState.alumnosFiltrados.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No se encontraron alumnos")
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                ) {
                                    items(uiState.alumnosFiltrados) { alumno ->
                                        AlumnoItem(
                                            alumno = alumno,
                                            isSelected = selectedAlumno?.dni == alumno.dni,
                                            onClick = {
                                                selectedAlumno = alumno
                                                viewModel.cargarFamiliaresPorAlumno(alumno.dni)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Familiares vinculados al alumno seleccionado
                    if (selectedAlumno != null && uiState.familiaresDelAlumno.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Familiares vinculados a ${selectedAlumno?.nombre} ${selectedAlumno?.apellidos}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Lista de familiares vinculados
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                ) {
                                    items(uiState.familiaresDelAlumno) { familiar ->
                                        FamiliarVinculadoItem(
                                            familiar = familiar,
                                            parentesco = selectedAlumno?.familiares?.find { it.id == familiar.dni }?.parentesco ?: "",
                                            onDesvincular = {
                                                selectedFamiliar = familiar
                                                showDesvincularDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Vincular nuevo familiar
                    if (selectedAlumno != null) {
                        Button(
                            onClick = { showFamiliarDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Vincular Nuevo Familiar")
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo para seleccionar familiar
    if (showFamiliarDialog) {
        AlertDialog(
            onDismissRequest = { showFamiliarDialog = false },
            title = { Text("Seleccionar Familiar") },
            text = {
                Column {
                    // Barra de búsqueda de familiares
                    SearchBar(
                        query = searchFamiliarQuery,
                        onQueryChange = { 
                            searchFamiliarQuery = it
                            viewModel.updateFiltroFamiliares(it)
                        },
                        onSearch = { viewModel.updateFiltroFamiliares(it) },
                        placeholder = "Buscar familiar..."
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Lista de familiares disponibles
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        items(uiState.familiaresFiltrados) { familiar ->
                            FamiliarItem(
                                familiar = familiar,
                                onClick = {
                                    selectedFamiliar = familiar
                                    showFamiliarDialog = false
                                    showTipoParentescoDialog = true
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFamiliarDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para seleccionar tipo de parentesco
    if (showTipoParentescoDialog) {
        AlertDialog(
            onDismissRequest = { showTipoParentescoDialog = false },
            title = { Text("Tipo de Parentesco") },
            text = {
                Column {
                    SubtipoFamiliar.values().forEach { parentesco ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedParentesco = parentesco
                                    showTipoParentescoDialog = false
                                    showConfirmDialog = true
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedParentesco == parentesco,
                                onClick = {
                                    selectedParentesco = parentesco
                                    showTipoParentescoDialog = false
                                    showConfirmDialog = true
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(parentesco.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTipoParentescoDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo de confirmación para vincular
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar Vinculación") },
            text = {
                Text(
                    "¿Desea vincular a ${selectedFamiliar?.nombre} ${selectedFamiliar?.apellidos} " +
                    "como ${selectedParentesco?.name} de ${selectedAlumno?.nombre} ${selectedAlumno?.apellidos}?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedAlumno?.let { alumno ->
                            selectedFamiliar?.let { familiar ->
                                selectedParentesco?.let { parentesco ->
                                    viewModel.vincularFamiliar(
                                        alumnoDni = alumno.dni,
                                        familiarDni = familiar.dni,
                                        tipoParentesco = parentesco
                                    )
                                }
                            }
                        }
                        showConfirmDialog = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo de confirmación para desvincular
    if (showDesvincularDialog) {
        AlertDialog(
            onDismissRequest = { showDesvincularDialog = false },
            title = { Text("Confirmar Desvinculación") },
            text = {
                Text(
                    "¿Está seguro de que desea desvincular a ${selectedFamiliar?.nombre} ${selectedFamiliar?.apellidos} " +
                    "de ${selectedAlumno?.nombre} ${selectedAlumno?.apellidos}?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedAlumno?.let { alumno ->
                            selectedFamiliar?.let { familiar ->
                                viewModel.desvincularFamiliar(
                                    alumnoId = alumno.dni,
                                    familiarId = familiar.dni
                                )
                            }
                        }
                        showDesvincularDialog = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDesvincularDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AlumnoItem(
    alumno: Alumno,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${alumno.nombre} ${alumno.apellidos}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "DNI: ${alumno.dni}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Text(
                    text = "Curso: ${alumno.curso}, Clase: ${alumno.clase}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun FamiliarItem(
    familiar: Usuario,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PersonOutline,
                contentDescription = null
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${familiar.nombre} ${familiar.apellidos}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "DNI: ${familiar.dni}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Text(
                    text = "Email: ${familiar.email}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun FamiliarVinculadoItem(
    familiar: Usuario,
    parentesco: String,
    onDesvincular: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = parentesco.take(1),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${familiar.nombre} ${familiar.apellidos}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Parentesco: $parentesco",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Text(
                    text = "DNI: ${familiar.dni}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            IconButton(onClick = onDesvincular) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Desvincular",
                    tint = Red500
                )
            }
        }
    }
} 