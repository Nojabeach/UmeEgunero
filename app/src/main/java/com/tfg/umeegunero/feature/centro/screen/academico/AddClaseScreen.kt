package com.tfg.umeegunero.feature.centro.screen.academico

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.centro.viewmodel.AddClaseUiState
import com.tfg.umeegunero.feature.centro.viewmodel.AddClaseViewModel
import com.tfg.umeegunero.ui.theme.CentroColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

// Clases de datos para el preview
data class CursoSimpleModel(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val edadMinima: Int,
    val edadMaxima: Int
)

data class ProfesorSimpleModel(
    val documentId: String,
    val nombre: String,
    val apellidos: String,
    val especialidad: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClaseScreen(
    viewModel: AddClaseViewModel,
    claseId: String = "",
    centroId: String = "",
    onNavigateBack: () -> Unit,
    onClaseAdded: () -> Unit
) {
    // TODO: Mejoras pendientes para la pantalla de añadir/editar clase
    // - Implementar asignación masiva de alumnos por listado
    // - Añadir visualización de horario semanal de la clase
    // - Mostrar estadísticas de capacidad y ocupación
    // - Implementar sistema de notificaciones específicas para la clase
    // - Añadir gestión de material escolar necesario para el aula
    // - Permitir configuración de espacios físicos asociados (aulas)
    // - Mostrar vista previa de la distribución del espacio físico
    // - Implementar indicadores de diversidad y necesidades especiales
    // - Añadir integración con calendario de actividades del centro
    
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Efecto para cargar la clase si estamos en modo edición
    LaunchedEffect(claseId) {
        if (claseId.isNotBlank()) {
            viewModel.loadClase(claseId)
        }
    }

    // Efecto para establecer el ID del centro
    LaunchedEffect(centroId) {
        if (centroId.isNotBlank()) {
            viewModel.setCentroId(centroId)
        }
    }

    // Efecto para mostrar mensajes de error
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Efecto para navegar de vuelta cuando se guarda con éxito
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onClaseAdded()
            viewModel.resetSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Editar Clase" else "Añadir Clase",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CentroColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono de clase
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "Clase",
                tint = CentroColor,
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )

            // Formulario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Selector de curso
                    CursoDropdown(
                        cursos = uiState.cursosDisponibles,
                        selectedCursoId = uiState.cursoId,
                        onCursoSelected = viewModel::updateCursoId,
                        isError = uiState.cursoError != null,
                        errorMessage = uiState.cursoError,
                        isLoading = uiState.isLoadingCursos
                    )

                    // Nombre de la clase
                    OutlinedTextField(
                        value = uiState.nombre,
                        onValueChange = viewModel::updateNombre,
                        label = { Text("Nombre de la clase *") },
                        isError = uiState.nombreError != null,
                        supportingText = { uiState.nombreError?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Selector de profesor titular
                    ProfesorDropdown(
                        profesores = uiState.profesoresDisponibles,
                        selectedProfesorId = uiState.profesorTitularId,
                        onProfesorSelected = viewModel::updateProfesorTitular,
                        isError = uiState.profesorTitularError != null,
                        errorMessage = uiState.profesorTitularError,
                        isLoading = uiState.isLoadingProfesores,
                        label = "Profesor titular *"
                    )

                    // Profesores auxiliares
                    Text(
                        text = "Profesores auxiliares",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )

                    ProfesoresAuxiliaresSelector(
                        profesores = uiState.profesoresDisponibles,
                        selectedProfesorIds = uiState.profesoresAuxiliaresIds,
                        onProfesorSelected = { profesorId, isSelected ->
                            if (isSelected) {
                                viewModel.addProfesorAuxiliar(profesorId)
                            } else {
                                viewModel.removeProfesorAuxiliar(profesorId)
                            }
                        },
                        isLoading = uiState.isLoadingProfesores
                    )

                    // Capacidad máxima
                    OutlinedTextField(
                        value = uiState.capacidadMaxima,
                        onValueChange = viewModel::updateCapacidadMaxima,
                        label = { Text("Capacidad máxima *") },
                        isError = uiState.capacidadMaximaError != null,
                        supportingText = { uiState.capacidadMaximaError?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Horario
                    OutlinedTextField(
                        value = uiState.horario,
                        onValueChange = viewModel::updateHorario,
                        label = { Text("Horario") },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Aula
                    OutlinedTextField(
                        value = uiState.aula,
                        onValueChange = viewModel::updateAula,
                        label = { Text("Aula *") },
                        isError = uiState.aulaError != null,
                        supportingText = { uiState.aulaError?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de guardar
            Button(
                onClick = viewModel::saveClase,
                enabled = uiState.isFormValid && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Guardar",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isEditMode) "Actualizar Clase" else "Guardar Clase",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nota sobre campos obligatorios
            Text(
                text = "* Campos obligatorios",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CursoDropdown(
    cursos: List<Curso>,
    selectedCursoId: String,
    onCursoSelected: (String) -> Unit,
    isError: Boolean,
    errorMessage: String?,
    isLoading: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCurso = cursos.find { it.id == selectedCursoId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCurso?.nombre ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Curso *") },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            isError = isError,
            supportingText = { errorMessage?.let { Text(it) } },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        if (!isLoading) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize()
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesorDropdown(
    profesores: List<Usuario>,
    selectedProfesorId: String,
    onProfesorSelected: (String) -> Unit,
    isError: Boolean,
    errorMessage: String?,
    isLoading: Boolean,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedProfesor = profesores.find { it.documentId == selectedProfesorId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = if (selectedProfesor != null) "${selectedProfesor.nombre} ${selectedProfesor.apellidos}" else "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            isError = isError,
            supportingText = { errorMessage?.let { Text(it) } },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        if (!isLoading) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                profesores.forEach { profesor ->
                    DropdownMenuItem(
                        text = { Text("${profesor.nombre} ${profesor.apellidos}") },
                        onClick = {
                            onProfesorSelected(profesor.documentId)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfesoresAuxiliaresSelector(
    profesores: List<Usuario>,
    selectedProfesorIds: List<String>,
    onProfesorSelected: (String, Boolean) -> Unit,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (profesores.isEmpty()) {
        Text(
            text = "No hay profesores disponibles",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            items(profesores) { profesor ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedProfesorIds.contains(profesor.documentId),
                        onCheckedChange = { isChecked ->
                            onProfesorSelected(profesor.documentId, isChecked)
                        }
                    )
                    Text(
                        text = "${profesor.nombre} ${profesor.apellidos}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HiltAddClaseScreen(
    viewModel: AddClaseViewModel = hiltViewModel(),
    claseId: String = "",
    centroId: String = "",
    onNavigateBack: () -> Unit,
    onClaseAdded: () -> Unit
) {
    AddClaseScreen(
        viewModel = viewModel,
        claseId = claseId,
        centroId = centroId,
        onNavigateBack = onNavigateBack,
        onClaseAdded = onClaseAdded
    )
}

@Preview(showBackground = true)
@Composable
fun AddClaseScreenPreview() {
    UmeEguneroTheme {
        Surface {
            // Crear un estado de UI para el preview
            val uiState = AddClaseUiState(
                nombre = "4ºA ESO",
                cursoId = "1",
                profesoresAuxiliaresIds = listOf("prof1", "prof2"),
                cursosDisponibles = listOf(
                    Curso("1", "4º ESO", "", "0", 0),
                    Curso("2", "3º ESO", "", "0", 0),
                    Curso("3", "2º ESO", "", "0", 0),
                ),
                profesoresDisponibles = listOf(
                    Usuario("prof1", nombre = "Juan", apellidos = "Pérez"),
                    Usuario("prof2", nombre = "Ana", apellidos = "Martínez"),
                    Usuario("prof3", nombre = "Carlos", apellidos = "López")
                ),
                isLoading = false,
                isLoadingProfesores = false,
                isLoadingCursos = false,
                error = null,
                success = false,
                isEditMode = false
            )
            
            // Usar un mock para AddClaseScreen
            AddClaseScreenContent(
                uiState = uiState,
                onNombreChange = {},
                onCursoSelected = {},
                onProfesorSelected = { _, _ -> },
                onSaveClase = {},
                onNavigateBack = {}
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AddClaseScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        Surface {
            // Crear un estado de UI para el preview
            val uiState = AddClaseUiState(
                nombre = "4ºA ESO",
                cursoId = "1",
                profesoresAuxiliaresIds = listOf("prof1", "prof2"),
                cursosDisponibles = listOf(
                    Curso("1", "4º ESO", "", "0", 0),
                    Curso("2", "3º ESO", "", "0", 0),
                    Curso("3", "2º ESO", "", "0", 0),
                ),
                profesoresDisponibles = listOf(
                    Usuario("prof1", nombre = "Juan", apellidos = "Pérez"),
                    Usuario("prof2", nombre = "Ana", apellidos = "Martínez"),
                    Usuario("prof3", nombre = "Carlos", apellidos = "López")
                ),
                isLoading = false,
                isLoadingProfesores = false,
                isLoadingCursos = false,
                error = null,
                success = false,
                isEditMode = false
            )
            
            // Usar un mock para AddClaseScreen
            AddClaseScreenContent(
                uiState = uiState,
                onNombreChange = {},
                onCursoSelected = {},
                onProfesorSelected = { _, _ -> },
                onSaveClase = {},
                onNavigateBack = {}
            )
        }
    }
}

/**
 * Versión simplificada de la pantalla principal para el preview
 */
@Composable
private fun AddClaseScreenContent(
    uiState: AddClaseUiState,
    onNombreChange: (String) -> Unit,
    onCursoSelected: (String) -> Unit,
    onProfesorSelected: (String, Boolean) -> Unit,
    onSaveClase: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Editar Clase" else "Añadir Clase",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CentroColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Formulario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nombre de la clase
                    OutlinedTextField(
                        value = uiState.nombre,
                        onValueChange = onNombreChange,
                        label = { Text("Nombre de la clase") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Selector de curso
                    val cursoExpanded = remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = cursoExpanded.value,
                        onExpandedChange = { cursoExpanded.value = !cursoExpanded.value },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = uiState.cursosDisponibles.find { it.id == uiState.cursoId }?.nombre ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Curso") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cursoExpanded.value) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        DropdownMenu(
                            expanded = cursoExpanded.value,
                            onDismissRequest = { cursoExpanded.value = false },
                            modifier = Modifier.exposedDropdownSize()
                        ) {
                            uiState.cursosDisponibles.forEach { curso ->
                                DropdownMenuItem(
                                    text = { Text(curso.nombre) },
                                    onClick = {
                                        onCursoSelected(curso.id)
                                        cursoExpanded.value = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Sección de profesores
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
                        text = "Profesores Asignados",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Lista de profesores disponibles
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(uiState.profesoresDisponibles) { profesor ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = uiState.profesoresAuxiliaresIds.contains(profesor.documentId),
                                    onCheckedChange = { isChecked ->
                                        onProfesorSelected(profesor.documentId, isChecked)
                                    }
                                )
                                Text(
                                    text = "${profesor.nombre} ${profesor.apellidos}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Botón para guardar
            Button(
                onClick = onSaveClase,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (uiState.isEditMode) "Actualizar Clase" else "Guardar Clase")
                }
            }
        }
    }
}

@Composable
fun AddClasePreviewContent(isDarkTheme: Boolean = false) {
    // Definir las variables necesarias para el preview
    val profesoresAsignados = listOf("prof1", "prof2")
    val cursos = listOf(
        CursoSimpleModel("1", "4º ESO", "", 0, 0),
        CursoSimpleModel("2", "3º ESO", "", 0, 0),
        CursoSimpleModel("3", "2º ESO", "", 0, 0)
    )
    val disponiblesProfesores = listOf(
        ProfesorSimpleModel("prof1", "Juan", "Pérez", ""),
        ProfesorSimpleModel("prof2", "Ana", "Martínez", ""),
        ProfesorSimpleModel("prof3", "Carlos", "López", "")
    )
    
    // Ahora usar estas variables para mostrar la interfaz
    Column(modifier = Modifier.padding(16.dp)) {
        // Sección de cursos
        Text(
            text = "Cursos asociados",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            items(cursos) { curso ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = true,
                        onCheckedChange = { }
                    )
                    
                    Text(
                        text = curso.nombre,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        // Sección de profesores
        Text(
            text = "Profesores asignados",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            items(disponiblesProfesores) { profesor ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = profesoresAsignados.contains(profesor.documentId),
                        onCheckedChange = { }
                    )
                    
                    Text(
                        text = "${profesor.nombre} ${profesor.apellidos}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

