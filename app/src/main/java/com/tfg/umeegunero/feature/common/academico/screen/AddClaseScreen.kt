package com.tfg.umeegunero.feature.common.academico.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.feature.common.academico.viewmodel.AddClaseViewModel
import com.tfg.umeegunero.ui.components.DefaultTopAppBar
import com.tfg.umeegunero.ui.components.LoadingIndicator
import com.tfg.umeegunero.ui.components.OutlinedTextFieldWithError
import kotlinx.coroutines.launch
import com.tfg.umeegunero.data.model.Usuario

/**
 * Pantalla para añadir una nueva clase a un curso
 * 
 * @param navController Controlador de navegación
 * @param viewModel ViewModel para la gestión de clases
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClaseScreen(
    navController: NavController,
    viewModel: AddClaseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Efecto para manejar la navegación después de un guardado exitoso
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = if (uiState.isEditMode) "Editar Clase" else "Nueva Clase",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.guardarClase() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Guardar clase"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nombre de la clase
                OutlinedTextFieldWithError(
                    value = uiState.nombre,
                    onValueChange = viewModel::updateNombre,
                    label = "Nombre de la clase",
                    placeholder = "Ej: A, B, Mañana, etc.",
                    errorMessage = uiState.nombreError ?: "",
                    isError = uiState.nombreError != null,
                    modifier = Modifier.fillMaxWidth()
                )

                // Aula
                OutlinedTextFieldWithError(
                    value = uiState.aula,
                    onValueChange = viewModel::updateAula,
                    label = "Aula",
                    placeholder = "Ubicación física del aula",
                    errorMessage = uiState.aulaError ?: "",
                    isError = uiState.aulaError != null,
                    modifier = Modifier.fillMaxWidth()
                )

                // Horario
                OutlinedTextFieldWithError(
                    value = uiState.horario,
                    onValueChange = viewModel::updateHorario,
                    label = "Horario",
                    placeholder = "Ej: Lunes a Viernes 9:00-14:00",
                    errorMessage = uiState.horarioError ?: "",
                    isError = uiState.horarioError != null,
                    modifier = Modifier.fillMaxWidth()
                )

                // Capacidad máxima
                OutlinedTextFieldWithError(
                    value = uiState.capacidadMaxima,
                    onValueChange = viewModel::updateCapacidadMaxima,
                    label = "Capacidad máxima",
                    placeholder = "Número máximo de alumnos",
                    errorMessage = uiState.capacidadMaximaError ?: "",
                    isError = uiState.capacidadMaximaError != null,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Selector de profesor titular
                Text(
                    text = "Profesor Titular",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                if (uiState.isLoadingProfesores) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else if (uiState.profesoresDisponibles.isEmpty()) {
                    Text(
                        text = "No hay profesores disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    ProfesorDropdown(
                        profesores = uiState.profesoresDisponibles,
                        selectedProfesorId = uiState.profesorTitularId,
                        onProfesorSelected = viewModel::updateProfesorTitular,
                        error = uiState.profesorTitularError,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Estado activo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.activo,
                        onCheckedChange = viewModel::updateActivo
                    )
                    Text(
                        text = "Clase activa",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Indicador de carga
            if (uiState.isLoading) {
                LoadingIndicator(fullScreen = true)
            }

            // Mostrar errores generales mediante Snackbar
            LaunchedEffect(uiState.error) {
                uiState.error?.let { error ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message = error)
                        viewModel.clearError()
                    }
                }
            }
        }
    }
}

/**
 * Componente para seleccionar un profesor de una lista desplegable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesorDropdown(
    profesores: List<Usuario>,
    selectedProfesorId: String,
    onProfesorSelected: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedProfesor = profesores.find { it.dni == selectedProfesorId }
    
    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedProfesor?.let { "${it.nombre} ${it.apellidos}" } ?: "Seleccionar profesor",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = error != null
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                profesores.forEach { profesor ->
                    DropdownMenuItem(
                        text = { Text("${profesor.nombre} ${profesor.apellidos}") },
                        onClick = {
                            onProfesorSelected(profesor.dni)
                            expanded = false
                        }
                    )
                }
            }
        }
        
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
} 