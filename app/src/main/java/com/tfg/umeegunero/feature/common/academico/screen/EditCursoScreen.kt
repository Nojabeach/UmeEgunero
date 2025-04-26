package com.tfg.umeegunero.feature.common.academico.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.feature.common.academico.viewmodel.EditCursoViewModel
import com.tfg.umeegunero.ui.components.FormProgressIndicator
import com.tfg.umeegunero.ui.components.OutlinedTextFieldWithError
import com.tfg.umeegunero.ui.components.SnackbarHost
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCursoScreen(
    navController: NavController,
    viewModel: EditCursoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.success) {
        uiState.success?.let { success ->
            snackbarHostState.showSnackbar(success)
            viewModel.clearSuccess()
            navController.navigateUp()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Editar Curso", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
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
                onClick = { viewModel.saveCurso() },
                icon = { Icon(Icons.Default.Save, contentDescription = "Guardar") },
                text = { Text("Guardar") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                FormProgressIndicator(
                    currentStep = 1,
                    totalSteps = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        OutlinedTextFieldWithError(
                            value = uiState.nombre,
                            onValueChange = { viewModel.updateNombre(it) },
                            label = "Nombre del curso",
                            errorMessage = uiState.validationErrors["nombre"] ?: "",
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextFieldWithError(
                            value = uiState.descripcion,
                            onValueChange = { viewModel.updateDescripcion(it) },
                            label = "Descripción",
                            errorMessage = uiState.validationErrors["descripcion"] ?: "",
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextFieldWithError(
                            value = uiState.edadMinima.toString(),
                            onValueChange = { viewModel.updateEdadMinima(it.toIntOrNull() ?: 0) },
                            label = "Edad mínima",
                            errorMessage = uiState.validationErrors["edadMinima"] ?: "",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextFieldWithError(
                            value = uiState.edadMaxima.toString(),
                            onValueChange = { viewModel.updateEdadMaxima(it.toIntOrNull() ?: 0) },
                            label = "Edad máxima",
                            errorMessage = uiState.validationErrors["edadMaxima"] ?: "",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextFieldWithError(
                            value = uiState.anioAcademico,
                            onValueChange = { viewModel.updateAnioAcademico(it) },
                            label = "Año académico",
                            errorMessage = uiState.validationErrors["anioAcademico"] ?: "",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Activo")
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(
                                checked = uiState.activo,
                                onCheckedChange = { viewModel.updateActivo(it) }
                            )
                        }
                    }
                }
            }
        }
    }
} 