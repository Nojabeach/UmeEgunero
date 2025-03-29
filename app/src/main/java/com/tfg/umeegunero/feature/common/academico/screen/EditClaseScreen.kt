package com.tfg.umeegunero.feature.common.academico.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.feature.common.academico.viewmodel.GestionClasesViewModel
import com.tfg.umeegunero.feature.common.academico.viewmodel.AddClaseViewModel
import com.tfg.umeegunero.ui.components.LoadingIndicator

/**
 * Pantalla para editar una clase existente
 * Reutiliza el componente AddClaseScreen pero en modo edición
 * 
 * @param navController Controlador de navegación
 * @param viewModel ViewModel para la gestión de clases
 */
@Composable
fun EditClaseScreen(
    navController: NavController,
    viewModel: AddClaseViewModel = hiltViewModel()
) {
    // Reutilizamos la pantalla de añadir clase pero con el viewModel en modo edición
    AddClaseScreen(
        navController = navController,
        viewModel = viewModel
    )
}

/**
 * Pantalla para editar o crear una clase
 * @param navController controlador de navegación
 * @param cursoId ID del curso al que pertenece la clase
 * @param claseId ID de la clase a editar. Si es null, se está creando una nueva clase
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditClaseScreen(
    navController: NavController,
    cursoId: String,
    claseId: String? = null,
    viewModel: GestionClasesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    var nombre by remember { mutableStateOf("") }
    var aula by remember { mutableStateOf("") }
    var horario by remember { mutableStateOf("") }
    var profesorTitularId by remember { mutableStateOf("") }
    var capacidadMaxima by remember { mutableStateOf("25") }
    
    val isEditing = claseId != null
    
    // Si está editando, cargar los datos de la clase existente
    LaunchedEffect(claseId) {
        if (isEditing) {
            // Aquí podríamos implementar la carga de datos de la clase por ID
            // Por ahora usamos la primera clase de la lista como ejemplo
            val claseSeleccionada = uiState.clases.firstOrNull { it.id == claseId }
            claseSeleccionada?.let {
                nombre = it.nombre
                horario = it.horario
                profesorTitularId = it.profesorTitularId
                aula = it.aula
                capacidadMaxima = it.capacidadMaxima.toString()
            }
        }
    }
    
    // Título de la pantalla según si es creación o edición
    val titulo = if (isEditing) "Editar Clase" else "Nueva Clase"
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Solo mostrar FAB si nombre no está vacío y no está cargando
            if (nombre.isNotBlank() && !uiState.isLoading) {
                FloatingActionButton(
                    onClick = {
                        val clase = Clase(
                            id = claseId ?: "",
                            cursoId = cursoId,
                            centroId = "", // Se establecerá en el ViewModel
                            nombre = nombre,
                            horario = horario,
                            profesorTitularId = profesorTitularId,
                            aula = aula,
                            capacidadMaxima = capacidadMaxima.toIntOrNull() ?: 25,
                            activo = true,
                            profesoresAuxiliaresIds = emptyList(),
                            alumnosIds = emptyList()
                        )
                        viewModel.guardarClase(clase)
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Guardar"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la clase") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    isError = nombre.isBlank()
                )
                
                OutlinedTextField(
                    value = horario,
                    onValueChange = { horario = it },
                    label = { Text("Horario") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )
                
                OutlinedTextField(
                    value = profesorTitularId,
                    onValueChange = { profesorTitularId = it },
                    label = { Text("ID del profesor titular") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )
                
                OutlinedTextField(
                    value = aula,
                    onValueChange = { aula = it },
                    label = { Text("Aula") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )
                
                OutlinedTextField(
                    value = capacidadMaxima,
                    onValueChange = { capacidadMaxima = it },
                    label = { Text("Capacidad máxima") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }
            
            // Mostrar el indicador de carga si está cargando
            LoadingIndicator(
                isLoading = uiState.isLoading,
                message = "Cargando datos de la clase..."
            )
            
            // Mostrar mensaje de error si hay alguno
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
} 