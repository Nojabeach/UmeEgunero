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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.feature.common.academico.viewmodel.AddCursoViewModel
import com.tfg.umeegunero.ui.components.DefaultTopAppBar
import com.tfg.umeegunero.ui.components.LoadingIndicator
import com.tfg.umeegunero.ui.components.OutlinedTextFieldWithError
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch

/**
 * Pantalla para añadir un nuevo curso o editar uno existente
 * 
 * @param navController Controlador de navegación
 * @param centroId ID del centro educativo donde se añadirá el curso
 * @param onNavigateBack Callback para navegar hacia atrás
 * @param onCursoAdded Callback que se ejecuta cuando se añade o actualiza un curso
 * @param viewModel ViewModel para la gestión del curso
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiltAddCursoScreen(
    navController: NavController,
    centroId: String? = null,
    onNavigateBack: () -> Unit = { navController.popBackStack() },
    onCursoAdded: () -> Unit = {},
    viewModel: AddCursoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var expandedCentroMenu by remember { mutableStateOf(false) }

    // Establecer el centroId al iniciar si se proporciona
    LaunchedEffect(centroId) {
        if (!centroId.isNullOrEmpty()) {
            viewModel.updateCentroId(centroId)
        }
    }

    // Efecto para manejar la navegación después de un guardado exitoso
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onCursoAdded()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (uiState.isEditMode) "Editar Curso" else "Nuevo Curso", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.guardarCurso() },
                icon = { Icon(Icons.Default.Check, contentDescription = "Guardar curso") },
                text = { Text("Guardar") },
                containerColor = MaterialTheme.colorScheme.primary
            )
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Selector de centro (solo para admin de la app)
                        if (uiState.isAdminApp) {
                            ExposedDropdownMenuBox(
                                expanded = expandedCentroMenu,
                                onExpandedChange = { expandedCentroMenu = !expandedCentroMenu }
                            ) {
                                OutlinedTextField(
                                    value = uiState.centros.find { it.id == uiState.centroId }?.nombre ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Centro Educativo") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCentroMenu)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    isError = uiState.centroError != null,
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                )

                                ExposedDropdownMenu(
                                    expanded = expandedCentroMenu,
                                    onDismissRequest = { expandedCentroMenu = false }
                                ) {
                                    uiState.centros.forEach { centro ->
                                        DropdownMenuItem(
                                            text = { Text(centro.nombre) },
                                            onClick = { 
                                                viewModel.updateCentroId(centro.id)
                                                expandedCentroMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            if (uiState.centroError != null) {
                                Text(
                                    text = uiState.centroError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Nombre del curso
                        OutlinedTextFieldWithError(
                            value = uiState.nombre,
                            onValueChange = viewModel::updateNombre,
                            label = "Nombre del curso",
                            placeholder = "Ej: Infantil 3 años, Primaria 1º",
                            errorMessage = uiState.nombreError ?: "",
                            isError = uiState.nombreError != null,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Descripción
                        OutlinedTextFieldWithError(
                            value = uiState.descripcion,
                            onValueChange = viewModel::updateDescripcion,
                            label = "Descripción",
                            placeholder = "Describe el curso académico",
                            errorMessage = uiState.descripcionError ?: "",
                            isError = uiState.descripcionError != null,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4
                        )

                        // Edad mínima
                        OutlinedTextFieldWithError(
                            value = uiState.edadMinima,
                            onValueChange = viewModel::updateEdadMinima,
                            label = "Edad mínima (años)",
                            placeholder = "Ej: 3",
                            errorMessage = uiState.edadMinimaError ?: "",
                            isError = uiState.edadMinimaError != null,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        // Edad máxima
                        OutlinedTextFieldWithError(
                            value = uiState.edadMaxima,
                            onValueChange = viewModel::updateEdadMaxima,
                            label = "Edad máxima (años)",
                            placeholder = "Ej: 4",
                            errorMessage = uiState.edadMaximaError ?: "",
                            isError = uiState.edadMaximaError != null,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        // Año académico
                        OutlinedTextFieldWithError(
                            value = uiState.anioAcademico,
                            onValueChange = viewModel::updateAnioAcademico,
                            label = "Año académico",
                            placeholder = "Ej: 2023-2024",
                            errorMessage = uiState.anioAcademicoError ?: "",
                            isError = uiState.anioAcademicoError != null,
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
                    }
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

@Preview(showBackground = true)
@Composable
fun AddCursoScreenPreview() {
    UmeEguneroTheme {
        HiltAddCursoScreen(
            navController = rememberNavController(),
            centroId = "centro1"
        )
    }
} 