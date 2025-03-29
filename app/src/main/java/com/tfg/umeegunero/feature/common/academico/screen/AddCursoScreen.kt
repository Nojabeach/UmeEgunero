package com.tfg.umeegunero.feature.common.academico.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tfg.umeegunero.feature.common.academico.viewmodel.AddCursoViewModel
import com.tfg.umeegunero.ui.components.FormProgressIndicator
import com.tfg.umeegunero.ui.components.LoadingIndicator
import com.tfg.umeegunero.ui.components.OutlinedTextFieldWithError
import com.tfg.umeegunero.ui.theme.AcademicoColor
import androidx.compose.ui.text.input.KeyboardType

/**
 * Pantalla para la adición de nuevos cursos académicos
 * 
 * @param viewModel ViewModel que gestiona la lógica de la pantalla
 * @param centroId ID del centro al que pertenecerá el curso
 * @param onNavigateBack Callback para navegar hacia atrás
 * @param onCursoAdded Callback que se ejecuta cuando se añade un curso correctamente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCursoScreen(
    centroId: String,
    viewModel: AddCursoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onCursoAdded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Inicializar con el ID del centro cuando se monta el componente
    LaunchedEffect(centroId) {
        viewModel.setCentroId(centroId)
    }
    
    // Efecto para mostrar Snackbar cuando hay un error
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            snackbarHostState.showSnackbar(
                message = uiState.error ?: "Error desconocido",
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    
    // Efecto para navegar hacia atrás cuando se completa con éxito
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar(
                message = "Curso académico creado con éxito",
                duration = SnackbarDuration.Short
            )
            onCursoAdded()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Añadir Curso Académico",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
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
                    containerColor = AcademicoColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Indicador de progreso del formulario
                FormProgressIndicator(
                    currentStep = 1,
                    totalSteps = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
                // Formulario
                OutlinedTextFieldWithError(
                    value = uiState.nombre,
                    onValueChange = { viewModel.updateNombre(it) },
                    label = "Nombre del curso",
                    isError = uiState.nombreError != null,
                    errorMessage = uiState.nombreError ?: "",
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextFieldWithError(
                    value = uiState.descripcion,
                    onValueChange = { viewModel.updateDescripcion(it) },
                    label = "Descripción",
                    isError = uiState.descripcionError != null,
                    errorMessage = uiState.descripcionError ?: "",
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextFieldWithError(
                        value = uiState.edadMinima,
                        onValueChange = { viewModel.updateEdadMinima(it) },
                        label = "Edad mínima",
                        isError = uiState.edadMinimaError != null,
                        errorMessage = uiState.edadMinimaError ?: "",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextFieldWithError(
                        value = uiState.edadMaxima,
                        onValueChange = { viewModel.updateEdadMaxima(it) },
                        label = "Edad máxima",
                        isError = uiState.edadMaximaError != null,
                        errorMessage = uiState.edadMaximaError ?: "",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                OutlinedTextFieldWithError(
                    value = uiState.anioAcademico,
                    onValueChange = { viewModel.updateAnioAcademico(it) },
                    label = "Año académico",
                    isError = uiState.anioAcademicoError != null,
                    errorMessage = uiState.anioAcademicoError ?: "",
                    placeholder = { Text("Ejemplo: 2023-2024") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Botón de guardar
                Button(
                    onClick = { viewModel.guardarCurso() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AcademicoColor
                    ),
                    enabled = !uiState.isLoading
                ) {
                    if (!uiState.isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null
                            )
                            Text("Guardar Curso")
                        }
                    } else {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
            
            // Indicador de carga
            if (uiState.isLoading) {
                LoadingIndicator(
                    isLoading = true,
                    message = "Guardando curso académico...",
                    fullScreen = true
                )
            }
        }
    }
}

/**
 * Versión del componente que utiliza Hilt para la inyección de dependencias,
 * diseñada para ser llamada desde Navigation.
 */
@Composable
fun HiltAddCursoScreen(
    centroId: String,
    viewModel: AddCursoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onCursoAdded: () -> Unit
) {
    AddCursoScreen(
        centroId = centroId,
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        onCursoAdded = onCursoAdded
    )
} 