package com.tfg.umeegunero.feature.common.academico.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.feature.common.academico.viewmodel.AddCursoViewModel
import com.tfg.umeegunero.ui.components.DefaultTopAppBar
import com.tfg.umeegunero.ui.components.LoadingIndicator
import com.tfg.umeegunero.ui.components.OutlinedTextFieldWithError
import kotlinx.coroutines.launch

/**
 * Pantalla para añadir un nuevo curso al sistema
 * 
 * @param navController Controlador de navegación
 * @param viewModel ViewModel para la gestión de cursos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCursoScreen(
    navController: NavController,
    viewModel: AddCursoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
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
                title = if (uiState.isEditMode) "Editar Curso" else "Nuevo Curso",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.guardarCurso() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Guardar curso"
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
                // Nombre del curso
                OutlinedTextFieldWithError(
                    value = uiState.nombre,
                    onValueChange = viewModel::updateNombre,
                    label = "Nombre del curso",
                    placeholder = "Ej: Infantil 3 años",
                    errorMessage = uiState.errorNombre ?: "",
                    isError = uiState.errorNombre != null,
                    modifier = Modifier.fillMaxWidth()
                )

                // Descripción
                OutlinedTextFieldWithError(
                    value = uiState.descripcion,
                    onValueChange = viewModel::updateDescripcion,
                    label = "Descripción",
                    placeholder = "Descripción del curso, objetivos, etc.",
                    errorMessage = uiState.errorDescripcion ?: "",
                    isError = uiState.errorDescripcion != null,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Edad mínima
                OutlinedTextFieldWithError(
                    value = uiState.edadMinima,
                    onValueChange = viewModel::updateEdadMinima,
                    label = "Edad mínima",
                    placeholder = "Ej: 3",
                    errorMessage = uiState.errorEdadMinima ?: "",
                    isError = uiState.errorEdadMinima != null,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Edad máxima
                OutlinedTextFieldWithError(
                    value = uiState.edadMaxima,
                    onValueChange = viewModel::updateEdadMaxima,
                    label = "Edad máxima",
                    placeholder = "Ej: 4",
                    errorMessage = uiState.errorEdadMaxima ?: "",
                    isError = uiState.errorEdadMaxima != null,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Año académico
                OutlinedTextFieldWithError(
                    value = uiState.anioAcademico,
                    onValueChange = viewModel::updateAnioAcademico,
                    label = "Año académico",
                    placeholder = "Ej: 2023-2024",
                    errorMessage = uiState.errorAnioAcademico ?: "",
                    isError = uiState.errorAnioAcademico != null,
                    modifier = Modifier.fillMaxWidth()
                )

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
                        text = "Curso activo",
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
 * Versión del componente que utiliza Hilt para la inyección de dependencias,
 * diseñada para ser llamada desde Navigation.
 */
@Composable
fun HiltAddCursoScreen(
    navController: NavController,
    centroId: String = "",
    onNavigateBack: () -> Unit = { navController.popBackStack() },
    onCursoAdded: () -> Unit = { navController.popBackStack() },
    viewModel: AddCursoViewModel = hiltViewModel()
) {
    AddCursoScreen(
        navController = navController,
        viewModel = viewModel
    )
} 